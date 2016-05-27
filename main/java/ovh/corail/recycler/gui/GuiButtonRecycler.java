package ovh.corail.recycler.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.handler.ConfigurationHandler;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class GuiButtonRecycler extends GuiButton {
	private TileEntityRecycler invent;

	public GuiButtonRecycler(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, TileEntityRecycler invent) {
		super(buttonId, x, y, buttonText);
		this.invent = invent;
		this.width = widthIn;
		this.height = heightIn;
	}

	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			int buttonHeight = 14;
			int buttonWidth = 74;
			FontRenderer fontrenderer = mc.fontRendererObj;
			mc.getTextureManager().bindTexture(ConfigurationHandler.fancyGui ? Main.textureFancyRecycler : Main.textureVanillaRecycler);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width	&& mouseY < this.yPosition + this.height;
			int isHovered = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,	GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			
			boolean valid = false;
			/** button take all */
			if (id == 2) {
				for (int i=invent.firstOutput;i<invent.getSizeInventory();i++) {
					if (invent.getStackInSlot(i) != null) {
						valid = true;
						break;
					}
				}
			/** input slot empty */
			} else if (invent.getStackInSlot(0) != null && invent.getStackInSlot(1) != null) {
				valid = false;
				/** input stacksize */
				if ((id == 0 || id == 1) && invent.getStackInSlot(0) != null) {
					int numRecipe = invent.recyclingManager.hasRecipe(invent.getStackInSlot(0));
					if (numRecipe >= 0 && invent.getStackInSlot(0).stackSize >= invent.recyclingManager.getRecipe(numRecipe).getItemRecipe().stackSize) {
						if (id == 1) {
							valid= true;
						/** button recycle */
						} else if (!invent.isWorking()) {
							valid = true;
						}				
					}
				}
			}
			this.enabled=valid;

			/** texture height to read the button */
			int readButton = (!valid?28:(isHovered==2?14:0));
			this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 183 + readButton, this.width / 2, this.height);
			this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, buttonWidth - this.width / 2, 183 + readButton, this.width / 2, this.height);
			this.mouseDragged(mc, mouseX, mouseY);
			int j = 14737632;

			if (packedFGColour != 0) {
				j = packedFGColour;
			} else if (!this.enabled) {
				j = 10526880;
			} else if (this.hovered) {
				j = 16777120;
			}
			this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
		}
	}
}
