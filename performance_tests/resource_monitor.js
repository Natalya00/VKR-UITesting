const si = require('systeminformation');
const fs = require('fs');
const path = require('path');
const { ChartJSNodeCanvas } = require('chartjs-node-canvas');

class ResourceMonitor {
    constructor() {
        this.monitoring = false;
        this.data = [];
        this.interval = null;
        this.startTime = null;
    }

    async startMonitoring(intervalMs = 1000) {
        console.log('Запуск отслеживания расхода ресурсов...');
        this.monitoring = true;
        this.data = [];
        this.startTime = Date.now();

        this.interval = setInterval(async () => {
            if (!this.monitoring) return;

            try {
                const timestamp = new Date().toISOString();
                const elapsedSeconds = (Date.now() - this.startTime) / 1000;
                const cpu = await si.currentLoad();
                const memory = await si.mem();
                const disk = await si.fsSize();
                const network = await si.networkStats();

                const dataPoint = {
                    timestamp,
                    elapsedSeconds,
                    cpu: {
                        usage: cpu.currentLoad,
                        user: cpu.currentLoadUser,
                        system: cpu.currentLoadSystem
                    },
                    memory: {
                        total: memory.total,
                        used: memory.used,
                        free: memory.free,
                        usagePercent: (memory.used / memory.total) * 100
                    },
                    disk: disk.length > 0 ? {
                        total: disk[0].size,
                        used: disk[0].used,
                        free: disk[0].available,
                        usagePercent: disk[0].use
                    } : null,
                    network: network.length > 0 ? {
                        rx: network[0].rx_sec,
                        tx: network[0].tx_sec
                    } : null
                };

                this.data.push(dataPoint);
                
                console.log(`[${elapsedSeconds.toFixed(0)}с] CPU: ${cpu.currentLoad.toFixed(1)}%, RAM: ${dataPoint.memory.usagePercent.toFixed(1)}%`);

            } catch (error) {
                console.error('Ошибка при сборе данных:', error.message);
            }
        }, intervalMs);
    }

    stopMonitoring() {
        this.monitoring = false;
        if (this.interval) {
            clearInterval(this.interval);
            this.interval = null;
        }
    }

