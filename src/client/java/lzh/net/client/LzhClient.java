package lzh.net.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import lzh.net.feature.Astar;
import lzh.net.feature.botSystem.autoBot;
import lzh.net.feature.hertaBot.TsServerManager;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static lzh.net.feature.botSystem.autoBot.onClientTick;

public class LzhClient implements ClientModInitializer {

	//Rendering path coor list
	public static List<path.PathUnit> blocKPosToRender = new ArrayList<>();

	//path Finding
	//public static Boolean runingWtfAlgo = false;
	public static BlockPos startPos;
	public static BlockPos tempEndPos;
	public static BlockPos endPos;
	public static float thressholdDivDist = 24;
	public static boolean isCalculating = false;

	//bot
	public static boolean startBot = false;

	//ts server
	public static boolean isTsSeverOnline = false;

	//debug mode
	public static boolean debugMode = false;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		//init the ts server
		// 【挂载实时监听器！】
		TsServerManager.registerListener(new TsLogListener() {
			@Override
			public void onNewLogReceived(String logLine) {
				// 收到日志后要做什么？打印到游戏的聊天框里！
				//Minecraft.getInstance().player.sendSystemMessage(Component.literal("TS SERVER: " + logLine));
				// 为了防止普通日志刷屏，我们可以只把包含 "ERROR" 或者特定前缀的弹到游戏里
				if (logLine.contains("ERROR") || logLine.contains("[TS Server]")) {
					Minecraft mc = Minecraft.getInstance();
					// 确保玩家已经进了世界，不是在主菜单
					if (mc.player != null) {
						// 把 TS 的日志包装成游戏里的文字发给玩家！
						mc.player.sendSystemMessage(Component.literal("赛博大脑" + logLine));
					}
				}
			}
		});

		// 挂载完毕后，启动服务器
		TsServerManager.startHiddenTsServer();


		//Register client side command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				registerCommands(dispatcher)
		);

		//for rendering
		LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> {
			blockRenderer.register(context);
			//RenderLine.register(context);
		});

		//for bot
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (startBot) onClientTick();
		});

		//for pathfinding
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null || endPos == null) return;

			BlockPos playerPos = client.player.blockPosition();

			// arrive destination
			if (playerPos.closerThan(endPos, 1.5)) {
				endPos = null;
				tempEndPos = null;
				blocKPosToRender.clear();
				client.player.sendSystemMessage(Component.literal("Arrived at destination!"));
				startBot = false;
				autoBot.killBot();
				return;
			}

			// arrive thresshold
			if (tempEndPos != null && !isCalculating && tempEndPos!=endPos) {
				if (playerPos.closerThan(tempEndPos, 2.0)) {
					// is calculation
					isCalculating = true;

					// clear the render
					blocKPosToRender.clear();

					client.player.sendSystemMessage(Component.literal("Recalculating..."));

					// start Astar
					Astar.Start(playerPos, endPos, thressholdDivDist);
				}
				if(blocKPosToRender.isEmpty()){
					//dono why dead lol
					System.out.println("dono why dead lol");
					// is calculation
					isCalculating = true;

					// clear the render
					blocKPosToRender.clear();

					client.player.sendSystemMessage(Component.literal("Recalculating..."));

					// start Astar
					Astar.Start(playerPos, endPos, thressholdDivDist);
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
				.then(ClientCommands.literal("debug")
						.executes(clientCommand::debug))
				.then(ClientCommands.literal("bot")
						.executes(clientCommand::startBot))
				.then(ClientCommands.literal("miniBot")
						.executes(clientCommand::summonLittleBot)
						.then(ClientCommands.literal("restart")
								.executes(clientCommand::restartServer))
						.then(ClientCommands.argument("name", StringArgumentType.string())
								.executes(clientCommand::summonBotWithName)
								.then(ClientCommands.literal("chat")
										.then(ClientCommands.argument("message",StringArgumentType.string())
												.executes(clientCommand::fireCommand)))))

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

	public interface TsLogListener {
		// 当 TS 吐出一行新日志时，这个方法就会被触发
		void onNewLogReceived(String logLine);
	}
}