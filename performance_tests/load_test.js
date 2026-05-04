const axios = require('axios');
const fs = require('fs');
const { ChartJSNodeCanvas } = require('chartjs-node-canvas');

class LoadTesterNoAuth {
    constructor(baseUrl = 'http://localhost:8080') {
        this.baseUrl = baseUrl;
        this.results = [];
        this.historyData = [];
    }

    async simulateUser(code, requests, userId) {
        const userResults = [];
        
        for (let i = 0; i < requests; i++) {
            const startTime = Date.now();
            try {
                const response = await axios.post(`${this.baseUrl}/api/code/run`, {
                    code: code,
                    exercise: 1,
                    exerciseId: `module-2-block-1-exercise-${(userId * requests + i) % 10 + 1}`,
                    moduleId: "module-2",
                    baseUrl: 'http://localhost:5173'
                }, {
                    timeout: 120000,
                    headers: { 'Content-Type': 'application/json' }
                });

                const responseTime = Date.now() - startTime;
                userResults.push({
                    userId,
                    requestId: i,
                    success: response.status === 200,
                    responseTime,
                    statusCode: response.status
                });

            } catch (error) {
                const responseTime = Date.now() - startTime;
                userResults.push({
                    userId,
                    requestId: i,
                    success: false,
                    responseTime,
                    statusCode: error.response?.status || 500
                });
            }

            await new Promise(resolve => setTimeout(resolve, 100));
        }

        return userResults;
    }

    generateCode(lines) {
        let code = `
            import static com.codeborne.selenide.Selenide.*;
            import static com.codeborne.selenide.Condition.*;
            
            public class TestScript {
                public static void run() {
                    open("/");
        `;

        for (let i = 0; i < lines; i++) {
            code += `        $("element${i}").shouldBe(visible); // Line ${i}\n`;
        }

        code += `    }
            }`;
        return code;
    }

    async generateResponseTimeGraph() {
        if (this.historyData.length === 0) return;
        
        const width = 800;
        const height = 500;
        const chartJSNodeCanvas = new ChartJSNodeCanvas({ width, height });

        const buffer = await chartJSNodeCanvas.renderToBuffer({
            type: 'line',
            data: {
                labels: this.historyData.map(d => `${d.users}`),
                datasets: [{
                    label: 'Среднее время ответа (мс)',
                    data: this.historyData.map(d => d.avgResponseTime),
                    borderColor: 'rgb(255, 99, 132)',
                    backgroundColor: 'rgba(255, 99, 132, 0.1)',
                    borderWidth: 2,
                    pointRadius: 5,
                    pointBorderColor: 'rgb(255, 99, 132)',
                    pointBackgroundColor: 'rgb(255, 255, 255)',
                    pointBorderWidth: 2,
                    tension: 0.1,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    title: { display: true, text: 'Зависимость времени ответа от нагрузки', font: { size: 14 } },
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true }
                },
                scales: {
                    y: { 
                        title: { display: true, text: 'Время ответа (мс)', font: { size: 12 } },
                        beginAtZero: true,
                        grid: { color: 'rgba(0, 0, 0, 0.1)' }
                    },
                    x: { 
                        title: { display: true, text: 'Количество пользователей', font: { size: 12 } },
                        grid: { display: false }
                    }
                }
            }
        });
        
        fs.writeFileSync('response_time_vs_users.png', buffer);
        console.log('График времени ответа сохранён: response_time_vs_users.png');
    }

    async generateThroughputGraph() {
        if (this.historyData.length === 0) return;
        
        const width = 800;
        const height = 500;
        const chartJSNodeCanvas = new ChartJSNodeCanvas({ width, height });

        const buffer = await chartJSNodeCanvas.renderToBuffer({
            type: 'line',
            data: {
                labels: this.historyData.map(d => `${d.users}`),
                datasets: [{
                    label: 'Пропускная способность (запросов/сек)',
                    data: this.historyData.map(d => d.throughput),
                    borderColor: 'rgb(54, 162, 235)',
                    backgroundColor: 'rgba(54, 162, 235, 0.1)',
                    borderWidth: 2,
                    pointRadius: 5,
                    pointBorderColor: 'rgb(54, 162, 235)',
                    pointBackgroundColor: 'rgb(255, 255, 255)',
                    pointBorderWidth: 2,
                    tension: 0.1,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    title: { display: true, text: 'Пропускная способность при разной нагрузке', font: { size: 14 } },
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true }
                },
                scales: {
                    y: { 
                        title: { display: true, text: 'Запросов в секунду', font: { size: 12 } },
                        beginAtZero: true,
                        grid: { color: 'rgba(0, 0, 0, 0.1)' }
                    },
                    x: { 
                        title: { display: true, text: 'Количество пользователей', font: { size: 12 } },
                        grid: { display: false }
                    }
                }
            }
        });
        
        fs.writeFileSync('throughput_vs_users.png', buffer);
        console.log('График пропускной способности сохранён: throughput_vs_users.png');
    }

