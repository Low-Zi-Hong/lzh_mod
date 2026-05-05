import * as net from 'net';

const PORT = 8080;

// 创建一个 TCP 服务器
const server = net.createServer((socket) => {
    console.log('\n[TS Server] 🟢 侦测到新连接！Java 指挥官已接入。');

    // 监听 Java 发来的数据
    socket.on('data', (data) => {
        // 记得用 trim() 去掉末尾的换行符 \n
        const message = data.toString().trim();
        console.log(`[TS Server] 📩 收到原始报文: ${message}`);

        // 尝试把它当成 JSON 解析
        try {
            const command = JSON.parse(message);
            console.log(`[TS Server] ⚙️ JSON 解析成功！`);
            console.log(`          -> 动作 (Action): ${command.action}`);
            console.log(`          -> 目标 (Target): X=${command.target.x}, Z=${command.target.z}`);
            
            // 收到后，顺便给 Java 回个信，证明我活着
            socket.write('{"status": "ACK", "message": "指令已确认执行"}\n');

        } catch (error) {
            console.log(`[TS Server] ⚠️ 警告：收到的不是标准 JSON 格式。`);
        }
    });

    // 监听断开连接
    socket.on('close', () => {
        console.log('[TS Server] 🔴 Java 指挥官已断开连接。等待下一次召唤...\n');
    });

    // 防止因为网络闪断导致 Node.js 崩溃
    socket.on('error', (err) => {
        console.error('[TS Server] ❌ 连接发生错误:', err.message);
    });
});

// 启动监听
server.listen(PORT, () => {
    console.log('======================================');
    console.log(`🚀 赛博召唤阵已激活！`);
    console.log(`📡 正在监听端口: ${PORT}`);
    console.log(`⏳ 等待 Java 端的 Socket 连接...`);
    console.log('======================================');
});