package factorization.common;

import java.util.List;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import factorization.api.Coord;
import factorization.common.TileEntityGreenware.ClayState;

public class ItemFactorization extends ItemBlock {
    public ItemFactorization() {
        super(Core.registry.factory_block.blockID + Core.block_item_id_offset);
        new Exception().printStackTrace();
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    public ItemFactorization(int id) {
        super(id);
        //Y'know, that -256 is really retarded.
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public boolean placeBlockAt(ItemStack is, EntityPlayer player,
            World w, int x, int y, int z, int side, float hitX, float hitY,
            float hitZ) {
        Coord here = new Coord(w, x, y, z);
        FactoryType f = FactoryType.fromMd(is.getItemDamage());
        if (f == null) {
            is.stackSize = 0;
            return false;
        }
        TileEntity te = f.makeTileEntity();
        if (te instanceof TileEntityCommon) {
            boolean good = ((TileEntityCommon) te).canPlaceAgainst(here.copy().towardSide(CubeFace.oppositeSide(side)), side);
            if (!good) {
                return false;
            }
        }
        if (super.placeBlockAt(is, player, w, x, y, z, side, hitX, hitY, hitZ)) {
            //create our TileEntityFactorization
            //Coord c = new Coord(w, x, y, z).towardSide(side);

            w.setBlockTileEntity(here.x, here.y, here.z, te);
            if (te instanceof TileEntityCommon) {
                TileEntityCommon tec = (TileEntityCommon) te;
                tec.onPlacedBy(player, is, side);
                tec.getBlockClass().enforce(here);
            }
            if (!w.isRemote) {
                if (te instanceof TileEntityCommon) {
                    Packet p = ((TileEntityCommon) te).getAuxillaryInfoPacket();
                    Core.network.broadcastPacket(null, here, p); //XXX TODO: Is this necessary?
                }
            }
            here.dirty();
            return true;
        }
        return false;
    }

    public int getIconFromDamage(int damage) {
        return Core.registry.factory_block.getBlockTextureFromSideAndMetadata(0, damage);
    }

    public int getMetadata(int i) {
        return 15;
        //return i;
    }

    @Override
    public String getItemNameIS(ItemStack itemstack) {
        //XXX I think this is actually supposed to return localization IDs like "factory.whatever"
        // I don't think this actually gets called...
        int md = itemstack.getItemDamage();
        return "item.factoryBlock" + md;
    }

    @Override
    public String getItemName() {
        return "ItemFactorization";
    }

    @Override
    public void addInformation(ItemStack is, List infoList) {
        super.addInformation(is, infoList);
        if (is.isItemEqual(Core.registry.greenware_item)) {
            NBTTagCompound tag = is.getTagCompound();
            if (tag != null) {
                if (tag.hasKey("sculptureName")) {
                    String name = tag.getString("sculptureName");
                    if (name != null && name.length() > 0) {
                        infoList.add(name);
                    }
                } else if (tag.hasKey("parts")) {
                    infoList.add("Use /nameclay to name this");
                }
                if (tag.hasKey("parts")) {
                    NBTTagList l = tag.getTagList("parts");
                    infoList.add(l.tagCount() + " parts");
                }
                ClayState state = TileEntityGreenware.getStateFromInfo(tag.getInteger("touch"), 0);
                infoList.add(state.english);
            }
        }
        Core.brand(infoList);
    }
    
    @Override
    public boolean getShareTag() {
        return true;
    }
}
