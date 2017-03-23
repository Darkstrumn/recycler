package ovh.corail.recycler.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import ovh.corail.recycler.block.BlockRecycler;
import ovh.corail.recycler.handler.CommandHandler;
import ovh.corail.recycler.handler.EventHandler;
import ovh.corail.recycler.handler.SoundHandler;
import ovh.corail.recycler.item.ItemAchievement001;
import ovh.corail.recycler.item.ItemDiamondDisk;
import ovh.corail.recycler.item.ItemDiamondFragment;
import ovh.corail.recycler.item.ItemRecyclingBook;

@Mod(modid = Main.MOD_ID, name = Main.MOD_NAME, version = Main.MOD_VER, guiFactory = "ovh.corail.recycler.gui.GuiFactoryRecycler")
public class Main {
	public static final String MOD_ID = "recycler";
	public static final String MOD_NAME = "Corail Recycler";
	public static final String MOD_VER = "1.3.4";
	@Instance(Main.MOD_ID)
	public static Main instance;
	@SidedProxy(clientSide = "ovh.corail.recycler.core.ClientProxy", serverSide = "ovh.corail.recycler.core.CommonProxy")
	public static CommonProxy proxy;
	
	public static CreativeTabs tabRecycler = new CreativeTabs(Main.MOD_ID) {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(Item.getItemFromBlock(Main.recycler),1);
		}

		@Override
		public String getTranslatedTabLabel() {
			return Main.MOD_NAME;
		}
	};
	public static ItemDiamondFragment diamond_fragment = new ItemDiamondFragment();
	public static ItemDiamondDisk diamond_disk = new ItemDiamondDisk();
	public static BlockRecycler recycler = new BlockRecycler();
	public static ItemRecyclingBook recycling_book = new ItemRecyclingBook();
	
	public static ItemAchievement001 itemAchievement001 = new ItemAchievement001();
	public static Achievement achievementPlaceRecycler = new Achievement("achievement.PlaceRecycler", "PlaceRecycler", 0, 0, Main.itemAchievement001, (Achievement) null);
	public static Achievement achievementBuildDisk = new Achievement("achievement.BuildDisk", "BuildDisk", 1, 1, Main.diamond_disk, achievementPlaceRecycler);
	public static Achievement achievementFirstRecycle = new Achievement("achievement.FirstRecycle", "FirstRecycle", 2, 2, Items.field_191525_da, achievementBuildDisk);
	public static Achievement achievementReadRecyclingBook = new Achievement("achievement.ReadRecyclingBook", "ReadRecyclingBook", -1, -1, Items.BOOK, achievementPlaceRecycler);
	
	/** TODO could move texture elsewhere */
	public static ResourceLocation textureVanillaRecycler = new ResourceLocation(Main.MOD_ID + ":textures/gui/vanilla_recycler.png");
	public static ResourceLocation textureFancyRecycler = new ResourceLocation(Main.MOD_ID + ":textures/gui/fancy_recycler.png");
	public static ResourceLocation textureRecyclingBook = new ResourceLocation(Main.MOD_ID + ":textures/gui/book.png");
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		SoundHandler.registerSounds();
		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}
	
	@Mod.EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandHandler());

	}
}
