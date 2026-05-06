package lzh.net.feature.botSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.Queue;

public class JumpnPut {

    // 接收目标坐标和子队列
    public static void JumpAndPutBlock(BlockPos pos, Queue<autoBot.ActionSubunit> subTask) {
        // 将跳跃判定动作包装成 Subunit，排进下一个 Tick
        subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 1, new WaitApexAndPlace(pos, subTask)));
    }

    // 核心递归类：起跳 -> 等待最高点 -> 放置
    private static class WaitApexAndPlace implements Runnable {
        private final BlockPos targetPos;
        private final Queue<autoBot.ActionSubunit> subTask;
        private boolean hasJumped = false;

        public WaitApexAndPlace(BlockPos pos, Queue<autoBot.ActionSubunit> subTask) {
            this.targetPos = pos.offset(0,-1,0);
            this.subTask = subTask;
        }

        @Override
        public void run() {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            // 1. 如果还没起跳，先执行跳跃
            if (!hasJumped) {
                if (client.player.onGround()) {
                    client.player.jumpFromGround(); // 使用原版的安全跳跃方法
                }
                hasJumped = true;

                // 把自己重新塞回下一帧，准备开始检查高度
                subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 1, this));
                return;
            }

            // 2. 获取当前的 Y 轴速度 (Mojang mapping 叫 getDeltaMovement)
            double velocityY = client.player.getDeltaMovement().y;

            // 3. 检查是否到达跳跃最高点
            if (velocityY <= 0.005) {
                // 【时机成熟】：到达最高点，或者开始下落！
                System.out.println("Apex reached, placing block at: " + targetPos.toShortString());

                // 【架构神级连招】：直接调用咱们写好的 PlaceBlock！
                // PlaceBlock 内部会自己把放置任务和延迟 100ms 的空任务塞进 subTask。
                PlaceBlock.PlaceBlock(targetPos, subTask);

                // 任务已经优雅地移交给了 PlaceBlock，这里直接 return 结束即可。
                return;
            }

            // 4. 【还没到最高点】：继续等！把自己塞到下一个 Tick！
            subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 1, this));
        }
    }
}