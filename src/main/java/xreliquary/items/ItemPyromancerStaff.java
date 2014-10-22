package xreliquary.items;

import com.google.common.collect.ImmutableMap;
import lib.enderwizards.sandstone.init.ContentHandler;
import lib.enderwizards.sandstone.init.ContentInit;
import lib.enderwizards.sandstone.items.ItemToggleable;
import lib.enderwizards.sandstone.util.ContentHelper;
import lib.enderwizards.sandstone.util.InventoryHelper;
import lib.enderwizards.sandstone.util.NBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import xreliquary.Reliquary;
import xreliquary.lib.Names;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Xeno on 10/11/2014.
 */
@ContentInit
public class ItemPyromancerStaff extends ItemToggleable {
    public ItemPyromancerStaff() {
        super(Names.pyromancer_staff);
        this.setCreativeTab(Reliquary.CREATIVE_TAB);
        this.setMaxStackSize(1);
        canRepair = false;
    }

    @Override
    public boolean isFull3D(){ return true; }

    @Override
    public void onUpdate(ItemStack ist, World world, Entity e, int i, boolean f) {
        // handleEyeEffect(ist);
        if (!(e instanceof EntityPlayer))
            return;
        EntityPlayer player = (EntityPlayer) e;
        if (player.getCurrentEquippedItem() == null)
            return;
        //always on!
        //if (player.getCurrentEquippedItem().getItem() instanceof ItemPyromancerStaff) {
            doFireballEffect(ist, player);
            if (!this.isEnabled(ist))
                doExtinguishEffect(player);
        //}

        if (this.isEnabled(ist)) {
            scanForFireChargeAndBlazePowder(ist, player);
        }
    }


