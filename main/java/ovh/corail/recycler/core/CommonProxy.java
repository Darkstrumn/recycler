package ovh.corail.recycler.core;

import java.io.File;

import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import ovh.corail.recycler.handler.ConfigurationHandler;
import ovh.corail.recycler.handler.GuiHandler;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class CommonProxy {
	public void preInit(FMLPreInitializationEvent event) {
		/** config */
		ConfigurationHandler.loadConfig(new File(event.getModConfigurationDirectory(), Main.MOD_ID));
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
		Main.achievementPlaceRecycler.registerStat();
		Main.achievementBuildDisk.registerStat();
		Main.achievementFirstRecycle.registerStat();
		Main.achievementReadRecyclingBook.registerStat();
		AchievementPage.registerAchievementPage(new AchievementPage(Main.MOD_NAME, new Achievement[] { Main.achievementPlaceRecycler, Main.achievementBuildDisk, Main.achievementFirstRecycle }));
		/** gui handler */
		NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance, new GuiHandler());
	}

	public void postInit(FMLPostInitializationEvent event) {
	}
}
