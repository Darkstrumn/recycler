package ovh.corail.recycler.core;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import ovh.corail.recycler.gui.GuiRecycler;

public class ClientProxy extends CommonProxy {
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		/** render items and blocks */
		Helper.render();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}
	
	@Override
	public Side getSide() {
		return Side.CLIENT;
	}
	
	@Override
	public void updateRecyclingScreen() {
		/** TODO could refresh buttons depending on test */
		((GuiRecycler)Minecraft.getMinecraft().currentScreen).refreshVisual();
		((GuiRecycler)Minecraft.getMinecraft().currentScreen).updateButtons();
	}
}