    async generateSuccessRateGraph() {
        if (this.historyData.length === 0) return;
        
        const width = 800;
        const height = 500;
        const chartJSNodeCanvas = new ChartJSNodeCanvas({ width, height });

        const buffer = await chartJSNodeCanvas.renderToBuffer({
            type: 'line',
            data: {
                labels: this.historyData.map(d => `${d.users}`),
                datasets: [{
                    label: 'Успешных запросов (%)',
                    data: this.historyData.map(d => d.successRate),
                    borderColor: 'rgb(75, 192, 75)',
                    backgroundColor: 'rgba(75, 192, 75, 0.1)',
                    borderWidth: 2,
                    pointRadius: 5,
                    pointBorderColor: 'rgb(75, 192, 75)',
                    pointBackgroundColor: 'rgb(255, 255, 255)',
                    pointBorderWidth: 2,
                    tension: 0.1,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    title: { display: true, text: 'Надёжность системы под нагрузкой', font: { size: 14 } },
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true }
                },
                scales: {
                    y: { 
                        title: { display: true, text: 'Успешных запросов (%)', font: { size: 12 } },
                        min: 0,
                        max: 100,
                        grid: { color: 'rgba(0, 0, 0, 0.1)' }
                    },
                    x: { 
                        title: { display: true, text: 'Количество пользователей', font: { size: 12 } },
                        grid: { display: false }
                    }
                }
            }
        });
        
        fs.writeFileSync('success_rate.png', buffer);
        console.log('График надёжности сохранён: success_rate.png');
    }

    async testCodeExecution(concurrentUsers = 5, requestsPerUser = 10) {
        console.log(`\n Тест: ${concurrentUsers} пользователей, ${requestsPerUser} запросов каждый`);
        
        const testCode = `
            import static com.codeborne.selenide.Selenide.*;
            import static com.codeborne.selenide.Condition.*;
            
            public class TestScript {
                public static void run() {
                    open("/");
                    $("h1").shouldBe(visible);
                }
            }
        `;

        const promises = [];
        const startTime = Date.now();

        for (let user = 0; user < concurrentUsers; user++) {
            promises.push(this.simulateUser(testCode, requestsPerUser, user));
        }

        const results = await Promise.all(promises);
        const endTime = Date.now();
        
        const allResults = results.flat();
        const totalRequests = concurrentUsers * requestsPerUser;
        const totalTime = endTime - startTime;
        const avgResponseTime = allResults.reduce((sum, r) => sum + r.responseTime, 0) / totalRequests;
        const successRate = allResults.filter(r => r.success).length / totalRequests * 100;
        
        const responseTimes = allResults.map(r => r.responseTime);
        const minResponseTime = Math.min(...responseTimes);
        const maxResponseTime = Math.max(...responseTimes);

        console.log(`   Среднее время: ${avgResponseTime.toFixed(2)}ms (мин: ${minResponseTime}ms, макс: ${maxResponseTime}ms)`);
        console.log(`   Пропускная способность: ${(totalRequests / (totalTime / 1000)).toFixed(2)} rps`);
        console.log(`   Успешность: ${successRate.toFixed(2)}%`);
        
        this.historyData.push({
            users: concurrentUsers,
            avgResponseTime,
            minResponseTime,
            maxResponseTime,
            throughput: totalRequests / (totalTime / 1000),
            successRate
        });

        return {
            avgResponseTime,
            throughput: totalRequests / (totalTime / 1000),
            successRate
        };
    }

    async testDifferentLoads() {
        console.log('\n ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ');
        
        const loadLevels = [
            { users: 1, requests: 10, name: '1 пользователь' },
            { users: 10, requests: 10, name: '10 пользователей' },
            { users: 20, requests: 10, name: '20 пользователей' },
            { users: 40, requests: 10, name: '40 пользователей' },
            { users: 60, requests: 10, name: '60 пользователей' },
            { users: 80, requests: 10, name: '80 пользователей' },
            { users: 100, requests: 10, name: '100 пользователей' }
        ];
        
        for (const level of loadLevels) {
            console.log(`\n--- ${level.name} ---`);
            await this.testCodeExecution(level.users, level.requests);
            await new Promise(resolve => setTimeout(resolve, 5000));
        }
        
        await this.generateResponseTimeGraph();
        await this.generateThroughputGraph();
        await this.generateSuccessRateGraph();
    }

