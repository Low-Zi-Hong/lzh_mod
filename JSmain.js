import * as net from 'net';

const PORT = 8080;

const bots = []

// 创建一个 TCP 服务器
const server = net.createServer((socket) => {
    console.log('\n[TS Server] 🟢 侦测到新连接！Java 指挥官已接入。');

    // 监听 Java 发来的数据
    socket.on('data', (data) => {
        // 记得用 trim() 去掉末尾的换行符 \n
        const message = data.toString().trim();
        console.log(`[TS Server] 📩 收到原始报文: ${message}`);

        // 尝试把它当成 JSON 解析
        try {
            const command = JSON.parse(message);
            console.log(`[TS Server] ⚙️ JSON 解析成功！`);
            console.log(`          -> 动作 (Action): ${command.action}`);
            //console.log(`          -> 目标 (Target): X=${command.target.x}, Z=${command.target.z}`);

            // 收到后，顺便给 Java 回个信，证明我活着
            socket.write('{"status": "ACK", "message": "指令已确认执行"}\n');

// --- 动作分发 ---
            switch (command.action) {
                case "SUMMON_BOT":
                    // 检查参数是否存在
                    if (command.botName && command.host) {
                        spawnBot(command.botName, command.host, command.port || 25565);
                    }
                    break;

                case "CHAT":
                    // 核心防御：检查机器人是否真的在线
                    if (bots && bots.length > 0 && bots[0]) {
                        chat(bots[0], command.target);
                    } else {
                        console.log("⚠️ 无法发送聊天：机器人尚未就绪！");
                        socket.write('{"status": "ERR", "message": "Bot not initialized"}\n');
                    }
                    break;

                default:
                    console.log(`❓ 未知动作: ${command.action}`);
            }

        } catch (error) {
            console.log(`[TS Server] ⚠️ 警告：收到的不是标准 JSON 格式。`);
        }
    });


    // 监听断开连接
    socket.on('close', () => {
        console.log('[TS Server] 🔴 Java 指挥官已断开连接。等待下一次召唤...\n');
    });

    // 防止因为网络闪断导致 Node.js 崩溃
    socket.on('error', (err) => {
        console.error('[TS Server] ❌ 连接发生错误:', err.message);
    });
});


server.listen(PORT, () => {
    console.log(`🚀 赛博召唤阵已激活 (端口: ${PORT})`);
	//reply java
    console.log("SERVER_READY_SIGNAL_ACK");
});

//mineflayer logic
import * as mineflayer from 'mineflayer';
import pkg from 'mineflayer-pathfinder';
import { once } from 'events';
const {pathfinder, Movements,goals} = pkg;
const { GoalBlock, GoalFollow,GoalNear } = goals

let done_spawn = false

const num_of_bot = 1

