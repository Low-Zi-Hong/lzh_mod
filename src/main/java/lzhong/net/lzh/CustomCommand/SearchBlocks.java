package lzhong.net.lzh.CustomCommand;

import com.mojang.brigadier.context.CommandContext;
import lzhong.net.ClientInitialiser;
import lzhong.net.MainInitialiser;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SearchBlocks {


    public static List<BlockPos> searchForBlocks(CommandContext<FabricClientCommandSource> context, Identifier resourceId, int _range) {

        BlockPos PlayerPos = Objects.requireNonNull(context.getSource().getClient().player).getBlockPos();
        PlayerEntity player = context.getSource().getClient().player;
        Block blockName = Registries.BLOCK.get(resourceId);

        int blockCount = 0;
        List<BlockPos> ResultPosition = new ArrayList<>();

        player.sendMessage(Text.literal(context.getSource().getClient().player.getName().getString() +" Searching:  " + blockName + " at " + PlayerPos.toString()));

        for (int i = PlayerPos.getX() - (_range / 2); i <= PlayerPos.getX() + (_range / 2); i++)
        {
            for (int o = PlayerPos.getZ() -  (_range / 2); o <= PlayerPos.getZ() +  (_range / 2); o++)
            {
                for (int p = PlayerPos.getY() -  (_range / 2); p <= PlayerPos.getY() +  (_range / 2); p++)
                {
                    BlockPos BPos = new BlockPos(i,p,o);
                    BlockState state = context.getSource().getWorld().getBlockState(BPos);

                    //player.sendMessage(Text.literal("Getting block: " + BPos + " is " + state.getBlock().asItem().getName().getString() + " is " + state.isOf(blockName)));

                    if(state.isOf(blockName)){
                        player.sendMessage(Text.literal("Found " + state.getBlock().asItem().getName().getString() + " at " +  BPos.toString())
                                        .styled(style -> style.withClickEvent(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lzh path " + BPos.getX() + " " + BPos.getY() + " " + BPos.getZ()))

                                        )
                                ,false);
                        ResultPosition.add(BPos);
                        blockCount++;

                        break;
                    }
                }
            }
        }

        if(blockCount <= 0) player.sendMessage(Text.literal("No " + blockName.asItem().toString() + " found"),true);
        else player.sendMessage(Text.literal("Total: " + blockCount + " found"));

        return ResultPosition;
    }
}
