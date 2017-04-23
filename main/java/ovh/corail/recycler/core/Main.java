package ovh.corail.recycler.core;

import static ovh.corail.recycler.core.ModProps.MOD_ID;
import static ovh.corail.recycler.core.ModProps.MOD_NAME;
import static ovh.corail.recycler.core.ModProps.MOD_VER;
import static ovh.corail.recycler.core.ModProps.MOD_UPDATE;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import ovh.corail.recycler.block.BlockRecycler;
import ovh.corail.recycler.handler.AchievementHandler;
import ovh.corail.recycler.handler.CommandHandler;
import ovh.corail.recycler.handler.EventHandler;
import ovh.corail.recycler.handler.SoundHandler;
import ovh.corail.recycler.item.ItemAchievement001;
import ovh.corail.recycler.item.ItemDiamondDisk;
import ovh.corail.recycler.item.ItemDiamondFragment;
import ovh.corail.recycler.item.ItemRecyclingBook;

@Mod(modid = MOD_ID, name = MOD_NAME, version = MOD_VER, updateJSON = MOD_UPDATE, guiFactory = "ovh.corail." + MOD_ID + ".gui.GuiFactory")
public class Main {
	@Instance(MOD_ID)
	public static Main instance;
	@SidedProxy(clientSide = "ovh.corail." + MOD_ID + ".core.ClientProxy", serverSide = "ovh.corail." + MOD_ID + ".core.CommonProxy")
	public static CommonProxy proxy;
	
	public static CreativeTabs tabRecycler = new CreativeTabs(MOD_ID) {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(Item.getItemFromBlock(Main.recycler),1);
		}

		@Override
		public String getTranslatedTabLabel() {
			return MOD_NAME;
		}
	};
	public static ItemDiamondFragment diamond_fragment = new ItemDiamondFragment();
	public static ItemDiamondDisk diamond_disk = new ItemDiamondDisk();
	public static BlockRecycler recycler = new BlockRecycler();
	public static ItemRecyclingBook recycling_book = new ItemRecyclingBook();
	
	public static ItemAchievement001 itemAchievement001 = new ItemAchievement001();
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		AchievementHandler.initAchievements();
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
