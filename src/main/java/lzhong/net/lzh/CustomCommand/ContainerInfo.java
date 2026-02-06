package lzhong.net.lzh.CustomCommand;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;

public class ContainerInfo{

    public BlockEntity containerType;
    public BlockPos containerPos;
    public ArrayList<ItemStack> containerContent;

    public ContainerInfo(BlockEntity c_type, BlockPos c_Pos, ArrayList<ItemStack> c_content)
    {
        containerType = c_type;
        containerPos = c_Pos;
        containerContent = c_content;
    }
}
