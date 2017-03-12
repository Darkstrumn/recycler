package ovh.corail.recycler.handler;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.Main;

public class ConfigurationHandler {
	private static Configuration config;
	private static File configDir;

	public static boolean unbalancedRecipes, onlyUserRecipes, fancyGui, allowCommand, enchantedBooks;
	
	private ConfigurationHandler() {
	}
	
	public static void loadConfig(File configDir) {
		ConfigurationHandler.configDir= configDir;
		if (!configDir.exists()) {
			configDir.mkdir();
		}
		config = new Configuration(new File(configDir, Main.MOD_ID + ".cfg"), Main.MOD_VER);
		config.load();
		ConfigurationHandler.refreshConfig();
	}
	
	private static void refreshConfig() {
		unbalancedRecipes=config.getBoolean("unbalancedRecipes", config.CATEGORY_GENERAL, false, Helper.getTranslation("config.unbalancedRecipes"));
		onlyUserRecipes=config.getBoolean("onlyUserRecipes", config.CATEGORY_GENERAL, false, Helper.getTranslation("config.onlyUserRecipes"));
		fancyGui=config.getBoolean("fancyGui", config.CATEGORY_GENERAL, false, Helper.getTranslation("config.fancyGui"));
		allowCommand=config.getBoolean("allowCommand", config.CATEGORY_GENERAL, true, Helper.getTranslation("config.allowCommand"));
		enchantedBooks=config.getBoolean("enchantedBooks", config.CATEGORY_GENERAL, true, Helper.getTranslation("config.enchantedBooks"));
		if (config.hasChanged()) {
			config.save();
		}
	}
	
	public static File getConfigDir() {
		return configDir;
	}
	
}
