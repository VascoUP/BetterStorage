package net.mcft.copy.betterstorage.item;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.mcft.copy.betterstorage.ILockable;
import net.mcft.copy.betterstorage.enchantments.EnchantmentBetterStorage;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class ItemLock extends ItemBetterStorage {
	
	public ItemLock(int id) {
		super(id);
		setIconCoord(1, 0);
		
		setItemName("lock");
		LanguageRegistry.addName(this, "Lock");
		
		setCreativeTab(CreativeTabs.tabMisc);
	}
	
	@Override
	public boolean isItemTool(ItemStack stack) { return true; }
	@Override
	public int getItemEnchantability() { return 20; }
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
	                         int x, int y, int z, int side, float subX, float subY, float subZ) {
		if (world.isRemote) return false;
		TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity != null && entity instanceof ILockable) {
			ILockable lockable = (ILockable)entity;
			if (!lockable.canLock(stack)) return false;
			lockable.setLock(stack);
			// Remove the lock from the player's inventory.
			player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			return true;
		} else return false;
	}
	
	public static boolean isLock(ItemStack stack) {
		return (stack != null && stack.getItem() instanceof ItemLock);
	}
	
	/** Gets called when a player tries to open a locked container. <br>
	 *  Returns if the container can be opened. */
	public boolean tryOpen(ItemStack lock, EntityPlayer player, ItemStack key) {
		boolean success = ItemKey.tryKeyOpenLock(lock, key);
		if (!success) applyEffects(lock, player, 1);
		return success;
	}
	/** Gets called when a player tries to unlock a locked container. <br>
	 *  Returns if the container can be unlocked. */
	public boolean tryUnlock(ItemStack lock, EntityPlayer player, ItemStack key) {
		boolean success = ItemKey.tryKeyOpenLock(lock, key);
		if (!success) applyEffects(lock, player, 2);
		return success;
	}
	/** Applies effects when a player tries to unsuccessfully open / unlock a locked container. */
	public void applyEffects(ItemStack lock, EntityPlayer player, int power) {
		int shock = EnchantmentHelper.getLevel(EnchantmentBetterStorage.shock.effectId, lock);
		player.attackEntityFrom(DamageSource.magic, shock * power * 5 / 2);
		if (shock >= 3)
			player.setFire(shock * 2 * power);
	}
	
	// Static helper methods which just call the above methods:
	
	/** Gets called when a player tries to open a locked container. <br>
	 *  Returns if the container can be opened. */
	public static boolean lockTryOpen(ItemStack lock, EntityPlayer player, ItemStack key) {
		if (!isLock(lock)) return false;
		return ((ItemLock)lock.getItem()).tryOpen(lock, player, key);
	}
	/** Gets called when a player tries to unlock a locked container. <br>
	 *  Returns if the container can be unlocked. */
	public static boolean lockTryUnlock(ItemStack lock, EntityPlayer player, ItemStack key) {
		if (!isLock(lock)) return false;
		return ((ItemLock)lock.getItem()).tryUnlock(lock, player, key);
	}
	/** Applies effects when a player tries to unsuccessfully open / unlock a locked container. */
	public static void lockApplyEffects(ItemStack lock, EntityPlayer player, int power) {
		if (!isLock(lock)) return;
		((ItemLock)lock.getItem()).applyEffects(lock, player, power);
	}
	
}