package ovh.corail.recycler.container;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import ovh.corail.recycler.core.Main;

public class ButtonRecyclingBook extends GuiButton {
	private final boolean isForward;

	public ButtonRecyclingBook(int buttonId, int x, int y) {
		super(buttonId, x, y, 23, 13, "");
		this.isForward = buttonId == 0 ? false : true;
	}
	
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(Main.textureRecyclingBook);
            int i = 0;
            int j = 192;
            if (flag) {
                i += 23;
            }
            if (!this.isForward) {
                j += 13;
            }
            this.drawTexturedModalRect(this.xPosition, this.yPosition, i, j, 23, 13);
        }
    }
}
