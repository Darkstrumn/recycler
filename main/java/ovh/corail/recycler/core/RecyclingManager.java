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
	private List<RecyclingRecipe> recipes = Lists.<RecyclingRecipe> newArrayList();
	private List<ItemStack> unbalanced = new ArrayList<ItemStack>();
	private List<ItemStack> blacklist = new ArrayList<ItemStack>();
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
		loadJsonRecipes(false);
		/** load json user defined recycling recipes */
		loadJsonRecipes(true);
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
	
	private boolean isUnbalanced(ItemStack stack) {
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
	
	private boolean isBlacklist(ItemStack stack) {
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

	/** TODO code not used
	public void addRecipe(ItemStack stack, Object... recipeComponents) {
		addRecipe(stack, false, recipeComponents);
	}
	
	public void addRecipe(ItemStack stackIn, ItemStack stackOut) {
		addRecipe(stackIn, false, stackOut);
	}
		
	public List<ItemStack> getResultList(List<ItemStack> itemsList) {
		List<ItemStack> newItemsList = new ArrayList<ItemStack>();
		for (int i = 0; i < itemsList.size(); i++) {
			ItemStack currentStack = itemsList.get(i);
			int numRecipe = hasRecipe(currentStack);
			if (numRecipe < 0) {
				newItemsList.add(itemsList.get(i).copy());
				continue;
			}
			// Calcul du résultat
			RecyclingRecipe currentRecipe = recipes.get(numRecipe);
			if (currentStack.getCount() < currentRecipe.getItemRecipe().getCount()) {
				newItemsList.add(currentStack.copy());
				continue;
			}
			// code en trop
			int nb_input = (int) Math.floor(currentStack.getCount() / currentRecipe.getItemRecipe().getCount());
			List<ItemStack> itemsList2 = getResultStack(currentStack, nb_input);
			for (int j = 0; j < itemsList2.size(); j++) {
				if (!itemsList2.get(j).isEmpty()) {
					newItemsList.add(itemsList2.get(j).copy());
				}
			}
		}
		return newItemsList;
	}*/

	private void addRecipe(ItemStack stack, Object... recipeComponents) {
		recipes.add(new RecyclingRecipe(stack, recipeComponents));
	}

	private void addRecipe(ItemStack stackIn, ItemStack stackOut) {
		recipes.add(new RecyclingRecipe(stackIn, stackOut));
	}
	
	public boolean removeRecipe(ItemStack stack) {
		if (stack.isEmpty()) { return false; }
		for (int i = 0 ; i < recipes.size() ; i++) {
			if (stack.isItemEqual(recipes.get(i).getItemRecipe())) {
				if (recipes.get(i).isUserDefined()) {
					recipes.remove(i);
					saveUserDefinedRecipes();
					return true;
				} else {
					recipes.get(i).setAllowed(false);
					saveBlacklist();
					return true;
				}
			}
		}
		return false;
	}

	public int hasRecipe(ItemStack stack) {
		if (stack.isEmpty() || stack.getCount() <= 0) {
			return -1;
		}
		// don't allow binding cursed items
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
		// For damaged items
		ItemStack testStack = stack.copy();
		if (testStack.getItem().isRepairable()) {
			testStack.setItemDamage(0);
		}
		for (int recipe_num = 0; recipe_num < getRecipesCount(); recipe_num++) {
			if (testStack.isItemEqual(recipes.get(recipe_num).getItemRecipe())) {
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
		}
		return -1;
	}

	public List<ItemStack> getResultStack(ItemStack stack, int nb_input) {
		List<ItemStack> itemsList = new ArrayList<ItemStack>();
		int num_recipe = hasRecipe(stack);
		if (num_recipe < 0) {
			return itemsList;
		}
		/* check enchants */
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

		/* Calcul du résultat du stack */
		for (int i = 0; i < currentRecipe.getCount(); i++) {
			/* Pour chaque stack résultat de la recette */
			ItemStack currentStack = currentRecipe.getStack(i);
			int newStackCount = currentStack.getCount() * nb_input;
			/* Objets abimés */
			if (currentRecipe.canBeRepaired() && stack.getItemDamage() > 0) {
				int currentSize = currentStack.getCount();
				/* Unités plus petites */
				if (currentStack.getItem()==Items.IRON_INGOT) {
					currentStack = new ItemStack(Items.field_191525_da, currentSize * 9, 0); // Iron Nugget
					newStackCount = currentStack.getCount() * nb_input;
				}
				if (currentStack.getItem()==Items.GOLD_INGOT) {
					currentStack = new ItemStack(Items.GOLD_NUGGET, currentSize * 9, 0);
					newStackCount = currentStack.getCount() * nb_input;
				}
				if (currentStack.getItem()==Items.DIAMOND) {
					currentStack = new ItemStack(Main.diamond_fragment, currentSize * 9, 0);
					newStackCount = currentStack.getCount() * nb_input;
				}
				if (currentStack.getItem()==Items.LEATHER) {
					currentStack = new ItemStack(Items.RABBIT_HIDE, currentSize * 4, 0);
					newStackCount = currentStack.getCount() * nb_input;
				}
				if (currentStack.getItem()==Item.getItemFromBlock(Blocks.PLANKS)) {
					currentStack = new ItemStack(Items.STICK, currentSize * 2, 0);
					newStackCount = currentStack.getCount() * nb_input;
				}
				int maxDamage = currentRecipe.getItemRecipe().getMaxDamage();
				float pourcent = (float) (maxDamage - (stack.getItemDamage())) / maxDamage;
				newStackCount = (int) Math.floor(newStackCount * pourcent);
			}
			int slotCount = (int) Math.floor(newStackCount / currentStack.getMaxStackSize());
			ItemStack fullStack;
			/* Ajout des full stacks */
			for (int j = 0; j < slotCount; j++) {
				fullStack = currentStack.copy();
				fullStack.setCount(fullStack.getMaxStackSize());
				itemsList.add(fullStack);
			}
			/* Reste de stack */
			int resteStackCount = newStackCount - (slotCount * currentStack.getMaxStackSize());
			if (resteStackCount > 0) {
				fullStack = currentStack.copy();
				fullStack.setCount(resteStackCount);
				itemsList.add(fullStack);
			}

		}
		return itemsList;
	}
	
	private List<JsonRecyclingRecipe> getJsonRecyclingRecipes() {
		List<JsonRecyclingRecipe> jsonRecipesList = new ArrayList<JsonRecyclingRecipe>();
		/** unbalanced recipe */
		/* granite */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone:1:1", new String[] { "minecraft:stone:1:3", "minecraft:quartz:1:0", }));
		/* diorite */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone:1:3", new String[] { "minecraft:cobblestone:1:0", "minecraft:quartz:1:0", }));
		/* andesite */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone:2:5", new String[] { "minecraft:cobblestone:1:0", "minecraft:stone:1:3", }));
		/* paper */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:paper:1:0", new String[] { "minecraft:reeds:1:0", }));
		/* sugar */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:sugar:1:0", new String[] { "minecraft:reeds:1:0", }));
		/* ender eye */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:ender_eye:1:0", new String[] { "minecraft:ender_pearl:1:0", "minecraft:blaze_powder:1:0", }));
		/* blaze powder */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:blaze_powder:2:0", new String[] { "minecraft:blaze_rod:1:0", }));
		/* magma cream */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:magma_cream:1:0", new String[] { "minecraft:blaze_powder:1:0", "minecraft:slime_ball:1:0", }));
		/* fire charge */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:fire_charge:3:0", new String[] { "minecraft:blaze_powder:1:0", "minecraft:gunpowder:1:0", "minecraft:coal:1:0", }));
			
		/** 1.9 recipes */
		/* purpur slab */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:purpur_slab:2:0", new String[] { "minecraft:purpur_block:1:0", }));
		/* end stone brick */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:end_bricks:1:0", new String[] { "minecraft:end_stone:1:0", }));		
		/* purpur stair */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:purpur_stairs:1:0", new String[] { "minecraft:purpur_slab:3:0", }));
		/* purpur block */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:purpur_block:1:0", new String[] { "minecraft:chorus_fruit_popped:1:0", }));		
		/* sculpted purpur */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:purpur_pillar:1:0", new String[] { "minecraft:purpur_block:1:0", }));
		/* end rod */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:end_rod:4:0", new String[] { "minecraft:blaze_rod:1:0", "minecraft:chorus_fruit_popped:1:0" }));		
		/* new boat */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:spruce_boat:1:0", new String[] { "minecraft:planks:5:1" }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:birch_boat:1:0", new String[] { "minecraft:planks:5:2" }));	
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:jungle_boat:1:0", new String[] { "minecraft:planks:5:3" }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:acacia_boat:1:0", new String[] { "minecraft:planks:5:4" }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:dark_oak_boat:1:0", new String[] { "minecraft:planks:5:5" }));
		/* shield */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:shield:1:0", new String[] { "minecraft:iron_ingot:1:0", "minecraft:planks:6:0", }));
		/** block */
		/* stone */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone:1:0", new String[] { "minecraft:cobblestone:1:0", }));
		/* polished granite, diorite, andesite */
		for (int i = 1; i <= 3; i++) {
			jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone:1:" + (2 * i),	new String[] { "minecraft:stone:1:" + (2 * i - 1), }));
		}
		/* coarse dirt */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:dirt:2:1", new String[] { "minecraft:dirt:1:0", "minecraft:gravel:1:0" }));
		/* clay */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:clay:1:0", new String[] { "minecraft:clay_ball:4:0", }));					
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:hardened_clay:1:0", new String[] { "minecraft:clay:1:0", }));								
		/* stained clay */
		for (int i=0;i<16;i++) {
			jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stained_hardened_clay:1:"+i, new String[] { "minecraft:clay:1:0", }));								
		}
		/* mossy cobblestone */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:mossy_cobblestone:1:0", new String[] { "minecraft:cobblestone:1:0", "minecraft:vine:1:0", }));
		/* glass */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:glass:1:0", new String[] { "minecraft:sand:1:0", }));		
		/* stained glass */
		for (int i=0;i<16;i++) {
			jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stained_glass:1:"+i, new String[] { "minecraft:glass:1:0", }));		
		}
		/* glass pane */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:glass_pane:8:0", new String[] { "minecraft:glass:3:0", }));		
		/* stained glass pane */
		for (int i=0;i<16;i++) {
			jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stained_glass_pane:8:"+i, new String[] { "minecraft:stained_glass:3:"+i, }));		
		}
		/* sandstone */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:sandstone:1:0", new String[] { "minecraft:sand:4:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:red_sandstone:1:0", new String[] { "minecraft:sand:4:1", }));
		/* chiseled smooth sandstone */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:sandstone:1:1", new String[] { "minecraft:sandstone:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:sandstone:1:2", new String[] { "minecraft:sandstone:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:red_sandstone:1:1", new String[] { "minecraft:red_sandstone:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:red_sandstone:1:2", new String[] { "minecraft:red_sandstone:1:0", }));	
		/* stonebrick */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stonebrick:1:0", new String[] { "minecraft:cobblestone:1:0", }));
		/* cracked, mossy, chiseled stonebrick */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stonebrick:1:1", new String[] { "minecraft:stonebrick:1:0", "minecraft:vine:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stonebrick:1:2", new String[] { "minecraft:stonebrick:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stonebrick:1:3", new String[] { "minecraft:stonebrick:1:0", }));
		/* bricks block */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:brick_block:1:0", new String[] { "minecraft:brick:4:0", }));		
		/* glowstone */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:glowstone:1:0", new String[] { "minecraft:glowstone_dust:4:0", }));				
		/* prismarine */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:prismarine:1:0", new String[] { "minecraft:prismarine_shard:4:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:prismarine:1:1", new String[] { "minecraft:prismarine_shard:9:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:prismarine:1:2", new String[] { "minecraft:prismarine_shard:8:0","minecraft:dye:1:0", }));
		/* sea lantern */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:sea_lantern:1:0", new String[] { "minecraft:prismarine_crystals:5:0","minecraft:prismarine_shard:4:0",}));
		/* quartz block */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:quartz_block:1:0", new String[] { "minecraft:quartz:4:0", }));		
		/* chiseled pillar quartz */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:quartz_block:1:1", new String[] { "minecraft:quartz_block:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:quartz_block:1:2", new String[] { "minecraft:quartz_block:1:0", }));
		/* planks */
		for (int i = 0; i < 4; i++) {
			jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:planks:4:"+i, new String[] { 
					"minecraft:log:1:"+i,
			}));
		}
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:planks:4:4", new String[] { 
				"minecraft:log2:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:planks:4:5", new String[] { 
				"minecraft:log2:1:1",
		}));
		/* brick */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:brick:1:0", new String[] { 
				"minecraft:clay_ball:1:0",
		}));
		/* snow */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:snow:1:0", new String[] { "minecraft:snowball:4:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:snow_layer:2:0", new String[] { "minecraft:snow:1:0", }));
		/** machine */
		/* dispenser */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:dispenser:1:0", new String[] { 
				"minecraft:cobblestone:7:0",
				"minecraft:redstone:1:0",
				"minecraft:bow:1:0",
		}));
		/* noteblock */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:noteblock:1:0", new String[] { 
				"minecraft:planks:8:0",
				"minecraft:redstone:1:0",
		}));
		/* chest */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:chest:1:0", new String[] { 
				"minecraft:planks:8:0",
		}));
		/* ender chest */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:ender_chest:1:0", new String[] { 
				"minecraft:obsidian:8:0",
				"minecraft:ender_eye:1:0",
		}));
		/* chest */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:trapped_chest:1:0", new String[] { 
				"minecraft:chest:1:0",
				"minecraft:tripwire_hook:1:0",
		}));
		/* crafting table */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:crafting_table:1:0", new String[] { 
				"minecraft:planks:4:0",
		}));
		/* furnace */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:furnace:1:0", new String[] { 
				"minecraft:cobblestone:8:0",
		}));
		/* jukebox */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:jukebox:1:0", new String[] { 
				"minecraft:planks:8:0",
				"minecraft:diamond:1:0",
		}));
		/* enchantment table */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:enchanting_table:1:0", new String[] { 
				"minecraft:diamond:2:0",
				"minecraft:obsidian:4:0",
				"minecraft:book:1:0",
		}));
		/* ender chest */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:chest:1:0", new String[] { 
				"minecraft:obsidian:8:0",
				"minecraft:ender_eye:1:0",
		}));
		/* beacon */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:beacon:1:0", new String[] { 
				"minecraft:obsidian:3:0",
				"minecraft:nether_star:1:0",
				"minecraft:glass:5:0",
		}));
		/* anvil */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:anvil:1:0", new String[] { 
				"minecraft:iron_ingot:31:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:anvil:1:1", new String[] { 
				"minecraft:iron_ingot:20:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:anvil:1:2", new String[] { 
				"minecraft:iron_ingot:10:0",
		}));
		/* daylight sensor */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:daylight_detector:1:0", new String[] { 
				"minecraft:wooden_slab:3:0",
				"minecraft:glass:3:0",
				"minecraft:quartz:3:0",
		}));
		/* hopper */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:hopper:1:0", new String[] { 
				"minecraft:iron_ingot:5:0",
				"minecraft:chest:1:0",
		}));
		/* dropper */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:dropper:1:0", new String[] { 
				"minecraft:cobblestone:7:0",
				"minecraft:redstone:1:0",
		}));
		/** rail */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:rail:16:0", new String[] {
				"minecraft:iron_ingot:6:0",
				"minecraft:stick:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:detector_rail:6:0", new String[] {
				"minecraft:iron_ingot:6:0",
				"minecraft:redstone:1:0",
				"minecraft:stone_pressure_plate:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:golden_rail:6:0", new String[] {
				"minecraft:gold_ingot:6:0",
				"minecraft:redstone:1:0",
				"minecraft:stick:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:activator_rail:6:0", new String[] {
				"minecraft:iron_ingot:6:0",
				"minecraft:redstone:1:0",
				"minecraft:stick:2:0",
		}));
		/** piston */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:sticky_piston:1:0", new String[] {
				"minecraft:slime_ball:1:0",
				"minecraft:piston:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:piston:1:0", new String[] {
				"minecraft:iron_ingot:1:0",
				"minecraft:cobblestone:4:0",
				"minecraft:redstone:1:0",
				"minecraft:planks:3:0",
		}));
		/** wool */
		for (int i = 0; i < 16; i++) {
			jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:wool:1:"+i, new String[] { 
					"minecraft:string:4:0",
			}));
		}
		/** slab */
		for (int i = 0; i <= 5; i++) {
			jsonRecipesList
					.add(new JsonRecyclingRecipe("minecraft:wooden_slab:2:" + i, new String[] { "minecraft:planks:1:" + i, }));
		}
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_slab:2:0", new String[] { "minecraft:stone:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_slab:2:1", new String[] { "minecraft:sandstone:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_slab:2:3", new String[] { "minecraft:cobblestone:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_slab:2:4", new String[] { "minecraft:brick_block:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_slab:2:5", new String[] { "minecraft:stonebrick:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_slab:2:6", new String[] { "minecraft:nether_brick:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_slab:2:7", new String[] { "minecraft:quartz_block:1:0", }));
		jsonRecipesList
				.add(new JsonRecyclingRecipe("minecraft:stone_slab2:2:0", new String[] { "minecraft:red_sandstone:1:0", }));
		/** TNT */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:tnt:1:0", new String[] { 
				"minecraft:sand:4:0",
				"minecraft:gunpowder:5:0",
		}));
		/** bookshelf */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:bookshelf:1:0", new String[] { 
				"minecraft:planks:6:0",
				"minecraft:book:3:0",
		}));
		/** torch */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:torch:4:0", new String[] { 
				"minecraft:coal:1:0",
				"minecraft:stick:1:0",
		}));
		/** stair */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:oak_stairs:1:0", new String[] { "minecraft:wooden_slab:3:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:spruce_stairs:1:0", new String[] { "minecraft:wooden_slab:3:1", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:birch_stairs:1:0", new String[] { "minecraft:wooden_slab:3:2", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:jungle_stairs:1:0", new String[] { "minecraft:wooden_slab:3:3", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:acacia_stairs:1:0", new String[] { "minecraft:wooden_slab:3:4", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:dark_oak_stairs:1:0", new String[] { "minecraft:wooden_slab:3:5", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:sandstone_stairs:1:0", new String[] { "minecraft:stone_slab:3:1", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_stairs:1:0", new String[] { "minecraft:stone_slab:3:3", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:brick_stairs:1:0", new String[] { "minecraft:stone_slab:3:4", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_brick_stairs:1:0", new String[] { "minecraft:stone_slab:3:5", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:nether_brick_stairs:1:0", new String[] { "minecraft:stone_slab:3:6", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:quartz_stairs:1:0", new String[] { "minecraft:stone_slab:3:7", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:red_sandstone_stairs:1:0", new String[] { "minecraft:stone_slab2:3:0", }));
		/** ladder */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:ladder:3:0", new String[] { "minecraft:stick:7:0", }));
		/** lever */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:lever:1:0", new String[] { "minecraft:stick:1:0", "minecraft:cobblestone:1:0", }));
		/** pressure plate */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:wooden_pressure_plate:1:0", new String[] { 
				"minecraft:planks:2:0", 
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_pressure_plate:1:0", new String[] { 
				"minecraft:stone:2:0", 
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:heavy_weighted_pressure_plate:1:0", new String[] { 
				"minecraft:iron_ingot:2:0", 
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:light_weighted_pressure_plate:1:0", new String[] { 
				"minecraft:gold_ingot:2:0", 
		}));
		/** redstone torch */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:redstone_torch:1:0", new String[] { 
				"minecraft:redstone:1:0",
				"minecraft:stick:1:0",
		}));
		/** button */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:wooden_button:1:0", new String[] { 
				"minecraft:planks:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_button:1:0", new String[] { 
				"minecraft:stone:1:0",
		}));
		/** fence */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:fence:3:0", new String[] { 
				"minecraft:planks:4:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:spruce_fence:3:0", new String[] { 
				"minecraft:planks:4:1",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:birch_fence:3:0", new String[] { 
				"minecraft:planks:4:2",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:jungle_fence:3:0", new String[] { 
				"minecraft:planks:4:3",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:acacia_fence:3:0", new String[] { 
				"minecraft:planks:4:4",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:dark_oak_fence:3:0", new String[] { 
				"minecraft:planks:4:5",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:nether_brick_fence:1:0", new String[] { 
				"minecraft:nether_brick:1:0",
		}));
		/** fence gate */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:fence_gate:1:0", new String[] { 
				"minecraft:planks:2:0",
				"minecraft:stick:4:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:spruce_fence_gate:1:0", new String[] { 
				"minecraft:planks:2:1",
				"minecraft:stick:4:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:birch_fence_gate:1:0", new String[] { 
				"minecraft:planks:2:2",
				"minecraft:stick:4:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:jungle_fence_gate:1:0", new String[] { 
				"minecraft:planks:2:3",
				"minecraft:stick:4:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:acacia_fence_gate:1:0", new String[] { 
				"minecraft:planks:2:4",
				"minecraft:stick:4:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:dark_oak_fence_gate:1:0", new String[] { 
				"minecraft:planks:2:5",
				"minecraft:stick:4:0",
		}));
		/** lit pumpkin */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:lit_pumpkin:1:0", new String[] { 
				"minecraft:pumpkin:1:0",
				"minecraft:torch:1:0",
		}));
		/** trap */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:trapdoor:1:0", new String[] { 
				"minecraft:planks:3:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_trapdoor:1:0", new String[] { 
				"minecraft:iron_ingot:2:0",
		}));
		/** iron bar */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_bars:1:0", new String[] { 
				"minecraft:iron_nugget:3:0", 
		}));
		/** redstone lamp */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:redstone_lamp:1:0", new String[] { 
				"minecraft:redstone:4:0",
				"minecraft:glowstone:1:0",
		}));
		/** tripwire hook */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:tripwire_hook:2:0", new String[] {
				"minecraft:stick:1:0",
				"minecraft:planks:1:0",
				"minecraft:iron_ingot:1:0",
		}));
		/** cobblestone wall */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:cobblestone_wall:1:0", new String[] {
				"minecraft:cobblestone:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:cobblestone_wall:1:1", new String[] {
				"minecraft:mossy_cobblestone:1:0",
		}));
		/** carpet */
		for (int i = 0; i < 16; i++) {
			jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:carpet:3:"+i, new String[] { 
					"minecraft:wool:2:"+i,
			}));
		}
		/** flint and steel */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:flint_and_steel:1:0", new String[] { "minecraft:iron_ingot:1:0", "minecraft:flint:1:0" }));
		/** stick */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stick:2:0", new String[] { "minecraft:planks:1:0", }));
		/** bowl */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:bowl:4:0", new String[] { "minecraft:planks:3:0", }));

		/** door */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:wooden_door:1:0", new String[] { "minecraft:planks:2:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:spruce_door:1:0", new String[] { 
				"minecraft:planks:2:1", 
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:birch_door:1:0", new String[] { 
				"minecraft:planks:2:2", 
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:jungle_door:1:0", new String[] { 
				"minecraft:planks:2:3", 
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:acacia_door:1:0", new String[] { 
				"minecraft:planks:2:4", 
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:dark_oak_door:1:0", new String[] { 
				"minecraft:planks:2:5", 
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_door:1:0", new String[] { 
				"minecraft:iron_ingot:2:0", 
		}));
		/** painting */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:painting:1:0", new String[] { 
				"minecraft:stick:8:0",
				"minecraft:wool:1:0",
		}));
		/** sign */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:sign:3:0", new String[] { 
				"minecraft:planks:6:0",
				"minecraft:stick:1:0",
		}));
		/** empty bucket */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:bucket:1:0", new String[] { 
				"minecraft:iron_ingot:3:0",
		}));
		/** minecart */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:minecart:1:0", new String[] { "minecraft:iron_ingot:5:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:hopper_minecart:1:0", new String[] { "minecraft:minecart:1:0", "minecraft:hopper:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:tnt_minecart:1:0", new String[] { "minecraft:minecart:1:0", "minecraft:tnt:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:furnace_minecart:1:0", new String[] { "minecraft:minecart:1:0", "minecraft:furnace:1:0", }));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:chest_minecart:1:0", new String[] { "minecraft:minecart:1:0", "minecraft:chest:1:0", }));
		/** boat */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:boat:1:0", new String[] { "minecraft:planks:5:0", }));		
		/** book */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:book:1:0", new String[] { "minecraft:paper:3:0", "minecraft:leather:1:0" }));		
		/** compass */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:compass:1:0", new String[] { 
				"minecraft:redstone:1:0",
				"minecraft:iron_ingot:4:0",
		}));
		/** fishing rod */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:fishing_rod:1:0", new String[] { 
				"minecraft:string:2:0",
				"minecraft:stick:3:0",
		}));
		/** clock */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:clock:1:0", new String[] { 
				"minecraft:gold_ingot:4:0",
				"minecraft:redstone:1:0",
		}));
		/** bed */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:bed:1:0", new String[] { 
				"minecraft:wool:3:0",
				"minecraft:planks:3:0",
		}));
		/** redstone repeater */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:repeater:1:0", new String[] { 
				"minecraft:stone:3:0",
				"minecraft:redstone_torch:2:0",
				"minecraft:redstone:1:0",
		}));
		/** redstone comparator */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:comparator:1:0", new String[] { 
				"minecraft:stone:3:0",
				"minecraft:redstone_torch:3:0",
				"minecraft:quartz:1:0",
		}));
		/** glass bottle */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:glass_bottle:1:0", new String[] { 
				"minecraft:glass:1:0",
		}));
		/** brewing stand */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:brewing_stand:1:0", new String[] { 
				"minecraft:cobblestone:3:0",
				"minecraft:blaze_rod:1:0",
		}));
		/** cauldron */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:cauldron:1:0", new String[] { 
				"minecraft:iron_ingot:7:0",
		}));
		/** item frame */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:item_frame:1:0", new String[] { 
				"minecraft:stick:8:0",
				"minecraft:leather:1:0",
		}));
		/** flower pot */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:flower_pot:1:0", new String[] { 
				"minecraft:brick:3:0",
		}));
		/** carrot on a stick */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:carrot_on_a_stick:1:0", new String[] { 
				"minecraft:carrot:1:0",
				"minecraft:fishing_rod:1:0",
		}));
		/** armor stand */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:armor_stand:1:0", new String[] { 
				"minecraft:stick:6:0",
				"minecraft:stone_slab:1:0",
		}));
		/** lead */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:lead:2:0", new String[] { 
				"minecraft:string:4:0",
				"minecraft:slime_ball:1:0",
		}));
		/** banner */
		for (int i=0;i<16;i++) {
			jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:banner:1:"+i, new String[] {
				"minecraft:wool:6:0", "minecraft:stick:1:0",}));
		}
		/** end crystal */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:end_crystal:1:0", new String[] { 
				"minecraft:glass:7:0",
				"minecraft:ender_eye:1:0",
				"minecraft:ghast_tear:1:0",
		}));
		/** empty map */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:map:1:0", new String[] { 
				"minecraft:compass:1:0",
				"minecraft:paper:8:0",
		}));
		/** shears */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:shears:1:0", new String[] {
				"minecraft:iron_ingot:2:0",
		}));
		/** pickaxe */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:wooden_pickaxe:1:0", new String[] {
				"minecraft:planks:3:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_pickaxe:1:0", new String[] {
				"minecraft:cobblestone:3:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_pickaxe:1:0", new String[] {
				"minecraft:iron_ingot:3:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:golden_pickaxe:1:0", new String[] {
				"minecraft:gold_ingot:3:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:diamond_pickaxe:1:0", new String[] {
				"minecraft:diamond:3:0",
				"minecraft:stick:2:0",
		}));
		/** axe */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:wooden_axe:1:0", new String[] {
				"minecraft:planks:3:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_axe:1:0", new String[] {
				"minecraft:cobblestone:3:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_axe:1:0", new String[] {
				"minecraft:iron_ingot:3:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:golden_axe:1:0", new String[] {
				"minecraft:gold_ingot:3:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:diamond_axe:1:0", new String[] {
				"minecraft:diamond:3:0",
				"minecraft:stick:2:0",
		}));
		/** shovel/spade */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:wooden_shovel:1:0", new String[] {
				"minecraft:planks:1:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_shovel:1:0", new String[] {
				"minecraft:cobblestone:1:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_shovel:1:0", new String[] {
				"minecraft:iron_ingot:1:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:golden_shovel:1:0", new String[] {
				"minecraft:gold_ingot:1:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:diamond_shovel:1:0", new String[] {
				"minecraft:diamond:1:0",
				"minecraft:stick:2:0",
		}));
		/** sword */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:wooden_sword:1:0", new String[] {
				"minecraft:planks:2:0",
				"minecraft:stick:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_sword:1:0", new String[] {
				"minecraft:cobblestone:2:0",
				"minecraft:stick:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_sword:1:0", new String[] {
				"minecraft:iron_ingot:2:0",
				"minecraft:stick:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:golden_sword:1:0", new String[] {
				"minecraft:gold_ingot:2:0",
				"minecraft:stick:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:diamond_sword:1:0", new String[] {
				"minecraft:diamond:2:0",
				"minecraft:stick:1:0",
		}));
		/** hoe */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:wooden_hoe:1:0", new String[] {
				"minecraft:planks:2:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:stone_hoe:1:0", new String[] {
				"minecraft:cobblestone:2:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_hoe:1:0", new String[] {
				"minecraft:iron_ingot:2:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:golden_hoe:1:0", new String[] {
				"minecraft:gold_ingot:2:0",
				"minecraft:stick:2:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:diamond_hoe:1:0", new String[] {
				"minecraft:diamond:2:0",
				"minecraft:stick:2:0",
		}));
		/** bow */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:bow:1:0", new String[] {
				"minecraft:string:3:0",
				"minecraft:stick:3:0",
		}));
		/** arrow */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:arrow:1:0", new String[] {
				"minecraft:feather:1:0",
				"minecraft:stick:1:0",
				"minecraft:flint:1:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:spectral_arrow:2:0", new String[] {
				"minecraft:glowstone_dust:4:0",
				"minecraft:arrow:1:0",
		}));
		/** armor */
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:leather_boots:1:0", new String[] {
				"minecraft:leather:4:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:leather_helmet:1:0", new String[] {
				"minecraft:leather:5:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:leather_chestplate:1:0", new String[] {
				"minecraft:leather:8:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:leather_leggings:1:0", new String[] {
				"minecraft:leather:7:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_boots:1:0", new String[] {
				"minecraft:iron_ingot:4:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_helmet:1:0", new String[] {
				"minecraft:iron_ingot:5:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_chestplate:1:0", new String[] {
				"minecraft:iron_ingot:8:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:iron_leggings:1:0", new String[] {
				"minecraft:iron_ingot:7:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:golden_boots:1:0", new String[] {
				"minecraft:gold_ingot:4:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:golden_helmet:1:0", new String[] {
				"minecraft:gold_ingot:5:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:golden_chestplate:1:0", new String[] {
				"minecraft:gold_ingot:8:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:golden_leggings:1:0", new String[] {
				"minecraft:gold_ingot:7:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:diamond_boots:1:0", new String[] {
				"minecraft:diamond:4:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:diamond_helmet:1:0", new String[] {
				"minecraft:diamond:5:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:diamond_chestplate:1:0", new String[] {
				"minecraft:diamond:8:0",
		}));
		jsonRecipesList.add(new JsonRecyclingRecipe("minecraft:diamond_leggings:1:0", new String[] {
				"minecraft:diamond:7:0",
		}));
		return jsonRecipesList;
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
	
	private void loadJsonRecipes(boolean userDefined) {
		List<JsonRecyclingRecipe> jsonRecipesList;
		if (!userDefined) {
			jsonRecipesList = getJsonRecyclingRecipes();
		} else {
			if (!userDefinedFile.exists()) {
				jsonRecipesList = new ArrayList<JsonRecyclingRecipe>();
				jsonRecipesList.add(new JsonRecyclingRecipe(Main.MOD_ID+":recycler:1:0", new String[] {
					"minecraft:cobblestone:6:0",
					"minecraft:iron_ingot:3:0",
				}));
				saveAsJson(userDefinedFile, jsonRecipesList);
			} else {
				Type token = new TypeToken<List<JsonRecyclingRecipe>>() {}.getType();
				jsonRecipesList = (List<JsonRecyclingRecipe>) loadAsJson(userDefinedFile, token);
			}
		}

		for (int i = 0; i < jsonRecipesList.size(); i++) {
			RecyclingRecipe recipe = convertJsonRecipe(jsonRecipesList.get(i));
			if (recipe != null && recipe.getCount() > 0) {
				/** check for same existing recipe */
				int foundRecipe = -1;
				if (userDefined) {
					for (int numRecipe=0 ; numRecipe < recipes.size() ; numRecipe++) {
						if (recipes.get(numRecipe).getItemRecipe() == recipe.getItemRecipe()) {
							foundRecipe = numRecipe;
							break;
						}
					}
					recipe.setUserDefined(true);
				}
				recipe.setAllowed(!isBlacklist(recipe.getItemRecipe()));
				if (foundRecipe == -1) {
					recipes.add(recipe);
				} else {
					recipes.set(foundRecipe, recipe);
				}
			} else {
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
			//Item item = (Item) GameRegistry.findItem(parts[0], parts[1]);
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
	
}
