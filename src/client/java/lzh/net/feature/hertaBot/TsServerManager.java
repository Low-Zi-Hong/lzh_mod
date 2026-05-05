package lzh.net.feature.hertaBot;

import java.io.File;
import java.io.IOException;

public class TsServerManager {

    private static Process tsProcess = null;

    // 在你的 Mod 初始化阶段（比如 FMLInitializationEvent 或 Fabric onInitialize）调用这个
    public static void startHiddenTsServer() {
        try {
            // 假设你把编译好的 ts 代码 (server.js) 放在了游戏目录的 ts_bot 文件夹下
            File tsDirectory = new File(System.getProperty("user.dir"), "ts_bot");

            ProcessBuilder pb = new ProcessBuilder("node", "server.js");
            pb.directory(tsDirectory); // 设置工作目录

            // 极其关键：将 Node 的输出重定向到操作系统的 "虚无"（或者你可以定向到一个 log 文件）
            // 如果不处理输出，Node 进程填满缓冲区后会直接卡死！
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD); // 丢弃所有 console.log，保持完全静默

            // 1. 召唤幽灵进程
            tsProcess = pb.start();
            System.out.println("成功在后台隐形启动 TS Server！");

            // 2. 签下“同生共死”契约 (Shutdown Hook)
            // 这样哪怕玩家直接点右上角的 X 关闭游戏，Java 也会在临死前拉着 TS 一起死
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (tsProcess != null && tsProcess.isAlive()) {
                    System.out.println("Java 即将关闭，正在击杀后台 TS 进程...");
                    tsProcess.destroy(); // 强制杀死 Node 进程
                }
            }));

        } catch (IOException e) {
            System.out.println("唤醒 TS Server 失败: " + e.getMessage());
        }
    }
}