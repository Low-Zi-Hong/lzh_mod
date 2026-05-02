package lzh.net.feature.botSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Queue;
import net.minecraft.core.BlockPos;

public class PlaceBlock {
    public static void PlaceBlock(BlockPos pos, Queue<autoBot.ActionSubunit> subTask) {

        // 第一步：把“放置方块”的动作安排在下一个 Tick 执行
        subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 1, () -> {
            Minecraft client = Minecraft.getInstance();

            // 安全检查
            if (client.player == null || client.gameMode == null || client.level == null) return;

            // 1. 计算准星命中的坐标点 (目标方块的中心)
            Vec3 hitPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            // 2. 构建 HitResult (假设我们点的是方块的上表面)
            BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, pos, false);

            // 3. 核心绝杀：使用原版自带的 gameMode (InteractionManager) 发送放置指令！
            // 绝对不要 new 新的！
            client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hitResult);

            // 4. (可选但推荐) 挥动一下手臂，让放置动作有视觉反馈，防作弊插件也更喜欢
            client.player.swing(InteractionHand.MAIN_HAND);

            // 5. 替代 Thread.sleep(100) 的神级操作：
            // 我们在子队列里再塞入一个 "空任务"，时间定在当前时间 + 2 Ticks (约等于 100ms)
            subTask.add(new autoBot.ActionSubunit(autoBot.GlobalTick + 2, () -> {
                // 这里什么都不用写。
                // 它的唯一作用就是占着子队列，让主队列 (actionQueue) 延后 2 Ticks 再拿出下一个动作。
            }));
        }));
    }
}