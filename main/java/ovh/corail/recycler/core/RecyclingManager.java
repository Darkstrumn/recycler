package ovh.corail.recycler.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import ovh.corail.recycler.handler.ConfigurationHandler;

public class RecyclingManager {
	private static final RecyclingManager instance = new RecyclingManager();
	public List<RecyclingRecipe> recipes = Lists.newArrayList();
	private List<ItemStack> unbalanced = Lists.newArrayList();
	private List<ItemStack> blacklist = Lists.newArrayList();
	private List<List<ItemStack>> grindList = Lists.newArrayList();
	private File unbalancedFile = new File(ConfigurationHandler.getConfigDir(), "unbalanced_recipes.json");
	private File blacklistFile = new File(ConfigurationHandler.getConfigDir(), "blacklist_recipes.json");
	private File userDefinedFile = new File(ConfigurationHandler.getConfigDir(), "user_defined_recipes.json");

	public static RecyclingManager getInstance() {
		return instance;
	}
	
	public void loadRecipes() {
		/** load unbalanced recipes */
		loadUnbalanced();
		/** load blacklist recipes */
		loadBlacklist();
		/** load default recycling recipes */
		loadDefaultRecipes();
		/** load json user defined recycling recipes */
		loadUserDefinedRecipes();
		/** load grind List for damaged items and when losses */
		loadGrindList();
	}
	
	private void loadUnbalanced() {
		List<String> stringlist;
		if (!unbalancedFile.exists()) {
			stringlist = new ArrayList<String>();
			stringlist.add("minecraft:stone:1:1"); /** granite */
			stringlist.add("minecraft:stone:1:3"); /** diorite */
			stringlist.add("minecraft:stone:2:5"); /** andesite */
			stringlist.add("minecraft:paper:1:0"); /** paper */
			stringlist.add("minecraft:sugar:1:0"); /** sugar */
			stringlist.add("minecraft:ender_eye:1:0"); /** ender eye */
			stringlist.add("minecraft:blaze_powder:2:0"); /** blaze powder */
			stringlist.add("minecraft:magma_cream:1:0"); /** magma cream */
			stringlist.add("minecraft:fire_charge:3:0"); /** fire charge */				
			saveAsJson(unbalancedFile, stringlist);
		} else {
			Type token = new TypeToken<List<String>>() {}.getType();
			stringlist = (List<String>) loadAsJson(unbalancedFile, token);
		}
		ItemStack currentStack;
		for (String currentString : stringlist) {
			currentStack = StringToItemStack(currentString);
			if (currentStack != null && !currentStack.isEmpty()) {
				unbalanced.add(currentStack);
			}
		}
	}
	
	public boolean saveUnbalanced() {
		List<String> unbalancedItems = new ArrayList<String>();
		for (int i = 0 ; i < recipes.size() ; i++) {
			if (recipes.get(i).isUnbalanced()) {
				unbalancedItems.add(ItemStackToString(recipes.get(i).getItemRecipe()));				
			}
		}
		return saveAsJson(unbalancedFile, unbalancedItems);
	}
	
	public boolean isUnbalanced(ItemStack stack) {
		return Helper.existInList(stack, unbalanced);
	}

	private void loadBlacklist() {
		List<String> stringlist;
		if (!blacklistFile.exists()) {
			stringlist = new ArrayList<String>();
			stringlist.add("recycler:recycler:1:0");
			saveAsJson(blacklistFile, stringlist);
		} else {
			Type token = new TypeToken<List<String>>() {}.getType();
			stringlist = (List<String>) loadAsJson(blacklistFile, token);
		}
		ItemStack currentStack;
		for (String currentString : stringlist) {
			currentStack = StringToItemStack(currentString);
			if (currentStack != null && !currentStack.isEmpty()) {
				blacklist.add(currentStack);
			}
		}
	}
	
	public boolean saveBlacklist() {
		List<String> blacklistItems = new ArrayList<String>();
		for (int i = 0 ; i < recipes.size() ; i++) {
			if (!recipes.get(i).isAllowed()) {
				blacklistItems.add(ItemStackToString(recipes.get(i).getItemRecipe()));				
			}
		}
		return saveAsJson(blacklistFile, blacklistItems);
	}
	
	public boolean isBlacklist(ItemStack stack) {
		return Helper.existInList(stack, blacklist);
	}

	public int getRecipesCount() {
		return recipes.size();
	}

	public RecyclingRecipe getRecipe(int index) {
		return recipes.get(index);
	}

	public void addRecipe(RecyclingRecipe recipe) {
		recipes.add(recipe);
	}

	private void addRecipe(ItemStack stack, Object... recipeComponents) {
		recipes.add(new RecyclingRecipe(stack, recipeComponents));
	}

	private void addRecipe(ItemStack stackIn, ItemStack stackOut) {
		recipes.add(new RecyclingRecipe(stackIn, stackOut));
	}
	
	public boolean removeRecipe(ItemStack stack) {
		if (stack.isEmpty()) { return false; }
		int index = Helper.indexOfList(stack, recipes);
		if (index < 0 ) { return false; }
		if (recipes.get(index).isUserDefined()) {
			recipes.remove(index);
			saveUserDefinedRecipes();
		} else {
			recipes.get(index).setAllowed(false);
			saveBlacklist();
		}
		return true;
	}

