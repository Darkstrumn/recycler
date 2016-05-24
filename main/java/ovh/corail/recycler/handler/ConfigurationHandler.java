package ovh.corail.recycler.handler;

import net.minecraftforge.common.config.Configuration;
import ovh.corail.recycler.core.Helper;

public class ConfigurationHandler {
	public static Configuration config;

	public static boolean unbalancedRecipes, fancyGui;

	public static void refreshConfig() {
		unbalancedRecipes=config.getBoolean("unbalancedRecipes", config.CATEGORY_GENERAL, false, Helper.getTranslation("config.unbalancedRecipes"));
		fancyGui=config.getBoolean("fancyGui", config.CATEGORY_GENERAL, false, Helper.getTranslation("config.fancyGui"));
		if (config.hasChanged()) {
			config.save();
		}
	}
}
