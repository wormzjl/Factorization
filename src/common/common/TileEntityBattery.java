package factorization.common;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import factorization.api.Charge;
import factorization.api.IChargeConductor;

public class TileEntityBattery extends TileEntityCommon implements IChargeConductor {
    Charge charge = new Charge(), storage = new Charge();
    private static final int max_storage = 60000;

    @Override
    public FactoryType getFactoryType() {
        return FactoryType.BATTERY;
    }

    @Override
    public Charge getCharge() {
        return charge;
    }

    @Override
    public BlockClass getBlockClass() {
        return BlockClass.Machine;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        charge.readFromNBT(tag, "charge");
        storage.readFromNBT(tag, "storage");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        charge.writeToNBT(tag, "charge");
        storage.writeToNBT(tag, "storage");
    }

    public static float getFullness(int value) {
        return value / ((float) max_storage);
    }

    public float getFullness() {
        return getFullness(storage.getValue());
    }

    private int getTargetCharge() {
        return (int) Math.max(getFullness() * 6, 3);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        charge.update(this);
        int val = getCharge().getValue();
        int delta = 0;
        if (val < getTargetCharge()) {
            delta = Math.min(getTargetCharge() - val, storage.getValue());
        } else if (val > getTargetCharge()) {
            int free = max_storage - storage.getValue();
            if (free <= 0) {
                return;
            }
            delta = -Math.min(free, val - getTargetCharge());
        } else {
            return;
        }
        if (delta != 0) {
            charge.addValue(delta);
            storage.addValue(-delta);
        }
    }

    @Override
    void onPlacedBy(EntityPlayer player, ItemStack is, int side) {
        super.onPlacedBy(player, is, side);
        NBTTagCompound tag = FactorizationUtil.getTag(is);
        if (tag.hasKey("storage")) {
            storage.setValue(tag.getInteger("storage"));
        } else {
            storage.setValue(max_storage);
        }
    }
}
