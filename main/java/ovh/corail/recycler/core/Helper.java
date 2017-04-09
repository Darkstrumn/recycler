package ovh.corail.recycler.core;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class Helper {
	private static Random random = new Random();
	
	public static int getRandom(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}
	
	public static boolean areItemEqual(ItemStack s1, ItemStack s2) {
		return s1.isItemEqual(s2) && s1.getMetadata() == s2.getMetadata();
	}
	
	/** merge same stack and remove empty */
	public static List<ItemStack> mergeStackInList(List<ItemStack> itemStackList) {
		List<ItemStack> outputList = Lists.newArrayList();
		for (ItemStack stack : itemStackList) {
			ItemStack currentStack = stack.copy();
			/** looking for existing same stack */
			for (int i = 0 ; i < outputList.size() ; i++) {
				if (currentStack.isEmpty()) { break; }
				ItemStack lookStack = outputList.get(i).copy();
				if (lookStack.getCount()==lookStack.getMaxStackSize()) { continue; }
				if (Helper.areItemEqual(currentStack, lookStack)) {
					int space = lookStack.getMaxStackSize() - lookStack.getCount();
					int add = Math.min(space, currentStack.getCount());
					if (add > 0) {
						currentStack.shrink(add);
						lookStack.grow(add);
						outputList.set(i, lookStack);
					}
				}
			}
			if (!currentStack.isEmpty()) {
				outputList.add(currentStack);
			}
		}
		return outputList;
	}
	
	public static <T1, T2> boolean existInList(T1 element, List<T2> list) {
		if (list.isEmpty()) { return false; }
		if (!(element instanceof ItemStack) && !(element instanceof Item)) { return list.contains(element); }
		ItemStack stack = element instanceof Item ? new ItemStack((Item) element, 1, 0) : (ItemStack) element;
		boolean compare;
		for (T2 elementList : list) {
			compare = areItemEqual(stack, (elementList instanceof RecyclingRecipe ? ((RecyclingRecipe)elementList).getItemRecipe() : (elementList instanceof Item ? new ItemStack((Item) elementList,1,0) : (ItemStack)elementList )));
			if (compare) { return true; }
		}
		return false;
	}
	
	public static <T1, T2> int indexOfList(T1 element, List<T2> list) {
		if (!(element instanceof ItemStack) && !(element instanceof Item)) { return list.indexOf(element); }
		int index = -1;
		if (list.isEmpty()) { return index; }
		ItemStack stack = element instanceof Item ? new ItemStack((Item) element, 1, 0) : (ItemStack) element;
		boolean compare;
		for (T2 elementList : list) {
			index++;
			compare = areItemEqual(stack, (elementList instanceof RecyclingRecipe ? ((RecyclingRecipe)elementList).getItemRecipe() : (elementList instanceof Item ? new ItemStack((Item) elementList,1,0) : (ItemStack)elementList )));
			if (compare) { return index; }
		}
		return -1;
	}
	
	public static ItemStack addToInventoryWithLeftover(ItemStack stack, IInventory inventory, boolean simulate) {
		int left = stack.getCount();
		int minus = inventory instanceof InventoryPlayer ? 4 : 0;
		int max = Math.min(inventory.getInventoryStackLimit(), stack.getMaxStackSize());
		for (int i = 0; i < inventory.getSizeInventory() - minus; i++) {
			ItemStack in = inventory.getStackInSlot(i);
			// if (!inventory.isItemValidForSlot(i, stack))
			// continue;
			if (!in.isEmpty() && areItemEqual(in, stack)) {
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
	
	public static void sendLog(String message) {
		boolean develop = false;
		if (develop) {
			System.out.println(message);
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
		GameRegistry.addRecipe(new ItemStack(Items.DIAMOND, 1),	new Object[] { "000", "000", "000", 
				Character.valueOf('0'), new ItemStack(Main.diamond_fragment, 1, 0), 
		});
		/** ingot => nugget */
		GameRegistry.addRecipe(new ItemStack(Main.diamond_fragment, 9), new Object[] { "0", 
				Character.valueOf('0'), new ItemStack(Items.DIAMOND, 1), 
		});
		/** recycler recipe */
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Main.recycler, 1), new Object[] { "000", "111", "000", 
				Character.valueOf('0'),	"cobblestone",  
				Character.valueOf('1'), "ingotIron", 
		}));
		/** diamond disk recipe */
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Main.diamond_disk, 1),	new Object[] { " 0 ", "010", " 0 ", 
				Character.valueOf('0'), Main.diamond_fragment,
				Character.valueOf('1'), "ingotIron", 
		}));
	}
}
