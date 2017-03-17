package ovh.corail.recycler.container;

import java.awt.Point;
import java.awt.Rectangle;

public class SlotRecyclingBook {
	private int slotNum;
	private Rectangle pos;
	
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
}
