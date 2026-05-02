package lzh.net.registry;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lzh.net.Lzh;
import lzh.net.client.LzhClient;
import lzh.net.feature.Astar;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
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
        return 0;
    }

    public static int startBot(CommandContext<FabricClientCommandSource> context) {
        LzhClient.startBot = true;
        return 0;
    }
}