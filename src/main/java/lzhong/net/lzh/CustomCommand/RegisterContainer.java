package lzhong.net.lzh.CustomCommand;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;

import java.awt.*;
import java.util.ArrayList;

public class RegisterContainer {

    public static ContainerInfo RegisterContainer(BlockEntity c_type, BlockPos c_pos)
    {
        ArrayList<ItemStack> b_content = new ArrayList<>();
        Thread chestInteractionThread = new Thread(() -> {

            // Access the player's open screen handler (chest inventory)
            PlayerEntity player = MinecraftClient.getInstance().player;

            // Delay to let the server sync chest contents
            try {
                Thread.sleep(100); // 500ms delay (adjust as needed)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ScreenHandler handler = player.currentScreenHandler;
            if (handler != null && handler.slots.size() > 41) {
                for (int i = 0; i < handler.slots.size() - 36; i++) {
                    ItemStack stack = handler.getSlot(i).getStack();
                    if (!stack.isEmpty()) {
                        //player.sendMessage(Text.literal("Slot " + i + ": " + stack.getItem().getName().getString()));
                        b_content.add(stack);
                    }
                }
            }
        });
        chestInteractionThread.start();
        ContainerInfo buffer = new ContainerInfo(c_type, c_pos, b_content);
        return buffer;
    }



}