    async testCompilationTime() {
        console.log('\n ТЕСТ ВРЕМЕНИ КОМПИЛЯЦИИ ');
        
        const codeSizes = [
            { name: '10 строк', lines: 10 },
            { name: '50 строк', lines: 50 },
            { name: '200 строк', lines: 200 },
            { name: '500 строк', lines: 500 },
            { name: '1000 строк', lines: 1000 }
        ];

        const compilationResults = [];

        for (const size of codeSizes) {
            const code = this.generateCode(size.lines);
            const times = [];

            console.log(`\n ${size.name}...`);
            
            for (let i = 0; i < 5; i++) {
                const startTime = Date.now();
                try {
                    await axios.post(`${this.baseUrl}/api/code/run`, {
                        code: code,
                        exercise: 1,
                        exerciseId: `compilation-test-${size.lines}`,
                        moduleId: "test",
                        baseUrl: 'http://localhost:5173'
                    }, {
                        timeout: 120000,
                        headers: { 'Content-Type': 'application/json' }
                    });
                    times.push(Date.now() - startTime);
                } catch (error) {
                    console.log(`   Ошибка: ${error.message}`);
                }
                await new Promise(resolve => setTimeout(resolve, 500));
            }

            if (times.length > 0) {
                const avgTime = times.reduce((sum, t) => sum + t, 0) / times.length;
                const minTime = Math.min(...times);
                const maxTime = Math.max(...times);
                compilationResults.push({
                    name: size.name,
                    lines: size.lines,
                    avgTime,
                    minTime,
                    maxTime,
                    times
                });
                console.log(`   Среднее: ${avgTime.toFixed(2)}ms (мин: ${minTime}ms, макс: ${maxTime}ms)`);
            }
        }

        await this.generateCompilationGraph(compilationResults);
        return compilationResults;
    }

    async generateCompilationGraph(compilationResults) {
        if (compilationResults.length === 0) return;
        
        const width = 800;
        const height = 500;
        const chartJSNodeCanvas = new ChartJSNodeCanvas({ width, height });

        const buffer = await chartJSNodeCanvas.renderToBuffer({
            type: 'line',
            data: {
                labels: compilationResults.map(r => r.name),
                datasets: [
                    {
                        label: 'Среднее время (мс)',
                        data: compilationResults.map(r => r.avgTime),
                        borderColor: 'rgb(255, 99, 132)',
                        backgroundColor: 'rgba(255, 99, 132, 0.1)',
                        borderWidth: 2,
                        pointRadius: 5,
                        pointBorderColor: 'rgb(255, 99, 132)',
                        pointBackgroundColor: 'rgb(255, 255, 255)',
                        pointBorderWidth: 2,
                        tension: 0.1,
                        fill: true
                    },
                    {
                        label: 'Минимум (мс)',
                        data: compilationResults.map(r => r.minTime),
                        borderColor: 'rgb(54, 162, 235)',
                        backgroundColor: 'rgba(54, 162, 235, 0.05)',
                        borderWidth: 1.5,
                        borderDash: [5, 5],
                        pointRadius: 4,
                        pointBorderColor: 'rgb(54, 162, 235)',
                        pointBackgroundColor: 'rgb(255, 255, 255)',
                        pointBorderWidth: 1.5,
                        tension: 0.1,
                        fill: false
                    },
                    {
                        label: 'Максимум (мс)',
                        data: compilationResults.map(r => r.maxTime),
                        borderColor: 'rgb(255, 159, 64)',
                        backgroundColor: 'rgba(255, 159, 64, 0.05)',
                        borderWidth: 1.5,
                        borderDash: [5, 5],
                        pointRadius: 4,
                        pointBorderColor: 'rgb(255, 159, 64)',
                        pointBackgroundColor: 'rgb(255, 255, 255)',
                        pointBorderWidth: 1.5,
                        tension: 0.1,
                        fill: false
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    title: { display: true, text: 'Время компиляции в зависимости от размера кода', font: { size: 14 } },
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true }
                },
                scales: {
                    y: { 
                        title: { display: true, text: 'Время (мс)', font: { size: 12 } },
                        beginAtZero: true,
                        grid: { color: 'rgba(0, 0, 0, 0.1)' }
                    },
                    x: { 
                        title: { display: true, text: 'Размер кода', font: { size: 12 } },
                        grid: { display: false }
                    }
                }
            }
        });
        
        fs.writeFileSync('compilation_time.png', buffer);
        console.log('\n График времени компиляции сохранён: compilation_time.png');
    }
}

async function runTests() {
    const tester = new LoadTesterNoAuth();
    
    try {
        await tester.testDifferentLoads();
        await tester.testCompilationTime();
        
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

if (require.main === module) {
    runTests();
}

module.exports = LoadTesterNoAuth;