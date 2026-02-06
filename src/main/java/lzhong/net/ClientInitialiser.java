package lzhong.net;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lzhong.net.lzh.CustomCommand.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClientInitialiser implements ClientModInitializer {

    private static BlockPos highlightBlockPos = null;
    private static int SearchBlockRange = 64;
    private static float red = 1.0f;
    private static float green = 1.0f;
    private static float blue = 1.0f;
    private static float alpha = 0.4f;

    private static List<RenderUnit> PosToRender = new ArrayList<>(0);
    public static BlockPos TargetPosition = new BlockPos(0,0,0);

//    public float PlayerHealth = 1;
//    private long firstEnterTime = -1;
    //safety
    private static String PlayerName = "Dev";

    //chest
    private boolean wasChestOpen = false;
    public static ArrayList<ContainerInfo> containerList = new ArrayList<>();
    private boolean StartRecording = true;

    //auto fill hopper with filter Item
    private static KeyBinding fillHopperKey;


    @Override
    public void onInitializeClient() {
        // Registering the client-side command

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                registerCommands(dispatcher)
        );

        HighlightBlocks.registerRenderEvent();
        RenderPath.registerRenderEvent();
        //BlockOutline.registerRenderEvent();

        //fill hopper
        fillHopperKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lzh.fill_hopper", // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H, // Default key
                "category.lzh" // Category
        ));


        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player != null && !PosToRender.isEmpty()) {
                trackPlayerPosition(client);
            }
        });
        //ClientTickEvents.END_CLIENT_TICK.register(this::trackPlayerHealth);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;

            // Check if a ChestScreen (chest GUI) is currently open
            if (minecraftClient.currentScreen instanceof GenericContainerScreen || minecraftClient.currentScreen instanceof ShulkerBoxScreen) {
                if (!wasChestOpen) { // If this is the first tick the screen is open
                    wasChestOpen = true;

                    BlockHitResult hitResult = (BlockHitResult) player.raycast(5.0, 0.0F, false);

                    BlockPos chestPos = hitResult.getBlockPos();

                    BlockEntity containerType =  client.world.getBlockEntity(chestPos);

                    for(ContainerInfo i : containerList)
                    {
                        if(i.containerPos.equals(chestPos)){
                            containerList.remove(i);
                            break;
                        }
                    }
                    if(StartRecording) {
                        containerList.add(RegisterContainer.RegisterContainer(containerType, chestPos));
                        //player.sendMessage(Text.literal(containerList.getLast().containerPos.toShortString()));

                        minecraftClient.player.sendMessage(Text.literal("We had recorded a chest at " + chestPos.toShortString()), true);
                        //player.sendMessage(Text.literal("we have" + containerList.size() + " containers recorded!") );
                    }
                }
            } else {
                wasChestOpen = false; // Reset when the chest screen is closed
            }

            //if(minecraftClient.currentScreen instanceof GenericContainerScreen || minecraftClient.currentScreen instanceof HopperScreen)

            if (fillHopperKey.wasPressed())
            {
                System.out.println("keybind pressed!");
                AutoFillFilterItem.fillFilterItem(client);

            }


        });


    }

    private void trackPlayerPosition(MinecraftClient client) {
        BlockPos playerPos = client.player.getBlockPos();

        for (int i = 0; i < PosToRender.size() && !PosToRender.isEmpty(); i++)
        {
            //client.player.sendMessage(Text.literal(PosToRender.get(i).Position.toShortString() + "  "));
            if(PosToRender.get(i).Position.equals(playerPos) && PosToRender.size() >= 2)
            {

                PosToRender.remove(i);
            }
            if(playerPos.equals(TargetPosition))
            {
                PosToRender.clear();
            }
        }

    }

