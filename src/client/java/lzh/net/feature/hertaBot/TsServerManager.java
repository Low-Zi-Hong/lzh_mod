package lzh.net.feature.hertaBot;

import lzh.net.client.LzhClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.Charsets;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TsServerManager {

    private static Process tsProcess = null;

    // 填入你 GitHub 仓库里的 RAW 链接 (注意看下面避坑指南！)
    private static final String GITHUB_RAW_URL = "https://raw.githubusercontent.com/Low-Zi-Hong/lzh_mod/mc26/JSmain.js";

    // 【新增】：存放所有订阅了日志的“监听者”
    private static final List<LzhClient.TsLogListener> listeners = new ArrayList<>();

    // 任何人想听 TS 的日志，调这个方法注册一下就行
    public static void registerListener(LzhClient.TsLogListener listener) {
        listeners.add(listener);
    }

    // 在你的 Mod 初始化阶段（比如 FMLInitializationEvent 或 Fabric onInitialize）调用这个
    public static void startHiddenTsServer() {
        Thread daemonThread = new Thread(() -> {
            try {
                File tsDirectory = new File(System.getProperty("user.dir"), "ts_bot");
                File scriptFile = new File(tsDirectory, "server.js");

                // 1. 检查母巢是否存在
                if (!tsDirectory.exists()) {
                    tsDirectory.mkdirs(); // 没有文件夹就建一个
                }

                // 2. 核心逻辑：文件不存在？那就从云端拉取！
                if (!scriptFile.exists()) {
                    System.out.println("检测到缺失 TS 大脑，正在从 GitHub 赛博空间召唤...");

                    // 打开网络连接
                    URL url = new URL(GITHUB_RAW_URL);
                    try (InputStream in = url.openStream()) {
                        // 极其硬核的一行代码下载：直接把网络数据流浇灌进本地文件
                        Files.copy(in, scriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    System.out.println("TS 大脑下载完毕，载体注入成功！");
                } else {
                    System.out.println("TS 大脑已存在，准备唤醒...");
                }

                ProcessBuilder pb = new ProcessBuilder("node", "server.js");
                pb.directory(tsDirectory); // 设置工作目录

                // 极其关键：将 Node 的输出重定向到操作系统的 "虚无"（或者你可以定向到一个 log 文件）
                // 如果不处理输出，Node 进程填满缓冲区后会直接卡死！
                pb.redirectErrorStream(true);// 丢弃所有 console.log，保持完全静默

                // 1. 召唤幽灵进程
                tsProcess = pb.start();
                System.out.println("成功在后台隐形启动 TS Server！");



                try {
                    // 【核心修复 1】：强制使用 UTF-8 去读 Node.js 的输出！
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(tsProcess.getInputStream(), Charsets.UTF_8)
                    );

                    String line;
                    while ((line = reader.readLine()) != null) {

                        // 【核心修复 2】：绝对不要闷声发大财！把读到的每一行全部原封不动打印在 Java 控制台！
                        // 如果你连这行都看不到，说明 TS 根本没跑起来！
                        System.out.println("[TS 真实底层输出] -> " + line);


                        // 【核心修复 3】：用纯英文暗号解锁，彻底避开乱码判定！
                        if (line.contains("SERVER_READY_SIGNAL_ACK")) {

                            LzhClient.isTsSeverOnline = true;
//                            System.out.println("🔥🔥🔥 捕捉到全英文握手暗号，状态锁已物理击碎！当前状态: " + LzhClient.isTsSeverOnline);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }

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
        });
        daemonThread.setName("TS-Deploy-Thread");
        daemonThread.setDaemon(true);
        daemonThread.start();
    }

    public static void fireCommand(String actionType, String detail) {
        if (!LzhClient.isTsSeverOnline) {
            System.out.println("⚠️ 赛博大脑未就绪！");
            return;
        }

        String jsonPayload = String.format("{\"action\": \"%s\", \"target\": \"%s\"}", actionType, detail);

        CompletableFuture.runAsync(() -> {
            // 【改动点】：把 BufferedReader 也加进 try 的资源列表里
            try (Socket socket = new Socket("127.0.0.1", 8080);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // 1. 开火发射！
                out.println(jsonPayload);
                System.out.println("✅ 指令已送达，等待 TS 回复...");

                // 2. 阻塞等待回复（因为在异步线程，所以卡在这里没事）
                // ⚠️ 极其注意：TS 那边回信的时候，末尾一定要加 \n，不然这里会死等！
                String reply = in.readLine();

                if (reply != null) {
                    System.out.println("📩 收到 TS 的直接回复: " + reply);

                    // 把回复直接弹到游戏左下角的聊天框里，方便你实机 Debug！
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.execute(() -> { // 确保UI更新回到主线程
                            mc.player.sendSystemMessage(
                                    Component.literal("[TS 回复]" + reply)
                            );
                        });
                    }
                } else {
                    System.out.println("⚠️ TS 挂断了电话，没有留下任何回复。");
                }

            } catch (Exception e) {
                System.out.println("❌ 发送或接收失败: " + e.getMessage());
            }
        });

    }

    public static void fireJson(String jsonPayload) {
        if (!LzhClient.isTsSeverOnline) {
            System.out.println("⚠️ 赛博大脑未就绪！");
            return;
        }

        CompletableFuture.runAsync(() -> {
            // 【改动点】：把 BufferedReader 也加进 try 的资源列表里
            try (Socket socket = new Socket("127.0.0.1", 8080);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // 1. 开火发射！
                out.println(jsonPayload);
                System.out.println("✅ 指令已送达，等待 TS 回复...");

                // 2. 阻塞等待回复（因为在异步线程，所以卡在这里没事）
                // ⚠️ 极其注意：TS 那边回信的时候，末尾一定要加 \n，不然这里会死等！
                String reply = in.readLine();

                if (reply != null) {
                    System.out.println("📩 收到 TS 的直接回复: " + reply);

                    // 把回复直接弹到游戏左下角的聊天框里，方便你实机 Debug！
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.execute(() -> { // 确保UI更新回到主线程
                            mc.player.sendSystemMessage(
                                    Component.literal("[TS 回复]" + reply)
                            );
                        });
                    }
                } else {
                    System.out.println("⚠️ TS 挂断了电话，没有留下任何回复。");
                }

            } catch (Exception e) {
                System.out.println("❌ 发送或接收失败: " + e.getMessage());
            }
        });
    }

    // 获取当前世界的连接信息 (返回格式: "host:port"，如果无法连接返回 null)
    public static String getCurrentWorldAddress() {
        Minecraft mc = Minecraft.getInstance();

        // 1. 玩家在多人服务器里 (Server)
        if (mc.getCurrentServer() != null) {
            String ipRaw = mc.getCurrentServer().ip;
            // 有些服务器 IP 自带端口 (比如 mc.xxx.com:25566)，有些没有 (默认 25565)
            if (!ipRaw.contains(":")) {
                return ipRaw + ":25565";
            }
            return ipRaw;
        }

        // 2. 玩家在单机世界里 (Singleplayer)
        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            // 极其致命的检查：单人世界必须对局域网开放！
            if (mc.getSingleplayerServer().isPublished()) {
                int lanPort = mc.getSingleplayerServer().getPort();
                return "127.0.0.1:" + lanPort; // 本地局域网 IP
            } else {
                System.out.println("⚠️ 召唤失败：单人世界未对局域网开放 (Open to LAN)！");
                return null;
            }
        }

        return null; // 玩家在主菜单
    }
}