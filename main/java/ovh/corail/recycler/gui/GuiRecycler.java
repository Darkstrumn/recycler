package ovh.corail.recycler.gui;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import ovh.corail.recycler.ModProps;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.core.RecyclingRecipe;
import ovh.corail.recycler.core.VisualManager;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.packet.RecycleMessage;
import ovh.corail.recycler.packet.ServerWorkingMessage;
import ovh.corail.recycler.packet.TakeAllMessage;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class GuiRecycler extends GuiContainer {
	private TileEntityRecycler inventory;
	private EntityPlayer currentPlayer;
	private VisualManager visual = new VisualManager();
	private float oldMouseX;
	private float oldMouseY;
	private int inputMax;
	public static ResourceLocation textureVanillaRecycler = new ResourceLocation(ModProps.MOD_ID + ":textures/gui/vanilla_recycler.png");
	
	public GuiRecycler(EntityPlayer player, World world, int x, int y, int z, TileEntityRecycler inventory) {
		super(new ContainerRecycler(player, world, x, y, z, inventory));
		this.inventory = inventory;
		this.currentPlayer = player;
		this.xSize = 177;
		this.ySize = 203;
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
		int startX = guiLeft + 117;
		int startY = guiTop + 10;
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
		//GL11.glScalef(1F, 1F, 1F);
		/** recycler texture */
		mc.renderEngine.bindTexture(textureVanillaRecycler);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
		/** draw slots */
		int dimCase = 18;
		List<Slot> slots = this.inventorySlots.inventorySlots;
		Slot slot;
		for (int i = 0; i < slots.size(); i++) {
			slot = slots.get(i);
			drawTexturedModalRect(guiLeft + slot.xPos-1, guiTop + slot.yPos-1, 238, 74, dimCase, dimCase);
		}
		/** draw visual grid */
		Point pos;
		for (int i = 0 ; i < visual.getVisualCount() ; i++)  {
			pos = visual.getPosInVisual(i);
			drawTexturedModalRect(pos.x-1, pos.y-1, 238, 74, dimCase, dimCase);	
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
		/** draw the player on gui */
		oldMouseX = (float)mouseX;
        oldMouseY = (float)mouseY;
		int entityX = 27;
		int entityY = 54;
		GuiInventory.drawEntityOnScreen(guiLeft + entityX, guiTop + entityY, 20, (float)(guiLeft + entityX) - oldMouseX, (float)(guiTop + entityY - entityX + 1) - oldMouseY, mc.player);
		zLevel = 100.0F;
		
	}

	protected void keyTyped(char par1, int par2) throws IOException {
		super.keyTyped(par1, par2);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		mc.renderEngine.bindTexture(textureVanillaRecycler);
		/** arrow in background */
		drawTexturedModalRect(85, 28, 207, 46, 22, 15);
		/** progress bar */
		if (inventory.isWorking() && inputMax > 0) {
			int widthWorking=(int) Math.floor((double) inventory.getPercentWorking()*22.0/100);
			drawTexturedModalRect(85, 28, 207, 61, widthWorking, 15);
		}
		fontRenderer.drawString("" + Integer.toString(inputMax), (inventorySlots.getSlot(0).xPos + 30), (inventorySlots.getSlot(0).yPos + 9), (inputMax > 0 ? Color.GREEN.getRGB() : Color.RED.getRGB()));
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
		buttonList.add(new ButtonRecycler(0, guiLeft + 8, guiTop + 105, 53, 14, Helper.getTranslation("button.recycle"), inventory));
		buttonList.add(new ButtonRecycler(1, guiLeft + 62, guiTop + 105, 53, 14, Helper.getTranslation("button.auto"), inventory));
		buttonList.add(new ButtonRecycler(2, guiLeft + 116, guiTop + 105, 53, 14, Helper.getTranslation("button.takeAll"), inventory));
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
			inventory.recycle(currentPlayer);
			break;
		case 1: /** Switch Working */
			inventory.updateWorking(!this.inventory.isWorking());
			updateButtons();
			PacketHandler.INSTANCE.sendToServer(new ServerWorkingMessage(inventory.getPos(), inventory.isWorking()));
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
		int old_nb_input=0;
		/** input slot not empty */
		if (inventory.canRecycle((EntityPlayer) null)) {
			int numRecipe = inventory.recyclingManager.hasRecipe(inventory.getStackInSlot(0));
			/** existing recipe and enough input stacksize */
			if (numRecipe >= 0) {
				RecyclingRecipe currentRecipe = inventory.recyclingManager.getRecipe(numRecipe);
				int nb_input = (int) Math.floor((double) inventory.getStackInSlot(0).getCount() / (double) currentRecipe.getItemRecipe().getCount());
				if (nb_input > 0) {	
					int maxDiskUse = (int) Math.floor((double) (inventory.getStackInSlot(1).getMaxDamage() - inventory.getStackInSlot(1).getItemDamage()) / 10.0D);
					if (maxDiskUse < nb_input) {
						nb_input = maxDiskUse;
					}
					old_nb_input = nb_input;
					if (inventory.isWorking()) { nb_input = 1; }
					/** calculation of the result */
					List<ItemStack> itemsList = inventory.recyclingManager.getResultStack(inventory.getStackInSlot(0), nb_input);
					if (inventory.hasSpaceInInventory(itemsList, true)) {
						valid = true;
					}
				}
			}
		}
		inputMax = old_nb_input;
		buttonList.get(0).enabled = inventory.isWorking()? false : valid;
		buttonList.get(1).enabled = inventory.isWorking() ? true : valid;
	}

}