	public int hasRecipe(ItemStack stack) {
		if (stack.isEmpty() || stack.getCount() <= 0) {
			return -1;
		}
		/** don't allow binding cursed items */
		Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
		Iterator i = enchants.entrySet().iterator();
		Enchantment enchant;
		while (i.hasNext()) {
			Map.Entry pair = (Map.Entry)i.next();
			enchant = (Enchantment) pair.getKey();
			if (enchant.getRegistryName().getResourcePath().equals("binding_curse")) {
				return -1;
			}
			i.remove();
		}
		/** damaged items */
		ItemStack testStack = stack.copy();
		if (testStack.getItem().isRepairable()) {
			testStack.setItemDamage(0);
		}
		int recipe_num = Helper.indexOfList(testStack, recipes);
		if (recipe_num < 0) { return -1; }
		/** unbalanced recipes */
		if (!ConfigurationHandler.unbalancedRecipes && recipes.get(recipe_num).isUnbalanced()) {
			return -1;
		}
		/** only user defined recipes */
		if (ConfigurationHandler.onlyUserRecipes && !recipes.get(recipe_num).isUserDefined()) {
			return -1;
		}
		/** only allowed recipes */
		if (!recipes.get(recipe_num).isAllowed()) {
			return -1;
		}
		return recipe_num;
	}
	public List<ItemStack> getResultStack(ItemStack stack, int nb_input) {
		return getResultStack(stack, nb_input, false);
	}
	
	public List<ItemStack> getResultStack(ItemStack stack, int nb_input, boolean half) {
		List<ItemStack> itemsList = new ArrayList<ItemStack>();
		int num_recipe = hasRecipe(stack);
		if (num_recipe < 0) {
			return itemsList;
		}
		/** check enchants, no loss */
		if (ConfigurationHandler.enchantedBooks) {
			Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
			if (!enchants.isEmpty()) {
				Iterator i = enchants.entrySet().iterator();
				ItemStack currentBook;
				Enchantment enchant;
				Integer level;
				while (i.hasNext()) {
					Map.Entry pair = (Map.Entry)i.next();
					enchant = (Enchantment) pair.getKey();
					level = (Integer) pair.getValue();
					currentBook = new ItemStack(Items.ENCHANTED_BOOK);
					Items.ENCHANTED_BOOK.addEnchantment(currentBook, new EnchantmentData(enchant, level));
					itemsList.add(currentBook);
				}
			}
		}
		
		RecyclingRecipe currentRecipe = recipes.get(num_recipe);
		ItemStack currentStack;
		Item currentItem;
		int currentSize, currentMeta;
		/** foreach stacks in the recipe */
		for (int i = 0; i < currentRecipe.getCount(); i++) {
			currentStack = currentRecipe.getStack(i).copy();
			currentSize = currentStack.getCount();
			/** smaller units for damaged items and when losses chance */
			if (half || (stack.isItemDamaged() && hasGrind(currentStack))) {
				currentStack = getGrind(currentStack);
				// TODO check for input amount > 1 for grind 
				currentSize *= currentStack.getCount();
				double pourcent = (double) (stack.getMaxDamage() - (stack.getItemDamage())) / (double) stack.getMaxDamage();
				currentSize = (int) Math.floor(currentSize * pourcent);
			}
			/** losses with chance */
			if (half) {
				currentSize = (int) Math.floor(currentSize/2.0D);
			}
			/** size for nb_input */
			currentSize *= nb_input;
			/** fill with fullstack */
			int slotCount = (int) Math.floor(currentSize / currentStack.getMaxStackSize());
			currentStack.setCount(currentStack.getMaxStackSize());
			for (int j = 0; j < slotCount; j++) {	
				itemsList.add(currentStack.copy());
			}
			/** stack left */
			int leftStackCount = currentSize - (slotCount * currentStack.getMaxStackSize());
			if (leftStackCount > 0) {
				currentStack.setCount(leftStackCount);
				itemsList.add(currentStack.copy());
			}
		}
		return itemsList;
	}
	// TODO Make in json format
	private void loadGrindList() {
		grindList.add(Lists.newArrayList(new ItemStack(Items.IRON_INGOT, 1, 0), new ItemStack(Items.field_191525_da, 9, 0))); /** iron nugget */
		grindList.add(Lists.newArrayList(new ItemStack(Items.GOLD_INGOT, 1, 0), new ItemStack(Items.GOLD_NUGGET, 9, 0)));
		grindList.add(Lists.newArrayList(new ItemStack(Items.LEATHER, 1, 0), new ItemStack(Items.RABBIT_HIDE, 4, 0)));
		for (int i = 0 ; i < 6 ; i++) {
			grindList.add(Lists.newArrayList(new ItemStack(Blocks.PLANKS, 1, i), new ItemStack(Items.STICK, 4 ,0)));
		}
		
	}

	public boolean hasGrind(ItemStack stack) {
		for (List<ItemStack> grind : grindList) {
			/** Helper.areItemEqual() doesn't check stacksize */
			if (Helper.areItemEqual(grind.get(0), stack)) {
				return true;
			}
		}
		return false;
	}
	
	/** only call when stack is damaged or there's losses to get smaller units */
	public ItemStack getGrind(ItemStack stack) {
		for (List<ItemStack> grind : grindList) {
			/** Helper.areItemEqual() doesn't check stacksize */
			if (Helper.areItemEqual(grind.get(0), stack)) {
				return grind.get(1).copy();
			}
		}
		return stack;
	}
	
	private RecyclingManager() {
	}
	
	public boolean saveUserDefinedRecipes() {
		List<JsonRecyclingRecipe> jRecipes = new ArrayList<JsonRecyclingRecipe>();
		for (int i = 0 ; i < recipes.size() ; i++) {
			if (recipes.get(i).isUserDefined()) {
				jRecipes.add(convertRecipeToJson(recipes.get(i)));				
			}
		}
		return saveAsJson(userDefinedFile, jRecipes);
	}
	
