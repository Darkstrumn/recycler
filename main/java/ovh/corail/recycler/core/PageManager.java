package ovh.corail.recycler.core;

import java.awt.Point;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import ovh.corail.recycler.container.SlotRecyclingBook;

public class PageManager {
	private int posX, posY;
	private int dimCase = 16;
	private List<SlotRecyclingBook> slots = Lists.newArrayList();
	private List<List<RecyclingRecipe>> pages;
	private int pageNum;
	private String search = "";
	
	public PageManager(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
		updatePages();
		createSlots();
	}
	
	public void setPage(int pageNum) {
		this.pageNum = pageNum;
		updateSlots();
	}
	
	private List<RecyclingRecipe> getSubList(List<RecyclingRecipe> listIn, String match) {
		List<RecyclingRecipe> listOut = Lists.newArrayList();
		match = match.toLowerCase();
		for (RecyclingRecipe recipe : listIn) {
			if (recipe.getItemRecipe().getDisplayName().toLowerCase().contains(match)) {
				listOut.add(recipe);
			}
		}
		return listOut;
	}
	
	public void setSearch(String text) {
		search = text;
		updatePages();
		updateSlots();
	}
	
	public void updatePages() {
		pageNum=0;
		List<RecyclingRecipe> list = Lists.newArrayList();
		if (search.isEmpty()) {
			list = RecyclingManager.getInstance().recipes;
		} else {
			list = getSubList(RecyclingManager.getInstance().recipes, search);
		}
		pages = Lists.newArrayList();
		int pageCount = (int) Math.ceil(list.size()/6.0D);
		int currentPage = 0;
		int startingId, endingId;
		for (int i = 0 ; i < pageCount ; i++) {
			startingId = currentPage*6;
			endingId = startingId + 6 < list.size() ? startingId + 6 : list.size();
			pages.add(currentPage, list.subList(startingId, endingId));
			currentPage++;
		}
		if (pages.size() == 0) {
			pages.add(0, list);
		}
	}
	
	public int getPageCount() {
		return pages.size();
	}
	
	public List<RecyclingRecipe> getPage(int pageNum) {
		if (pageNum >= 0 && pageNum<pages.size()) {
			return pages.get(pageNum);
		}
		return null;
	}
	
	public int getPageNum() {
		return pageNum;
	}
	
	public void updateSlots() {
		List<RecyclingRecipe> recipes = pages.get(pageNum);
		ItemStack stack;
		int slotNum = 0;
		for (SlotRecyclingBook slot : slots) {
			slot.setStack(ItemStack.EMPTY);
		}
		for (RecyclingRecipe recipe : recipes) {
			slots.get(slotNum).setStack(recipe.getItemRecipe());
			slotNum++;
			for (int i = 0 ; i < 9 ; i++) {
				if (i < recipe.getCount()) {
					stack = recipe.getStack(i);
				} else {
					stack = ItemStack.EMPTY;
				}
				slots.get(slotNum).setStack(stack);
				slotNum++;
			}
		}
	}
	
	private void createSlots() {
		int startX, startY;
		int slotNum = 0;
		/** each recipes line */
		for (int j=0 ; j < 3 ; j++) {
			startX = posX+30;
			startY = posY+10+(j*dimCase*3)+(2*j);
			/** 2 recipes on each line */
			for (int i=0 ; i < 2 ; i++) {
				/** recycle item */
				slots.add(new SlotRecyclingBook(slotNum++, startX, (startY+dimCase), dimCase));
				startX += dimCase+5;
				/** each result item */
				for (int caseY=0 ; caseY < 3 ; caseY++) {
					for (int caseX=0 ; caseX < 3 ; caseX++) {
						slots.add(new SlotRecyclingBook(slotNum++, (startX+(caseX*dimCase)), (startY+(caseY*dimCase)), dimCase));
					}
				}
				startX = posX+175;
			}
		}
		updateSlots();
	}
	
	public int getSlotCount() {
		return slots.size();
	}
	
	public int getSlotAtPos(int mouseX, int mouseY) {
		int slotHover = -1;
		for (int i = 0 ; i < slots.size() ; i++) {
			if (slots.get(i).hasPos(mouseX, mouseY)) {
				slotHover=i;
				break;
			}
		}
		return slotHover;
	}
	
	public Point getSlotPos(int slotNum) {
		if (slotNum >= 0 && slotNum<slots.size()) {
			return slots.get(slotNum).getPos();
		}
		return null;
		
	}
	
	public ItemStack getStackForSlot(int slotNum) {
		if (slotNum >= 0 && slotNum<slots.size()) {
			return slots.get(slotNum).getStack();
		}
		return ItemStack.EMPTY;
	}
}
