package lzhong.net.lzh.CustomCommand;

import com.mojang.brigadier.context.CommandContext;
import lzhong.net.ClientInitialiser;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LookForPath {

    public static void GeneratePathToCoor(CommandContext<FabricClientCommandSource> context, BlockPos FinalPos)
    {
        Thread CalculatepathThread = new Thread(() -> {
            context.getSource().getPlayer().sendMessage(Text.literal("Calculating"));

            ClientInitialiser.ClearPosList();
            List<ClientInitialiser.RenderUnit>  path = new ArrayList<ClientInitialiser.RenderUnit>();

            BlockPos StartingPos = context.getSource().getPlayer().getBlockPos();
            List<PathUnit> active_array = new ArrayList<PathUnit>(0);
            List<PathUnit> unactive_array = new ArrayList<PathUnit>(0);

            boolean reachedEnd = false;

            int currentid = 0;
            int BestPathId = 0;

            while (!reachedEnd && unactive_array.size() <= 1000000) {
                //context.getSource().getPlayer().sendMessage(Text.literal("??  " + reachedEnd));
                PathUnit Lowest_F_Cost_Path = null;
                float F_cost;
                if (active_array.isEmpty()) {
                    active_array.add(new PathUnit(currentid, StartingPos, FinalPos));
                    currentid += 1;

                    Lowest_F_Cost_Path = active_array.getFirst();
                    F_cost = Lowest_F_Cost_Path.F_cost;
                } else {

                    //Find lowest F Cost

                    Lowest_F_Cost_Path = active_array.getFirst();
                    F_cost = Lowest_F_Cost_Path.F_cost;
                    //context.getSource().getPlayer().sendMessage(Text.literal( " " + F_cost));
                    for (PathUnit i : active_array) {
                        //context.getSource().getPlayer().sendMessage(Text.literal("asdasd " + i.F_cost));
                        if (i.F_cost <= F_cost) {
                            Lowest_F_Cost_Path = i;
                            F_cost = i.F_cost;
                            //context.getSource().getPlayer().sendMessage(Text.literal("Lowest coor" + i.PathUnit_Coor.toString()));
                        }
                    }
                }
                //up
                if (CheckBlockAvalaible(context, Lowest_F_Cost_Path.PathUnit_Coor.add(0, 1, 0)) && Lowest_F_Cost_Path.previous_Direction != Direction.Down) {
                    PathUnit buffer = new PathUnit(context,currentid, Lowest_F_Cost_Path, Direction.Up, FinalPos);
                    active_array.add(buffer);
                    if (Reachingend(buffer.PathUnit_Coor, FinalPos)) {
                        reachedEnd = true;
                        BestPathId = buffer.step_id;
                    }
                    currentid++;
                }

                //down
                if (CheckBlockAvalaible(context, Lowest_F_Cost_Path.PathUnit_Coor.add(0, -1, 0)) && Lowest_F_Cost_Path.previous_Direction != Direction.Up) {
                    PathUnit buffer = new PathUnit(context,currentid, Lowest_F_Cost_Path, Direction.Down, FinalPos);
                    active_array.add(buffer);
                    if (Reachingend(buffer.PathUnit_Coor, FinalPos)) {
                        reachedEnd = true;
                        BestPathId = buffer.step_id;
                    }
                    currentid++;
                }
                //forward
                if (CheckBlockAvalaible(context, Lowest_F_Cost_Path.PathUnit_Coor.add(1, 0, 0)) && Lowest_F_Cost_Path.previous_Direction != Direction.Back) {
                    PathUnit buffer = new PathUnit(context,currentid, Lowest_F_Cost_Path, Direction.Forward, FinalPos);
                    active_array.add(buffer);
                    if (Reachingend(buffer.PathUnit_Coor, FinalPos)) {
                        reachedEnd = true;
                        BestPathId = buffer.step_id;
                    }
                    currentid++;
                }
                //back
                if (CheckBlockAvalaible(context, Lowest_F_Cost_Path.PathUnit_Coor.add(-1, 0, 0)) && Lowest_F_Cost_Path.previous_Direction != Direction.Forward) {
                    PathUnit buffer = new PathUnit(context,currentid, Lowest_F_Cost_Path, Direction.Back, FinalPos);
                    active_array.add(buffer);
                    if (Reachingend(buffer.PathUnit_Coor, FinalPos)) {
                        reachedEnd = true;
                        BestPathId = buffer.step_id;
                    }
                    currentid++;
                }

                //left
                if (CheckBlockAvalaible(context, Lowest_F_Cost_Path.PathUnit_Coor.add(0, 0, -1)) && Lowest_F_Cost_Path.previous_Direction != Direction.Right) {
                    PathUnit buffer = new PathUnit(context,currentid, Lowest_F_Cost_Path, Direction.Left, FinalPos);
                    active_array.add(buffer);
                    if (Reachingend(buffer.PathUnit_Coor, FinalPos)) {
                        reachedEnd = true;
                        BestPathId = buffer.step_id;
                    }
                    currentid++;
                }

                //right
                if (CheckBlockAvalaible(context, Lowest_F_Cost_Path.PathUnit_Coor.add(0, 0, 1)) && Lowest_F_Cost_Path.previous_Direction != Direction.Left) {
                    PathUnit buffer = new PathUnit(context,currentid, Lowest_F_Cost_Path, Direction.Right, FinalPos);
                    active_array.add(buffer);
                    if (Reachingend(buffer.PathUnit_Coor, FinalPos)) {
                        reachedEnd = true;
                        BestPathId = buffer.step_id;
                    }
                    currentid++;
                }

                unactive_array.add(Lowest_F_Cost_Path);
                active_array.remove(Lowest_F_Cost_Path);

            }



            PathUnit addedPath = null;
            //trace back the coor
            for (PathUnit i : active_array)
            {
                if(i.step_id == BestPathId)
                {
                    addedPath = i;
                }
            }

            if(addedPath == null) context.getSource().getPlayer().sendMessage(Text.literal("Error Occor!!" + BestPathId),true);

            path.add(new ClientInitialiser.RenderUnit(context,addedPath.PathUnit_Coor));

            while (addedPath != null && !addedPath.PathUnit_Coor.equals(StartingPos))
            {
                boolean found = false;
                for (PathUnit i : unactive_array) {
                    if (i.step_id == addedPath.previous_step_id) {
                        addedPath = i;
                        path.add(new ClientInitialiser.RenderUnit(context,addedPath.PathUnit_Coor));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // If no matching PathUnit was found, break the loop to avoid infinite looping
                    break;
                }
            }

            path.add(new ClientInitialiser.RenderUnit(context,FinalPos) );
            ClientInitialiser.TargetPosition = FinalPos;

            //haven't test but logically finish!!!
            //context.getSource().getPlayer().sendMessage(Text.literal(path.size() + " "));
            ClientInitialiser.SetBlockPosListToRender(path);

        });

        CalculatepathThread.start();

    }

    private static boolean CheckBlockAvalaible(CommandContext<FabricClientCommandSource> context, BlockPos pos) {

        BlockPos[] PosList = {pos.add(0, -1, 0),pos.add(0, 2, 0),pos.add(1, 0, 0),pos.add(1, 1, 0),pos.add(-1, 0, 0),pos.add(-1, 1, 0),pos.add(0, 0, 1),pos.add(0, 1, 1),pos.add(0, 0, -1),pos.add(0, 1, -1)};
        Boolean canMove= true;
        for (BlockPos i : PosList)
        {
            String BlockNameFull =context.getSource().getWorld().getBlockState(i).toString();
            String BlockName = BlockNameFull.substring(BlockNameFull.indexOf("{")+1 , BlockNameFull.indexOf("}"));
            if (Objects.equals(BlockName, "minecraft:lava") || Objects.equals(BlockName, "minecraft:water") )
            {
                canMove = false;
            }
        }
        //BlockPos i = pos.add(0,-1,0);
        //context.getSource().getPlayer().sendMessage(Text.literal(context.getSource().getWorld().getBlockState(i).toString().substring(context.getSource().getWorld().getBlockState(i).toString().indexOf("{")+1 , context.getSource().getWorld().getBlockState(i).toString().indexOf("}"))));
        return canMove;

//        String BlockNameFull =context.getSource().getWorld().getBlockState(pos.add(0, -1, 0)).toString();
//        String BlockName = BlockNameFull.substring(BlockNameFull.indexOf("{")+1 , BlockNameFull.indexOf("}"));
//        if (!Objects.equals(BlockName, "minecraft:grass_block") )
//        {
//            context.getSource().getPlayer().sendMessage(Text.literal(BlockName.substring(BlockName.indexOf("{")+1 , BlockName.indexOf("}"))));
//            return true;
//        }
//        else {
//            return  false;
//        }
    }

    private static Boolean Reachingend(BlockPos CurrentPos, BlockPos EndPos)
    {
        return CurrentPos.getX() == EndPos.getX() && CurrentPos.getY() == EndPos.getY() && CurrentPos.getZ() == EndPos.getZ();
    }//if not reach return true


    enum Direction
    {
        Up,Down,Forward,Back,Left,Right
    }

    static class PathUnit
    {
        public int step_id;

        BlockPos PathUnit_Coor;

        public int previous_step_id;
        public Direction previous_Direction;

        public float G_cost; //how far away from starting node
        public float H_cost; //how far away to end node
        public float F_cost; //G_cost + H_cost

        PathUnit (int _step_id, BlockPos Start_Coor, BlockPos End_Coor) {
            step_id = _step_id;

            PathUnit_Coor = Start_Coor;
            previous_step_id = 0;
            previous_Direction = null;

            G_cost = 0;
            H_cost = Math.abs(PathUnit_Coor.getX() - End_Coor.getX())+ Math.abs((PathUnit_Coor.getY() - End_Coor.getY()))+ Math.abs((PathUnit_Coor.getZ() - End_Coor.getZ()));
            F_cost = G_cost + H_cost;

        }
        PathUnit(CommandContext<FabricClientCommandSource> context, int _step_id, PathUnit PreviousPath, Direction Direction, BlockPos End_Coor) {
            step_id = _step_id;
            previous_step_id = PreviousPath.step_id;
            previous_Direction = Direction;

            switch (Direction)
            {
                case Up ->  PathUnit_Coor = PreviousPath.PathUnit_Coor.add(0,1,0);
                case Down -> PathUnit_Coor = PreviousPath.PathUnit_Coor.add(0,-1,0);
                case Forward -> PathUnit_Coor = PreviousPath.PathUnit_Coor.add(1,0,0);
                case Back -> PathUnit_Coor = PreviousPath.PathUnit_Coor.add(-1,0,0);
                case Left -> PathUnit_Coor = PreviousPath.PathUnit_Coor.add(0,0,-1);
                case Right -> PathUnit_Coor = PreviousPath.PathUnit_Coor.add(0,0,1);
            }

            G_cost = PreviousPath.G_cost + 1;
            //H_cost = Math.abs(PathUnit_Coor.getX() - End_Coor.getX())+ Math.abs((PathUnit_Coor.getY() - End_Coor.getY()))+ Math.abs((PathUnit_Coor.getZ() - End_Coor.getZ()));
            H_cost = heuristic(PathUnit_Coor.getX(), PathUnit_Coor.getY(), PathUnit_Coor.getZ(), End_Coor.getX(), End_Coor.getY(), End_Coor.getZ());
            F_cost = G_cost + H_cost;

//            if(!context.getSource().getWorld().getBlockState(PathUnit_Coor).isOf(Blocks.AIR) && !context.getSource().getWorld().getBlockState(PathUnit_Coor.add(0,-1,0)).isOf(Blocks.AIR) && !context.getSource().getWorld().getBlockState(PathUnit_Coor.add(0,1,0)).isOf(Blocks.AIR)){
//                F_cost+= 0.2;
//            }
//            else if(context.getSource().getWorld().getBlockState(PathUnit_Coor).isOf(Blocks.AIR) && !context.getSource().getWorld().getBlockState(PathUnit_Coor.add(0,-1,0)).isOf(Blocks.AIR) && context.getSource().getWorld().getBlockState(PathUnit_Coor.add(0,1,0)).isOf(Blocks.AIR))
//            {
//                F_cost -= 0.1;
//            }
//            else if(!context.getSource().getWorld().getBlockState(PathUnit_Coor).isOf(Blocks.AIR) && context.getSource().getWorld().getBlockState(PathUnit_Coor.add(0,-1,0)).isOf(Blocks.AIR) && !context.getSource().getWorld().getBlockState(PathUnit_Coor.add(0,1,0)).isOf(Blocks.AIR))
//            {
//                F_cost += 1;
//            } else if (context.getSource().getWorld().getBlockState(PathUnit_Coor).isOf(Blocks.AIR) && context.getSource().getWorld().getBlockState(PathUnit_Coor.add(0,-1,0)).isOf(Blocks.AIR) && context.getSource().getWorld().getBlockState(PathUnit_Coor.add(0,1,0)).isOf(Blocks.AIR))
//            {
//                F_cost += 0.4;
//            }
        }
    }

//    public static float CalculateDistance(BlockPos data1, BlockPos data2) {
//        return (float) Math.sqrt(square(data1.getX() - data2.getX()) +
//                square(data1.getY() - data2.getY()) +
//                square(data1.getZ() - data2.getZ()));
//    }
//
//    private static float square(float x) {
//        return x * x;
//    }

    private static int heuristic(int x1, int y1, int z1, int x2, int y2, int z2) {
        int dx = x1 - x2; // Difference in x
        int dy = y1 - y2; // Difference in y
        int dz = z1 - z2; // Difference in z

        // Custom heuristic: a^2 + b^2 + c^2
        return dx * dx + dy * dy + dz * dz;
    }



}