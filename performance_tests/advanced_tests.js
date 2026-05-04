const axios = require('axios');
const { exec } = require('child_process');
const util = require('util');
const fs = require('fs');
const execPromise = util.promisify(exec);

class AdvancedTests {
    constructor(baseUrl = 'http://localhost:8080') {
        this.baseUrl = baseUrl;
        this.results = {};
    }

    async isSystemReady() {
        try {
            const response = await axios.post(`${this.baseUrl}/api/code/run`, {
                code: 'System.out.println("ping");',
                exercise: 1,
                exerciseId: "ping",
                moduleId: "test",
                baseUrl: 'http://localhost:5173'
            }, { timeout: 5000 });
            return response.status === 200;
        } catch (error) {
            return false;
        }
    }

    async testColdStart(skipIfNotConfirmed = true) {
        console.log('\nТЕСТ 1: СТАРТ СИСТЕМЫ');
        
        if (skipIfNotConfirmed) {
            this.results.coldStart = { skipped: true };
            return;
        }
        
        try {
            await execPromise('docker compose down');
            
            console.log('Запускаем Docker...');
            const startTime = Date.now();
            await execPromise('docker compose up -d');
            
            console.log('Ожидание готовности системы...');
            let isReady = false;
            let attempts = 0;
            const maxAttempts = 300;  
            
            while (!isReady && attempts < maxAttempts) {
                await new Promise(resolve => setTimeout(resolve, 1000));
                attempts++;
                process.stdout.write('.');
                
                if (await this.isSystemReady()) {
                    isReady = true;
                    console.log(`\n Система готова через ${attempts} секунд`);
                }
            }
            
            const startupTime = Date.now() - startTime;
            
            if (!isReady) {
                console.log('\n Система не запустилась за 5 минут');
                return;
            }
            
            console.log('Измеряем первый запрос...');
            const firstStart = Date.now();
            await axios.post(`${this.baseUrl}/api/code/run`, {
                code: 'System.out.println("first");',
                exercise: 1,
                exerciseId: "first",
                moduleId: "test",
                baseUrl: 'http://localhost:5173'
            });
            const firstTime = Date.now() - firstStart;
            
            const secondStart = Date.now();
            await axios.post(`${this.baseUrl}/api/code/run`, {
                code: 'System.out.println("second");',
                exercise: 1,
                exerciseId: "second",
                moduleId: "test",
                baseUrl: 'http://localhost:5173'
            });
            const secondTime = Date.now() - secondStart;
            
            this.results.coldStart = {
                dockerStartupTime: startupTime,
                firstRequestTime: firstTime,
                secondRequestTime: secondTime,
                warmupImprovement: ((firstTime - secondTime) / firstTime * 100).toFixed(1) + '%'
            };
            
            console.log('\n РЕЗУЛЬТАТЫ:');
            console.log(`   Запуск Docker: ${(startupTime/1000).toFixed(1)} сек`);
            console.log(`   Первый запрос: ${firstTime} мс`);
            console.log(`   Второй запрос: ${secondTime} мс`);
            console.log(`   Ускорение: ${this.results.coldStart.warmupImprovement}`);
            
        } catch (error) {
            console.error('Ошибка при запуске теста:', error.message);
            this.results.coldStart = { error: error.message };
        }
    }