	public boolean saveAsJson(File file, List<?> list) {
		if (file.exists()) {
			file.delete();
		}
		try {
			if (file.createNewFile()) {
				FileWriter fw = new FileWriter(file);
				fw.write(new GsonBuilder().setPrettyPrinting().create().toJson(list));
				fw.close();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private List<?> loadAsJson(File file, Type token) {
		List<?> list = null;
		try {
			list = new Gson().fromJson(new BufferedReader(new FileReader(file)), token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	private void loadUserDefinedRecipes() {
		List<JsonRecyclingRecipe> jsonRecipesList;
		if (!userDefinedFile.exists()) {
			jsonRecipesList = new ArrayList<JsonRecyclingRecipe>();
			jsonRecipesList.add(new JsonRecyclingRecipe(Main.MOD_ID+":recycler:1:0", new String[] { "minecraft:cobblestone:6:0", "minecraft:iron_ingot:3:0", }));
			saveAsJson(userDefinedFile, jsonRecipesList);
		} else {
			Type token = new TypeToken<List<JsonRecyclingRecipe>>() {}.getType();
			jsonRecipesList = (List<JsonRecyclingRecipe>) loadAsJson(userDefinedFile, token);
		}

		for (int i = 0; i < jsonRecipesList.size(); i++) {
			RecyclingRecipe recipe = convertJsonRecipe(jsonRecipesList.get(i));
			if (recipe != null && recipe.getCount() > 0) {
				/** check for same existing recipe */
				int foundRecipe = Helper.indexOfList(recipe, recipes);
				recipe.setUserDefined(true);
				recipe.setAllowed(!isBlacklist(recipe.getItemRecipe()));
				if (foundRecipe == -1) {
					recipes.add(recipe);
				} else {
					recipes.set(foundRecipe, recipe);
				}
			} else {
				// TODO translate
				System.out.println("Error while reading json recipe : "+jsonRecipesList.get(i).inputItem);
			}
		}
	}

	private RecyclingRecipe convertJsonRecipe(JsonRecyclingRecipe jRecipe) {
		ItemStack inputItem = StringToItemStack(jRecipe.inputItem);
		if (inputItem.isEmpty()) { return null; }
		RecyclingRecipe recipe = new RecyclingRecipe(inputItem);
		for (int i = 0; i < jRecipe.outputItems.length; i++) {
			ItemStack outputItem = StringToItemStack(jRecipe.outputItems[i]);
			if (!outputItem.isEmpty()) {
				recipe.addStack(outputItem);
			}
		}
		recipe.setUnbalanced(isUnbalanced(recipe.getItemRecipe()));
		return recipe;
	}
	
	public JsonRecyclingRecipe convertRecipeToJson(RecyclingRecipe recipe) {
		String inputItem = ItemStackToString(recipe.getItemRecipe());
		if (inputItem.isEmpty()) { return null; }
		String outputItems[] = new String[recipe.getCount()];
		for (int i = 0 ; i < recipe.getCount() ; i++) {
			String outputItem = ItemStackToString(recipe.getStack(i));
			outputItems[i] = outputItem;
		}
		JsonRecyclingRecipe jRecipe = new JsonRecyclingRecipe(inputItem, outputItems);
		return jRecipe;
	}

	private String ItemStackToString(ItemStack stack) {
		return stack.getItem().getRegistryName().toString() + ":" + stack.getCount() + ":" + stack.getMetadata();
	}
	
	private ItemStack StringToItemStack(String value) {
		String[] parts = value.split(":");
		if (parts.length == 4) {
			Item item = Item.REGISTRY.getObject(new ResourceLocation(parts[0], parts[1]));
			if (item != null) {
				return new ItemStack(item, Integer.valueOf(parts[2]), Integer.valueOf(parts[3]));
			}
		}
		return ItemStack.EMPTY;

	}
	
	public RecyclingRecipe convertCraftingRecipe(IRecipe iRecipe) {
		RecyclingRecipe recipe = new RecyclingRecipe(iRecipe.getRecipeOutput());		
		if (iRecipe instanceof ShapedRecipes) {
			ShapedRecipes craftingRecipe = (ShapedRecipes) iRecipe;
			for (int j = 0; j < craftingRecipe.recipeItems.length; j++) {
				if (!craftingRecipe.recipeItems[j].isEmpty()) {
					recipe.addStack(craftingRecipe.recipeItems[j]);
				}
			}
		} else if (iRecipe instanceof ShapelessRecipes) {
			ShapelessRecipes craftingRecipe = (ShapelessRecipes) iRecipe;
			for (int j = 0; j < craftingRecipe.recipeItems.size(); j++) {
				if (!craftingRecipe.recipeItems.get(j).isEmpty()) {
					recipe.addStack(craftingRecipe.recipeItems.get(j));
				}
			}
		} else if (iRecipe  instanceof ShapedOreRecipe) {
			ShapedOreRecipe craftingRecipe = (ShapedOreRecipe) iRecipe;
			ItemStack currentStack = ItemStack.EMPTY;
			for (int j = 0; j < craftingRecipe.getInput().length; j++) {
				if (craftingRecipe.getInput()[j] instanceof ItemStack) {
					currentStack = (ItemStack) craftingRecipe.getInput()[j];
				} else if (craftingRecipe.getInput()[j] instanceof List) {
					Object o = ((List) craftingRecipe.getInput()[j]).get(0);
					if (o instanceof ItemStack) {
						currentStack = (ItemStack) o;
		            }
				}
				if (!currentStack.isEmpty()) {
					recipe.addStack(currentStack);
				}
			}
		} else if (iRecipe  instanceof ShapelessOreRecipe) {
			ShapelessOreRecipe craftingRecipe = (ShapelessOreRecipe) iRecipe;
			ItemStack currentStack = ItemStack.EMPTY;
			for (int j = 0; j < craftingRecipe.getInput().size(); j++) {
				if (craftingRecipe.getInput().get(j) instanceof ItemStack) {
					currentStack = (ItemStack) craftingRecipe.getInput().get(j);
				} else if (craftingRecipe.getInput().get(j) instanceof List) {
					Object o = ((List) craftingRecipe.getInput().get(j)).get(0);
					if (o instanceof ItemStack) {
						currentStack = (ItemStack) o;
					}
				}
				if (!currentStack.isEmpty()) {
					recipe.addStack(currentStack);
				}
			}
		}
		recipe.setUnbalanced(false);
		recipe.setUserDefined(true);
		recipe.setAllowed(true);
		return recipe;
	}
	
	private void loadDefaultRecipes() {
		/* granite */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE,1,1), new ItemStack[] { new ItemStack(Blocks.STONE,1,3), new ItemStack(Items.QUARTZ,1,0), }));
		/* diorite */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE,1,3), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,1,0), new ItemStack(Items.QUARTZ,1,0), }));
		/* andesite */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE,2,5), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,1,0), new ItemStack(Blocks.STONE,1,3), }));
		/* paper */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.PAPER,1,0), new ItemStack[] { new ItemStack(Items.REEDS,1,0), }));
		/* sugar */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.SUGAR,1,0), new ItemStack[] { new ItemStack(Items.REEDS,1,0), }));
		/* ender eye */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.ENDER_EYE,1,0), new ItemStack[] { new ItemStack(Items.ENDER_PEARL,1,0), new ItemStack(Items.BLAZE_POWDER,1,0), }));
		/* blaze powder */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BLAZE_POWDER,2,0), new ItemStack[] { new ItemStack(Items.BLAZE_ROD,1,0), }));
		/* magma cream */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.MAGMA_CREAM,1,0), new ItemStack[] { new ItemStack(Items.BLAZE_POWDER,1,0), new ItemStack(Items.SLIME_BALL,1,0), }));
		/* fire charge */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.FIRE_CHARGE,3,0), new ItemStack[] { new ItemStack(Items.BLAZE_POWDER,1,0), new ItemStack(Items.GUNPOWDER,1,0), new ItemStack(Items.COAL,1,0), }));
			
		/** 1.9 recipes */
		/* purpur slab */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPUR_SLAB,2,0), new ItemStack[] { new ItemStack(Blocks.PURPUR_BLOCK,1,0), }));
		/* end stone brick */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.END_BRICKS,1,0), new ItemStack[] { new ItemStack(Blocks.END_STONE,1,0), }));		
		/* purpur stair */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPUR_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.PURPUR_SLAB,3,0), }));
		/* purpur block */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPUR_BLOCK,1,0), new ItemStack[] { new ItemStack(Items.CHORUS_FRUIT_POPPED,1,0), }));		
		/* sculpted purpur */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPUR_PILLAR,1,0), new ItemStack[] { new ItemStack(Blocks.PURPUR_BLOCK,1,0), }));
		/* end rod */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.END_ROD,4,0), new ItemStack[] { new ItemStack(Items.BLAZE_ROD,1,0), new ItemStack(Items.CHORUS_FRUIT_POPPED,1,0), }));		
		/* new boat */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.SPRUCE_BOAT,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,5,1), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BIRCH_BOAT,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,5,2), }));	
		recipes.add(new RecyclingRecipe(new ItemStack(Items.JUNGLE_BOAT,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,5,3), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.ACACIA_BOAT,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,5,4), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DARK_OAK_BOAT,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,5,5), }));
		/* shield */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.SHIELD,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,1,0), new ItemStack(Blocks.PLANKS,6,0), }));
		/** block */
		/* stone */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,1,0), }));
		/* polished granite, diorite, andesite */
		for (int i = 1; i <= 3; i++) {
			recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE,1,(2*i)), new ItemStack[] { new ItemStack(Blocks.STONE,1,(2*i-1)), }));
		}
		/* coarse dirt */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DIRT,2,1), new ItemStack[] { new ItemStack(Blocks.DIRT,1,0), new ItemStack(Blocks.GRAVEL,1,0), }));
		/* clay */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CLAY,1,0), new ItemStack[] { new ItemStack(Items.CLAY_BALL,4,0), }));					
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.HARDENED_CLAY,1,0), new ItemStack[] { new ItemStack(Blocks.CLAY,1,0), }));
		/* stained clay */
		for (int i=0;i<16;i++) {
			recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STAINED_HARDENED_CLAY,1,i), new ItemStack[] { new ItemStack(Blocks.CLAY,1,0), }));								
		}
		/* mossy cobblestone */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MOSSY_COBBLESTONE,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,1,0), new ItemStack(Blocks.VINE,1,0), }));
		/* glass */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GLASS,1,0), new ItemStack[] { new ItemStack(Blocks.SAND,1,0), }));		
		/* stained glass */
		for (int i=0;i<16;i++) {
			recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STAINED_GLASS,1,i), new ItemStack[] { new ItemStack(Blocks.GLASS,1,0), }));		
		}
		/* glass pane */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GLASS_PANE,8,0), new ItemStack[] { new ItemStack(Blocks.GLASS,3,0), }));		
		/* stained glass pane */
		for (int i=0;i<16;i++) {
			recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STAINED_GLASS_PANE,8,i), new ItemStack[] { new ItemStack(Blocks.STAINED_GLASS,3,i), }));		
		}
		/* sandstone */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SANDSTONE,1,0), new ItemStack[] { new ItemStack(Blocks.SAND,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_SANDSTONE,1,0), new ItemStack[] { new ItemStack(Blocks.SAND,4,1), }));
		/* chiseled smooth sandstone */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SANDSTONE,1,1), new ItemStack[] { new ItemStack(Blocks.SANDSTONE,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SANDSTONE,1,2), new ItemStack[] { new ItemStack(Blocks.SANDSTONE,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_SANDSTONE,1,1), new ItemStack[] { new ItemStack(Blocks.RED_SANDSTONE,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_SANDSTONE,1,2), new ItemStack[] { new ItemStack(Blocks.RED_SANDSTONE,1,0), }));	
		/* stonebrick */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONEBRICK,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,1,0), }));
		/* cracked, mossy, chiseled stonebrick */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONEBRICK,1,1), new ItemStack[] { new ItemStack(Blocks.STONEBRICK,1,0), new ItemStack(Blocks.VINE,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONEBRICK,1,2), new ItemStack[] { new ItemStack(Blocks.STONEBRICK,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONEBRICK,1,3), new ItemStack[] { new ItemStack(Blocks.STONEBRICK,1,0), }));
		/* bricks block */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BRICK_BLOCK,1,0), new ItemStack[] { new ItemStack(Items.BRICK,4,0), }));		
		/* glowstone */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GLOWSTONE,1,0), new ItemStack[] { new ItemStack(Items.GLOWSTONE_DUST,4,0), }));				
		/* prismarine */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PRISMARINE,1,0), new ItemStack[] { new ItemStack(Items.PRISMARINE_SHARD,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PRISMARINE,1,1), new ItemStack[] { new ItemStack(Items.PRISMARINE_SHARD,9,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PRISMARINE,1,2), new ItemStack[] { new ItemStack(Items.PRISMARINE_SHARD,8,0), new ItemStack(Items.DYE,1,0), }));
		/* sea lantern */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SEA_LANTERN,1,0), new ItemStack[] { new ItemStack(Items.PRISMARINE_CRYSTALS,5,0), new ItemStack(Items.PRISMARINE_SHARD,4,0),}));
		/* quartz block */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.QUARTZ_BLOCK,1,0), new ItemStack[] { new ItemStack(Items.QUARTZ,4,0), }));		
		/* chiseled pillar quartz */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.QUARTZ_BLOCK,1,1), new ItemStack[] { new ItemStack(Blocks.QUARTZ_BLOCK,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.QUARTZ_BLOCK,1,2), new ItemStack[] { new ItemStack(Blocks.QUARTZ_BLOCK,1,0), }));
		/* planks */
		for (int i = 0; i < 4; i++) {
			recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PLANKS,4,i), new ItemStack[] { new ItemStack(Blocks.LOG,1,i), }));
		}
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PLANKS,4,4), new ItemStack[] { new ItemStack(Blocks.LOG2,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PLANKS,4,5), new ItemStack[] { new ItemStack(Blocks.LOG2,1,1), }));
		/* brick */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BRICK,1,0), new ItemStack[] { new ItemStack(Items.CLAY_BALL,1,0), }));
		/* snow */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SNOW,1,0), new ItemStack[] { new ItemStack(Items.SNOWBALL,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SNOW_LAYER,2,0), new ItemStack[] { new ItemStack(Blocks.SNOW,1,0), }));
		/** machine */
		/* dispenser */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DISPENSER,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,7,0), new ItemStack(Items.REDSTONE,1,0), new ItemStack(Items.BOW,1,0), }));
		/* noteblock */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.NOTEBLOCK,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,8,0), new ItemStack(Items.REDSTONE,1,0), }));
		/* chest */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CHEST,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,8,0), }));
		/* ender chest */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ENDER_CHEST,1,0), new ItemStack[] { new ItemStack(Blocks.OBSIDIAN,8,0), new ItemStack(Items.ENDER_EYE,1,0), }));
		/* chest */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.TRAPPED_CHEST,1,0), new ItemStack[] { new ItemStack(Blocks.CHEST,1,0),	new ItemStack(Blocks.TRIPWIRE_HOOK,1,0), }));
		/* crafting table */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CRAFTING_TABLE,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,4,0), }));
		/* furnace */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.FURNACE,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,8,0),	}));
		/* jukebox */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUKEBOX,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,8,0), new ItemStack(Items.DIAMOND,1,0), }));
		/* enchantment table */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ENCHANTING_TABLE,1,0), new ItemStack[] { new ItemStack(Items.DIAMOND,2,0),	new ItemStack(Blocks.OBSIDIAN,4,0), new ItemStack(Items.BOOK,1,0), }));
		/* ender chest */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CHEST,1,0), new ItemStack[] { new ItemStack(Blocks.OBSIDIAN,8,0), new ItemStack(Items.ENDER_EYE,1,0), }));
		/* beacon */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BEACON,1,0), new ItemStack[] { new ItemStack(Blocks.OBSIDIAN,3,0),	new ItemStack(Items.NETHER_STAR,1,0), new ItemStack(Blocks.GLASS,5,0), }));
		/* anvil */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ANVIL,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,31,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ANVIL,1,1), new ItemStack[] { new ItemStack(Items.IRON_INGOT,20,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ANVIL,1,2), new ItemStack[] { new ItemStack(Items.IRON_INGOT,10,0), }));
		/* daylight sensor */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DAYLIGHT_DETECTOR,1,0), new ItemStack[] { new ItemStack(Blocks.WOODEN_SLAB,3,0), new ItemStack(Blocks.GLASS,3,0), new ItemStack(Items.QUARTZ,3,0), }));
		/* hopper */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.HOPPER,1,0), new ItemStack[] {	new ItemStack(Items.IRON_INGOT,5,0), new ItemStack(Blocks.CHEST,1,0), }));
		/* dropper */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DROPPER,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,7,0), new ItemStack(Items.REDSTONE,1,0), }));
		/** rail */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RAIL,16,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,6,0), new ItemStack(Items.STICK,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DETECTOR_RAIL,6,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,6,0), new ItemStack(Items.REDSTONE,1,0),	new ItemStack(Blocks.STONE_PRESSURE_PLATE,1,0),	}));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GOLDEN_RAIL,6,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,6,0), new ItemStack(Items.REDSTONE,1,0),	new ItemStack(Items.STICK,1,0),	}));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACTIVATOR_RAIL,6,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,6,0), new ItemStack(Items.REDSTONE,1,0), new ItemStack(Items.STICK,2,0), }));
		/** piston */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STICKY_PISTON,1,0), new ItemStack[] { new ItemStack(Items.SLIME_BALL,1,0), new ItemStack(Blocks.PISTON,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PISTON,1,0), new ItemStack[] {	new ItemStack(Items.IRON_INGOT,1,0), new ItemStack(Blocks.COBBLESTONE,4,0),	new ItemStack(Items.REDSTONE,1,0), new ItemStack(Blocks.PLANKS,3,0), }));
		/** wool */
		for (int i = 0; i < 16; i++) {
			recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WOOL,1,i), new ItemStack[] { new ItemStack(Items.STRING,4,0), }));
		}
		/** slab */
		for (int i = 0; i <= 5; i++) {
			recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WOODEN_SLAB,2,i), new ItemStack[] { new ItemStack(Blocks.PLANKS,1,i), }));
		}
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WOODEN_SLAB,2,0), new ItemStack[] { new ItemStack(Blocks.STONE,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WOODEN_SLAB,2,1), new ItemStack[] { new ItemStack(Blocks.SANDSTONE,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_SLAB,2,3), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_SLAB,2,4), new ItemStack[] { new ItemStack(Blocks.BRICK_BLOCK,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_SLAB,2,5), new ItemStack[] { new ItemStack(Blocks.STONEBRICK,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_SLAB,2,6), new ItemStack[] { new ItemStack(Blocks.NETHER_BRICK,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_SLAB,2,7), new ItemStack[] { new ItemStack(Blocks.QUARTZ_BLOCK,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_SLAB2,2,0), new ItemStack[] { new ItemStack(Blocks.RED_SANDSTONE,1,0), }));
		/** TNT */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.TNT,1,0), new ItemStack[] { new ItemStack(Blocks.SAND,4,0),	new ItemStack(Items.GUNPOWDER,5,0),	}));
		/** bookshelf */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BOOKSHELF,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,6,0), new ItemStack(Items.BOOK,3,0), }));
		/** torch */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.TORCH,4,0), new ItemStack[] { new ItemStack(Items.COAL,1,0), new ItemStack(Items.STICK,1,0), }));
		/** stair */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.WOODEN_SLAB,3,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.WOODEN_SLAB,3,1), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.WOODEN_SLAB,3,2), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.WOODEN_SLAB,3,3), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.WOODEN_SLAB,3,4), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.WOODEN_SLAB,3,5), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SANDSTONE_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.STONE_SLAB,3,1), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.STONE_SLAB,3,3), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BRICK_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.STONE_SLAB,3,4), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_BRICK_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.STONE_SLAB,3,5), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.NETHER_BRICK_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.STONE_SLAB,3,6), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.QUARTZ_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.STONE_SLAB,3,7), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_SANDSTONE_STAIRS,1,0), new ItemStack[] { new ItemStack(Blocks.STONE_SLAB2,3,0), }));
		/** ladder */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LADDER,3,0), new ItemStack[] { new ItemStack(Items.STICK,7,0), }));
		/** lever */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LEVER,1,0), new ItemStack[] { new ItemStack(Items.STICK,1,0), new ItemStack(Blocks.COBBLESTONE,1,0), }));
		/** pressure plate */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_PRESSURE_PLATE,1,0), new ItemStack[] { new ItemStack(Blocks.STONE,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,1,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,2,0), }));
		/** redstone torch */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.REDSTONE_TORCH,1,0), new ItemStack[] { new ItemStack(Items.REDSTONE,1,0), new ItemStack(Items.STICK,1,0), }));
		/** button */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WOODEN_BUTTON,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_BUTTON,1,0), new ItemStack[] { new ItemStack(Blocks.STONE,1,0), }));
		/** fence */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_FENCE,3,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,4,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_FENCE,3,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,4,1), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_FENCE,3,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,4,2), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_FENCE,3,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,4,3), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_FENCE,3,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,4,4), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_FENCE,3,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,4,5), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.NETHER_BRICK_FENCE,1,0), new ItemStack[] { new ItemStack(Blocks.NETHER_BRICK,1,0), }));
		/** fence gate */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_FENCE_GATE,1,0), new ItemStack[] {	new ItemStack(Blocks.PLANKS,2,0), new ItemStack(Items.STICK,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_FENCE_GATE,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,1), new ItemStack(Items.STICK,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_FENCE_GATE,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,2), new ItemStack(Items.STICK,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_FENCE_GATE,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,3), new ItemStack(Items.STICK,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_FENCE_GATE,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,4), new ItemStack(Items.STICK,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_FENCE_GATE,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,5), new ItemStack(Items.STICK,4,0), }));
		/** lit pumpkin */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIT_PUMPKIN,1,0), new ItemStack[] { new ItemStack(Blocks.PUMPKIN,1,0), new ItemStack(Blocks.TORCH,1,0), }));
		/** trap */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.TRAPDOOR,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,3,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.IRON_TRAPDOOR,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,2,0), }));
		/** iron bar */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.IRON_BARS,1,0), new ItemStack[] { new ItemStack(Items.field_191525_da,3,0), }));
		/** redstone lamp */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.REDSTONE_LAMP,1,0), new ItemStack[] { new ItemStack(Items.REDSTONE,4,0), new ItemStack(Blocks.GLOWSTONE,1,0), }));
		/** tripwire hook */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.TRIPWIRE_HOOK,2,0), new ItemStack[] { new ItemStack(Items.STICK,1,0),	new ItemStack(Blocks.PLANKS,1,0), new ItemStack(Items.IRON_INGOT,1,0), }));
		/** cobblestone wall */
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.COBBLESTONE_WALL,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Blocks.COBBLESTONE_WALL,1,1), new ItemStack[] { new ItemStack(Blocks.MOSSY_COBBLESTONE,1,0), }));
		/** carpet */
		for (int i = 0; i < 16; i++) {
			recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CARPET,3,i), new ItemStack[] { new ItemStack(Blocks.WOOL,2,i), }));
		}
		/** flint and steel */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.FLINT_AND_STEEL,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,1,0), new ItemStack(Items.FLINT,1,0), }));
		/** Items.STICK */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.STICK,2,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,1,0), }));
		/** bowl */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BOWL,4,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,3,0), }));

		/** door */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.OAK_DOOR,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.SPRUCE_DOOR,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,1), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BIRCH_DOOR,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,2), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.JUNGLE_DOOR,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,3), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.ACACIA_DOOR,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,4), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DARK_OAK_DOOR,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,5), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_DOOR,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,2,0), }));
		/** painting */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.PAINTING,1,0), new ItemStack[] { new ItemStack(Items.STICK,8,0), new ItemStack(Blocks.WOOL,1,0), }));
		/** sign */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.SIGN,3,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,6,0), new ItemStack(Items.STICK,1,0), }));
		/** empty bucket */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BUCKET,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,3,0), }));
		/** minecart */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.MINECART,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,5,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.HOPPER_MINECART,1,0), new ItemStack[] { new ItemStack(Items.MINECART,1,0), new ItemStack(Blocks.HOPPER,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.TNT_MINECART,1,0), new ItemStack[] { new ItemStack(Items.MINECART,1,0), new ItemStack(Blocks.TNT,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.FURNACE_MINECART,1,0), new ItemStack[] { new ItemStack(Items.MINECART,1,0), new ItemStack(Blocks.FURNACE,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.CHEST_MINECART,1,0), new ItemStack[] { new ItemStack(Items.MINECART,1,0), new ItemStack(Blocks.CHEST,1,0), }));
		/** boat */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BOAT,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,5,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.SPRUCE_BOAT,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,5,1), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BIRCH_BOAT,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,5,2), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.JUNGLE_BOAT,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,5,3), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.ACACIA_BOAT,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,5,4), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DARK_OAK_BOAT,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,5,5), }));
		/** book */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BOOK,1,0), new ItemStack[] { new ItemStack(Items.PAPER,3,0), new ItemStack(Items.LEATHER,1,0), }));		
		/** compass */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.COMPASS,1,0), new ItemStack[] {	new ItemStack(Items.REDSTONE,1,0), new ItemStack(Items.IRON_INGOT,4,0), }));
		/** fishing rod */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.FISHING_ROD,1,0), new ItemStack[] {	new ItemStack(Items.STRING,2,0), new ItemStack(Items.STICK,3,0), }));
		/** clock */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.CLOCK,1,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,4,0), new ItemStack(Items.REDSTONE,1,0),	}));
		/** bed */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BED,1,0), new ItemStack[] { 
				new ItemStack(Blocks.WOOL,3,0),
				new ItemStack(Blocks.PLANKS,3,0),
		}));
		/** redstone repeater */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.REPEATER,1,0), new ItemStack[] { new ItemStack(Blocks.STONE,3,0), new ItemStack(Blocks.REDSTONE_TORCH,2,0), new ItemStack(Items.REDSTONE,1,0), }));
		/** redstone comparator */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.COMPARATOR,1,0), new ItemStack[] { new ItemStack(Blocks.STONE,3,0), new ItemStack(Blocks.REDSTONE_TORCH,3,0), new ItemStack(Items.QUARTZ,1,0), }));
		/** glass bottle */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.GLASS_BOTTLE,1,0), new ItemStack[] { new ItemStack(Blocks.GLASS,1,0), }));
		/** brewing stand */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BREWING_STAND,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,3,0), new ItemStack(Items.BLAZE_ROD,1,0), }));
		/** cauldron */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.CAULDRON,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,7,0), }));
		/** item frame */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.ITEM_FRAME,1,0), new ItemStack[] { new ItemStack(Items.STICK,8,0),	new ItemStack(Items.LEATHER,1,0), }));
		/** flower pot */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.FLOWER_POT,1,0), new ItemStack[] { new ItemStack(Items.BRICK,3,0), }));
		/** carrot on a Items.STICK */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.CARROT_ON_A_STICK,1,0), new ItemStack[] { new ItemStack(Items.CARROT,1,0), new ItemStack(Items.FISHING_ROD,1,0), }));
		/** armor stand */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.ARMOR_STAND,1,0), new ItemStack[] { 
				new ItemStack(Items.STICK,6,0),
				new ItemStack(Blocks.STONE_SLAB,1,0),
		}));
		/** lead */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.LEAD,2,0), new ItemStack[] { new ItemStack(Items.STRING,4,0), new ItemStack(Items.SLIME_BALL,1,0), }));
		/** banner */
		for (int i=0;i<16;i++) {
			recipes.add(new RecyclingRecipe(new ItemStack(Items.BANNER,1,i), new ItemStack[] { new ItemStack(Blocks.WOOL,6,0), new ItemStack(Items.STICK,1,0), }));
		}
		/** end crystal */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.END_CRYSTAL,1,0), new ItemStack[] {	new ItemStack(Blocks.GLASS,7,0), new ItemStack(Items.ENDER_EYE,1,0),	new ItemStack(Items.GHAST_TEAR,1,0), }));
		/** empty map */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.MAP,1,0), new ItemStack[] {	new ItemStack(Items.COMPASS,1,0), new ItemStack(Items.PAPER,8,0), }));
		/** shears */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.SHEARS,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,2,0), }));
		/** pickaxe */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.WOODEN_PICKAXE,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,3,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.STONE_PICKAXE,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,3,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_PICKAXE,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,3,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_PICKAXE,1,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,3,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_PICKAXE,1,0), new ItemStack[] { new ItemStack(Items.DIAMOND,3,0), new ItemStack(Items.STICK,2,0), }));
		/** axe */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.WOODEN_AXE,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,3,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.STONE_AXE,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,3,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_AXE,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,3,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_AXE,1,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,3,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_AXE,1,0), new ItemStack[] {	new ItemStack(Items.DIAMOND,3,0), new ItemStack(Items.STICK,2,0), }));
		/** shovel/spade */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.WOODEN_SHOVEL,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,1,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.STONE_SHOVEL,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,1,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_SHOVEL,1,0), new ItemStack[] {	new ItemStack(Items.IRON_INGOT,1,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_SHOVEL,1,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,1,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_SHOVEL,1,0), new ItemStack[] { new ItemStack(Items.DIAMOND,1,0), new ItemStack(Items.STICK,2,0), }));
		/** sword */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.WOODEN_SWORD,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,0), new ItemStack(Items.STICK,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.STONE_SWORD,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,2,0), new ItemStack(Items.STICK,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_SWORD,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,2,0), new ItemStack(Items.STICK,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_SWORD,1,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,2,0), new ItemStack(Items.STICK,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_SWORD,1,0), new ItemStack[] { new ItemStack(Items.DIAMOND,2,0), new ItemStack(Items.STICK,1,0), }));
		/** hoe */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.WOODEN_HOE,1,0), new ItemStack[] { new ItemStack(Blocks.PLANKS,2,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.STONE_HOE,1,0), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE,2,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_HOE,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,2,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_HOE,1,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,2,0), new ItemStack(Items.STICK,2,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_HOE,1,0), new ItemStack[] { new ItemStack(Items.DIAMOND,2,0), new ItemStack(Items.STICK,2,0), }));
		/** bow */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.BOW,1,0), new ItemStack[] {	new ItemStack(Items.STRING,3,0), new ItemStack(Items.STICK,3,0), }));
		/** arrow */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.ARROW,1,0), new ItemStack[] { new ItemStack(Items.FEATHER,1,0), new ItemStack(Items.STICK,1,0),	new ItemStack(Items.FLINT,1,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.SPECTRAL_ARROW,2,0), new ItemStack[] { new ItemStack(Items.GLOWSTONE_DUST,4,0), new ItemStack(Items.ARROW,1,0),	}));
		/** armor */
		recipes.add(new RecyclingRecipe(new ItemStack(Items.LEATHER_BOOTS,1,0), new ItemStack[] { new ItemStack(Items.LEATHER,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.LEATHER_HELMET,1,0), new ItemStack[] { new ItemStack(Items.LEATHER,5,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.LEATHER_CHESTPLATE,1,0), new ItemStack[] { new ItemStack(Items.LEATHER,8,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.LEATHER_LEGGINGS,1,0), new ItemStack[] { new ItemStack(Items.LEATHER,7,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_BOOTS,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_HELMET,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,5,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_CHESTPLATE,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,8,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_LEGGINGS,1,0), new ItemStack[] { new ItemStack(Items.IRON_INGOT,7,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_BOOTS,1,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_HELMET,1,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,5,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_CHESTPLATE,1,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,8,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_LEGGINGS,1,0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT,7,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_BOOTS,1,0), new ItemStack[] { new ItemStack(Items.DIAMOND,4,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_HELMET,1,0), new ItemStack[] { new ItemStack(Items.DIAMOND,5,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_CHESTPLATE,1,0), new ItemStack[] { new ItemStack(Items.DIAMOND,8,0), }));
		recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_LEGGINGS,1,0), new ItemStack[] { new ItemStack(Items.DIAMOND,7,0), }));
	}
	
}
