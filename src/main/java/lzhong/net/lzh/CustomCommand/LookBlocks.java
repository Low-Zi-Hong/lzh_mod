package lzhong.net.lzh.CustomCommand;

import com.mojang.brigadier.context.CommandContext;
import lzhong.net.ClientInitialiser;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class LookBlocks {
    public static void LookToCoor(CommandContext<FabricClientCommandSource> context, BlockPos lookPos,Boolean sendFeedBack) {


        MinecraftClient client = context.getSource().getClient();
        // Get player's current position
        if (client.player != null) {
            double playerX = client.player.getX();
            double playerY = client.player.getEyeY();  // Eye height (not feet)
            double playerZ = client.player.getZ();

            // Calculate the delta between the player's position and the target
            double deltaX = (lookPos.getX() - playerX)+0.5;
            double deltaY = (lookPos.getY() - playerY)+0.5;
            double deltaZ = (lookPos.getZ() - playerZ)+0.5;

            // Calculate yaw and pitch
            float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90);  // Rotation around Y-axis (horizontal)
            float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, Math.sqrt(deltaX * deltaX + deltaZ * deltaZ)));  // Rotation around X-axis (vertical)

            // Apply yaw and pitch to the player
            client.player.setYaw(yaw);
            client.player.setPitch(pitch);

            ClientInitialiser.setHighlightBlockPos(lookPos);
            // Provide feedback to the client
            if(sendFeedBack) context.getSource().getClient().player.sendMessage(Text.literal("Looking at: " + lookPos.toString()),true);
        }


    }


}
