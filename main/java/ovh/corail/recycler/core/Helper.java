package ovh.corail.recycler.core;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Helper {
	private static Random random = new Random();
	
	public static int getRandom(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}
	
	public static <T1, T2> boolean existInList(T1 element, List<T2> list) {
		if (!(element instanceof ItemStack)) { return list.contains(element); }
		if (list.isEmpty()) { return false; }
		boolean compare;
		for (T2 elementList : list) {
			compare = ((ItemStack)element).isItemEqual(elementList instanceof RecyclingRecipe ? ((RecyclingRecipe)elementList).getItemRecipe() : ((ItemStack)elementList));
			if (compare) { return true; }
		}
		return false;
	}
	
	public static <T1, T2> int indexOfList(T1 element, List<T2> list) {
		if (!(element instanceof ItemStack)) { return list.indexOf(element); }
		int index = -1;
		if (list.isEmpty()) { return index; }
		boolean compare;
		for (T2 elementList : list) {
			index++;
			compare = ((ItemStack)element).isItemEqual(elementList instanceof RecyclingRecipe ? ((RecyclingRecipe)elementList).getItemRecipe() : ((ItemStack)elementList));
			if (compare) { return index; }
		}
		return index;
	}
	
	public static ItemStack addToInventoryWithLeftover(ItemStack stack, IInventory inventory, boolean simulate) {
		int left = stack.getCount();
		int minus = inventory instanceof InventoryPlayer ? 4 : 0;
		int max = Math.min(inventory.getInventoryStackLimit(), stack.getMaxStackSize());
		for (int i = 0; i < inventory.getSizeInventory() - minus; i++) {
			ItemStack in = inventory.getStackInSlot(i);
			// if (!inventory.isItemValidForSlot(i, stack))
			// continue;
			if (!in.isEmpty() && stack.isItemEqual(in) && ItemStack.areItemStackTagsEqual(stack, in)) {
				int space = max - in.getCount();
				int add = Math.min(space, stack.getCount());
				if (add > 0) {
					if (!simulate)
						in.grow(add);
					left -= add;
					if (left <= 0)
						return ItemStack.EMPTY;
				}
			}
		}
		for (int i = 0; i < inventory.getSizeInventory() - minus; i++) {
			ItemStack in = inventory.getStackInSlot(i);
			// if (!inventory.isItemValidForSlot(i, stack))
			// continue;
			if (in.isEmpty()) {
				int add = Math.min(max, left);
				if (!simulate)
					inventory.setInventorySlotContents(i, copyStack(stack, add));
				left -= add;
				if (left <= 0)
					return ItemStack.EMPTY;
			}
		}
		return copyStack(stack, left);
	}
	
	private static ItemStack copyStack(ItemStack stack, int size) {
		if (stack.isEmpty() || size == 0)
			return ItemStack.EMPTY;
		ItemStack tmp = stack.copy();
		tmp.setCount(Math.min(size, stack.getMaxStackSize()));
		return tmp;
	}
	
	public static void sendMessage(String message, EntityPlayer currentPlayer, boolean translate) {
		if (currentPlayer != null) {
			if (translate) {
				message = getTranslation(message);
			}
			currentPlayer.sendMessage(new TextComponentString(message));
		}
	}
	
	public static String getTranslation(String message) {
		return I18n.translateToLocal(message);
	}

	public static void render() {
		/** blocks */
		render(Main.recycler);
		/** items */
		render(Main.diamond_fragment);
		render(Main.diamond_disk);
		render(Main.itemAchievement001);
		render(Main.recycling_book);
	}

	private static void render(Block block) {
		render(Item.getItemFromBlock(block), 0);
	}
	
	private static void render(Item item) {
		render(item, 0);
	}

	private static void render(Block block, int meta) {
		render(Item.getItemFromBlock(block), meta);
	}
	
	private static void render(Item item, int meta) {
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta,
				new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
	
	public static void register() {
		/** blocks */
		register(Main.recycler);
		/** items */
		register(Main.diamond_fragment);
		register(Main.diamond_disk);
		register(Main.itemAchievement001);
		register(Main.recycling_book);
	}
	
	private static void register(Block block) {
		GameRegistry.register(block);
		GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
	}

	private static void register(Item item) {
		GameRegistry.register(item);
	}

	public static void getNewRecipes() {
		/** nugget => ingot */
		GameRegistry.addRecipe(new ItemStack(Items.DIAMOND, 1),
				new Object[] { "000", "000", "000", Character.valueOf('0'), new ItemStack(Main.diamond_fragment, 1), });
		/** ingot => nugget */
		GameRegistry.addRecipe(new ItemStack(Main.diamond_fragment, 9),
				new Object[] { "0", Character.valueOf('0'), new ItemStack(Items.DIAMOND, 1), });
		/** recycler recipe */
		GameRegistry.addRecipe(new ItemStack(Main.recycler, 1), new Object[] { "000", "111", "000", Character.valueOf('0'),
				new ItemStack(Blocks.COBBLESTONE, 1), Character.valueOf('1'), new ItemStack(Items.IRON_INGOT, 1), });
		/** diamond disk recipe */
		GameRegistry.addRecipe(new ItemStack(Main.diamond_disk, 1),
				new Object[] { " 0 ", "010", " 0 ", Character.valueOf('0'), new ItemStack(Main.diamond_fragment, 1),
						Character.valueOf('1'), new ItemStack(Items.IRON_INGOT, 1), });	
	}
}
