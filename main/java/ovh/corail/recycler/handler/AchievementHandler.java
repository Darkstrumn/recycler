package ovh.corail.recycler.handler;
/*
import java.util.HashMap;
import java.util.Map;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.core.ModProps;

// TODO Advancement
public class AchievementHandler {
	private static Map<String, Achievement> achievements = new HashMap<String, Achievement>();
	private static int achievementCount = 0;
	
	public static void initAchievements() {
		addAchievement("placeRecycler", 0, 0, Main.itemAchievement001, null);
		addAchievement("buildDisk", 1, 1, Main.diamond_disk, "placeRecycler");
		addAchievement("firstRecycle", 2, 2, Items.field_191525_da, "buildDisk");
		addAchievement("readRecyclingBook", -1, -1, Items.BOOK, "placeRecycler");

	}
	
	private static void addAchievement(String name, int col, int row, Item icon, String parent) {
		String upperName = name.substring(0,1).toUpperCase()+name.substring(1);
		String lowerName = name.substring(0,1).toLowerCase()+name.substring(1);
		achievements.put(lowerName, new Achievement("achievement."+upperName, upperName, row, col, icon, achievements.get(parent)).registerStat());
		achievementCount++;
	}
	
	public static Achievement getAchievement(String name) {
		return achievements.get(name);
	}
	
	public static void registerAchievements() {
		AchievementPage.registerAchievementPage(new AchievementPage(ModProps.MOD_ID, achievements.values().toArray(new Achievement[achievements.values().size()])));
	}
}*/
