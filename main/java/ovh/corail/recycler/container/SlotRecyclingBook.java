package ovh.corail.recycler.container;

import java.awt.Point;
import java.awt.Rectangle;

import net.minecraft.item.ItemStack;

public class SlotRecyclingBook {
	private int slotNum;
	private Rectangle pos;
	private ItemStack stack = ItemStack.EMPTY;
	
	public SlotRecyclingBook(int slotNum, int x, int y, int dimCase) {
		this.slotNum = slotNum;
		pos = new Rectangle(x, y, dimCase, dimCase);
	}
	
	public boolean hasPos(int x, int y) {
		return pos.contains(x, y);
	}
	
	public  Point getPos() {
		return pos.getLocation();
	}
	
	public int getSlotNum() {
		return slotNum;
	}
	
	public ItemStack getStack() {
		return stack;
	}
	
	public void setStack(ItemStack stack) {
		this.stack = stack;
	}
}
