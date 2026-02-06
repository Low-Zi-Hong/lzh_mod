package lzhong.net.lzh.CustomCommand;

import com.mojang.brigadier.context.CommandContext;
import lzhong.net.ClientInitialiser;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class FindItem {

//    public static int locateChest(CommandContext<FabricClientCommandSource> context)
//    {
//        List<BlockPos> BlockList = SearchBlocks.searchForBlocks(context, Identifier.tryParse("minecraft:chest"),6);
//        ClientPlayerEntity player = MinecraftClient.getInstance().player;
//        MinecraftClient client = MinecraftClient.getInstance();
//
//        // Simulate a right-click interaction on the chest
//        if(BlockList.isEmpty())
//        {
//            return 0;
//        }
//
//        player.sendMessage(Text.literal("Searching For item!" + BlockList.size()));
//
//            Thread chestInteractionThread = new Thread(() -> {
//
//                for (BlockPos chestPos : BlockList) {
//
//                    // Simulate opening the chest
//                    LookBlocks.LookToCoor(context, chestPos,false);
//                    BlockHitResult hitResult = new BlockHitResult(player.getPos(), Direction.UP, chestPos, true);
//                    client.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult,0));
//
//                    // Delay to let the server sync chest contents
//                    try {
//                        Thread.sleep(500); // 500ms delay (adjust as needed)
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    // Access the player's open screen handler (chest inventory)
//                    ScreenHandler handler = player.currentScreenHandler;
//                    if (handler != null && handler.slots.size() > 0) {
//                        for (int i = 0; i < handler.slots.size() - 36; i++) {
//                            ItemStack stack = handler.getSlot(i).getStack();
//                            if (!stack.isEmpty()) {
//                                player.sendMessage(Text.literal("Slot " + i + ": " + stack.getItem().getName().getString()));
//                            }
//                        }
//                    }
//
//                    // Send packet to close the chest screen
//                    client.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(handler.syncId));
//                    client.interactionManager.clickButton(handler.syncId, 0);
//
//                    // Manually reset client-side state
//                    player.closeHandledScreen();
//
//                    // Ensure the player can move again
//                    client.setScreen(null);
//
//                    // Delay to let the server sync chest contents
//                    try {
//                        Thread.sleep(1000); // 500ms delay (adjust as needed)
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            );
//            chestInteractionThread.start();
//        return 0;
//    }

    public static int SearchItem(CommandContext<FabricClientCommandSource> context, String item)
    {
        String itemString = item;
        if (!itemString.startsWith("minecraft:")) {
            itemString = "minecraft:" + itemString; // Prepend if missing
        }

        Identifier ItemId = Identifier.tryParse(itemString);
        Item item_item = Registries.ITEM.get(ItemId);

        ItemStack itemToSearch = new ItemStack(item_item);

        Thread SearchItem = new Thread(() -> {
            for(int i = 0; i <ClientInitialiser.containerList.size();i++)
            {
                ArrayList<ItemStack> _buffer = ClientInitialiser.containerList.get(i).containerContent;

                for (int o = 0; o < _buffer.size(); o++)
                {
                    if(itemToSearch.itemMatches(_buffer.get(o).getRegistryEntry()))
                    {
                        BlockPos pos = ClientInitialiser.containerList.get(i).containerPos;
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Found item at: "
                        + pos.toShortString() + " slot " + o
                        ).styled(style -> style.withClickEvent(
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lzh highlight " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))

                                )
                                ,false);
                    }
                }


            }







        });

        SearchItem.start();

        return 0;
    }

}
