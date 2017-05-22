package xreliquary.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import xreliquary.init.ModItems;
import xreliquary.reference.Settings;
import xreliquary.util.NBTHelper;
import xreliquary.util.RegistryHelper;
import xreliquary.util.alkahestry.AlkahestChargeRecipe;
import xreliquary.util.alkahestry.Alkahestry;

import java.util.Map;

public class AlkahestryChargingRecipe implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		ItemStack tome = null;
		int amount = 0;

		for(int count = 0; count < inv.getSizeInventory(); count++) {
			ItemStack stack = inv.getStackInSlot(count);
			if(stack != null) {
				if(RegistryHelper.getItemRegistryName(stack.getItem()).equals(RegistryHelper.getItemRegistryName(ModItems.alkahestryTome))) {
					tome = stack.copy();
				} else {
					AlkahestChargeRecipe recipe = Alkahestry.matchChargeRecipe(stack);

					if(recipe != null) {
						amount += recipe.charge;
					} else {
						return false;
					}
				}
			}
		}

		return tome != null && NBTHelper.getInteger("charge", tome) + amount <= Settings.AlkahestryTome.chargeLimit;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack tome = null;
		int amount = 0;
		for(int count = 0; count < inv.getSizeInventory(); count++) {
			ItemStack stack = inv.getStackInSlot(count);
			if(stack != null) {
				if(RegistryHelper.getItemRegistryName(stack.getItem()).equals(RegistryHelper.getItemRegistryName(ModItems.alkahestryTome))) {
					tome = stack.copy();
				} else {
					AlkahestChargeRecipe recipe = Alkahestry.matchChargeRecipe(stack);
					if(recipe != null) {
						amount += recipe.charge;
					}
				}
			}
		}

		NBTHelper.setInteger("charge", tome, NBTHelper.getInteger("charge", tome) + amount);
		//noinspection ConstantConditions
		tome.setItemDamage(tome.getMaxDamage() - NBTHelper.getInteger("charge", tome));
		return tome;
	}

	@Override
	public int getRecipeSize() {
		return 9;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(ModItems.alkahestryTome, 1);
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv) {
		ItemStack[] aitemstack = new ItemStack[inv.getSizeInventory()];

		for(int i = 0; i < aitemstack.length; ++i) {
			aitemstack[i] = null;
		}

		return aitemstack;
	}
}
