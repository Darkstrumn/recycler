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
import ovh.corail.recycler.container.ButtonRecyclingBook;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.core.PageManager;
import ovh.corail.recycler.core.RecyclingRecipe;
import ovh.corail.recycler.core.VisualManager;

public class GuiRecyclingBook extends GuiScreen {
	private int bookWidth = 290;
	private int bookHeight = 179;
	private int dimCase = 16;
	private PageManager pm;
	private VisualManager visual = new VisualManager();

	private GuiTextField searchBox;
	private ItemStack currentBook;
	
	public GuiRecyclingBook() {
		super();
	}
	
	public void enableButton(int buttonNum, boolean state) {
		if (buttonNum < 0 || buttonNum >= buttonList.size()) { return; }
		buttonList.get(buttonNum).enabled = state;
		buttonList.get(buttonNum).visible = state;
	}
	
	private void enableButtons() {
		int pageNum = pm.getPageNum();
		if (pageNum > 0 && pageNum+1 < pm.getPageCount()) {
			enableButton(0, true);
			enableButton(1, true);
		} else if (pageNum == 0) {
			enableButton(0, false);
			enableButton(1, (pm.getPageCount() > 1 ? true : false));
		} else {
			enableButton(0, true);
			enableButton(1, false);
		}
	}
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (searchBox.textboxKeyTyped(typedChar, keyCode)) {
			pm.setSearch(searchBox.getText());
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
		pm = new PageManager();
		loadButtons(posX, posY);
		createVisual();
	}
	
	private void loadButtons(int posX, int posY) {
		buttonList.clear();
		buttonList.add(new ButtonRecyclingBook(0, posX+20, posY+150));
		buttonList.add(new ButtonRecyclingBook(1, posX+247, posY+150));
		enableButtons();
	}
	
	private void loadSearchBox(int posX, int posY) {
		searchBox = new GuiTextField(2, fontRenderer, (width/2)-32, posY+159, 64, 12);
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
        mc.getTextureManager().bindTexture(Main.textureRecyclingBook);
        int posX = ((this.width - this.bookWidth) / 2);
		int posY = ((this.height - this.bookHeight) / 2);
		int bookSize = 162;
		drawTexturedModalRect(posX, posY, 20, 1, 145, 179);//this.bookHeight);
		drawTexturedModalRect(posX+145, posY, 20, 1, 145, 179);//this.bookHeight);
    }
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float par3) {
		drawDefaultBackground();
		int posX = ((this.width - this.bookWidth) / 2);
		int posY = ((this.height - this.bookHeight) / 2);

		int startX = posX+10;
		int startY = posY+5;
		
		/** draw grid */
		for (int slot = 0 ; slot < visual.getVisualCount() ; slot++) {
			displayGrid(visual.getPosInVisual(slot));
		}
		refreshVisual();
		/** draw items render */
		RenderHelper.enableGUIStandardItemLighting();
		RecyclingRecipe recipe;
		List<RecyclingRecipe> currentPage = pm.getPage(pm.getPageNum());
		ItemStack stack = ItemStack.EMPTY;
		for (int slot = 0 ; slot < visual.getVisualCount() ; slot++) {
			displayItems(visual.getStackInVisual(slot), visual.getPosInVisual(slot));
		}
		RenderHelper.disableStandardItemLighting();
		/** draw buttons */
		super.drawScreen(mouseX, mouseY, par3);
		/** draw tooltip on hover slot */
		int slotHover = visual.getSlotAtPos(mouseX, mouseY);
		if (slotHover > -1) {
			stack = visual.getStackInVisual(slotHover);
			if (!stack.isEmpty()) {
				Point pos = visual.getPosInVisual(slotHover);
				this.renderToolTip(stack, pos.x, pos.y);
			}
		}
		/** draw search box */
		searchBox.drawTextBox();
	}
	
	private void displayItems(ItemStack stack, Point pos) {
		if (stack == null || stack.isEmpty()) { return; }
		itemRender.renderItemAndEffectIntoGUI(stack, pos.x, pos.y);
		itemRender.renderItemOverlays(fontRenderer, stack, pos.x, pos.y);
	}
	
	private void displayGrid(Point pos) {
		mc.getTextureManager().bindTexture(Main.textureVanillaRecycler);
		drawTexturedModalRect(pos.x, pos.y, 240, 0, dimCase, dimCase);
	}
	
	public void refreshVisual() {
		List<RecyclingRecipe> recipes = pm.getPage(pm.getPageNum());
		visual.emptyVisual();
		ItemStack stack;
		int slotNum = 0;
		List<ItemStack> itemsList = Lists.newArrayList();
		for (RecyclingRecipe recipe : recipes) {
			itemsList.add(recipe.getItemRecipe());
			slotNum++;
			for (int i = 0 ; i < 9 ; i++) {
				if (i < recipe.getCount()) {
					itemsList.add(recipe.getStack(i));
				} else {
					itemsList.add(ItemStack.EMPTY);
				}
				slotNum++;
			}
		}
		visual.fillVisual(itemsList);
	}
	
	private void createVisual() {
		int posX = ((this.width - this.bookWidth) / 2);
		int posY = ((this.height - this.bookHeight) / 2);
		int startX, startY;
		int slotNum = 0;
		/** each recipes line */
		for (int j=0 ; j < 3 ; j++) {
			startX = posX+30;
			startY = posY+10+(j*dimCase*3)+(2*j);
			/** 2 recipes on each line */
			for (int i=0 ; i < 2 ; i++) {
				/** recycle item */
				visual.addVisual(slotNum++, startX, (startY+dimCase));
				startX += dimCase+5;
				/** each result item */
				for (int caseY=0 ; caseY < 3 ; caseY++) {
					for (int caseX=0 ; caseX < 3 ; caseX++) {
						visual.addVisual(slotNum++, (startX+(caseX*dimCase)), (startY+(caseY*dimCase)));
					}
				}
				startX = posX+175;
			}
		}
	}

}