    async testResponseTimeBreakdown() {
        console.log('\n ТЕСТ 2: ДЕКОМПОЗИЦИЯ ВРЕМЕНИ ОТВЕТА');
        
        const testCases = [
            {
                name: 'Только компиляция',
                code: `
                    public class CompileOnly {
                        public static void run() {
                            int sum = 0;
                            for(int i = 0; i < 10000; i++) sum += i;
                            System.out.println(sum);
                        }
                    }
                `
            },
            {
                name: 'Selenide (без компиляции)',
                code: `
                    import static com.codeborne.selenide.Selenide.*;
                    public class SelenideOnly {
                        public static void run() {
                            open("/");
                        }
                    }
                `
            },
            {
                name: 'Полный цикл',
                code: `
                    import static com.codeborne.selenide.Selenide.*;
                    import static com.codeborne.selenide.Condition.*;
                    public class FullCycle {
                        public static void run() {
                            open("/");
                            $("body").shouldBe(visible);
                        }
                    }
                `
            }
        ];
        
        const results = [];
        
        for (const testCase of testCases) {
            console.log(`\n ${testCase.name}...`);
            const times = [];
            
            for (let i = 0; i < 10; i++) {
                const start = Date.now();
                try {
                    await axios.post(`${this.baseUrl}/api/code/run`, {
                        code: testCase.code,
                        exercise: 1,
                        exerciseId: `breakdown-${i}`,
                        moduleId: "test",
                        baseUrl: 'http://localhost:5173'
                    });
                    times.push(Date.now() - start);
                } catch (e) {
                    console.log(`   Ошибка: ${e.message}`);
                }
                await new Promise(r => setTimeout(r, 500));
            }
            
            const avg = times.reduce((a, b) => a + b, 0) / times.length;
            results.push({ name: testCase.name, avgTime: avg });
            console.log(`   Среднее: ${avg.toFixed(2)} мс`);
        }
        
        if (results.length === 3) {
            const compile = results[0].avgTime;
            const selenide = results[1].avgTime;
            const full = results[2].avgTime;
            
            this.results.breakdown = {
                compilationTime: compile,
                selenideTime: selenide - compile,
                networkOverhead: full - selenide,
                totalTime: full,
                percentages: {
                    compilation: (compile / full * 100).toFixed(1) + '%',
                    selenide: ((selenide - compile) / full * 100).toFixed(1) + '%',
                    overhead: ((full - selenide) / full * 100).toFixed(1) + '%'
                }
            };
            
            console.log('\n РЕЗУЛЬТАТЫ:');
            console.log(`   Компиляция: ${compile.toFixed(2)} мс (${this.results.breakdown.percentages.compilation})`);
            console.log(`   Selenide: ${(selenide - compile).toFixed(2)} мс (${this.results.breakdown.percentages.selenide})`);
            console.log(`   Накладные расходы: ${(full - selenide).toFixed(2)} мс (${this.results.breakdown.percentages.overhead})`);
        }
        
        return this.results.breakdown;
    }

    async testParallelismVsSequential() {
        console.log('\n ТЕСТ 3: ПАРАЛЛЕЛЬНЫЕ И ПОСЛЕДОВАТЕЛЬНЫЕ ЗАПРОСЫ');
        
        const count = 10;
        const code = `
            import static com.codeborne.selenide.Selenide.*;
            public class Test { public static void run() { open("/"); } }
        `;
        
        console.log(`\n Последовательные ${count} запросов...`);
        const seqStart = Date.now();
        for (let i = 0; i < count; i++) {
            await axios.post(`${this.baseUrl}/api/code/run`, {
                code, exercise: 1,
                exerciseId: `seq-${i}`, moduleId: "test",
                baseUrl: 'http://localhost:5173'
            });
        }
        const seqTime = Date.now() - seqStart;
        
        console.log(` Параллельные ${count} запросов...`);
        const parStart = Date.now();
        const promises = [];
        for (let i = 0; i < count; i++) {
            promises.push(axios.post(`${this.baseUrl}/api/code/run`, {
                code, exercise: 1,
                exerciseId: `par-${i}`, moduleId: "test",
                baseUrl: 'http://localhost:5173'
            }));
        }
        await Promise.all(promises);
        const parTime = Date.now() - parStart;
        
        this.results.parallelism = {
            sequential: { totalTime: seqTime, avgTime: seqTime / count },
            parallel: { totalTime: parTime, avgTime: parTime / count },
            speedup: (seqTime / parTime).toFixed(2) + 'x'
        };
        
        console.log(`\n РЕЗУЛЬТАТЫ:`);
        console.log(`   Последовательно: ${seqTime} мс`);
        console.log(`   Параллельно: ${parTime} мс`);
        console.log(`   Ускорение: ${this.results.parallelism.speedup}`);
        
        return this.results.parallelism;
    }

    async runAllAdvancedTests(includeColdStart = false) {
        
        console.log('\n Проверка доступности системы...');
        if (!await this.isSystemReady()) {
            console.log('Система не доступна! Запустите: docker compose up -d');
            return;
        }
        console.log('Система доступна');
        
        if (includeColdStart) {
            await this.testColdStart(false);
        }
        
        await this.testResponseTimeBreakdown();
        await this.testParallelismVsSequential();
        
        const filename = `advanced_results_${Date.now()}.json`;
        fs.writeFileSync(filename, JSON.stringify(this.results, null, 2));
        console.log(`\n Результаты сохранены: ${filename}`);
        
        return this.results;
    }
}


if (require.main === module) {
    const tester = new AdvancedTests();
    tester.runAllAdvancedTests(false).catch(console.error);
}

module.exports = AdvancedTests;