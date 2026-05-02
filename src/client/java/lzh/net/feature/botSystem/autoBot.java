    package lzh.net.feature.botSystem;

    import lzh.net.Lzh;
    import lzh.net.client.LzhClient;
    import lzh.net.feature.path;
    import net.minecraft.client.Minecraft;
    import net.minecraft.client.multiplayer.ClientLevel;
    import net.minecraft.core.BlockPos;
    import net.minecraft.world.level.block.state.BlockState;

    import java.util.LinkedList;
    import java.util.List;
    import java.util.Objects;
    import java.util.Queue;

    public class autoBot {

        //state mechine
        public enum BotState {
            IDLE,
            WALKING,
        }

        //this time we use another logic
        static boolean runningTask = false;
        public static boolean updateGeneratedPath = false;
        public static int GlobalTick = 0;
        public static Queue<ActionSubunit> subTask = new LinkedList<>();
        private static volatile int currentGenerationId = 0;
        public static void onClientTick() {
            GlobalTick++;

            // 1. 优先级最高：执行微操作 (SubTask)
            if (!subTask.isEmpty()) {
                if (GlobalTick >= subTask.peek().time) {
                    Objects.requireNonNull(subTask.poll()).task.run();
                }
                return;
            }

            // 2. 优先级二：如果需要更新路径（由 A* 触发）
            if (updateGeneratedPath) {
                actionQueue.clear();
                subTask.clear();
                generateActionQueue(); // 内部会处理 isGenetating 锁
                updateGeneratedPath = false;
                runningTask = true;
                return;
            }

            // 3. 优先级三：执行动作队列
            if (!actionQueue.isEmpty()) {
                if (!isGenetating) {
                    // 注意：这里不再 removeFirst，渲染用的路径点由生成器或寻路器统一管理
                    actionQueue.poll().start();
                }
                return;
            }

            // 4. 终点判定：队列全空且正在运行
            if (runningTask && !isGenetating) {
                System.out.println("Destination Reached!");
                runningTask = false;
                LzhClient.startBot = false;
            }
        }

        public static void killBot() {
            actionQueue.clear();
            subTask.clear();
            runningTask = false;
            Minecraft.getInstance().options.keyUp.setDown(false);
        }

        //action queue
        public static Queue<ActionUnit> actionQueue =  new LinkedList<>();

        public static boolean isGenetating = false;
        public static void generateActionQueue()
        {
            if(isGenetating) return;
            final int genId = ++currentGenerationId;
            //using the
            final List<path.PathUnit> paths = new LinkedList<>(LzhClient.blocKPosToRender);

            //here copy the world down
            ClientLevel world = Minecraft.getInstance().level;

            //below can do under a thread so we start a thread la
            isGenetating = true;
            Thread generateActionQueueThread = new Thread( () -> {
                try {
                    for (int i = 0; i < paths.size(); i++) {
                        if (genId != currentGenerationId) return;

                        path.PathUnit unit = paths.get(i);
                        path.PathUnit CurrentUnit = paths.get(i);
                        if (i != 0) CurrentUnit = paths.get(i - 1);

                        BlockPos block_0_pos = new BlockPos(unit.targetPos).offset(0, -1, 0);
                        BlockPos block_1_pos = new BlockPos(unit.targetPos);
                        BlockPos block_2_pos = new BlockPos(unit.targetPos).offset(0, 1, 0);
                        BlockPos block_3_pos = new BlockPos(unit.targetPos).offset(0, 2, 0);
                        BlockPos block_4_pos = new BlockPos(unit.targetPos).offset(0, 3, 0);

                        assert world != null;
                        BlockState block_0 = world.getBlockState(block_0_pos);
                        BlockState block_1 = world.getBlockState(block_1_pos);
                        BlockState block_2 = world.getBlockState(block_2_pos);
                        BlockState block_3 = world.getBlockState(block_3_pos);
                        BlockState block_4 = world.getBlockState(block_4_pos);

                        switch (unit.dirFromPrevious) {
                            case Up -> {
                                System.out.println("A Up Operation is added");

                                if (!emptyBlockPos(block_2)) {
                                    actionQueue.add(new ActionUnit(block_2_pos, OperationEnum.Break));
                                }
                                if (emptyBlockPos(block_0)) {
                                    actionQueue.add(new ActionUnit(block_0_pos, OperationEnum.JumpnPlace));
                                }
                                // Add your logic for the "Up" direction here
                            }
                            case Down -> {
                                System.out.println("A Down Operation is added");

                                if (!emptyBlockPos(block_1))
                                    actionQueue.add(new ActionUnit(block_1_pos, OperationEnum.Break));

                                actionQueue.add(new ActionUnit(block_1_pos, OperationEnum.Wait));
                                // Add your logic for the "Down" direction here
                            }
                            case Forward, Back, Left, Right -> {
                                System.out.println("A Forward Operation is added");

                                if (!emptyBlockPos(block_1))
                                    actionQueue.add(new ActionUnit(block_1_pos, OperationEnum.Break));

                                if (!emptyBlockPos(block_2))
                                    actionQueue.add(new ActionUnit(block_2_pos, OperationEnum.Break));

                                if (emptyBlockPos(block_0))
                                    actionQueue.add(new ActionUnit(block_0_pos, OperationEnum.Place));

                                actionQueue.add(new ActionUnit(block_1_pos, OperationEnum.Walk));
                                // Add your logic for the "Forward" direction here
                            }
                            case JumpUpForward, JumpUpBack, JumpUpLeft, JumpUpRight -> {
                                System.out.println("A JumpUp Operation is added");

                                //check block above player
                                assert CurrentUnit != null;
                                if (!emptyBlockPos(world.getBlockState(CurrentUnit.targetPos.offset(0, 2, 0))))
                                    actionQueue.add(new ActionUnit(CurrentUnit.targetPos.offset(0, 2, 0), OperationEnum.Break));

                                if (emptyBlockPos(block_0))
                                    actionQueue.add(new ActionUnit(block_0_pos, OperationEnum.Place));

                                if (!emptyBlockPos(block_1))
                                    actionQueue.add(new ActionUnit(block_1_pos, OperationEnum.Break));
                                if (!emptyBlockPos(block_2))
                                    actionQueue.add(new ActionUnit(block_2_pos, OperationEnum.Break));

                                actionQueue.add(new ActionUnit(block_1_pos, OperationEnum.WalknJump));
                                // Add your logic for the "JumpUpForward" direction here
                            }
                            case JumpDownForward, JumpDownBack, JumpDownLeft, JumpDownRight -> {
                                System.out.println("A JumpDownForward Operation is added");
                                // Add your logic for the "JumpDownForward" direction here
                                if (!emptyBlockPos(block_3))
                                    actionQueue.add(new ActionUnit(block_3_pos, OperationEnum.Break));
                                if (!emptyBlockPos(block_2))
                                    actionQueue.add(new ActionUnit(block_2_pos, OperationEnum.Break));
                                if (!emptyBlockPos(block_1))
                                    actionQueue.add(new ActionUnit(block_1_pos, OperationEnum.Break));

                                if (emptyBlockPos(block_0))
                                    actionQueue.add(new ActionUnit(block_0_pos, OperationEnum.Place));

                                actionQueue.add(new ActionUnit(block_1_pos, OperationEnum.Walk));
                            }
                            default -> {
                                System.out.println("No direction matched");
                                // Add your logic for unmatched directions here
                            }
                        }

                    }

                    System.out.println("Operation Queue:");
                    for (ActionUnit operation : actionQueue) {
                        System.out.println("Operation: " + operation.Operation + " at " + operation.OperationPos);
                    }
                }
                finally {

                //release lock
                isGenetating = false;
                }
            });

            generateActionQueueThread.start();

        }

        public static boolean emptyBlockPos(BlockState state){
            return state.canBeReplaced();
        }

        public enum OperationEnum
        {
            Wait,
            Walk,
            WalknJump,
            Place,
            JumpnPlace,
            Break,
            swim,
        }

        public static class ActionUnit {
            public BlockPos OperationPos;
            public OperationEnum Operation;

            private Runnable task;

            ActionUnit(BlockPos pos, OperationEnum operation)
            {
                this.OperationPos = pos;
                this.Operation = operation;

                switch (Operation)
                {
                    case Wait -> task = () -> WaitToReact.waitAwhile(subTask);
                    case Walk -> task = () -> Walking.WalkToBlock(pos,false,subTask);
                    case WalknJump -> task = () -> Walking.WalkToBlock(pos, true,subTask);
                    case Break -> task = () -> BreakBlock.setBreakBlock(pos,subTask);
                    case Place -> task = () -> PlaceBlock.PlaceBlock(pos,subTask);
                    case JumpnPlace -> task = () -> JumpnPut.JumpAndPutBlock(pos, subTask);
                    case swim -> task = () -> Swim.swim(pos,subTask);
                    default -> task = () -> System.out.println("No operation call");
                }


            }

            public void start(){task.run();}

        }

        public static class ActionSubunit{
            int time;
            Runnable task;

            ActionSubunit(int _time, Runnable _task){
                time = _time;
                task = _task;
            }
        }


        // Callback interface for feedback
        public interface Callback {
            void onComplete(boolean success);
        }



        //Behaviour tree
    }