    async generateGraphs(outputDir = './') {
        const width = 1200;
        const height = 600;
        const chartJSNodeCanvas = new ChartJSNodeCanvas({ width, height });

        const timeLabels = this.data.map(d => `${d.elapsedSeconds.toFixed(0)}с`);
        const cpuData = this.data.map(d => d.cpu.usage);
        const ramData = this.data.map(d => d.memory.usagePercent);
        
        const cpuRamBuffer = await chartJSNodeCanvas.renderToBuffer({
            type: 'line',
            data: {
                labels: timeLabels,
                datasets: [
                    {
                        label: 'CPU Usage (%)',
                        data: cpuData,
                        borderColor: 'rgb(255, 99, 132)',
                        backgroundColor: 'rgba(255, 99, 132, 0.1)',
                        tension: 0.1,
                        fill: true,
                        yAxisID: 'y'
                    },
                    {
                        label: 'RAM Usage (%)',
                        data: ramData,
                        borderColor: 'rgb(54, 162, 235)',
                        backgroundColor: 'rgba(54, 162, 235, 0.1)',
                        tension: 0.1,
                        fill: true,
                        yAxisID: 'y'
                    }
                ]
            },
            options: {
                plugins: {
                    title: { display: true, text: 'Динамика использования ресурсов CPU и RAM' },
                    tooltip: { mode: 'index', intersect: false }
                },
                scales: {
                    y: { 
                        title: { display: true, text: 'Использование (%)' }, 
                        min: 0, 
                        max: 100,
                        beginAtZero: true
                    },
                    x: { title: { display: true, text: 'Время (секунды)' } }
                },
                responsive: true,
                maintainAspectRatio: true
            }
        });
        fs.writeFileSync(path.join(outputDir, 'resource_cpu_ram_timeline.png'), cpuRamBuffer);
        console.log(' График 1 сохранён: resource_cpu_ram_timeline.png');

        const cpuUserData = this.data.map(d => d.cpu.user);
        const cpuSystemData = this.data.map(d => d.cpu.system);
        
        const cpuDetailBuffer = await chartJSNodeCanvas.renderToBuffer({
            type: 'line',
            data: {
                labels: timeLabels,
                datasets: [
                    {
                        label: 'CPU User Space (%)',
                        data: cpuUserData,
                        borderColor: 'rgb(75, 192, 192)',
                        backgroundColor: 'rgba(75, 192, 192, 0.1)',
                        tension: 0.1,
                        fill: true
                    },
                    {
                        label: 'CPU System Space (%)',
                        data: cpuSystemData,
                        borderColor: 'rgb(255, 159, 64)',
                        backgroundColor: 'rgba(255, 159, 64, 0.1)',
                        tension: 0.1,
                        fill: true
                    }
                ]
            },
            options: {
                plugins: {
                    title: { display: true, text: 'Детализация использования CPU (User vs System)' }
                },
                scales: {
                    y: { title: { display: true, text: 'CPU Usage (%)' }, min: 0, max: 100 },
                    x: { title: { display: true, text: 'Время (секунды)' } }
                }
            }
        });
        fs.writeFileSync(path.join(outputDir, 'resource_cpu_detail.png'), cpuDetailBuffer);
        console.log(' График 2 сохранён: resource_cpu_detail.png');

        const memUsedMB = this.data.map(d => d.memory.used / 1024 / 1024);
        const memFreeMB = this.data.map(d => d.memory.free / 1024 / 1024);
        const memTotalMB = this.data[0].memory.total / 1024 / 1024;
        
        const memoryBuffer = await chartJSNodeCanvas.renderToBuffer({
            type: 'line',
            data: {
                labels: timeLabels,
                datasets: [
                    {
                        label: 'Используемая память (MB)',
                        data: memUsedMB,
                        borderColor: 'rgb(153, 102, 255)',
                        backgroundColor: 'rgba(153, 102, 255, 0.1)',
                        tension: 0.1,
                        fill: true
                    },
                    {
                        label: 'Свободная память (MB)',
                        data: memFreeMB,
                        borderColor: 'rgb(201, 203, 207)',
                        backgroundColor: 'rgba(201, 203, 207, 0.1)',
                        tension: 0.1,
                        fill: true
                    }
                ]
            },
            options: {
                plugins: {
                    title: { display: true, text: `Использование памяти (всего: ${memTotalMB.toFixed(0)} MB)` }
                },
                scales: {
                    y: { title: { display: true, text: 'Память (MB)' }, beginAtZero: true },
                    x: { title: { display: true, text: 'Время (секунды)' } }
                }
            }
        });
        fs.writeFileSync(path.join(outputDir, 'resource_memory_timeline.png'), memoryBuffer);
        console.log(' График 3 сохранён: resource_memory_timeline.png');

        const analysis = this.analyzeData();
        if (analysis) {
            const statsBuffer = await chartJSNodeCanvas.renderToBuffer({
                type: 'bar',
                data: {
                    labels: ['CPU', 'RAM'],
                    datasets: [
                        {
                            label: 'Среднее использование (%)',
                            data: [analysis.cpu.avg, analysis.memory.avg],
                            backgroundColor: 'rgba(54, 162, 235, 0.6)',
                            borderColor: 'rgb(54, 162, 235)',
                            borderWidth: 1
                        },
                        {
                            label: 'Максимальное использование (%)',
                            data: [analysis.cpu.max, analysis.memory.max],
                            backgroundColor: 'rgba(255, 99, 132, 0.6)',
                            borderColor: 'rgb(255, 99, 132)',
                            borderWidth: 1
                        },
                        {
                            label: 'Минимальное использование (%)',
                            data: [analysis.cpu.min, analysis.memory.min],
                            backgroundColor: 'rgba(75, 192, 192, 0.6)',
                            borderColor: 'rgb(75, 192, 192)',
                            borderWidth: 1
                        }
                    ]
                },
                options: {
                    plugins: {
                        title: { display: true, text: 'Сводная статистика использования ресурсов' }
                    },
                    scales: {
                        y: { title: { display: true, text: 'Использование (%)' }, beginAtZero: true, max: 100 }
                    }
                }
            });
            fs.writeFileSync(path.join(outputDir, 'resource_statistics.png'), statsBuffer);
            console.log(' График 4 сохранён: resource_statistics.png');
        }
    }

