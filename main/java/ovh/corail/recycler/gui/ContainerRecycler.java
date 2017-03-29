package ovh.corail.recycler.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.packet.ClientProgressMessage;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class ContainerRecycler extends Container {
	private TileEntityRecycler inventory;

	public ContainerRecycler(EntityPlayer player, World world, int x, int y, int z, TileEntityRecycler inventory) {
		this.inventory = inventory;
		/** input slot 0 */
		this.addSlotToContainer(new SlotRecycler(inventory, 0, 56, 17));
		/** disk slot 1 */
		this.addSlotToContainer(new SlotRecycler(inventory, 1, 56, 39));
		/** output slots 2-19 */
		for (int i = inventory.firstOutput; i <= 10; i++) {
			this.addSlotToContainer(new SlotRecycler(inventory, i, ((i - inventory.firstOutput) * 18) + 9, 68));
			this.addSlotToContainer(new SlotRecycler(inventory, i + 9, ((i - inventory.firstOutput) * 18) + 9, 86));
		}
		/** player slots 20-55 */
		bindPlayerInventory(player.inventory);
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
		ItemStack stack = super.slotClick(slotId, dragType, clickType, player);
		/** reset progress */
		if (!inventory.getWorld().isRemote && (slotId == 0 || slotId == 1) && inventory.isWorking()) {
				inventory.setProgress(0);
				PacketHandler.INSTANCE.sendToAllAround(new ClientProgressMessage(inventory.getPos(), 0),
					new TargetPoint(inventory.getWorld().provider.getDimension(), (double) inventory.getPos().getX(), (double) inventory.getPos().getY(), (double) inventory.getPos().getZ(), 12.0d));
		}
		return stack;
	}

	private void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		int i;
		int j;
		for (i = 0; i < 3; i++) {
			for (j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(inventoryPlayer, ((i + 1) * 9) + j, (j * 18) + 9, (i * 18) + 123));
			}
		}
		for (j = 0; j < 9; j++) {
			this.addSlotToContainer(new Slot(inventoryPlayer, j, (j * 18) + 9, 179));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = (Slot) this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			
			if (index < 19) {
				if (!this.mergeItemStack(itemstack1, 20, 55, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, 19, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}
		}

		return itemstack;
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
	}
}
