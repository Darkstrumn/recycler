package ovh.corail.recycler.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ovh.corail.recycler.blocks.RecyclerBlock;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.core.MainUtil;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.packets.ProgressMessage;
import ovh.corail.recycler.recycling.RecyclingManager;
import ovh.corail.recycler.recycling.RecyclingRecipe;

public class TileEntityRecycler extends TileEntity implements ISidedInventory, ITickable {
	private int slotsCount = 20;
	public int firstOutput = 2;
	public ItemStack[] inventory;
	public InventoryBasic visual;
	private RecyclingManager recyclingManager;
	private int countTicks = 0;
	private final int maxTicks = 200;
	private boolean isWorking = false;
	private int progress = 0;

	public TileEntityRecycler() {
		super();
		this.inventory = new ItemStack[slotsCount];
		this.visual = new InventoryBasic("visual", true, 6);
		recyclingManager = RecyclingManager.getInstance();
	}

	public boolean canRecycle() {
		/** Disk input slot empty */
		ItemStack diskStack = getStackInSlot(1);
		if (diskStack == null) {
			return false;
		}
		if (diskStack.stackSize <= 0) {
			setInventorySlotContents(1, null);
			return false;
		}
		/** Item input slot empty */
		if (getStackInSlot(0) == null) {
			return false;
		}
		if (getStackInSlot(0).stackSize <= 0) {
			setInventorySlotContents(0, null);
			return false;
		}
		/** TODO ?necessary? */
		if (getStackInSlot(1).getItemDamage() >= getStackInSlot(1).getMaxDamage()) {
			setInventorySlotContents(1, null);
			return false;
		}
		return true;
	}

	public boolean recycle() {
		if (!canRecycle()) {
			return false;
		}
		ItemStack diskStack = getStackInSlot(1);
		/** Recette correspondante */
		int num_recipe = recyclingManager.hasRecipe(getStackInSlot(0));
		if (num_recipe < 0) {
			return false;
		}
		RecyclingRecipe currentRecipe = recyclingManager.getRecipe(num_recipe);
		/** Stacksize suffisant du slot input */
		if (getStackInSlot(0).stackSize < currentRecipe.getItemRecipe().stackSize) {
			return false;
		}
		int nb_input = (int) Math
				.floor((double) getStackInSlot(0).stackSize / (double) currentRecipe.getItemRecipe().stackSize);
		/** TODO Current Changes */
		if (isWorking) {
			nb_input = 1;
		}
		/** Limite d'utilisation du disque */
		int maxDiskUse = (int) Math.floor((double) (diskStack.getMaxDamage() - diskStack.getItemDamage()) / 10.0);
		if (maxDiskUse < nb_input) {
			nb_input = maxDiskUse;
		}
		/** Calcul du résultat */
		List<ItemStack> itemsList = recyclingManager.getResultStack(getStackInSlot(0), nb_input);
		/** TODO calcul des stacksizes pour les slots libres à mettre plus bas */
		int emptyCount = hasEmptySlot();
		if (emptyCount >= itemsList.size()) {
			/** Remplir les slots identiques non complets */
			/** Pour chaque résultat de la recette */
			for (int i = 0; i < itemsList.size(); i++) {
				/** Si le slot n'est pas à sa taille maximale */
				if (itemsList.get(i).stackSize != itemsList.get(i).getMaxStackSize()) {
					/** Pour chaque slot */
					for (int j = firstOutput; j < this.slotsCount; j++) {
						/** Même objet */
						if (itemsList.get(i) != null && itemsList.get(i)==inventory[j]) {
							int sommeStackSize = inventory[j].stackSize + itemsList.get(i).stackSize;
							if (sommeStackSize > inventory[j].getMaxStackSize()) {
								inventory[j].stackSize = inventory[j].getMaxStackSize();
								ItemStack resteStack = itemsList.get(i).copy();
								resteStack.stackSize = sommeStackSize - inventory[j].getMaxStackSize();
								itemsList.set(i, resteStack);
							} else {
								inventory[j].stackSize = sommeStackSize;
								itemsList.set(i, null);
								// break;
							}
						}
					}
				}
			}
			/** Remplissage des slots restants Output */
			for (int i = 0; i < itemsList.size(); i++) {
				if (itemsList.get(i) != null) {
					int emptySlot = getEmptySlot();
					setInventorySlotContents(emptySlot, itemsList.get(i).copy());
				}
			}

		} else {
			/** TODO to change... */
			Minecraft.getMinecraft().thePlayer
					.sendChatMessage(I18n.translateToLocal("message.recycler.notEnoughOutputSlots"));
			return false;
		}
		/** Vide le slot input */
		if (currentRecipe.getItemRecipe().stackSize * nb_input == getStackInSlot(0).stackSize) {
			setInventorySlotContents(0, null);
			emptyVisual();
		} else {
			ItemStack stack = getStackInSlot(0).copy();
			stack.stackSize = getStackInSlot(0).stackSize - (nb_input * currentRecipe.getItemRecipe().stackSize);
			setInventorySlotContents(0, stack);
		}
		/** Abime le disque */

		diskStack.setItemDamage(diskStack.getItemDamage() + (10 * nb_input));
		if (diskStack.getItemDamage() >= diskStack.getMaxDamage()) {
			this.setInventorySlotContents(1, null);
		} else {
			this.setInventorySlotContents(1, diskStack);
		}
		return true;
	}

