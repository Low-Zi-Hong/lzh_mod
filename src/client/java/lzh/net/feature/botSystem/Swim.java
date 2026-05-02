package lzh.net.feature.botSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import java.util.Queue;

public class Swim {

    public static void swim(BlockPos endPos, Queue<autoBot.ActionSubunit> subTask) {
        // 开启第一帧的游泳任务
        subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 1, new SwimStep(endPos, subTask)));
    }

    private static class SwimStep implements Runnable {
        private final BlockPos endPos;
        private final Queue<autoBot.ActionSubunit> subTask;

        public SwimStep(BlockPos endPos, Queue<autoBot.ActionSubunit> subTask) {
            this.endPos = endPos;
            this.subTask = subTask;
        }

        @Override
        public void run() {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            // 1. 获取玩家与目标的精确坐标
            double pX = client.player.getX();
            double pY = client.player.getY();
            double pZ = client.player.getZ();

            double tX = endPos.getX() + 0.5;
            double tY = endPos.getY(); // 水中目标通常以脚下为准
            double tZ = endPos.getZ() + 0.5;

            // 2. 三维距离判定 (XZ 距离 + Y 距离)
            double distSqXZ = Math.pow(tX - pX, 2) + Math.pow(tZ - pZ, 2);
            double distY = Math.abs(tY - pY);

            // 如果横向距离够近，且高度差在合理范围内（比如 0.5 格），视为到达
            if (distSqXZ < 0.1 && distY < 0.5) {
                // 停止所有动作
                client.options.keyUp.setDown(false);
                client.options.keyJump.setDown(false);
                client.options.keySprint.setDown(false);
                System.out.println("Swam to destination: " + endPos.toShortString());
                return;
            }

            // 3. 转向目标
            float targetYaw = (float) (Math.toDegrees(Math.atan2(tZ - pZ, tX - pX)) - 90);
            client.player.setYRot(targetYaw);

            // 4. 控制按键
            client.options.keyUp.setDown(true);    // 永远向前移动
            client.options.keySprint.setDown(true); // 进入快速游泳模式

            // 5. 【关键】：纵向高度控制
            // 如果目标在上方，按住跳跃键上浮
            if (tY > pY + 0.2) {
                client.options.keyJump.setDown(true);
                client.options.keyShift.setDown(false);
            }
            // 如果目标在下方，松开跳跃键下沉（或者按住 Shift 强行下潜）
            else if (tY < pY - 0.2) {
                client.options.keyJump.setDown(false);
                client.options.keyShift.setDown(true); // 1.13+ 下潜
            }
            // 如果高度差不多，保持中立稳定
            else {
                client.options.keyJump.setDown(false);
                client.options.keyShift.setDown(false);
            }

            // 6. 递归进入下一帧
            subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 1, this));
        }
    }
}