    analyzeData() {
        const cpuUsages = this.data.map(d => d.cpu.usage);
        const memoryUsages = this.data.map(d => d.memory.usagePercent);

        const maxCpuIndex = cpuUsages.indexOf(Math.max(...cpuUsages));
        const maxRamIndex = memoryUsages.indexOf(Math.max(...memoryUsages));

        const analysis = {
            duration: this.data.length,
            monitoringSeconds: this.data.length,
            cpu: {
                avg: cpuUsages.reduce((sum, val) => sum + val, 0) / cpuUsages.length,
                max: Math.max(...cpuUsages),
                min: Math.min(...cpuUsages),
                peakAtSeconds: this.data[maxCpuIndex]?.elapsedSeconds || 0
            },
            memory: {
                avg: memoryUsages.reduce((sum, val) => sum + val, 0) / memoryUsages.length,
                max: Math.max(...memoryUsages),
                min: Math.min(...memoryUsages),
                avgUsedMB: this.data.reduce((sum, d) => sum + d.memory.used, 0) / this.data.length / 1024 / 1024,
                peakAtSeconds: this.data[maxRamIndex]?.elapsedSeconds || 0
            }
        };

        console.log('\n АНАЛИЗ ИСПОЛЬЗОВАНИЯ РЕСУРСОВ ');
        console.log(`Длительность анализа: ${analysis.duration} секунд`);
        console.log(`CPU - Среднее: ${analysis.cpu.avg.toFixed(2)}%, Максимум: ${analysis.cpu.max.toFixed(2)}% (на ${analysis.cpu.peakAtSeconds}с), Минимум: ${analysis.cpu.min.toFixed(2)}%`);
        console.log(`Память - Среднее: ${analysis.memory.avg.toFixed(2)}%, Максимум: ${analysis.memory.max.toFixed(2)}% (на ${analysis.memory.peakAtSeconds}с), Минимум: ${analysis.memory.min.toFixed(2)}%`);
        console.log(`Среднее потребление памяти: ${analysis.memory.avgUsedMB.toFixed(2)} MB`);

        return analysis;
    }

    saveData(filename = 'resource_monitoring.json') {
        const filepath = path.join(__dirname, filename);
        const analysis = this.analyzeData();
        fs.writeFileSync(filepath, JSON.stringify({
            metadata: {
                startTime: this.startTime,
                durationSeconds: this.data.length,
                dataPoints: this.data.length
            },
            data: this.data,
            analysis: analysis
        }, null, 2));
        console.log(` Данные сохранены в ${filepath}`);
    }

    async runWithGraphs(durationSeconds = 60, outputDir = './') {
        await this.startMonitoring(1000);
        
        for (let i = 0; i <= durationSeconds; i += 5) {
            await new Promise(resolve => setTimeout(resolve, 5000));
            const percent = (i / durationSeconds * 100).toFixed(0);
            console.log(`   Прогресс: ${percent}% (${i}/${durationSeconds} сек)`);
        }
        
        this.stopMonitoring();
        
        const analysis = this.analyzeData();
        
        this.saveData(`resource_test_${Date.now()}.json`);
        
        await this.generateGraphs(outputDir);
        
        return analysis;
    }

    async monitorJavaProcess() {
        try {
            const processes = await si.processes();
            const javaProcesses = processes.list.filter(p => 
                p.name.toLowerCase().includes('java') || 
                p.command.toLowerCase().includes('java')
            );

            if (javaProcesses.length === 0) {
                console.log('Java процессы не найдены');
                return null;
            }

            console.log('\n=== JAVA ПРОЦЕССЫ ===');
            javaProcesses.forEach(proc => {
                console.log(`PID: ${proc.pid}, CPU: ${proc.cpu}%, Memory: ${proc.mem}%, Command: ${proc.command.substring(0, 100)}...`);
            });

            return javaProcesses;
        } catch (error) {
            console.error('Ошибка при мониторинге Java процессов:', error.message);
            return null;
        }
    }
}

async function runResourceTest(testDurationSeconds = 60) {
    const monitor = new ResourceMonitor();
    await monitor.monitorJavaProcess();
    const analysis = await monitor.runWithGraphs(testDurationSeconds);
    await monitor.monitorJavaProcess();
    return analysis;
}

async function runMonitorWithLoadTest(loadTestDurationSeconds = 30) {
    const monitor = new ResourceMonitor();
    
    console.log('\n ЗАПУСК ВО ВРЕМЯ НАГРУЗОЧНОГО ТЕСТА');
    
    await monitor.startMonitoring(1000);

    const axios = require('axios');
    const loadStart = Date.now();
    
    const promises = [];
    while (Date.now() - loadStart < loadTestDurationSeconds * 1000) {
        for (let i = 0; i < 5; i++) {
            promises.push(
                axios.post('http://localhost:8080/api/code/run', {
                    code: 'System.out.println("load test");',
                    exercise: 1,
                    exerciseId: `load-${Date.now()}`,
                    moduleId: "test",
                    baseUrl: 'http://localhost:5173'
                }).catch(() => {})
            );
        }
        await Promise.all(promises);
        await new Promise(resolve => setTimeout(resolve, 100));
    }
    
    monitor.stopMonitoring();
    
    await monitor.generateGraphs();
    monitor.saveData(`load_test_monitoring_${Date.now()}.json`);
    
    return monitor.analyzeData();
}

if (require.main === module) {
    runMonitorWithLoadTest(30);
}

module.exports = ResourceMonitor;