package ovh.corail.recycler.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.config.Configuration;
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
	public void preInit(FMLPreInitializationEvent event) throws IOException {
		/** config */
		ConfigurationHandler.config = new Configuration(event.getSuggestedConfigurationFile(), Main.MOD_VER);
		ConfigurationHandler.config.load();
		ConfigurationHandler.refreshConfig();
		ConfigurationHandler.configDir = event.getModConfigurationDirectory();
		/** register items and blocks */
		Helper.register();
		/** register tileentities */
		GameRegistry.registerTileEntity(TileEntityRecycler.class, "inventory");
		/** new crafting recipes */
		Helper.getNewRecipes();
		/** packet handler */
		PacketHandler.init();
		/** load blacklist recipes */
		RecyclingManager.getInstance().loadBlacklist();
		/** load default recycling recipes */
		RecyclingManager.getInstance().loadJsonRecipes(false);
		/** load json user defined recycling recipes */
		RecyclingManager.getInstance().loadJsonRecipes(true);
	}

	public void init(FMLInitializationEvent event) {
		/** achievements */
		Main.achievementPlaceRecycler.registerStat();
		Main.achievementBuildDisk.registerStat();
		Main.achievementFirstRecycle.registerStat();
		AchievementPage.registerAchievementPage(new AchievementPage(Main.MOD_NAME, new Achievement[] { Main.achievementPlaceRecycler, Main.achievementBuildDisk, Main.achievementFirstRecycle }));
		/** gui handler */
		NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance, new GuiHandler());
	}

	public void postInit(FMLPostInitializationEvent event) {
	}
}
