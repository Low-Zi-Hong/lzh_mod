package lzh.net.feature.botSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;

import java.util.Queue;

public class BreakBlock {

    public static void setBreakBlock(BlockPos pos, Queue<autoBot.ActionSubunit> subTask) {
        // 第一步：开始挖掘
        subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 1, new BreakingTask(pos, subTask)));
    }

    private static class BreakingTask implements Runnable {
        private final BlockPos pos;
        private final Queue<autoBot.ActionSubunit> subTask;
        private boolean started = false;

        public BreakingTask(BlockPos pos, Queue<autoBot.ActionSubunit> subTask) {
            this.pos = pos;
            this.subTask = subTask;
        }

        @Override
        public void run() {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.gameMode == null || client.level == null) return;

            // 1. 检查方块是否已经被挖掉了（变为空气或者可替换方块）
            if (autoBot.emptyBlockPos(client.level.getBlockState(pos))) {
                System.out.println("Block broken successfully at: " + pos.toShortString());

                // 停止挖掘动画
                client.gameMode.stopDestroyBlock();
                return; // 结束递归，subTask 清空，主队列继续
            }

            // 2. 如果还没开始，先触发一次 attackBlock
            if (!started) {
                // 自动选择朝向：看向方块的中心，或者简单起见用 UP/DOWN
                client.gameMode.startDestroyBlock(pos, Direction.UP);
                client.player.swing(InteractionHand.MAIN_HAND);
                started = true;
            }

            // 3. 核心：每 Tick 更新一次挖掘进度
            // continueDestroyBlock 会返回是否仍在挖掘
            boolean stillBreaking = client.gameMode.continueDestroyBlock(pos, Direction.UP);

            if (stillBreaking) {
                client.player.swing(InteractionHand.MAIN_HAND); // 持续挥手动画
                // 【递归】：下一帧继续敲
                subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 1, this));
            } else {
                // 如果返回 false，通常意味着方块已经爆了，或者挖掘被打断
                // 这里我们不做处理，下一帧进入时会自动被 emptyBlockPos 检查捕获
                subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 1, this));
            }
        }
    }
}