//    private void trackPlayerHealth(MinecraftClient client)
//    {
//        if (client.player == null) {
//            return; // Exit if the player is not initialized
//        }
//
//        // Track the first time this method is called
//        if (firstEnterTime == -1) {
//            firstEnterTime = System.currentTimeMillis(); // Record the current time
//            return;
//        }
//        // Wait for 5 seconds (5000 milliseconds) after entering
//        if (System.currentTimeMillis() - firstEnterTime < 10000) {
//            return; // Skip until 5 seconds have passed
//        }
//
//
//        PlayerHealth = client.player.getHealth();
//
//        if(PlayerHealth <= 2.0f)
//        {
//            client.player.sendMessage(Text.literal("Health Too low!!!"));
//
//            boolean totem = false;
//
//            for (ItemStack item : client.player.getInventory().main) { // Iterates over the main inventory
//                if (item.getItem() == Items.TOTEM_OF_UNDYING) {
//                    totem = true;
//                    break; // No need to check further if a totem is found
//                }
//            }
//            if(!totem){
//            client.getNetworkHandler().getConnection().disconnect(Text.literal("Your health is too low!"));
//            firstEnterTime = -1;
//        }
//        }
//    }


    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        // Use a string argument instead of IdentifierArgumentType
        dispatcher.register(ClientCommandManager.literal("lzh")
                .then(ClientCommandManager.literal("find")
                        .then(ClientCommandManager.argument("block", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    // Suggest block IDs
                                    for (Identifier blockId : Registries.BLOCK.getIds()) {
                                        builder.suggest(blockId.toString().substring(10));
                                    }
                                    return builder.buildFuture();
                                })// Accepts a string
                                .then(ClientCommandManager.argument("highlight", BoolArgumentType.bool())
                                .executes(context -> {

                                    if(context.getSource().getPlayer().getName().getString().equals(PlayerName) || PlayerName.equals("Dev")) {

                                        // Get the FabricClientCommandSource
                                        FabricClientCommandSource source = context.getSource();

                                        // Retrieve the string from the argument
                                        String resourceIdString = StringArgumentType.getString(context, "block").trim();
                                        boolean highlight_or_not = BoolArgumentType.getBool(context, "highlight");

                                        if (!resourceIdString.startsWith("minecraft:")) {
                                            resourceIdString = "minecraft:" + resourceIdString; // Prepend if missing
                                        }
                                        // Convert the string to an Identifier
                                        Identifier resourceId = Identifier.tryParse(resourceIdString);
                                        ;
                                        //try {
                                        //    resourceId = Identifier.ofVanilla(resourceIdString);
                                        //} catch (Exception e) {
                                        //    source.sendFeedback(Text.literal("Invalid resource ID!"));
                                        //    return 0; // Return error status
                                        //}
                                        List<BlockPos> ResultPosition = new ArrayList<>();

                                        if (resourceId == null || !Registries.BLOCK.containsId(resourceId)) {
                                            context.getSource().sendFeedback(Text.literal("Block not found: " + resourceId));
                                            return 0; // Indicates failure
                                        } else {
                                             ResultPosition = SearchBlocks.searchForBlocks(context, resourceId,ClientInitialiser.getBlockSearchRange());
                                        }

                                        if(!ResultPosition.isEmpty() && highlight_or_not)
                                        {
                                            List<RenderUnit> RenderEveryTargetBlock = new ArrayList<>();
                                            for (BlockPos i :ResultPosition)
                                            {
                                                RenderEveryTargetBlock.add(new RenderUnit(context,i));
                                            }
                                            SetBlockPosListToRender(RenderEveryTargetBlock);
                                        }
                                        // Send feedback to the client
                                        //mu class


                                        return 1;  // Return success status
                                    }
                                    else context.getSource().getPlayer().sendMessage(Text.literal("Please Contact LZH to activate ur account!! \n Discord: lozhong " +
                                                                                "\nEmail: lowzihong11@gmail.com"));
                                    return 0;
                                })
                        )
                        )
                )
                .then(ClientCommandManager.literal("look").then(ClientCommandManager.argument("x", IntegerArgumentType.integer())  // X coordinate
                                .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())  // Y coordinate
                                        .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())  // Z coordinate
                                                .executes(context -> {

                                                            if (context.getSource().getPlayer().getName().getString().equals(PlayerName) || PlayerName.equals("Dev")) {


                                                                // Get the FabricClientCommandSource
                                                                FabricClientCommandSource source = context.getSource();

                                                                // Extract block position arguments (x, y, z)
                                                                int x = IntegerArgumentType.getInteger(context, "x");
                                                                int y = IntegerArgumentType.getInteger(context, "y");
                                                                int z = IntegerArgumentType.getInteger(context, "z");

                                                                // Create a BlockPos with the provided coordinates
                                                                BlockPos lookPos = new BlockPos(x, y, z);

                                                                LookBlocks.LookToCoor(context, lookPos, false);
                                                                return 1;
                                                            } else
                                                                context.getSource().getPlayer().sendMessage(Text.literal("Please Contact LZH to activate ur account!! \n Discord: lozhong " +
                                                                        "\nEmail: lowzihong11@gmail.com"));
                                                            return 0;
                                                        }

                                                )
                                        )
                                )
                        )


                ).then(ClientCommandManager.literal("highlight")
                        .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())  // X coordinate
                                .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())  // Y coordinate
                                        .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                                                .executes(context -> {

                                                    if (context.getSource().getPlayer().getName().getString().equals(PlayerName) || PlayerName.equals("Dev")) {


                                                        // Extract coordinates
                                                        int x = IntegerArgumentType.getInteger(context, "x");
                                                        int y = IntegerArgumentType.getInteger(context, "y");
                                                        int z = IntegerArgumentType.getInteger(context, "z");

                                                        BlockPos lookPos = new BlockPos(x, y, z);

                                                        // Set the block position to be highlighted
                                                        ClientInitialiser.setHighlightBlockPos(lookPos);

                                                        // Provide feedback to the client
                                                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Highlighting block at: " + lookPos.toString()),true);

                                                        return 1;
                                                    } else
                                                        context.getSource().getPlayer().sendMessage(Text.literal("Please Contact LZH to activate ur account!! \n Discord: lozhong " +
                                                                "\nEmail: lowzihong11@gmail.com"));
                                                    return 0;
                                                })
                                        )


                                )
                        )
                ).then(ClientCommandManager.literal("help").executes(context -> {
                            context.getSource().sendFeedback(Text.literal(("Welcome to use the mod type /lzh <function> \n" +
                                    "Function list: \n" +
                                    "find <block name> : find block at range\n" +
                                    "look <x> <y> <z> : \n" +
                                    "highlight <x> <y> <z>\n\n" +
                                    "If have any problem or need any further aids please contact lzh, \n" +
                                    "Discord: lozhong\n" +
                                    "Email: lowzihong11@gmail.com")));


                            return 1;
                        })
                ).then(ClientCommandManager.literal("setting")
                        .then(ClientCommandManager.literal("find")
                                .then(ClientCommandManager.argument("range", IntegerArgumentType.integer())
                                        .executes(context -> {

                                                    if (context.getSource().getPlayer().getName().getString().equals(PlayerName) || PlayerName.equals("Dev")) {

                                                        int x = IntegerArgumentType.getInteger(context, "range");
                                                        setBlockSearchRange(x);
                                                        context.getSource().sendFeedback(Text.literal("Succesfully set range to: " + getBlockSearchRange()));
                                                        return 1;
                                                    } else
                                                        context.getSource().getPlayer().sendMessage(Text.literal("Please Contact LZH to activate ur account!! \n Discord: lozhong " +
                                                                "\nEmail: lowzihong11@gmail.com"));
                                                    return 0;
                                                }
                                        )
                                )
                        ).then(ClientCommandManager.literal("highlight")
                                .then(ClientCommandManager.argument("red", FloatArgumentType.floatArg())  // X coordinate
                                        .then(ClientCommandManager.argument("green", FloatArgumentType.floatArg())  // Y coordinate
                                                .then(ClientCommandManager.argument("blue", FloatArgumentType.floatArg())
                                                        .then(ClientCommandManager.argument("alpha", FloatArgumentType.floatArg())
                                                                .executes(context -> {

                                                                            if (context.getSource().getPlayer().getName().getString().equals(PlayerName) || PlayerName.equals("Dev")) {

                                                                                // Extract coordinates
                                                                                float _red = FloatArgumentType.getFloat(context, "red");
                                                                                float _green = FloatArgumentType.getFloat(context, "green");
                                                                                float _blue = FloatArgumentType.getFloat(context, "blue");
                                                                                float _alpha = FloatArgumentType.getFloat(context, "alpha");
                                                                                setHighlightColor(_red, _green, _blue, _alpha);
                                                                                context.getSource().getPlayer().sendMessage(Text.literal("Succesfully set color to: " + getRed() + " " + getGreen() + " " + getBlue() + " " + getAlpha()),true);
                                                                                return 1;
                                                                            } else
                                                                                context.getSource().getPlayer().sendMessage(Text.literal("Please Contact LZH to activate ur account!! \n Discord: lozhong " +
                                                                                        "\nEmail: lowzihong11@gmail.com"));
                                                                            return 0;
                                                                        }

                                                                )
                                                        )


                                                )

                                        )
                                )
                        ).then(ClientCommandManager.literal("StartRecordChest")
                                .then(ClientCommandManager.argument("toogle", BoolArgumentType.bool())
                                        .executes(context -> {

                                            StartRecording = BoolArgumentType.getBool(context,"toogle");


                                            return 0;
                                        })))
                ).then(ClientCommandManager.literal("path")
                        .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())  // X coordinate
                                .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())  // Y coordinate
                                        .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                                                .executes(context -> {

                                                    if (context.getSource().getPlayer().getName().getString().equals(PlayerName) || PlayerName.equals("Dev")) {

                                                        // Extract coordinates
                                                        ClientInitialiser.ClearPosList();

                                                        int x = IntegerArgumentType.getInteger(context, "x");
                                                        int y = IntegerArgumentType.getInteger(context, "y");
                                                        int z = IntegerArgumentType.getInteger(context, "z");

                                                        BlockPos start = new BlockPos(x, y, z);

                                                        ClientInitialiser.SetBlockPosListToRender(new ArrayList<RenderUnit>(0));
                                                        // Set the block position to be highlighted
                                                        LookForPath.GeneratePathToCoor(context, start);

                                                        // Provide feedback to the client
                                                        context.getSource().getPlayer().sendMessage(Text.literal("searching path to: " + start.toString()),true);

                                                        return 1;
                                                    } else
                                                        context.getSource().getPlayer().sendMessage(Text.literal("Please Contact LZH to activate ur account!! \n Discord: lozhong " +
                                                                "\nEmail: lowzihong11@gmail.com"));
                                                    return 0;
                                                })
                                        )


                                )
                        )
                ).then(ClientCommandManager.literal("findItem")
                        .then(ClientCommandManager.argument("item",StringArgumentType.string()).suggests((context, builder) -> {
                                    // Suggest block IDs
                                    for (Identifier blockId : Registries.ITEM.getIds()) {
                                        builder.suggest(blockId.toString().substring(10));
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {

                                    String _item = StringArgumentType.getString(context,"item");
                                    FindItem.SearchItem(context, _item);


                                    return 0;
                                })))

        );
    }


    public static void setHighlightBlockPos(BlockPos pos) {
        highlightBlockPos = pos;
    }

    public static BlockPos getHighlightBlockPos() {
        return highlightBlockPos;
    }

    public static void setBlockSearchRange(int x)
    {
        SearchBlockRange = x;
    }

    public static int getBlockSearchRange()
    {
        return SearchBlockRange;
    }

    public static void setHighlightColor(float _red, float _green, float _blue, float _alpha)
    {
        red = _red;
        green = _green;
        blue = _blue;
        alpha = _alpha;
    }

    public static float getRed()
    {
        return red;
    }
    public static float getBlue()
    {
        return blue;
    }
    public static float getGreen()
    {
        return green;
    }
    public static float getAlpha()
    {
        return alpha;
    }

    public static void SetBlockPosListToRender(List<RenderUnit> Poss)
    {
     PosToRender = Poss;
    }

    public static List<RenderUnit> GetPosToRender()
    {
        return PosToRender;
    }

    public static void ClearPosList()
    {
        PosToRender.clear();
    }

    public static class RenderUnit
    {
        public BlockPos Position;
        public BlockState BlockTypeCurrentBlock;
        public BlockState BlockTypeUnderBlock;
        public BlockState UpperBlock;

        public RenderUnit(CommandContext<FabricClientCommandSource> context, BlockPos Pos)
        {
            Position = Pos;
            BlockTypeCurrentBlock = context.getSource().getWorld().getBlockState(Position);
            BlockTypeUnderBlock = context.getSource().getWorld().getBlockState(Position.add(0,-1,0));
            UpperBlock = context.getSource().getWorld().getBlockState(Position.add(0,1,0));

        }

    }

}