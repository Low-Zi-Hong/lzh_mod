package lzh.net.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import lzh.net.feature.Astar;
import lzh.net.feature.path;

import lzh.net.feature.renderingSystem.blockRenderer;
import lzh.net.registry.clientCommand;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class LzhClient implements ClientModInitializer {

	//Rendering path coor list
	public static List<path.PathUnit> blocKPosToRender = new ArrayList<>();

	//path Finding
	//public static Boolean runingWtfAlgo = false;
	public static BlockPos startPos;
	public static BlockPos tempEndPos;
	public static BlockPos endPos;
	public static float thressholdDivDist = 24;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		//Register client side command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				registerCommands(dispatcher)
		);

		//for rendering
		LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> {
			blockRenderer.register(context);
			//RenderLine.register(context);
		});

		//for pathfinding
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			//track player pos
			if (client.player == null || client.level == null) {
				return;
			}
			assert Minecraft.getInstance().player != null;
			BlockPos playerPos = Minecraft.getInstance().player.blockPosition();

//			if(runingWtfAlgo)
//			{
//
//				//check if player arrive the TempEndPoint
//				if (Astar.checkReachEnd(playerPos,tempEndPos)) {
//
//					MinecraftClient.getInstance().player.sendMessage(Text.literal("renewing path"),true);
//					tempEndPos = null;
//					blocKPosToRender.clear();
//					Astar.Start();
//				}
//
//			}
			//check if player arrive at the End point
			if (playerPos.equals(endPos)) {
				//blocKPosToRender.clear();
				endPos = null;
				startPos = null;

				client.player.sendSystemMessage(Component.literal("your arrived at ur destination"));
			} else if (tempEndPos != null) {
				if (playerPos.getBottomCenter().distanceTo(tempEndPos.getBottomCenter()) < 2) {
					blocKPosToRender.clear();
					tempEndPos = new BlockPos(0,0,0);
					Astar.Start(playerPos, endPos, thressholdDivDist);

					client.player.sendSystemMessage(Component.literal("Recalculating.."));
				}
			}
		});
	}

	private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		// Use a string argument instead of IdentifierArgumentType
		dispatcher.register(ClientCommands.literal("lzh")
			.then(ClientCommands.literal("help")
				.executes(LzhClient::helpCommand)
			).then(ClientCommands.literal("nav")
						.then(ClientCommands.argument("x", IntegerArgumentType.integer())
								.then(ClientCommands.argument("y",IntegerArgumentType.integer())
										.then(ClientCommands.argument("z",IntegerArgumentType.integer())
												.executes(clientCommand::navigate)
												.then(ClientCommands.argument("thres",IntegerArgumentType.integer())
													.executes(clientCommand::navigate)
										)))))
		);
	}

	private static int helpCommand(CommandContext<FabricClientCommandSource> context) {
		context.getSource().sendFeedback(Component.literal(

				"Welcome to use the mod type /lzh <function> \n" +
						"Function list: \n" +
						"find <block name> : find block at range\n" +
						"look <x> <y> <z> : \n" +
						"highlight <x> <y> <z>\n\n" +
						"If have any problem or need any further aids please contact lzh, \n" +
						"Discord: lozhong\n" +
						"Email: lowzihong11@gmail.com"

		));
		return 1;
	}
}