// Helper to stringify objects safely
function pf(obj) {
  try { return JSON.stringify(obj) } catch(e) { return String(obj) }
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// Async helper to toss a stack
async function tossStackAsync(bot, item) {
    return new Promise((resolve, reject) => {
        bot.tossStack(item, (err) => {
            if (err) reject(err)
            else resolve()
        })
    })
}
//garding function

let guardPos = null

// Assign the given location to be guarded
function guardArea (pos) {
  guardPos = pos

  // We we are not currently in combat, move to the guard pos
  if (!bot.pvp.target) {
    moveToGuardPos()
  }
}

// Cancel all pathfinder and combat
function stopGuarding () {
  guardPos = null
  bot.pvp.stop()
  bot.pathfinder.setGoal(null)
}

// Pathfinder to the guard position
function moveToGuardPos () {
  bot.pathfinder.setMovements(new Movements(bot))
  bot.pathfinder.setGoal(new goals.GoalBlock(guardPos.x, guardPos.y, guardPos.z))
}

function chat(bot, text)
{
		bot.chat(text)
}

function followMe(bot,username)
{
		if(bot.username === "Herta1"){
		const player = bot.players[username]?.entity
		if(player) {
			const goal = new GoalFollow(player, 1) // follow at 1 block distance
			bot.pathfinder.setGoal(goal, true)     // true = dynamic goal, follows player
		}
		}
		else{
			const num = parseInt(bot.username.toString().slice(5)) - 1
			//console.log(num)

			const target = `Herta${num}`
						//console.log(target)
			const player = bot.players[target]?.entity
			if(player) {
				const goal = new GoalFollow(player, 1) // follow at 1 block distance
				bot.pathfinder.setGoal(goal, true)     // true = dynamic goal, follows player
			}
		}
}

function follow(bot,target)
{
		const player = bot.players[target]?.entity
		if(player) {
				const goal = new GoalFollow(player, 1) // follow at 1 block distance
				bot.pathfinder.setGoal(goal, true)     // true = dynamic goal, follows player
		}
}

function unFollow(bot)
{
	bot.pathfinder.setGoal(null)
}

function mineBlock(bot, blockname){
		// Get the correct block type
		const blockType = bot.mcData.blocksByName[blockname]
		if (!blockType) {
		bot.chat("I don't know any blocks with that name.")
		return
		}

		bot.chat('Collecting the nearest ' + blockType.name)

		const blocks = bot.findBlocks({
			matching: blockType.id,
			maxDistance: 64,
			count: 10
		})

		// Remove undefined/null entries just in case
		const validBlocks = blocks.filter(b => b != null)

		// Sort by distance to bot
		validBlocks.sort((a, b) => bot.entity.position.distanceTo(a) - bot.entity.position.distanceTo(b))

		// Pick nth nearest, e.g., 2nd nearest
		const num = parseInt(bot.username.toString().slice(5)) - 1 // 0 = nearest, 1 = 2nd nearest
		const targetPos = validBlocks[num]

		if (!targetPos) {
			bot.chat("I don't see that block nearby.")
			return
		}

		// Get the actual block object at that position
		const targetBlock = bot.blockAt(targetPos)

		// Dig it
		bot.pathfinder.setGoal(new GoalBlock(targetBlock.position.x, targetBlock.position.y, targetBlock.position.z))

		bot.once('goal_reached', async () => {
			try {
				await bot.dig(targetBlock)
				bot.chat(`Mined ${blockType.name} successfully!`)
			} catch (err) {
				bot.chat(`Failed to mine ${blockType.name}: ${err.message}`)
			}
		})
}

function attackPlayer(bot,targetName){

		const target = bot.players[targetName]?.entity ||
					Object.values(bot.entities).find(e => e.username === targetName || e.mobType === targetName)

		if(!target) {
			bot.chat(`I can't find ${targetName}!`)
			return
		}

		bot.chat(`Attacking ${targetName}...`)

		const { GoalFollow } = require('mineflayer-pathfinder').goals
		const goal = new GoalFollow(target, 2)
		bot.pathfinder.setGoal(goal, true)

		if(bot.attackInterval) clearInterval(bot.attackInterval)

		bot.attackInterval = setInterval(() => {
			if(!target || !target.isValid) {
				clearInterval(bot.attackInterval)
				bot.attackInterval = null
				bot.chat(`Stopped attacking ${targetName}.`)
				bot.pathfinder.setGoal(null)
				return
			}
			bot.attack(target)
		}, 300)

}

function stopAttack(bot)
{
		if(bot.attackInterval) {
			clearInterval(bot.attackInterval)
			bot.attackInterval = null
		}
		bot.pathfinder.setGoal(null)
		bot.chat('Stopped attacking.')
}

async function serveMe(bot,username){
	 // find player entity
		 playerName = username

		const player = bot.players[username]?.entity
		if (!player) {
		bot.chat(`I can't find ${username} to give items.`)
		return
		}

		// store return position (current bot position)
		returnPos = bot.entity.position.clone()

		// go to near the player (within 2 blocks)
		bot.chat(`Going to ${username} to give items...`)
		bot.pathfinder.setGoal(new GoalNear(player.position.x, player.position.y, player.position.z),2, true)

		// wait until reached or timeout
		try {
		    await onceEvent(bot, 'goal_reached', 30000) // resolves when pathfinder signals goal reached
		} catch (err) {
		// fallback: wait until near by checking distance
		let waited = 0
		while (bot.entity.position.distanceTo(player.position) > 3 && waited < 30000) {
			await sleep(500)
			waited += 500
		}
		}

		const items = bot.inventory.items() // array of Item objects
		if (items.length === 0) {
		bot.chat("I have no items to give.")
		} else {
		bot.chat(`Dropping ${items.length} item stacks for ${username}...`)
		// drop each stack; skip armor/equipment if you want:
		for (const item of items) {
			// optionally skip if item.slot is armor/equipment; example:
			// if (item.slot >= 100) continue
			try {
			await tossStackAsync(bot, item)
			await sleep(200) // tiny pause so player can pick up
			} catch (err) {
			bot.chat(`Failed to drop ${item.name}: ${err.message}`)
			}
		}
		bot.chat('Done dropping items.')
		}
}


async function RunCommand(bot,command,username)
{
	//chating
	if(command.startsWith('!chat '))
	{
		chat(command.slice(6))

	}

	//following
	if(command.startsWith('!followMe'))
	{
		followMe(bot,username)
	}

	else 	if(command.startsWith('!follow '))
	{
		follow(bot, command.slice(8))
	}
	//unfollow
	if(command.startsWith('!unfollow'))
	{
		unFollow(bot)
	}

	//praise
	if(command.startsWith('!praiseMe'))
	{
		bot.chat(`黑塔女士和${username}举世无双！`)
		await sleep(1000)
		bot.chat(`黑塔女士和${username}聪明绝顶！`)
		await sleep(1000)
		bot.chat(`黑塔女士和${username}沉鱼落雁！`)
	}

	//automining
	if(command.startsWith('!mine '))
	{
		blockname = command.slice(6)
		mineBlock(bot,blockname)

	}

	// At the top of your script, per bot:
	bot.attackInterval = null
	bot.defendInterval = null

	// --- inside RunCommand ---

	// Attack
	if(command.startsWith('!attack ')) {
		const targetName = command.slice(8).trim()

		attackPlayer(bot,targetName)
	}

	// Stop attack
	if(command.startsWith('!stopAttack')) {
		stopAttack(bot)
	}

	// Defend a player
	if(command.startsWith('!defend ')) {
		const playerName = command.slice(8).trim()
		const player = bot.players[playerName]?.entity
		if(!player) {
			bot.chat(`I can't find ${playerName} to defend!`)
			return
		}

		bot.chat(`Defending ${playerName}...`)
		guardArea(player.entity.position)
		}

	// Stop defending
	if(command.startsWith('!stopDefend')) {
    bot.chat('I will no longer guard this area.')
    stopGuarding()
	}

	if(command.startsWith('!serveMe'))
	{
		 serveMe(bot,username)
	}

	if(command.startsWith('!fuck '))
	{
		let targetn = null
		if(message.length < 6)
		{
			targetn = username
		}
		else
		{
			targetn = message.slice(6)
		}

		const target = bot.players[targetn]?.entity ||
				Object.values(bot.entities).find(e => e.username === targetn || e.mobType === targetn)

		if(!target) {
			bot.chat(`I can't find ${targetn}!`)
			return
		}

		bot.chat(`I am comming :D ${targetn}`)
		const { GoalFollow } = require('mineflayer-pathfinder').goals
		const goal = new GoalFollow(target, 0)
		bot.pathfinder.setGoal(goal, true)

		  let isSneaking = false
		let interval = setInterval(() => {
			isSneaking = !isSneaking
			bot.setControlState('sneak', isSneaking)
		}, 100)

		// stop after 10 seconds
		setTimeout(() => {
			clearInterval(interval)
			bot.setControlState('sneak', false)
			bot.chat('Damn Am Tired!!!!')
			bot.pathfinder.setGoal(null)

		}, 10000)

	}



    return 1;
}

import minecraftData from 'minecraft-data';
import { plugin as pvp } from 'mineflayer-pvp';
import { plugin as collectBlock } from 'mineflayer-collectblock';


const owner = ['RiceBuckket','EnderRL','Lzh2.0','_EnderRL1334','ChickenWhitte']

function createBot(name, _host, _port) {
  const bot = mineflayer.createBot({
    host: _host, // replace with your server IP if needed
    port: _port,
    username: name,
    version: "1.21.1",
    auth: 'offline'
  })

  	  //this for load plugin
	      // Create mcData for this bot version
  	bot.mcData = minecraftData(bot.version);
  	bot.loadPlugin(pathfinder)
 	bot.loadPlugin(collectBlock)
	bot.loadPlugin(pvp)

  	// --- connection / login events ---
 	bot.on('login', () => console.log(`${name}: event -> login`))
  	bot.once('spawn', () => {

	    const defaultMove = new Movements(bot)
		bot.pathfinder.setMovements(defaultMove)


		//this for rl server
	  //console.log(`${name}: event -> spawn`)
	  //setTimeout(() => {
	  //bot.chat('/register strong_password')
	  //bot.chat('/l strong_password')
	  //},2000)

	  })
  	bot.on('kicked', (reason) => console.log(`${name}: event -> kicked ->`, reason))
  	bot.on('end', () => console.log(`${name}: event -> disconnected`))
  	bot.on('error', err => console.log(`${name}: event -> error ->`, err && err.message ? err.message : pf(err)))

  // --- resource pack handling ---
// This is for Minecraft Version 1.20.3 - 1.21.8 (See https://prismarinejs.github.io/minecraft-data/?v=1.21.8&d=protocol#types.packet_common_add_resource_pack)
// The name of the received datapack was changed. Also now you have to send back the uuid instead of the hash code.
	bot._client.on('add_resource_pack', (data) => {
    	console.log(`Accept Server-Resource-Pack. url: ${data.url} , uuid: ${data.uuid}`);

    // result:
    // SUCCESSFULLY_LOADED: 0
    // DECLINED: 1
    // FAILED_DOWNLOAD: 2
    // ACCEPTED: 3
    bot._client.write('resource_pack_receive', { uuid: data.uuid, result: 3 }); // report that the resource-pack was accepted
    bot._client.write('resource_pack_receive', { uuid: data.uuid, result: 0 }); // report that the resource-pack was downloaded succesfully
});

	bot.counter = 5
	bot.asking = false


	//	// --- chat and messages ---
	bot.on('message', async (msg) => {

		//this for llm but not now
	const text = msg.toString() // full chat text
	console.log(`${name}: raw message -> ${text}`)
//
//	chatHistory = chatHistory + text + "\n";
//
//	if (bot.counter <= 0 && bot.username === 'Herta1' && done_spawn && !bot.asking) {
//		bot.asking = true
//		const responds = await askAI(chatHistory);
//		const cut_respond = responds.split("\n");
//
//		for (let i = 0; i < cut_respond.length; i++)
//		{
//			const context = cut_respond[i];
//			console.log("AI" + context);
//			bot.chat(context);
//
//			await RunCommand(bot,context,username)
//			await sleep(300)
//		}
//
//
//
//
//		chatHistory += "Herta1: " + responds + "\n";
//		bot.counter = 3;
//		await sleep(1000);
//		bot.asking = false;
//		return;
//	} else {
//		bot.counter -= 1;
//	}
//	console.log(bot.counter);


  // Match messages like "<RiceBuckket> !say hello"

  //we are not going to use this system but can just remain?
  const match = text.match(/^<(\w+)> (.+)$/)
  const match2 = text.match(/^(?:\[DC\] )?<([^>]+)> (.+)$/)

  let username = ''
  let message = ''
  if(match)  // ignore non-player messages
  {
   username = match[1] // e.g., "RiceBuckket"
   message = match[2]  // e.g., "!say hello"
  }
  else if (match2)
	  {
   username = match2[1] // e.g., "RiceBuckket"
   message = match2[2]  // e.g., "!say hello"
  }  else return

  console.log(username)

  if(username === "Herta") {
	  console.log("herta commanding")
	  RunCommand(bot,message,username);}

	if (username.startsWith('Herta')) return



	if(message.startsWith("fuck"))
	{
		bot.chat(`fuck ${username}`)
	}

  // Only allow trusted owners
  // Only respond to trusted owners
  if(!owner.includes(username)) return


  console.log(`${name} command from ${username}: ${message}`)

	//bot.chat(message)

	RunCommand(bot,message,username)


})

bot.on('raw', (packet) => {
    // ignore move_minecart or other noisy packets
    if(packet.name === 'move_minecart') return;
});



  // --- watchdog (optional, increased timeout) ---
  const WATCHDOG_TIMEOUT = 10000 // 60s
  let watchdog = setTimeout(() => {
    console.log(`${name} WATCHDOG: no spawn/login/kick/error within ${WATCHDOG_TIMEOUT/1000}s`)
  }, WATCHDOG_TIMEOUT)
  ;['spawn','login','kicked','end','error','message'].forEach(e => {
    bot.once(e, () => clearTimeout(watchdog))
  })

  // Called when the bot has killed it's target.
bot.on('stoppedAttacking', () => {
  if (guardPos) {
    moveToGuardPos()
  }
})


	// Check for new enemies to attack
bot.on('physicsTick', () => {
  if (!guardPos) return // Do nothing if bot is not guarding anything

  // Only look for mobs within 16 blocks
  const filter = e => e.type === 'mob' && e.position.distanceTo(bot.entity.position) < 16 &&
                    e.displayName !== 'Armor Stand' // Mojang classifies armor stands as mobs for some reason?

  const entity = bot.nearestEntity(filter)
  if (entity) {
    // Start attacking
    bot.pvp.attack(entity)
  }
})



  return bot
}

// --- create multiple bots ---

async function spawnBots(num_of_bot) {

  for (let i = 1; i <= num_of_bot; i++) {
    const name = `bot${i}`
    console.log(`Spawning bot: ${name}`)
    const bot = createBot(name,'endymining.ddns.net',25565)
    bots.push(bot)

    // wait 1 minute before spawning next bot
    await sleep(5000) // 60,000 ms = 1 minute
  }

  console.log("All bots created!")
  done_spawn = true
  return bots
}

async function spawnBot(name,host,port) {

    console.log(`Spawning bot: ${name}`)
    const bot = createBot(name,host,port)
    bots.push(bot)

  console.log("bot created!")
  done_spawn = true
  return bots
}






//main function
// call it like:
//spawnBots(num_of_bot)

// --- simple movement ---
if(done_spawn === true){
setInterval(() => {
  bots.forEach(bot => {
    bot.setControlState('jump', true)
    setTimeout(() => bot.setControlState('jump', false), 500)
  })
}, 5000)
}