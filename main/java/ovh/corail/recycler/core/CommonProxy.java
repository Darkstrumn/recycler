package ovh.corail.recycler.core;

import java.io.File;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import ovh.corail.recycler.handler.AchievementHandler;
import ovh.corail.recycler.handler.ConfigurationHandler;
import ovh.corail.recycler.handler.GuiHandler;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class CommonProxy {
	public void preInit(FMLPreInitializationEvent event) {
		/** config */
		ConfigurationHandler.loadConfig(new File(event.getModConfigurationDirectory(), ModProps.MOD_ID));
		/** register items and blocks */
		Helper.register();
		/** register tileentities */
		GameRegistry.registerTileEntity(TileEntityRecycler.class, "inventory");
		/** new crafting recipes */
		Helper.getNewRecipes();
		/** packet handler */
		PacketHandler.init();
		/** load recycling recipes */
		RecyclingManager.getInstance().loadRecipes();
	}

	public void init(FMLInitializationEvent event) {
		/** achievements */
		AchievementHandler.registerAchievements();
		/** gui handler */
		NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance, new GuiHandler());
	}

	public void postInit(FMLPostInitializationEvent event) {
	}

	public void updateRecyclingScreen() {	
	}

	public Side getSide() {
		return Side.SERVER;
	}
}
