package lzh.net.feature;

import net.minecraft.core.BlockPos;

public class path {
    public static class PathUnit {
        public BlockPos targetPos;
        public Astar.Direction dirFromPrevious;

        public PathUnit(BlockPos _targetPos, Astar.Direction _dir) {
            targetPos = _targetPos;
            dirFromPrevious = _dir;
        }

        public PathUnit(int x, int y, int z, Astar.Direction _dir) {
            targetPos = new BlockPos(x, y, z);
            dirFromPrevious = _dir;
        }
    }
}