    @Override
    public void addInformation(ItemStack ist, EntityPlayer player, List list, boolean par4) {
        //maps the contents of the Pyromancer's staff to a tooltip, so the player can review the torches stored within.
        String charges = "0";
        String blaze = "0";
        NBTTagCompound tagCompound = NBTHelper.getTag(ist);
        if (tagCompound != null) {
            NBTTagList tagList = tagCompound.getTagList("Items", 10);
            for (int i = 0; i < tagList.tagCount(); ++i) {
                NBTTagCompound tagItemData = tagList.getCompoundTagAt(i);
                String itemName = tagItemData.getString("Name");
                Item containedItem = ContentHandler.getItem(itemName);
                int quantity = tagItemData.getInteger("Quantity");

                if (containedItem == Items.blaze_powder) {
                    blaze = Integer.toString(quantity);
                } else if (containedItem == Items.fire_charge) {
                    charges = Integer.toString(quantity);
                }
            }
        }
        this.formatTooltip(ImmutableMap.of("charges", charges, "blaze", blaze), ist, list);
    }


    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 32000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack ist) {
        return EnumAction.block;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack ist, World world, EntityPlayer player) {
        if (player.isSneaking())
            super.onItemRightClick(ist, world, player);
        else
            player.setItemInUse(ist, this.getMaxItemUseDuration(ist));
        return ist;
    }

    @Override
    public void onUsingTick(ItemStack ist, EntityPlayer player, int count) {
        //mop call and fakes onItemUse, getting read to do the eruption effect. If the item is enabled, it just sets a bunch of fires!
        MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(player.worldObj, player, true);
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            float xOff = (float) (mop.blockX - player.posX);
            float yOff = (float) (mop.blockY - player.posY);
            float zOff = (float) (mop.blockZ - player.posZ);
            this.onItemUse(ist, player, player.worldObj, mop.blockX, mop.blockY, mop.blockZ, mop.sideHit, xOff, yOff, zOff);
        }
    }

    public boolean onItemUse(ItemStack ist, EntityPlayer player, World world, int x, int y, int z, int sideHit, float xOff, float yOff, float zOff)
    {
        //while enabled only, if disabled, it will do an eruption effect instead.
        if (this.isEnabled(ist)) {
            if (sideHit == 0) {
                --y;
            }

            if (sideHit == 1) {
                ++y;
            }

            if (sideHit == 2) {
                --z;
            }

            if (sideHit == 3) {
                ++z;
            }

            if (sideHit == 4) {
                --x;
            }

            if (sideHit == 5) {
                ++x;
            }

            if (!player.canPlayerEdit(x, y, z, sideHit, ist)) {
                return false;
            } else {
                if (world.isAirBlock(x, y, z)) {
                    world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
                    world.setBlock(x, y, z, Blocks.fire);
                }
                return false;
            }
        } else {
            double areaCoefficient = 3D;

            double soundX = x;
            double soundY = y;
            double soundZ = z;
            player.worldObj.playSound(soundX, soundY, soundZ, "mob.ghast.fireball", 0.3F, 0.13F + (0.1F * itemRand.nextFloat()), false);

            if (player.getItemInUseDuration() != 0 && player.getItemInUseDuration() % 20 == 0) {
                if (removeItemFromInternalStorage(ist, Items.blaze_powder, getBlazePowderCost(), player.worldObj.isRemote)) {
                    doEruptionEffect(ist, player, x, y, z, areaCoefficient);
                }
            }
            for (int particleCount = 0; particleCount < 4; ++particleCount) {
                double randX = x + (player.worldObj.rand.nextFloat() - 0.5F) * areaCoefficient;
                double randY = y + 0.5D + (player.worldObj.rand.nextFloat() / 2D);
                double randZ = z + (player.worldObj.rand.nextFloat() - 0.5F) * areaCoefficient;
                player.worldObj.spawnParticle("lava", randX, randY, randZ, 0D,0D,0D);
            }
            return false;
        }
    }


    public void doEruptionEffect(ItemStack ist, EntityPlayer player, int x, int y, int z, double areaCoefficient) {
        if (player.worldObj.isRemote)
            return;
        double lowerX = x - areaCoefficient;
        double lowerY = y;
        double lowerZ = z - areaCoefficient;
        double upperX = x + areaCoefficient;
        double upperY = y + areaCoefficient;
        double upperZ = z + areaCoefficient;
        List eList = player.worldObj.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getBoundingBox(lowerX, lowerY, lowerZ, upperX, upperY, upperZ));
        Iterator iterator = eList.iterator();


        while (iterator.hasNext()) {
            Entity e = (Entity)iterator.next();
            if (e instanceof EntityLivingBase && !e.isEntityEqual(player)) {
                if (!e.isImmuneToFire())
                    e.attackEntityFrom(DamageSource.causePlayerDamage(player), 4F);
                e.setFire(40);
            }
        }
    }

    private void scanForFireChargeAndBlazePowder(ItemStack ist, EntityPlayer player) {
        List<Item> absorbItems = new ArrayList<Item>();
        absorbItems.add(Items.fire_charge);
        absorbItems.add(Items.blaze_powder);
        for (Item absorbItem : absorbItems) {
            if (!isInternalStorageFullOfItem(ist, absorbItem) && InventoryHelper.consumeItem(absorbItem, player)) {
                addItemToInternalStorage(ist, absorbItem, false);
            }
        }
    }


    private void addItemToInternalStorage(ItemStack ist, Item item, boolean isAbsorb) {
        int quantityIncrease = item == Items.fire_charge ? (isAbsorb ? getGhastAbsorbWorth() : getFireChargeWorth()) : (isAbsorb ? getBlazeAbsorbWorth() : getBlazePowderWorth());
        NBTTagCompound tagCompound = NBTHelper.getTag(ist);

        if (tagCompound.getTag("Items") == null)
            tagCompound.setTag("Items", new NBTTagList());
        NBTTagList tagList = tagCompound.getTagList("Items", 10);

        boolean added = false;
        for (int i = 0; i < tagList.tagCount(); ++i)
        {
            NBTTagCompound tagItemData = tagList.getCompoundTagAt(i);
            String itemName = tagItemData.getString("Name");
            if (itemName.equals(ContentHelper.getIdent(item))) {
                int quantity = tagItemData.getInteger("Quantity");
                tagItemData.setInteger("Quantity", quantity + quantityIncrease);
                added = true;
            }
        }
        if (!added) {
            NBTTagCompound newTagData = new NBTTagCompound();
            newTagData.setString("Name", ContentHelper.getIdent(item));
            newTagData.setInteger("Quantity", quantityIncrease);
            tagList.appendTag(newTagData);
        }

        tagCompound.setTag("Items", tagList);

        NBTHelper.setTag(ist, tagCompound);
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack ist) {
        if (!(entityLiving instanceof EntityPlayer))
            return true;
        EntityPlayer player = (EntityPlayer)entityLiving;
        Vec3 lookVec = player.getLookVec();

        if (player.isSneaking()) {
            //ghast fireball!
            if (removeItemFromInternalStorage(ist, Items.fire_charge, getFireChargeCost(), player.worldObj.isRemote)) {
                player.worldObj.playAuxSFXAtEntity(player, 1008, (int)player.posX, (int)player.posY, (int)player.posZ, 0);
                //if (!player.worldObj.isRemote) {
                EntityLargeFireball fireball = new EntityLargeFireball(player.worldObj, player, lookVec.xCoord, lookVec.yCoord, lookVec.zCoord);
                fireball.accelerationX = lookVec.xCoord;
                fireball.accelerationY = lookVec.yCoord;
                fireball.accelerationZ = lookVec.zCoord;
                fireball.posX += lookVec.xCoord;
                fireball.posY += lookVec.yCoord;
                fireball.posZ += lookVec.zCoord;
                fireball.posY = player.posY + player.getEyeHeight();
                player.worldObj.spawnEntityInWorld(fireball);
                //}

            }

        } else {
            //blaze fireball!
            if (removeItemFromInternalStorage(ist, Items.blaze_powder, getBlazePowderCost(), player.worldObj.isRemote)) {
                player.worldObj.playAuxSFXAtEntity(player, 1009, (int)player.posX, (int)player.posY, (int)player.posZ, 0);
                //if (!player.worldObj.isRemote) {
                EntitySmallFireball fireball = new EntitySmallFireball(player.worldObj, player, lookVec.xCoord, lookVec.yCoord, lookVec.zCoord);
                fireball.accelerationX = lookVec.xCoord;
                fireball.accelerationY = lookVec.yCoord;
                fireball.accelerationZ = lookVec.zCoord;
                fireball.posX += lookVec.xCoord;
                fireball.posY += lookVec.yCoord;
                fireball.posZ += lookVec.zCoord;
                fireball.posY = player.posY + player.getEyeHeight();
                player.worldObj.spawnEntityInWorld(fireball);
                //}
            }
        }
        return false;
    }

    public boolean removeItemFromInternalStorage(ItemStack ist, Item item, int cost, boolean simulate) {
        if (hasItemInInternalStorage(ist, item, cost)) {
            NBTTagCompound tagCompound = NBTHelper.getTag(ist);

            NBTTagList tagList = tagCompound.getTagList("Items", 10);

            NBTTagList replacementTagList = new NBTTagList();

            for (int i = 0; i < tagList.tagCount(); ++i)
            {
                NBTTagCompound tagItemData = tagList.getCompoundTagAt(i);
                String itemName = tagItemData.getString("Name");
                if (itemName.equals(ContentHelper.getIdent(item))) {
                    int quantity = tagItemData.getInteger("Quantity");
                    if (!simulate)
                        tagItemData.setInteger("Quantity", quantity - cost);
                }
                replacementTagList.appendTag(tagItemData);
            }
            tagCompound.setTag("Items", replacementTagList);
            NBTHelper.setTag(ist, tagCompound);
            return true;
        }
        return false;

    }

    private boolean hasItemInInternalStorage(ItemStack ist, Item item, int cost) {
        NBTTagCompound tagCompound = NBTHelper.getTag(ist);
        if (tagCompound.hasNoTags()) {
            tagCompound.setTag("Items", new NBTTagList());
            return false;
        }

        NBTTagList tagList = tagCompound.getTagList("Items", 10);
        for (int i = 0; i < tagList.tagCount(); ++i)
        {
            NBTTagCompound tagItemData = tagList.getCompoundTagAt(i);
            String itemName = tagItemData.getString("Name");
            if (itemName.equals(ContentHelper.getIdent(item))) {
                int quantity = tagItemData.getInteger("Quantity");
                return quantity >= cost;
            }
        }

        return false;
    }

    private boolean isInternalStorageFullOfItem(ItemStack ist, Item item) {
        int quantityLimit = item == Items.fire_charge ? getFireChargeLimit() : getBlazePowderLimit();
        if (hasItemInInternalStorage(ist, item, 1)) {
            NBTTagCompound tagCompound = NBTHelper.getTag(ist);
            NBTTagList tagList = tagCompound.getTagList("Items", 10);

            for (int i = 0; i < tagList.tagCount(); ++i)
            {
                NBTTagCompound tagItemData = tagList.getCompoundTagAt(i);
                String itemName = tagItemData.getString("Name");
                if (itemName.equals(ContentHelper.getIdent(item))) {
                    int quantity = tagItemData.getInteger("Quantity");
                    return quantity >= quantityLimit;
                }
            }
        }
        return false;
    }

    private int getFireChargeWorth() {
        return Reliquary.CONFIG.getInt(Names.pyromancer_staff, "fire_charge_worth");
    }
    private int getFireChargeCost() {
        return Reliquary.CONFIG.getInt(Names.pyromancer_staff, "fire_charge_cost");
    }
    private int getFireChargeLimit() {
        return Reliquary.CONFIG.getInt(Names.pyromancer_staff, "fire_charge_limit");
    }
    private int getBlazePowderWorth() {
        return Reliquary.CONFIG.getInt(Names.pyromancer_staff, "blaze_powder_worth");
    }
    private int getBlazePowderCost() {
        return Reliquary.CONFIG.getInt(Names.pyromancer_staff, "blaze_powder_cost");
    }
    private int getBlazePowderLimit() {
        return Reliquary.CONFIG.getInt(Names.pyromancer_staff, "blaze_powder_limit");
    }
    private int getBlazeAbsorbWorth() {
        return Reliquary.CONFIG.getInt(Names.pyromancer_staff, "blaze_absorb_worth");
    }
    private int getGhastAbsorbWorth() {
        return Reliquary.CONFIG.getInt(Names.pyromancer_staff, "ghast_absorb_worth");
    }


    private void doExtinguishEffect(EntityPlayer player) {
        if (player.isBurning()) {
            player.extinguish();
        }
        int x = (int) Math.floor(player.posX);
        int y = (int) Math.floor(player.posY);
        int z = (int) Math.floor(player.posZ);
        for (int xOff = -3; xOff <= 3; xOff++) {
            for (int yOff = -3; yOff <= 3; yOff++) {
                for (int zOff = -3; zOff <= 3; zOff++)
                    if (ContentHelper.getIdent(player.worldObj.getBlock(x + xOff, y + yOff, z + zOff)).equals(ContentHelper.getIdent(Blocks.fire))) {
                        player.worldObj.setBlock(x + xOff, y + yOff, z + zOff, Blocks.air);
                        player.worldObj.playSoundEffect(x + xOff + 0.5D, y + yOff + 0.5D, z + zOff + 0.5D, "random.fizz", 0.5F, 2.6F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.8F);
                    }
            }
        }
    }

    private void doFireballEffect(ItemStack ist, EntityPlayer player) {
        List ghastFireballs = player.worldObj.getEntitiesWithinAABB(EntityLargeFireball.class, AxisAlignedBB.getBoundingBox(player.posX - 5, player.posY - 5, player.posZ - 5, player.posX + 5, player.posY + 5, player.posZ + 5));
        Iterator fire1 = ghastFireballs.iterator();
        while (fire1.hasNext()) {
            EntityLargeFireball fireball = (EntityLargeFireball) fire1.next();
            if (fireball.shootingEntity == player)
                continue;
            if (player.getDistanceToEntity(fireball) < 4) {
                if (!isInternalStorageFullOfItem(ist, Items.fire_charge) && InventoryHelper.consumeItem(Items.fire_charge, player)) {
                        addItemToInternalStorage(ist, Items.fire_charge, true);
                    player.worldObj.playSoundEffect(fireball.posX, fireball.posY, fireball.posZ, "random.fizz", 0.5F, 2.6F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.8F);
                }
                    fireball.setDead();
            }
        }
        List blazeFireballs = player.worldObj.getEntitiesWithinAABB(EntitySmallFireball.class, AxisAlignedBB.getBoundingBox(player.posX - 3, player.posY - 3, player.posZ - 3, player.posX + 3, player.posY + 3, player.posZ + 3));
        Iterator fire2 = blazeFireballs.iterator();
        while (fire2.hasNext()) {
            EntitySmallFireball fireball = (EntitySmallFireball) fire2.next();
            if (fireball.shootingEntity == player)
                continue;
            for (int particles = 0; particles < 4; particles++) {
                player.worldObj.spawnParticle("reddust", fireball.posX, fireball.posY, fireball.posZ, 0.0D, 1.0D, 1.0D);
            }
            player.worldObj.playSoundEffect(fireball.posX, fireball.posY, fireball.posZ, "random.fizz", 0.5F, 2.6F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.8F);

            if (!isInternalStorageFullOfItem(ist, Items.blaze_powder) && InventoryHelper.consumeItem(Items.blaze_powder, player)) {
                    addItemToInternalStorage(ist, Items.blaze_powder, true);
            }
            fireball.setDead();
        }
    }
}
