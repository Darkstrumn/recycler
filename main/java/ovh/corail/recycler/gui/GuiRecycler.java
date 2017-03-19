package ovh.corail.recycler.gui;

import java.awt.Point;
import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ovh.corail.recycler.container.ContainerRecycler;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.core.VisualManager;
import ovh.corail.recycler.handler.ConfigurationHandler;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.packet.RecycleMessage;
import ovh.corail.recycler.packet.TakeAllMessage;
import ovh.corail.recycler.packet.WorkingMessage;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class GuiRecycler extends GuiContainer {
	private TileEntityRecycler inventory;
	private EntityPlayer currentPlayer;
	private VisualManager visual = new VisualManager();
	
	public GuiRecycler(EntityPlayer player, World world, int x, int y, int z, TileEntityRecycler inventory) {
		super(new ContainerRecycler(player, world, x, y, z, inventory));
		this.inventory = inventory;
		this.currentPlayer = player;
		this.xSize = 176;
		this.ySize = ConfigurationHandler.fancyGui? 176 : 179;
	}
	
	public void refreshVisual(ItemStack stack) {
		List<ItemStack> itemsList = RecyclingManager.getInstance().getResultStack(stack, 1);
		visual.emptyVisual();
		/** no recipe */
		if (itemsList.isEmpty() && !inventory.getStackInSlot(0).isEmpty()) {
			itemsList.add(inventory.getStackInSlot(0));
		}
		visual.fillVisual(itemsList);
	}
	
	private void createVisual() {
		int posX = ((this.width - this.xSize) / 2);
		int posY = ((this.height - this.ySize) / 2);
		int startX = posX + 118;
		int startY = posY + 3;
		int dimCase = 16;
		int slotNum = 0;
		for (int caseY = 0 ; caseY < 3 ; caseY++) {
			for (int caseX = 0 ; caseX < 3 ; caseX++) {
				visual.addVisual(slotNum, startX + (caseX*dimCase), startY + (caseY*dimCase));
				slotNum++;
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glScalef(1F, 1F, 1F);
		/** recycler texture */
		mc.renderEngine.bindTexture(ConfigurationHandler.fancyGui ? Main.textureFancyRecycler : Main.textureVanillaRecycler);
		int posX = ((this.width - this.xSize) / 2);
		int posY = ((this.height - this.ySize) / 2);
		this.drawTexturedModalRect(posX, posY, 0, 0, this.xSize, this.ySize);
		/** draw slots */
		int dimCase = 16;
		List<Slot> slots = this.inventorySlots.inventorySlots;
		Slot slot;
		for (int i = 0; i < slots.size(); i++) {
			slot = slots.get(i);
			this.drawTexturedModalRect(posX + slot.xPos, posY + slot.yPos, 240, 0, dimCase, dimCase);
		}
		/** draw visual grid */
		Point pos;
		for (int i = 0 ; i < visual.getVisualCount() ; i++)  {
			pos = visual.getPosInVisual(i);
			drawTexturedModalRect(pos.x, pos.y, 240, 0, dimCase, dimCase);	
		}
		/** draw visual item and tootip */
		RenderHelper.enableGUIStandardItemLighting();
		ItemStack stack = inventory.getStackInSlot(0);
		if (stack.isEmpty()) {
			visual.emptyVisual();
		} else {
			refreshVisual(stack);
		}
		int slotHover = visual.getSlotAtPos(mouseX, mouseY);
		for (int i = 0 ; i < visual.getVisualCount() ; i++)  {
			stack = visual.getStackInVisual(i);
			pos = visual.getPosInVisual(i);
			itemRender.renderItemAndEffectIntoGUI(stack, pos.x, pos.y);
			itemRender.renderItemOverlays(fontRenderer, stack, pos.x, pos.y);
			if (!stack.isEmpty() && slotHover == i) {
				pos = visual.getPosInVisual(i);
				this.renderToolTip(stack, pos.x, pos.y);
			}
			
		}
		RenderHelper.disableStandardItemLighting();
		zLevel = 100.0F;
		
	}

	protected void keyTyped(char par1, int par2) throws IOException {
		super.keyTyped(par1, par2);
	}

	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if (!inventory.getStackInSlot(0).isEmpty()) {
			RecyclingManager rm = RecyclingManager.getInstance();
			int num_recipe=rm.hasRecipe(inventory.getStackInSlot(0));
			if (num_recipe>=0) {
				int inputCount=rm.getRecipe(num_recipe).getItemRecipe().getCount();
				boolean enoughStackSize = inventory.getStackInSlot(0).getCount() >= inputCount;
				// TODO Current Changes
				if (inventory.isWorking() && enoughStackSize) {
					mc.renderEngine.bindTexture(ConfigurationHandler.fancyGui ? Main.textureFancyRecycler : Main.textureVanillaRecycler);
					drawTexturedModalRect(78, 41, 0, 225, 19, 4);
					int widthWorking=(int) Math.floor((double) inventory.getPercentWorking()*17.0/100);
					drawTexturedModalRect(79, 42, 1, 229, widthWorking, 2);
					//this.fontRendererObj.drawString(Integer.toString(inventory.getPercentWorking())+" %", (74), (11), 0xffffff);
				}
				this.fontRenderer.drawString("X " + Integer.toString(inventory.getStackInSlot(0).getCount()/inputCount), (70), (13), (enoughStackSize?0x00ff00:0xff0000));
			}
		}
		ItemStack disk = inventory.getStackInSlot(1);
		int diskMaxUse;
		if (disk.isEmpty()) {
			diskMaxUse = 0;
		} else {
			diskMaxUse = (disk.getMaxDamage()-disk.getItemDamage())/10;
		}
		this.fontRenderer.drawString("X "+Integer.toString(diskMaxUse), (70), (31), (diskMaxUse>0?0x00ff00:0xff0000));

	}

	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - xSize) / 2;
		this.guiTop = (this.height - ySize) / 2;
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(new GuiButtonRecycler(0, this.guiLeft + 8, this.guiTop + 90, 53, 14, Helper.getTranslation("button.recycle"), inventory));
		this.buttonList.add(new GuiButtonRecycler(1, this.guiLeft + 62, this.guiTop + 90, 53, 14, Helper.getTranslation("button.auto"), inventory));
		this.buttonList.add(new GuiButtonRecycler(2, this.guiLeft + 116, this.guiTop + 90, 53, 14, Helper.getTranslation("button.takeAll"), inventory));
		createVisual();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		switch (button.id) {
		case 0: /** Recycle */
			PacketHandler.INSTANCE.sendToServer(new RecycleMessage(button.id, inventory.getPos()));
			if (inventory.recycle(currentPlayer)) {
				currentPlayer.addStat(Main.achievementFirstRecycle, 1);
			}
			break;
		case 1: /** Switch Working */
			inventory.setWorking(!this.inventory.isWorking());
			PacketHandler.INSTANCE.sendToServer(new WorkingMessage(inventory.getPos(), inventory.isWorking()));
			break;
		case 2: /** Take All */
			PacketHandler.INSTANCE.sendToServer(new TakeAllMessage(inventory.getPos()));
			break;
		default:
		}
	}

	public boolean doesGuiPauseGame() {
		return false;
	}

}
