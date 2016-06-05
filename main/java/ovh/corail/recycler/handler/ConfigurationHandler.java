package ovh.corail.recycler.handler;

import net.minecraftforge.common.config.Configuration;
import ovh.corail.recycler.core.Helper;

public class ConfigurationHandler {
	public static Configuration config;

	public static boolean unbalancedRecipes, onlyUserRecipes, fancyGui;

	public static void refreshConfig() {
		unbalancedRecipes=config.getBoolean("unbalancedRecipes", config.CATEGORY_GENERAL, false, Helper.getTranslation("config.unbalancedRecipes"));
		onlyUserRecipes=config.getBoolean("onlyUserRecipes", config.CATEGORY_GENERAL, false, Helper.getTranslation("config.onlyUserRecipes"));
		fancyGui=config.getBoolean("fancyGui", config.CATEGORY_GENERAL, false, Helper.getTranslation("config.fancyGui"));
		if (config.hasChanged()) {
			config.save();
		}
	}
}
