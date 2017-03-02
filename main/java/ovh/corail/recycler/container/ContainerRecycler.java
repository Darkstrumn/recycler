package ovh.corail.recycler.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ovh.corail.recycler.handler.ConfigurationHandler;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.packet.ServerProgressMessage;
import ovh.corail.recycler.packet.VisualMessage;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class ContainerRecycler extends Container {
	private TileEntityRecycler inventory;

	public ContainerRecycler(EntityPlayer player, World world, int x, int y, int z, TileEntityRecycler inventory) {
		this.inventory = inventory;
		/** input slot 0 */
		this.addSlotToContainer(new SlotRecycler(inventory, 0, 27, 9));
		/** disk slot 1 */
		this.addSlotToContainer(new SlotRecycler(inventory, 1, 27, 27));
		/** output slots 2-19 */
		for (int i = inventory.firstOutput; i <= 10; i++) {
			this.addSlotToContainer(new SlotRecycler(inventory, i, ((i - inventory.firstOutput) * 18) + 8, 54));
			this.addSlotToContainer(new SlotRecycler(inventory, i + 9, ((i - inventory.firstOutput) * 18) + 8, 72));
		}
		/** visual slots 20-28 */
		for (int i = 0 ; i < 3 ; i++) {
			for (int j = 0 ; j < 3 ; j++) {
				this.addSlotToContainer(new SlotVisual(inventory.visual, (i*3) + j, (j * 16) + 118, i*16 + (ConfigurationHandler.fancyGui?2:3)));
			}
		}
		PacketHandler.INSTANCE.sendToServer(new VisualMessage(inventory.getPos()));
		inventory.refreshVisual(inventory.getStackInSlot(0));
		/** player slots 29-64 */
		bindPlayerInventory(player.inventory);
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
		ItemStack stack = super.slotClick(slotId, dragType, clickType, player);
		if (slotId == 0) {
			PacketHandler.INSTANCE.sendToServer(new VisualMessage(inventory.getPos()));
			inventory.refreshVisual(inventory.getStackInSlot(0));
		}
		/** reset progress */
		if ((slotId == 0 || slotId == 1) && inventory.isWorking()) {
			PacketHandler.INSTANCE.sendToServer(new ServerProgressMessage(inventory.getPos(), 0));
		}
		return stack;
	}

	private void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		int i;
		int j;
		for (i = 0; i < 3; i++) {
			for (j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(inventoryPlayer, ((i + 1) * 9) + j, (j * 18) + 8, (i * 18) + 106));
			}
		}
		for (j = 0; j < 9; j++) {
			this.addSlotToContainer(new Slot(inventoryPlayer, j, (j * 18) +8, 160));
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
				if (!this.mergeItemStack(itemstack1, 29, 64, true)) {
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
		/*if (index < 0 || index > 19) { return null; }
		Slot slot = (Slot)this.inventorySlots.get(index);
        return slot != null ? slot.getStack() : null;*/
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
	}
}