	public int getEmptySlot() {
		for (int i = firstOutput; i < getSizeInventory(); i++) {
			ItemStack item = getStackInSlot(i);
			if (item == null) {
				return i;
			}
		}
		return -1;
	}
	
	public int hasEmptySlot() {
		int count = 0;
		for (int i = firstOutput; i < getSizeInventory(); i++) {
			if (getStackInSlot(i) == null) {
				count++;
			}
		}
		return count;
	}

	public void fillVisual(List<ItemStack> itemsList) {
		int num_slot = 0;
		for (int i = 0; i < itemsList.size(); i++) {
			if (num_slot < visual.getSizeInventory()) {
				visual.setInventorySlotContents(num_slot++, itemsList.get(i));
			}
		}
	}

	public void emptyVisual() {
		for (int i = 0; i < visual.getSizeInventory(); i++) {
			visual.setInventorySlotContents(i, null);
		}
	}

	public void refreshVisual(ItemStack stack) {
		emptyVisual();
		List<ItemStack> itemsList = recyclingManager.getResultStack(stack, 1);
		fillVisual(itemsList);
	}

	/** TODO changes about progress bar */
	@Override
	public void update() {
		if (worldObj.isRemote) {
			return;
		}
		if (isWorking) {
			countTicks--;
			if (countTicks <= 0) {
				if (!recycle()) {
					isWorking = false;
				} else {
					if (canRecycle()) {
						countTicks += maxTicks;
					} else {
						isWorking = false;
					}
				}
			}
		}
		progress = (int) Math.floor(((double) (maxTicks - countTicks) / (double) maxTicks) * 100.0);
		PacketHandler.INSTANCE.sendToAllAround(
				new ProgressMessage(getPos().getX(), getPos().getY(), getPos().getZ(), progress, isWorking),
				new TargetPoint(worldObj.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(),
						12));
	}

	public int getPercentWorking() {
		return progress;
	}

	public boolean isWorking() {
		return isWorking;
	}

	public int getCountTicks() {
		return countTicks;
	}

	public void refreshProgress(int progress, boolean isWorking) {
		this.progress = progress;
		this.isWorking = isWorking;
	}
	
	public void switchWorking() {
		isWorking = (isWorking ? false : true);
		countTicks = maxTicks;
		progress = 0;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		/* Output Slots */
		if (index > 1) {
			return false;
		}
		/* Disk Input Slot */
		if (index == 1) {
			if (stack.getItem() == Main.diamond_disk) {
				return true;
			} else {
				return false;
			}
		}
		/* Item Input Slot */
		int currentRecipe = recyclingManager.hasRecipe(stack);
		if (currentRecipe < 0) {
			return false;
		}
		return true;
	}
	
	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(I18n.translateToLocal("tile.recycler.name"));
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}
	
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.worldObj.getTileEntity(this.pos) != this ? false
				: player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
	}

	@Override
	public int getSizeInventory() {
		return slotsCount;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= this.getSizeInventory()) {
			return null;
		} else {
			return this.inventory[index];
		}
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index >= 0 && index < slotsCount) {
			inventory[index] = stack;
		} else {
			inventory[index] = null;
		}
		markDirty();
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = getStackInSlot(index);
		if (stack != null) {
			if (stack.stackSize <= count) {
				setInventorySlotContents(index, null);
				this.markDirty();
			} else {
				stack = stack.splitStack(count);
				if (stack.stackSize == 0) {
					setInventorySlotContents(index, null);
					this.markDirty();
				}
			}
		}
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack stack;
		if (index >= 0 && index < slotsCount) {
			stack = inventory[index];
		} else {
			stack = null;
		}
		return stack;
	}
	
	@Override
	public void clear() {
		for (int i = 0; i < this.getSizeInventory(); i++) {
			this.setInventorySlotContents(i, null);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setInteger("countTicks", countTicks);
		compound.setBoolean("isWorking", isWorking);
		compound.setInteger("progress", progress);
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < inventory.length; i++) {
			ItemStack stack = inventory[i];
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		compound.setTag("inventory", itemList);
		super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		countTicks = compound.getInteger("countTicks");
		isWorking = compound.getBoolean("isWorking");
		progress = compound.getInteger("progress");
		NBTTagList tagList = compound.getTagList("inventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inventory.length) {
				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
	}
}
