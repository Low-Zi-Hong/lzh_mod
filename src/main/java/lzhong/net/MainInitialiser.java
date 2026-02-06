package lzhong.net;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.sun.jdi.connect.Connector;
import lzhong.net.lzh.CustomCommand.FindItem;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.player.*;

import java.util.Map;
import java.util.function.Supplier;

public class MainInitialiser implements ModInitializer {
	public static final String MOD_ID = "lzh";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Boolean RedStoneMode = true;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		CommandRegistrationCallback.EVENT.register((dispatcher,dedicated,listener) -> {
			registerHelloCommand(dispatcher);
		});


		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!world.isClient() && hand == Hand.MAIN_HAND && player instanceof ServerPlayerEntity && RedStoneMode) {
				BlockPos targetPos = hitResult.getBlockPos().offset(hitResult.getSide());

				// Check if the player is holding white wool
				if (WOOL_TO_GLASS_MAP.containsKey(player.getStackInHand(Hand.MAIN_HAND).getItem()) || GLASS_TO_WOOL_MAP.containsKey(player.getStackInHand(Hand.MAIN_HAND).getItem()) && world.getBlockState(targetPos.up()).isOf(Blocks.AIR) && world.getBlockState(targetPos).isOf(Blocks.AIR)) {
					ItemStack item = player.getStackInHand(Hand.MAIN_HAND);
					Block BlockToPlace = Block.getBlockFromItem(item.getItem());
					// Allow the wool block to be placed normally
					world.setBlockState(targetPos, BlockToPlace.getDefaultState());

					// Place redstone dust one block above the wool block
					BlockPos redstonePos = targetPos.up();
					world.setBlockState(redstonePos, Blocks.REDSTONE_WIRE.getDefaultState());

					// Cancel further processing to prevent double placement
					return ActionResult.SUCCESS;
				}
			}
			return ActionResult.PASS;
		});

		UseItemCallback.EVENT.register(((player, world, hand) ->
		{

			if (!world.isClient() && hand == Hand.MAIN_HAND && player instanceof ServerPlayerEntity && RedStoneMode) {
				ItemStack item = player.getStackInHand(Hand.MAIN_HAND);

				if(WOOL_TO_GLASS_MAP.containsKey(item.getItem()))
				{
					ItemStack stainedGlass = new ItemStack(WOOL_TO_GLASS_MAP.get(item.getItem()));
					player.setStackInHand(hand, stainedGlass);
					return TypedActionResult.success(stainedGlass);
				}
				// Check if the item is stained glass, and replace it with wool
				else if (GLASS_TO_WOOL_MAP.containsKey(item.getItem())) {
					ItemStack wool = new ItemStack(GLASS_TO_WOOL_MAP.get(item.getItem()));
					player.setStackInHand(hand, wool);
					return TypedActionResult.success(wool);
				}
			}
			// Allow other item interactions if conditions are not met
			return TypedActionResult.pass(player.getStackInHand(hand));
        }));


	}

	// Map of wool items to corresponding stained glass items
	private static final Map<Item, Item> WOOL_TO_GLASS_MAP = Map.ofEntries(
			Map.entry(Items.WHITE_WOOL, Items.WHITE_STAINED_GLASS),
			Map.entry(Items.ORANGE_WOOL, Items.ORANGE_STAINED_GLASS),
			Map.entry(Items.MAGENTA_WOOL, Items.MAGENTA_STAINED_GLASS),
			Map.entry(Items.LIGHT_BLUE_WOOL, Items.LIGHT_BLUE_STAINED_GLASS),
			Map.entry(Items.YELLOW_WOOL, Items.YELLOW_STAINED_GLASS),
			Map.entry(Items.LIME_WOOL, Items.LIME_STAINED_GLASS),
			Map.entry(Items.PINK_WOOL, Items.PINK_STAINED_GLASS),
			Map.entry(Items.GRAY_WOOL, Items.GRAY_STAINED_GLASS),
			Map.entry(Items.LIGHT_GRAY_WOOL, Items.LIGHT_GRAY_STAINED_GLASS),
			Map.entry(Items.CYAN_WOOL, Items.CYAN_STAINED_GLASS),
			Map.entry(Items.PURPLE_WOOL, Items.PURPLE_STAINED_GLASS),
			Map.entry(Items.BLUE_WOOL, Items.BLUE_STAINED_GLASS),
			Map.entry(Items.BROWN_WOOL, Items.BROWN_STAINED_GLASS),
			Map.entry(Items.GREEN_WOOL, Items.GREEN_STAINED_GLASS),
			Map.entry(Items.RED_WOOL, Items.RED_STAINED_GLASS),
			Map.entry(Items.BLACK_WOOL, Items.BLACK_STAINED_GLASS)
	);

	// Map of stained glass items to corresponding wool items
	private static final Map<Item, Item> GLASS_TO_WOOL_MAP = Map.ofEntries(
			Map.entry(Items.WHITE_STAINED_GLASS, Items.WHITE_WOOL),
			Map.entry(Items.ORANGE_STAINED_GLASS, Items.ORANGE_WOOL),
			Map.entry(Items.MAGENTA_STAINED_GLASS, Items.MAGENTA_WOOL),
			Map.entry(Items.LIGHT_BLUE_STAINED_GLASS, Items.LIGHT_BLUE_WOOL),
			Map.entry(Items.YELLOW_STAINED_GLASS, Items.YELLOW_WOOL),
			Map.entry(Items.LIME_STAINED_GLASS, Items.LIME_WOOL),
			Map.entry(Items.PINK_STAINED_GLASS, Items.PINK_WOOL),
			Map.entry(Items.GRAY_STAINED_GLASS, Items.GRAY_WOOL),
			Map.entry(Items.LIGHT_GRAY_STAINED_GLASS, Items.LIGHT_GRAY_WOOL),
			Map.entry(Items.CYAN_STAINED_GLASS, Items.CYAN_WOOL),
			Map.entry(Items.PURPLE_STAINED_GLASS, Items.PURPLE_WOOL),
			Map.entry(Items.BLUE_STAINED_GLASS, Items.BLUE_WOOL),
			Map.entry(Items.BROWN_STAINED_GLASS, Items.BROWN_WOOL),
			Map.entry(Items.GREEN_STAINED_GLASS, Items.GREEN_WOOL),
			Map.entry(Items.RED_STAINED_GLASS, Items.RED_WOOL),
			Map.entry(Items.BLACK_STAINED_GLASS, Items.BLACK_WOOL)
	);

