package ovh.corail.recycler.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import ovh.corail.recycler.core.Main;

public class TileEntityInventory extends TileEntity implements ISidedInventory {
	public final int count = 20;
	public int firstOutput = 2;
	protected ItemStack[] inventory;

	public TileEntityInventory() {
		super();
		this.inventory = new ItemStack[count];
		for (int i = 0; i < count ;i++) {
			this.inventory[i] = ItemStack.EMPTY;
		}
	}
	
	public int getEmptySlot() {
		for (int i = firstOutput; i < getSizeInventory(); i++) {
			if (getStackInSlot(i).isEmpty()) {
				return i;
			}
		}
		return -1;
	}
	
	public int hasEmptySlot() {
		int count = 0;
		for (int i = firstOutput; i < getSizeInventory(); i++) {
			if (getStackInSlot(i).isEmpty()) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	public int getSizeInventory() {
		return count;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= this.getSizeInventory()) {
			return ItemStack.EMPTY;
		} else {
			return this.inventory[index];
		}
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = getStackInSlot(index);
		if (!stack.isEmpty()) {
			if (stack.getCount() <= count) {
				setInventorySlotContents(index, ItemStack.EMPTY);
				this.markDirty();
			} else {
				stack = stack.splitStack(count);
				if (stack.getCount() == 0) {
					setInventorySlotContents(index, ItemStack.EMPTY);
					this.markDirty();
				}
			}
		}
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack stack;
		if (index >= 0 && index < count) {
			stack = inventory[index];
		} else {
			stack = ItemStack.EMPTY;
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index >= 0 && index < count) {
			inventory[index] = stack;
		} else {
			inventory[index] = ItemStack.EMPTY;
		}
		markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
		super.markDirty();
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		pos = this.getPos();
		return world.getTileEntity(pos) == this && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
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
	public void clear() {
		for (int i = 0; i < this.getSizeInventory(); i++) {
			this.setInventorySlotContents(i, ItemStack.EMPTY);
		}
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
	public ITextComponent getDisplayName() {
		return null;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		switch (side) {
		/** insert */
		case DOWN:
			int[] slotsExtract = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
			return slotsExtract;
		/** extract */
		default: 
			int[] slotsInsert = {0, 1};
			return slotsInsert;
		}
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return direction == EnumFacing.DOWN && index > 1 && index < count;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
		return direction != EnumFacing.DOWN && ((index == 1 && stack.getItem() == Main.diamond_disk) || (index == 0 && stack.getItem() != Main.diamond_disk));
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < inventory.length; i++) {
			ItemStack stack = (inventory[i]==null?ItemStack.EMPTY:inventory[i]);
			if (!stack.isEmpty()) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		if (!itemList.hasNoTags()) {
			compound.setTag("inventory", itemList);
		}
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagList tagList = compound.getTagList("inventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			int j = tag.getByte("Slot") & 255;
			if (j >= 0 && j < inventory.length) {
				inventory[j] = new ItemStack(tag);
			}
		}
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.inventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
	}


}
