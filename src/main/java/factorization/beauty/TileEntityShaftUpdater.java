package factorization.beauty;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import factorization.api.Coord;
import factorization.api.IShaftPowerSource;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityShaftUpdater extends TileEntity implements IShaftPowerSource {
    IShaftPowerSource base;
    int baseX, baseY, baseZ;
    ForgeDirection dir;

    public TileEntityShaftUpdater(IShaftPowerSource src, ForgeDirection dir) {
        TileEntity te = (TileEntity) src;
        baseX = te.xCoord;
        baseY = te.yCoord;
        baseZ = te.zCoord;
        this.base = src;
        this.dir = dir;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("baseX", baseX);
        tag.setInteger("baseY", baseY);
        tag.setInteger("baseZ", baseZ);
        tag.setInteger("dir", dir.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        baseX = tag.getInteger("baseX");
        baseY = tag.getInteger("baseY");
        baseZ = tag.getInteger("baseZ");
        dir = ForgeDirection.getOrientation(tag.getInteger("dir"));
    }

    @Override
    public boolean canUpdate() {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;
        if (base == null) {
            base = KineticProxy.cast(worldObj.getTileEntity(baseX, baseY, baseZ));
            if (base == null) {
                invalidate();
                return;
            }
            propagate();
            return;
        }
        if (worldObj.getTotalWorldTime() % 5 != 0) return;
        propagate();
    }

    void propagate() {
        BlockShaft.propagateVelocity(this, new Coord(this), dir);
    }
    
    boolean broken() {
        return base == null || isInvalid();
    }

    @Override
    public boolean canConnect(ForgeDirection direction) {
        if (broken()) return false;
        return base.canConnect(direction);
    }

    @Override
    public double availablePower(ForgeDirection direction) {
        if (broken()) return 0;
        return base.availablePower(direction);
    }

    @Override
    public double powerConsumed(ForgeDirection direction, double maxPower) {
        if (broken()) return 0;
        return base.powerConsumed(direction, maxPower);
    }

    @Override
    public double getAngularVelocity(ForgeDirection direction) {
        if (broken()) return 0;
        return base.getAngularVelocity(direction);
    }
}
