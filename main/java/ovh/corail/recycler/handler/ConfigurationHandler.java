package ovh.corail.recycler.handler;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.Main;

public class ConfigurationHandler {
	public static Configuration config;
	private static File configDir;

	public static boolean unbalancedRecipes, onlyUserRecipes, allowCommand, enchantedBooks, allowSound;
	public static int chanceLoss;
	
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
	
	public static void refreshConfig() {
		unbalancedRecipes=config.getBoolean("unbalancedRecipes", config.CATEGORY_GENERAL, false, Helper.getTranslation("config.unbalancedRecipes"));
		onlyUserRecipes=config.getBoolean("onlyUserRecipes", config.CATEGORY_GENERAL, false, Helper.getTranslation("config.onlyUserRecipes"));
		allowCommand=config.getBoolean("allowCommand", config.CATEGORY_GENERAL, true, Helper.getTranslation("config.allowCommand"));
		enchantedBooks=config.getBoolean("enchantedBooks", config.CATEGORY_GENERAL, true, Helper.getTranslation("config.enchantedBooks"));
		chanceLoss=config.getInt("chanceLoss", config.CATEGORY_GENERAL, 0, 0, 100, Helper.getTranslation("config.chanceLoss"));
		allowSound=config.getBoolean("allowSound", config.CATEGORY_GENERAL, true, Helper.getTranslation("config.allowSound"));
		if (config.hasChanged()) {
			config.save();
		}
	}
	
	public static File getConfigDir() {
		return configDir;
	}
	
}
