package ovh.corail.recycler;

import static ovh.corail.recycler.ModProps.MC_ACCEPT;
import static ovh.corail.recycler.ModProps.MOD_ID;
import static ovh.corail.recycler.ModProps.MOD_NAME;
import static ovh.corail.recycler.ModProps.MOD_UPDATE;
import static ovh.corail.recycler.ModProps.MOD_VER;
import static ovh.corail.recycler.ModProps.ROOT;

import java.io.File;

import org.apache.logging.log4j.Logger;

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
import net.minecraftforge.oredict.OreDictionary;
import ovh.corail.recycler.core.CommonProxy;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.handler.CommandHandler;
import ovh.corail.recycler.handler.ConfigurationHandler;
import ovh.corail.recycler.handler.EventHandler;
import ovh.corail.recycler.handler.GuiHandler;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.handler.SoundHandler;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

@Mod(modid = MOD_ID, name = MOD_NAME, version = MOD_VER, acceptedMinecraftVersions = MC_ACCEPT, updateJSON = MOD_UPDATE, guiFactory = ROOT + ".gui.GuiFactory")
public class ModRecycler {
	@Instance(MOD_ID)
	public static ModRecycler instance;
	@SidedProxy(clientSide = ROOT + ".core.ClientProxy", serverSide = ROOT + ".core.CommonProxy")
	public static CommonProxy proxy;
	public static Logger logger;
	
	public static CreativeTabs tabRecycler = new CreativeTabs(MOD_ID) {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(Item.getItemFromBlock(ModBlocks.recycler));
		}

		@Override
		public String getTranslatedTabLabel() {
			return MOD_NAME;
		}
	};
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		SoundHandler.registerSounds();
		/** config */
		ConfigurationHandler.loadConfig(new File(event.getModConfigurationDirectory(), ModProps.MOD_ID));
		/** register tileentities */
		GameRegistry.registerTileEntity(TileEntityRecycler.class, "inventory");
		/** packet handler */
		PacketHandler.init();
		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		OreDictionary.registerOre("nuggetDiamond", ModItems.diamond_fragment);
		/** load recycling recipes */
		RecyclingManager.getInstance().loadRecipes();
		/** gui handler */
		NetworkRegistry.INSTANCE.registerGuiHandler(ModRecycler.instance, new GuiHandler());
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
