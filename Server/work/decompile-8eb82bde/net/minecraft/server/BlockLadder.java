package net.minecraft.server;

import com.google.common.base.Predicate;
import java.util.Iterator;

public class BlockLadder extends Block {

    public static final BlockStateDirection FACING = BlockStateDirection.of("facing", (Predicate) EnumDirectionLimit.HORIZONTAL);

    protected BlockLadder() {
        super(Material.ORIENTABLE);
        this.j(this.blockStateList.getBlockData().set(BlockLadder.FACING, EnumDirection.NORTH));
        this.a(CreativeModeTab.c);
    }

    public AxisAlignedBB a(World world, BlockPosition blockposition, IBlockData iblockdata) {
        this.updateShape(world, blockposition);
        return super.a(world, blockposition, iblockdata);
    }

    public void updateShape(IBlockAccess iblockaccess, BlockPosition blockposition) {
        IBlockData iblockdata = iblockaccess.getType(blockposition);

        if (iblockdata.getBlock() == this) {
            float f = 0.125F;

            switch (SwitchHelperDirection16.a[((EnumDirection) iblockdata.get(BlockLadder.FACING)).ordinal()]) {
            case 1:
                this.a(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
                break;

            case 2:
                this.a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
                break;

            case 3:
                this.a(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                break;

            case 4:
            default:
                this.a(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
            }

        }
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public boolean canPlace(World world, BlockPosition blockposition) {
        return world.getType(blockposition.west()).getBlock().isOccluding() ? true : (world.getType(blockposition.east()).getBlock().isOccluding() ? true : (world.getType(blockposition.north()).getBlock().isOccluding() ? true : world.getType(blockposition.south()).getBlock().isOccluding()));
    }

    public IBlockData getPlacedState(World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2, int i, EntityLiving entityliving) {
        if (enumdirection.k().c() && this.b(world, blockposition, enumdirection)) {
            return this.getBlockData().set(BlockLadder.FACING, enumdirection);
        } else {
            Iterator iterator = EnumDirectionLimit.HORIZONTAL.iterator();

            EnumDirection enumdirection1;

            do {
                if (!iterator.hasNext()) {
                    return this.getBlockData();
                }

                enumdirection1 = (EnumDirection) iterator.next();
            } while (!this.b(world, blockposition, enumdirection1));

            return this.getBlockData().set(BlockLadder.FACING, enumdirection1);
        }
    }

    public void doPhysics(World world, BlockPosition blockposition, IBlockData iblockdata, Block block) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.get(BlockLadder.FACING);

        if (!this.b(world, blockposition, enumdirection)) {
            this.b(world, blockposition, iblockdata, 0);
            world.setAir(blockposition);
        }

        super.doPhysics(world, blockposition, iblockdata, block);
    }

    protected boolean b(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        return world.getType(blockposition.shift(enumdirection.opposite())).getBlock().isOccluding();
    }

    public IBlockData fromLegacyData(int i) {
        EnumDirection enumdirection = EnumDirection.fromType1(i);

        if (enumdirection.k() == EnumAxis.Y) {
            enumdirection = EnumDirection.NORTH;
        }

        return this.getBlockData().set(BlockLadder.FACING, enumdirection);
    }

    public int toLegacyData(IBlockData iblockdata) {
        return ((EnumDirection) iblockdata.get(BlockLadder.FACING)).a();
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockLadder.FACING});
    }
}