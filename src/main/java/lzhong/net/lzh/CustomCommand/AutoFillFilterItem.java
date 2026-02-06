package lzhong.net.lzh.CustomCommand;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class AutoFillFilterItem {

    public static void fillFilterItem(MinecraftClient client) {
        Thread FillItemThread = new Thread(() -> {

            if (client.player == null || client.player.currentScreenHandler == null) return;

// Get the player's current position
            Vec3d playerPosition = client.player.getPos();

// Get the position of the block directly below the player (subtract 1 from the Y coordinate)
            BlockPos targetBlockPos = new BlockPos((int)(playerPosition.x -0.5), (int)playerPosition.y - 1, (int)(playerPosition.z-0.5));

// Create the BlockHitResult for interacting with the block below
            BlockHitResult hitResult = new BlockHitResult(playerPosition, Direction.DOWN, targetBlockPos, false);

// Now simulate a left-click on the block below (the hopper in this case)
            client.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult,1));



            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Check if the screen is a hopper UI
            if (!(client.player.currentScreenHandler instanceof HopperScreenHandler handler)) {
                //client.player.sendMessage(Text.literal("Not a hopper UI!"), true);
                return;
            }

            ClientPlayerEntity player = client.player;
//            if (player.getInventory().getStack(40).isEmpty()) {
//                player.sendMessage(Text.literal("Hold an item to set as the filter!"), true);
//                return;
//            }


            Slot slot = handler.getSlot(1);
            ItemStack iS = slot.getStack();

            if(!iS.isEmpty()) return;




            // Auto-fill the hopper with the filter item
            if (client.interactionManager != null) {
                // First, pick up the filter item (simulate a pickup action)
                client.interactionManager.clickSlot(
                        handler.syncId, // Sync ID for the current screen
                        40, // Slot index in the hopper
                        0, // Mouse button (0 = left click)
                        SlotActionType.PICKUP, // Action type (pick up item)
                        player
                );

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                for (int slotIndex = 1; slotIndex < 5; slotIndex++) { // Hopper slots are from 1 to 4

                    // Place the filter item in the hopper slot
                    client.interactionManager.clickSlot(
                            handler.syncId, // Sync ID for the current screen
                            slotIndex, // Slot index in the hopper
                            1, // Mouse button (0 = left click)
                            SlotActionType.PICKUP, // Action type (place item)
                            player
                    );

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
                // First, pick up the filter item (simulate a pickup action)
                client.interactionManager.clickSlot(
                        handler.syncId, // Sync ID for the current screen
                        40, // Slot index in the hopper
                        0, // Mouse button (0 = left click)
                        SlotActionType.PICKUP, // Action type (pick up item)
                        player
                );

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // First, pick up the filter item (simulate a pickup action)
                client.interactionManager.clickSlot(
                        handler.syncId, // Sync ID for the current screen
                        39, // Slot index in the hopper
                        0, // Mouse button (0 = left click)
                        SlotActionType.PICKUP, // Action type (pick up item)
                        player
                );

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Place the filter item in the hopper slot
                client.interactionManager.clickSlot(
                        handler.syncId, // Sync ID for the current screen
                        0, // Slot index in the hopper
                        1, // Mouse button (0 = left click)
                        SlotActionType.PICKUP, // Action type (place item)
                        player
                );

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // First, pick up the filter item (simulate a pickup action)
                client.interactionManager.clickSlot(
                        handler.syncId, // Sync ID for the current screen
                        39, // Slot index in the hopper
                        0, // Mouse button (0 = left click)
                        SlotActionType.PICKUP, // Action type (pick up item)
                        player
                );

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }


            } else {
                System.out.println("Interaction manager is null");
                return;
            }
            player.sendMessage(Text.literal("Hopper auto-filled with filter item!"), true);
        });

        FillItemThread.start();
    }



}
