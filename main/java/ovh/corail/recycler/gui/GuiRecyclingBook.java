package ovh.corail.recycler.gui;

import java.awt.Point;
import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import ovh.corail.recycler.container.SlotRecyclingBook;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.core.RecyclingRecipe;

public class GuiRecyclingBook extends GuiScreen {
	private int pageNum = 0;
	private int bookWidth = 176;
	private int bookHeight = 179;
	private int dimCase = 16;
	private List<List<RecyclingRecipe>> pages = Lists.newArrayList();
	private List<SlotRecyclingBook> slots = Lists.newArrayList();
	
	public GuiRecyclingBook() {
		super();
	}
	
	private void createSlots() {
		int posX = ((this.width - this.bookWidth) / 2);
		int posY = ((this.height - this.bookHeight) / 2);
		int startX, startY;
		int slotNum = -1;
		/** each recipes line */
		for (int j=0 ; j < 3 ; j++) {
			startX = posX+10;
			startY = posY+5+(j*dimCase*3)+(5*j);
			/** 2 recipes on each line */
			for (int i=0 ; i < 2 ; i++) {
				/** recycle item */
				slots.add(new SlotRecyclingBook(++slotNum, startX, (startY+dimCase), dimCase));
				startX += dimCase+5;
				/** each result item */
				for (int caseY=0 ; caseY < 3 ; caseY++) {
					for (int caseX=0 ; caseX < 3 ; caseX++) {
						slots.add(new SlotRecyclingBook(++slotNum, (startX+(caseX*dimCase)), (startY+(caseY*dimCase)), dimCase));
					}
				}
				startX += dimCase*4;
			}
		}
	}
	
	private void createPages() {
		List<RecyclingRecipe> list = RecyclingManager.getInstance().recipes;
		int pageCount = (int) Math.ceil(list.size()/6.0D);
		int currentPage = 0;
		int startingId, endingId;
		for (int i = 0 ; i < pageCount ; i++) {
			startingId = currentPage*6;
			endingId = startingId + 6 < list.size() ? startingId + 6 : list.size();
			pages.add(currentPage, list.subList(startingId, endingId));
			currentPage++;
		}
	}
	
	private void enableButtons() {
		if (pageNum > 0 && pageNum+1 < pages.size()) {
			buttonList.get(0).enabled=true;
			buttonList.get(1).enabled=true;
		} else if (pageNum == 0) {
			buttonList.get(0).enabled=false;
			buttonList.get(1).enabled=true;
		} else {
			buttonList.get(0).enabled=true;
			buttonList.get(1).enabled=false;
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int posX = ((this.width - this.bookWidth) / 2);
		int posY = ((this.height - this.bookHeight) / 2);
		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		buttonList.add(new GuiButton(0, posX+10, posY+160, 40, 14, "<--"));
		buttonList.add(new GuiButton(1, posX+120, posY+160, 40, 14, "-->"));
		enableButtons();
		createSlots();
		createPages();
	}
	
	@Override
	public boolean doesGuiPauseGame() {
	    return false;
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		switch (button.id) {
			case 0: /** Button 1 */
				if (pageNum > 0) {
					pageNum--;
				}
				break;
			case 1: /** Button 2 */
				if (pageNum+1 < pages.size()) {
					pageNum++;
				}
				break;
			default:
		}
		enableButtons();
	}
	
	@Override
    public void drawDefaultBackground() {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(Main.textureVanillaRecycler);
        int posX = ((this.width - this.bookWidth) / 2);
		int posY = ((this.height - this.bookHeight) / 2);
		drawTexturedModalRect(posX, posY, 0, 0, this.bookWidth, this.bookHeight);
    }
	@Override
	public void drawScreen(int mouseX, int mouseY, float par3) {
		drawDefaultBackground();
		int posX = ((this.width - this.bookWidth) / 2);
		int posY = ((this.height - this.bookHeight) / 2);
		/** grid render */
		int startX = posX+10;
		int startY = posY+5;
		for (int j=0 ; j < 3 ; j++) {
			for (int i=0 ; i < 2 ; i++) {
				displayGrid((i==0?startX:startX+(5*dimCase)+5), startY+(3*dimCase*j)+(5*j));
			}
		}
		/** items render */
		RenderHelper.enableGUIStandardItemLighting();
		RecyclingRecipe recipe;
		List<RecyclingRecipe> currentPage = pages.get(pageNum);
		int num_recipe = 0;
		for (int j=0 ; j < 3 ; j++) {
			for (int i=0 ; i < 2 ; i++) {
				/** for last page */
				if (num_recipe>=currentPage.size()) { break; }
				recipe = currentPage.get(num_recipe);
				displayItems(recipe, (i==0?startX:startX+(5*dimCase)+5), startY+(3*dimCase*j)+(5*j));
				num_recipe++;
			}
		}
		RenderHelper.disableStandardItemLighting();
		super.drawScreen(mouseX, mouseY, par3);
		int slotHover = -1;
		for (int i = 0 ; i < slots.size() ; i++) {
			if (slots.get(i).hasPos(mouseX, mouseY)) {
				slotHover=i;
				break;
			}
		}
		/** hover slots */
		if (slotHover > -1) {
			ItemStack stack;
			int recipe_num = (int) Math.floor(slotHover/10.0D);
			int reste = slotHover%10;
			if (recipe_num < pages.get(pageNum).size()) {
				recipe = pages.get(pageNum).get(recipe_num);
				if (reste == 0) {
					stack = recipe.getItemRecipe();
				} else {
					reste--;
					if (reste < recipe.getCount()) {
						stack = recipe.getStack(reste);
					} else {
						stack = ItemStack.EMPTY;
					}
				}
				if (!stack.isEmpty()) {
					Point pos = slots.get(slotHover).getPos();
					this.renderToolTip(stack, pos.x, pos.y);
				}
			}
		}
	}
	
	private void displayItems(RecyclingRecipe recipe, int startX, int startY) {
		itemRender.renderItemAndEffectIntoGUI(recipe.getItemRecipe(), startX, startY+dimCase);
		itemRender.renderItemOverlays(fontRenderer, recipe.getItemRecipe(), startX, startY+dimCase);
		int x,y;
		ItemStack stack;
		startX += dimCase+5;
		int num_stack = 0;
		for (int j=0 ; j < 3 ; j++) {
			for (int i=0 ; i < 3 ; i++) {
				x = startX+(i*dimCase);
				y = startY+(j*dimCase);
				stack = (num_stack) < recipe.getCount() ? recipe.getStack(num_stack) : ItemStack.EMPTY;
				itemRender.renderItemAndEffectIntoGUI(stack, x, y);
				itemRender.renderItemOverlays(fontRenderer, stack, x, y);
				num_stack++;
			}
		}
	}
	
	private void displayGrid(int startX, int startY) {
		drawTexturedModalRect(startX, startY+dimCase, 240, 0, dimCase, dimCase);
		int x,y;
		startX += dimCase+5;
		for (int j=0 ; j < 3 ; j++) {
			for (int i=0 ; i < 3 ; i++) {
				x = startX+(i*dimCase);
				y = startY+(j*dimCase);
				drawTexturedModalRect(x, y, 240, 0, dimCase, dimCase);
			}
		}
	}
}
