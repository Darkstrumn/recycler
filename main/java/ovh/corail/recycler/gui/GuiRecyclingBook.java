package ovh.corail.recycler.gui;

import java.awt.Point;
import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ovh.corail.recycler.container.SlotRecyclingBook;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.core.PageManager;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.core.RecyclingRecipe;

public class GuiRecyclingBook extends GuiScreen {
	private int bookWidth = 176;
	private int bookHeight = 179;
	private int dimCase = 16;
	private PageManager pm;

	private GuiTextField searchBox;
	private ItemStack currentBook;
	
	public GuiRecyclingBook() {
		super();
	}
	
	private void enableButtons() {
		int pageNum = pm.getPageNum();
		if (pageNum > 0 && pageNum+1 < pm.getPageCount()) {
			buttonList.get(0).enabled = true;
			buttonList.get(1).enabled = true;
		} else if (pageNum == 0) {
			buttonList.get(0).enabled = false;
			buttonList.get(1).enabled = pm.getPageCount() > 1 ? true : false;
		} else {
			buttonList.get(0).enabled = true;
			buttonList.get(1).enabled = false;
		}
	}
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (searchBox.textboxKeyTyped(typedChar, keyCode)) {
			pm.addCharToSearch(typedChar);
			enableButtons();
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int posX = ((this.width - this.bookWidth) / 2);
		int posY = ((this.height - this.bookHeight) / 2);
		Keyboard.enableRepeatEvents(true);
		loadSearchBox(posX, posY);
		pm = new PageManager(posX, posY);
		loadButtons(posX, posY);
	}
	
	private void loadButtons(int posX, int posY) {
		buttonList.clear();
		buttonList.add(new GuiButton(0, posX+10, posY+160, 40, 14, "<--"));
		buttonList.add(new GuiButton(1, posX+125, posY+160, 40, 14, "-->"));
		enableButtons();
	}
	
	private void loadSearchBox(int posX, int posY) {
		searchBox = new GuiTextField(2, fontRenderer, posX+57, posY+160, 64, 12);
		searchBox.setEnableBackgroundDrawing(true);
		searchBox.setFocused(true);
		searchBox.setMaxStringLength(20);
		searchBox.setText("");
	}
	
	@Override
	public boolean doesGuiPauseGame() {
	    return false;
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		int pageNum = pm.getPageNum();
		switch (button.id) {
			case 0: /** Button 1 */
				if (pageNum > 0) {
					pm.setPage(pageNum-1);
				}
				break;
			case 1: /** Button 2 */
				if (pageNum+1 < pm.getPageCount()) {
					pm.setPage(pageNum+1);
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

		int startX = posX+10;
		int startY = posY+5;
		
		/** grid render */
		for (int slot = 0 ; slot < pm.getSlotCount() ; slot++) {
			displayGrid(pm.getSlotPos(slot));
		}
		/** items render */
		RenderHelper.enableGUIStandardItemLighting();
		RecyclingRecipe recipe;
		List<RecyclingRecipe> currentPage = pm.getPage(pm.getPageNum());
		ItemStack stack = ItemStack.EMPTY;
		for (int slot = 0 ; slot < pm.getSlotCount() ; slot++) {
			displayItems(pm.getStackForSlot(slot), pm.getSlotPos(slot));
		}
		RenderHelper.disableStandardItemLighting();
		super.drawScreen(mouseX, mouseY, par3);
		/** hover slots */
		//RecyclingRecipe recipe;
		int slotHover = pm.getSlotAtPos(mouseX, mouseY);
		if (slotHover > -1) {
			stack = pm.getStackForSlot(slotHover);
			if (!stack.isEmpty()) {
				Point pos = pm.getSlotPos(slotHover);
				this.renderToolTip(stack, pos.x, pos.y);
			}
		}
		/** search box */
		searchBox.drawTextBox();
	}
	
	private void displayItems(ItemStack stack, Point pos) {
		if (stack == null || stack.isEmpty()) { return; }
		itemRender.renderItemAndEffectIntoGUI(stack, pos.x, pos.y);
		itemRender.renderItemOverlays(fontRenderer, stack, pos.x, pos.y);
	}
	
	private void displayGrid(Point pos) {
		drawTexturedModalRect(pos.x, pos.y, 240, 0, dimCase, dimCase);
	}

}
