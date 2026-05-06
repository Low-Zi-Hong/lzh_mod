package lzh.net.registry;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lzh.net.Lzh;
import lzh.net.client.LzhClient;
import lzh.net.feature.Astar;
import lzh.net.feature.hertaBot.TsServerManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import static lzh.net.client.LzhClient.thressholdDivDist;

public class clientCommand{

    //all other registered command body sits here
    public static int navigate(CommandContext<FabricClientCommandSource> context){

        LzhClient.blocKPosToRender.clear();
        BlockPos targetPos = new BlockPos(IntegerArgumentType.getInteger(context, "x"),IntegerArgumentType.getInteger(context, "y"),IntegerArgumentType.getInteger(context, "z"));
        context.getSource().sendFeedback(Component.literal("targetPos set to " + targetPos.toShortString()));
        //try {
            LzhClient.startPos = context.getSource().getPlayer().blockPosition();
            LzhClient.endPos = targetPos;

            try{
                thressholdDivDist = IntegerArgumentType.getInteger(context,"thres");
            } catch (Exception e)
            {
                context.getSource().getPlayer().sendSystemMessage(Component.literal("Defaulting the thresshold to 24 blocks."));
            }

            Astar.Start(LzhClient.startPos,LzhClient.endPos,thressholdDivDist);
            //Astar.AstarAlgorithm(context.getSource().getPlayer().blockPosition(),targetPos,false);
        //} catch (InterruptedException e) {
        //    throw new RuntimeException(e);
        //}

        return 1;
    }

    public static int debug(CommandContext<FabricClientCommandSource> context) {
        if(LzhClient.isCalculating) {
            context.getSource().getPlayer().sendSystemMessage(Component.literal("true"));
        }else {
            context.getSource().getPlayer().sendSystemMessage(Component.literal("false"));
        }

        context.getSource().getPlayer().sendSystemMessage(Component.literal(String.valueOf((LzhClient.blocKPosToRender.size()))));
        context.getSource().getPlayer().sendSystemMessage(Component.literal(String.valueOf((LzhClient.endPos))));
        context.getSource().getPlayer().sendSystemMessage(Component.literal(String.valueOf((LzhClient.tempEndPos))));
        context.getSource().getPlayer().sendSystemMessage(Component.literal(String.valueOf((LzhClient.startBot))));

        TsServerManager.fireCommand("testing","test");

        return 0;
    }

    public static int startBot(CommandContext<FabricClientCommandSource> context) {
        LzhClient.startBot = true;
        return 0;
    }

    public static int summonLittleBot(CommandContext<FabricClientCommandSource> context) {
        // 在你之前写的手动触发器 (!!fire) 或者别的什么地方调用：
        String address = TsServerManager.getCurrentWorldAddress();

        if (address != null) {
            String[] parts = address.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            // 现在的指令变成了极其高级的动态指令：
            String jsonPayload = String.format(
                    "{\"action\": \"SUMMON_BOT\", \"botName\": \"CyberBot_01\", \"host\": \"%s\", \"port\": %d}",
                    host, port
            );

            // 把这条带有 IP 和端口的 JSON 射给 TS！
            TsServerManager.fireJson(jsonPayload); // 假设你弄了个可以直接发 json 字符串的方法
        } else {
            // 提示玩家开局域网
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Pls open port first!"));
        }

        return 0;
    }

    public static int restartServer(CommandContext<FabricClientCommandSource> context) {
        TsServerManager.startHiddenTsServer();
        return 0;
    }

    public static int summonBotWithName(CommandContext<FabricClientCommandSource> context) {
        // 在你之前写的手动触发器 (!!fire) 或者别的什么地方调用：
        String address = TsServerManager.getCurrentWorldAddress();
        String name = StringArgumentType.getString(context,"name");

        if (address != null) {
            String[] parts = address.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            // 现在的指令变成了极其高级的动态指令：
            String jsonPayload = String.format(
                    "{\"action\": \"SUMMON_BOT\", \"botName\": \"%s\", \"host\": \"%s\", \"port\": %d}",
                    name,host, port
            );

            // 把这条带有 IP 和端口的 JSON 射给 TS！
            TsServerManager.fireJson(jsonPayload); // 假设你弄了个可以直接发 json 字符串的方法
        } else {
            // 提示玩家开局域网
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Pls open port first!"));
        }

        return 0;
    }

    public static int fireCommand(CommandContext<FabricClientCommandSource> context) {
        String command = StringArgumentType.getString(context,"message");
        TsServerManager.fireCommand("CHAT",command.replace("\"",""));
        return 0;
    }
}