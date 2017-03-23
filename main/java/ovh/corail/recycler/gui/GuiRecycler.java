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
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.core.RecyclingRecipe;
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
	
	public void refreshVisual() {
		List<ItemStack> itemsList = RecyclingManager.getInstance().getResultStack(inventory.getStackInSlot(0), 1);
		visual.emptyVisual();
		/** no recipe */
		if (itemsList.isEmpty() && !inventory.getStackInSlot(0).isEmpty()) {
			itemsList.add(inventory.getStackInSlot(0));
		}
		visual.fillVisual(itemsList);
	}
	
	private void createVisual() {
		int startX = guiLeft + 118;
		int startY = guiTop + 2 + (ConfigurationHandler.fancyGui ? 0 : 1);
		int dimCase = visual.getDimCase();
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
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
		/** draw slots */
		int dimCase = 16;
		List<Slot> slots = this.inventorySlots.inventorySlots;
		Slot slot;
		for (int i = 0; i < slots.size(); i++) {
			slot = slots.get(i);
			this.drawTexturedModalRect(guiLeft + slot.xPos, guiTop + slot.yPos, 240, 0, dimCase, dimCase);
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
				/** progress bar */
				if (inventory.isWorking() && enoughStackSize) {
					mc.renderEngine.bindTexture(ConfigurationHandler.fancyGui ? Main.textureFancyRecycler : Main.textureVanillaRecycler);
					drawTexturedModalRect(78, 41, 0, 225, 19, 4);
					int widthWorking=(int) Math.floor((double) inventory.getPercentWorking()*17.0/100);
					drawTexturedModalRect(79, 42, 1, 229, widthWorking, 2);
					//this.fontRendererObj.drawString(Integer.toString(inventory.getPercentWorking())+" %", (74), (11), 0xffffff);
				}
				/** max recipe for input stacksize */
				int nb_input = (int) Math.floor((double) inventory.getStackInSlot(0).getCount() / (double) inputCount);
				this.fontRenderer.drawString("X " + Integer.toString(nb_input), (inventorySlots.getSlot(0).xPos + 40), (inventorySlots.getSlot(0).yPos + 4), (enoughStackSize?0x00ff00:0xff0000));
			}
		}
		ItemStack disk = inventory.getStackInSlot(1);
		int diskMaxUse;
		if (disk.isEmpty()) {
			diskMaxUse = 0;
		} else {
			diskMaxUse = (int) Math.floor((disk.getMaxDamage()-disk.getItemDamage())/10.0D);
		}
		this.fontRenderer.drawString("X "+Integer.toString(diskMaxUse), (inventorySlots.getSlot(1).xPos + 40), (inventorySlots.getSlot(1).yPos + 4), (diskMaxUse>0?0x00ff00:0xff0000));
	}

	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	public void initGui() {
		super.initGui();
		guiLeft = (this.width - xSize) / 2;
		guiTop = (this.height - ySize) / 2;
		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		buttonList.add(new ButtonRecycler(0, guiLeft + 8, guiTop + 90, 53, 14, Helper.getTranslation("button.recycle"), inventory));
		buttonList.add(new ButtonRecycler(1, guiLeft + 62, guiTop + 90, 53, 14, Helper.getTranslation("button.auto"), inventory));
		buttonList.add(new ButtonRecycler(2, guiLeft + 116, guiTop + 90, 53, 14, Helper.getTranslation("button.takeAll"), inventory));
		createVisual();
		updateButtons();
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
			updateButtons();
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

	public void updateButtons() {
		/** button take all */
		/** at least one stack to transfer */
		boolean valid = false;
		for (int i=inventory.firstOutput;i<inventory.getSizeInventory();i++) {
			if (!inventory.getStackInSlot(i).isEmpty()) {
				valid = true;
				break;
			}
		}
		buttonList.get(2).enabled = valid;
		/** button recycle and auto */
		valid = false;
		if (inventory.isWorking()) {
			buttonList.get(0).enabled = false;
			buttonList.get(1).enabled = true;
		} else {
			/** input slot not empty */
			if (inventory.canRecycle((EntityPlayer) null)) {
				int numRecipe = inventory.recyclingManager.hasRecipe(inventory.getStackInSlot(0));
				/** existing recipe and enough input stacksize */
				if (numRecipe >= 0) {
					RecyclingRecipe currentRecipe = inventory.recyclingManager.getRecipe(numRecipe);
					int nb_input = (int) Math.floor((double) inventory.getStackInSlot(0).getCount() / (double) currentRecipe.getItemRecipe().getCount());
					if (nb_input > 0) {
						if (inventory.isWorking()) { nb_input = 1; }	
						int maxDiskUse = (int) Math.floor((double) (inventory.getStackInSlot(1).getMaxDamage() - inventory.getStackInSlot(1).getItemDamage()) / 10.0D);
						if (maxDiskUse < nb_input) {
							nb_input = maxDiskUse;
						}
						/** calculation of the result */
						List<ItemStack> itemsList = inventory.recyclingManager.getResultStack(inventory.getStackInSlot(0), nb_input);
						if (inventory.hasSpaceInInventory(itemsList, true)) {
							valid = true;
						}
					}
				}
			}
			buttonList.get(0).enabled = valid;
			buttonList.get(1).enabled = valid;
		}
	}

}
