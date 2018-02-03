package ovh.corail.recycler.handler;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import ovh.corail.recycler.ModBlocks;
import ovh.corail.recycler.ModItems;

@Mod.EventBusSubscriber
public class RegistryHandler {
	private static final Block[] blocks = {
		ModBlocks.recycler
	};
	private static final Item[] items = {
		ModItems.diamond_disk,
		ModItems.diamond_fragment,
		ModItems.recycling_book
	}; 

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> blocksRegistry = event.getRegistry();
		blocksRegistry.registerAll(blocks);
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> itemsRegistry = event.getRegistry();
		itemsRegistry.registerAll(items);
		boolean isClientSide = FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
		Item itemBlock = new ItemBlock(ModBlocks.recycler).setRegistryName(ModBlocks.recycler.getRegistryName());
		itemsRegistry.register(itemBlock);
		if (isClientSide) {
			ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(itemBlock.getRegistryName(), "inventory"));
		}
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void renderItems(ModelRegistryEvent event) {
		for (Item item : items) {
			ModelLoader.setCustomModelResourceLocation(item, 0,	new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
	}
}
