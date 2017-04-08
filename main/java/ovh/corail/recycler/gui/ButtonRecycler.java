package ovh.corail.recycler.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import ovh.corail.recycler.core.ModProps;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class ButtonRecycler extends GuiButton {
	private TileEntityRecycler invent;
	private int textureX=182;
	private int textureY=0;
	private int buttonHeight = 14;
	private int buttonWidth = 74;
	public static ResourceLocation textureVanillaRecycler = new ResourceLocation(ModProps.MOD_ID + ":textures/gui/vanilla_recycler.png");

	public ButtonRecycler(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, TileEntityRecycler invent) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.invent = invent;
	}

	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			FontRenderer fontrenderer = mc.fontRenderer;
			mc.getTextureManager().bindTexture(textureVanillaRecycler);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width	&& mouseY < this.yPosition + this.height;
			int isHovered = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,	GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			/** texture height to read the button */
			int readButton = (!enabled?28:(isHovered==2?14:0));
			int halfWidth = width / 2;
			drawTexturedModalRect(xPosition, yPosition, textureX, textureY + readButton, halfWidth, height);
			drawTexturedModalRect(xPosition + halfWidth, yPosition, textureX + buttonWidth - halfWidth, textureY + readButton, halfWidth, height);
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