//	private Boolean CheckWoolOnHand(ItemStack item)
//	{
//		if(		item.getItem() == Items.WHITE_WOOL 		||
//				item.getItem() == Items.ORANGE_WOOL 	||
//				item.getItem() == Items.MAGENTA_WOOL 	||
//				item.getItem() == Items.LIGHT_BLUE_WOOL ||
//				item.getItem() == Items.YELLOW_WOOL 	||
//				item.getItem() == Items.LIME_WOOL 		||
//				item.getItem() == Items.PINK_WOOL 		||
//				item.getItem() == Items.GRAY_WOOL 		||
//				item.getItem() == Items.LIGHT_GRAY_WOOL ||
//				item.getItem() == Items.CYAN_WOOL 		||
//				item.getItem() == Items.PURPLE_WOOL 	||
//				item.getItem() == Items.BLUE_WOOL 		||
//				item.getItem() == Items.BROWN_WOOL 		||
//				item.getItem() == Items.GREEN_WOOL 		||
//				item.getItem() == Items.RED_WOOL 		||
//				item.getItem() == Items.BLACK_WOOL
//		) {
//			return true;
//		}
//		else
//		{
//			return	false;
//		}
//	}
//
//	private Boolean CheckGlassOnHand(ItemStack item)
//	{
//		if (
//				item.getItem() == Items.WHITE_STAINED_GLASS 		||
//				item.getItem() == Items.ORANGE_STAINED_GLASS 		||
//				item.getItem() == Items.MAGENTA_STAINED_GLASS 	 	||
//				item.getItem() == Items.LIGHT_BLUE_STAINED_GLASS 	||
//				item.getItem() == Items.YELLOW_STAINED_GLASS 	 	||
//				item.getItem() == Items.LIME_STAINED_GLASS 	 		||
//				item.getItem() == Items.PINK_STAINED_GLASS 			||
//				item.getItem() == Items.GRAY_STAINED_GLASS 	 		||
//				item.getItem() == Items.LIGHT_GRAY_STAINED_GLASS 	||
//				item.getItem() == Items.CYAN_STAINED_GLASS 	 		||
//				item.getItem() == Items.PURPLE_STAINED_GLASS 		||
//				item.getItem() == Items.BLUE_STAINED_GLASS 	 		||
//				item.getItem() == Items.BROWN_STAINED_GLASS 		||
//				item.getItem() == Items.GREEN_STAINED_GLASS 		||
//				item.getItem() == Items.RED_STAINED_GLASS 	 		||
//				item.getItem() == Items.BLACK_STAINED_GLASS
//		){
//			return true;
//		}
//		else
//		{
//			return	false;
//		}
//	}

	private void registerHelloCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("lzhsv")
				.then(CommandManager.literal("redStoneMode")
						.then(CommandManager.argument("Boolean", BoolArgumentType.bool())
								.executes(context -> toogleRedStoneMode(context, BoolArgumentType.getBool(context, "Boolean")))
						)
								)

						);
	}

	private int toogleRedStoneMode(CommandContext<ServerCommandSource> context, Boolean toggle)
	{
		RedStoneMode = toggle;

		ServerCommandSource source = context.getSource();
		try {
			source.sendMessage(Text.literal("Redstone mode toggled: " + RedStoneMode));
			// Your redstone logic here
		} catch (Exception e) {
			source.sendError(Text.literal("An error occurred: " + e.getMessage()));
			e.printStackTrace(); // Logs the error to the console
			return 0; // Return 0 to indicate failure
		}
		return 1; // Return success
	}
}