package ovh.corail.recycler.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotVisual extends Slot {
	private int id;

	public SlotVisual(InventoryBasic visual, int index, int xPos, int yPos) {
		super(visual, index, xPos, yPos);
		this.id = index;
	}
	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}
	@Override
	public void onSlotChange(ItemStack item1, ItemStack item2) {
		super.onSlotChange(item1, item2);
	}
	@Override
	public boolean canTakeStack(EntityPlayer playerIn) {
        return false;
    }
}
