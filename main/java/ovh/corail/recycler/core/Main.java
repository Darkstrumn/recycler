package ovh.corail.recycler.core;

import static ovh.corail.recycler.core.ModProps.MC_ACCEPT;
import static ovh.corail.recycler.core.ModProps.MOD_ID;
import static ovh.corail.recycler.core.ModProps.MOD_NAME;
import static ovh.corail.recycler.core.ModProps.MOD_UPDATE;
import static ovh.corail.recycler.core.ModProps.MOD_VER;
import static ovh.corail.recycler.core.ModProps.ROOT;

import java.io.File;

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
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import ovh.corail.recycler.block.BlockRecycler;
import ovh.corail.recycler.handler.CommandHandler;
import ovh.corail.recycler.handler.ConfigurationHandler;
import ovh.corail.recycler.handler.EventHandler;
import ovh.corail.recycler.handler.GuiHandler;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.handler.SoundHandler;
import ovh.corail.recycler.item.ItemAdvancement001;
import ovh.corail.recycler.item.ItemDiamondDisk;
import ovh.corail.recycler.item.ItemDiamondFragment;
import ovh.corail.recycler.item.ItemRecyclingBook;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

@Mod(modid = MOD_ID, name = MOD_NAME, version = MOD_VER, acceptedMinecraftVersions = MC_ACCEPT, updateJSON = MOD_UPDATE, guiFactory = ROOT + ".gui.GuiFactory")
public class Main {
	@Instance(MOD_ID)
	public static Main instance;
	@SidedProxy(clientSide = ROOT + ".core.ClientProxy", serverSide = ROOT + ".core.CommonProxy")
	public static CommonProxy proxy;
	
	public static CreativeTabs tabRecycler = new CreativeTabs(MOD_ID) {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(Item.getItemFromBlock(Main.recycler));
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
	
	public static ItemAdvancement001 itemAchievement001 = new ItemAdvancement001();
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		SoundHandler.registerSounds();
		/** config */
		ConfigurationHandler.loadConfig(new File(event.getModConfigurationDirectory(), ModProps.MOD_ID));
		/** register items and blocks */
		Helper.register();
		/** register tileentities */
		GameRegistry.registerTileEntity(TileEntityRecycler.class, "inventory");
		/** packet handler */
		PacketHandler.init();
		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		/** load recycling recipes */
		RecyclingManager.getInstance().loadRecipes();
		/** gui handler */
		NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance, new GuiHandler());
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
