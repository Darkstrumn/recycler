package ovh.corail.recycler.gui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import ovh.corail.recycler.container.ContainerRecycler;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.packet.ButtonMessage;
import ovh.corail.recycler.packet.ProgressMessage;
import ovh.corail.recycler.packet.SwitchWorkingMessage;
import ovh.corail.recycler.packet.TakeAllMessage;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class GuiRecycler extends GuiContainer {
	public int i = 0;
	public int j = 0;
	public int k = 0;
	public TileEntityRecycler inventory;
	public EntityPlayer currentPlayer;
	private static ResourceLocation textureBg = new ResourceLocation(Main.MOD_ID + ":textures/gui/recycler.png");
	private static ResourceLocation textureProgressColor = new ResourceLocation(Main.MOD_ID + ":textures/gui/progress_color.png");
	private static ResourceLocation textureProgressBg = new ResourceLocation(Main.MOD_ID + ":textures/gui/progress_bg.png");
	
	public GuiRecycler(EntityPlayer player, World world, int x, int y, int z, TileEntityRecycler inventory) {
		super(new ContainerRecycler(player, world, x, y, z, inventory));
		this.inventory = inventory;
		this.currentPlayer = player;
		this.i = x;
		this.j = y;
		this.k = z;
		this.xSize = 176;
		this.ySize = 176;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glScalef(1F, 1F, 1F);
		mc.renderEngine.bindTexture(textureBg);
		int posX = ((this.width - this.xSize) / 2);
		int posY = ((this.height - this.ySize) / 2);
		this.drawTexturedModalRect(posX, posY, 0, 0, this.xSize, this.ySize);
		int i, j;
		int dimCase = 16;
		List<Slot> slots = this.inventorySlots.inventorySlots;
		Slot slot;
		for (i = 0; i < slots.size(); i++) {
			slot = slots.get(i);
			this.drawTexturedModalRect(posX + slot.xDisplayPosition, posY + slot.yDisplayPosition, 661, 150, dimCase, dimCase);
		}
		zLevel = 100.0F;
	}

	protected void keyTyped(char par1, int par2) {

		if (par2 != 28 && par2 != 156) {
			if (par2 == 1) {
				this.mc.thePlayer.closeScreen();
			}
		}

	}

	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if (inventory.getStackInSlot(0)!=null) {
			RecyclingManager rm = RecyclingManager.getInstance();
			int num_recipe=rm.hasRecipe(inventory.getStackInSlot(0));
			if (num_recipe>=0) {
				int inputCount=rm.getRecipe(num_recipe).getItemRecipe().stackSize;
				boolean enoughStackSize = inventory.getStackInSlot(0).stackSize >= inputCount;
				this.fontRendererObj.drawString(Integer.toString(inputCount), (50), (20), (enoughStackSize?0x00ff00:0xff0000));
			
				// TODO Current Changes
				if (inventory.isWorking() && enoughStackSize) {
					mc.renderEngine.bindTexture(textureProgressBg);
					drawTexturedModalRect(62, 32, 0, 0, 34, 10);
					int widthWorking=(int) Math.floor((double) inventory.getPercentWorking()*32.0/100);
					mc.renderEngine.bindTexture(textureProgressColor);
					drawTexturedModalRect(63, 33, 0, 0, widthWorking, 8);
					this.fontRendererObj.drawString(Integer.toString(inventory.getPercentWorking())+" %", (69), (34), 0xffffff);
				}
			}
		}
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
		this.buttonList.add(new GuiButton(0, this.guiLeft + 7, this.guiTop + 77, 80, 20, Helper.getTranslation("button.recycle")));
		this.buttonList.add(new GuiButton(1, this.guiLeft + 7, this.guiTop + 32, 36, 10, Helper.getTranslation("button.auto")));
		this.buttonList.add(new GuiButton(2, this.guiLeft + 89, this.guiTop + 77, 80, 20, Helper.getTranslation("button.takeAll")));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		switch (button.id) {
		case 0: // Recycle
			PacketHandler.INSTANCE.sendToServer(new ButtonMessage(button.id, inventory.getPos()));
			if (inventory.recycle(currentPlayer)) {
				currentPlayer.addStat(Main.achievementFirstRecycle, 1);
			}
			break;
		case 1: // Switch Working
			PacketHandler.INSTANCE.sendToServer(new SwitchWorkingMessage(inventory.getPos()));
			break;
		case 2: // Take All
			PacketHandler.INSTANCE.sendToServer(new TakeAllMessage(inventory.getPos()));
			break;
		default:
		}
	}

	public boolean doesGuiPauseGame() {
		return false;
	}

}
