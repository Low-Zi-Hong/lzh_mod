package lzh.net.feature.botSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.Queue;

public class Walking {
    // 入口方法，被 actionQueue 调用
    public static void WalkToBlock(BlockPos endPos, boolean jump, Queue<autoBot.ActionSubunit> subTask) {
        // 把第一步塞进下一个 Tick 执行
        subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 1, new WalkStep(endPos, jump, subTask)));
    }

    // 核心逻辑：实现 Runnable 的内部类，方便它“自我复制”
    private static class WalkStep implements Runnable {
        private final BlockPos endPos;
        private final boolean jump;
        private final Queue<autoBot.ActionSubunit> subTask;

        public WalkStep(BlockPos endPos, boolean jump, Queue<autoBot.ActionSubunit> subTask) {
            this.endPos = endPos;
            this.jump = jump;
            this.subTask = subTask;
        }

        @Override
        public void run() {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            // 1. 获取玩家和目标的中心点坐标
            double playerX = client.player.getX();
            double playerZ = client.player.getZ();
            double targetX = endPos.getX() + 0.5;
            double targetZ = endPos.getZ() + 0.5;

            // 2. 计算距离的平方 (比算 Math.sqrt 性能高很多)
            double distSq = Math.pow(targetX - playerX, 2) + Math.pow(targetZ - playerZ, 2);

            // 3. 检查是否到达终点 (横向距离小于 0.2 格视为到达)
            if (distSq < 0.04) {
                // 【到达终点】：松开按键，刹车！
                client.options.keyUp.setDown(false);
                System.out.println("Reached: " + endPos.toShortString());

                // ⚠️ 注意：这里我们直接 return，不再往 subTask 里加新任务了。
                // 此时 subTask 变为空，主队列 actionQueue 就会自动推进到下一个大动作！
                return;
            }

            // 4. 【未到达终点】：计算偏航角 (Yaw) 并看向目标
            float targetYaw = (float) (Math.toDegrees(Math.atan2(targetZ - playerZ, targetX - playerX)) - 90);
            client.player.setYRot(targetYaw); // 让玩家转头

            // 5. 模拟按下 "W" 键 (前进)
            client.options.keyUp.setDown(true);

            // 6. 跳跃逻辑：如果需要跳跃，且玩家正踩在地上
            if ((jump && client.player.onGround() || endPos.getY() > client.player.getY())) {
                client.player.jumpFromGround();
            }

            // 7. 【终极绝杀】：这一步走完了，还没到终点怎么办？
            // 把自己重新包装成一个新的 ActionSubunit，塞入下一个 Tick！
            subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 1, this));
        }
    }
}
