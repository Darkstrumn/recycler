package ovh.corail.recycler.core;

import java.awt.Point;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import ovh.corail.recycler.gui.SlotVisual;

public class VisualManager {
	private List<SlotVisual> visual = Lists.newArrayList();
	private int dimCase = 16;
	
	public VisualManager() {
		
	}
	
	public int getDimCase() {
		return dimCase;
	}
	
	public void addVisual(int slotNum, int x, int y) {
		if (slotNum >= 0 && slotNum < visual.size()) {
			visual.set(slotNum, new SlotVisual(slotNum, x, y, dimCase));
		} else {
			visual.add(slotNum, new SlotVisual(slotNum, x, y, dimCase));
		}
	}
	
	public void setVisual(int slotNum, int x, int y) {
		
	}
	
	public int getVisualCount() {
		return visual.size();
	}
	
	public ItemStack getStackInVisual(int slotNum) {
		if (slotNum < 0 && slotNum >= visual.size()) { return ItemStack.EMPTY; }
		return visual.get(slotNum).getStack();
	}
	
	public Point getPosInVisual(int slotNum) {
		if (slotNum < 0 && slotNum >= visual.size()) { return null; }
		return visual.get(slotNum).getPos();
	}
	
	public int getSlotAtPos(int mouseX, int mouseY) {
		int slotHover = -1;
		for (int i = 0 ; i < visual.size() ; i++) {
			if (visual.get(i).hasPos(mouseX, mouseY)) {
				slotHover=i;
				break;
			}
		}
		return slotHover;
	}
	
	public void fillVisual(List<ItemStack> itemsList) {
		int num_slot = 0;
		for (int i = 0; i < itemsList.size(); i++) {
			if (num_slot >= visual.size()) { break; }
			visual.get(i).setStack(itemsList.get(i));
			num_slot++;
		}
	}

	public void emptyVisual() {
		for (int i = 0; i < visual.size(); i++) {
			visual.get(i).setStack(ItemStack.EMPTY);
		}
	}

}
