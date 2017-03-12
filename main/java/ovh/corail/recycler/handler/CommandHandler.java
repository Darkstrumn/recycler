package ovh.corail.recycler.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.core.RecyclingRecipe;

public class CommandHandler implements ICommand {
	private final List<String> aliases = new ArrayList<String>();
	private final List<String> commands = new ArrayList<String>();

	public CommandHandler() {
		aliases.add("recycler");
		aliases.add("corail");
		commands.add("exportRecipes");
		commands.add("addRecipe");
		commands.add("removeRecipe");
	}
	
	@Override
	public int compareTo(ICommand arg0) {
		return 0;
	}

	@Override
	public String getName() {
		return aliases.get(0);
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO translate
		return "recycler exportRecipes|addRecipe|removeRecipe\n" + 
		"exportRecipes   - create a json file in the config directory with the list of all items that are allowed to recycle\n" + 
		"addRecipe    - add the recycling recipe of the crafting result of the item hold in main hand\n" + 
		"removeRecipe - remove the recycling recipe of the item hold in main hand";
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();
		//TODO translate
		if (world.isRemote) {
			System.out.println("Not processing on Client side");
			return;
		}
		if (args.length != 1) {
			Helper.sendMessage("Invalid argument", (EntityPlayer) sender, false);
			return;
		}
		if (args[0].equals("exportRecipes")) {
			processExportRecipes(world, sender);
		} else if (args[0].equals("addRecipe")) {
			processAddRecipe(world, sender);
		} else if (args[0].equals("removeRecipe")) {
			processRemoveRecipe(world, sender);
		} else {
			Helper.sendMessage("Invalid argument", (EntityPlayer) sender, false);
			return;
		}
		
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if (!ConfigurationHandler.allowCommand) {
			return false;
		}
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		return commands;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
	
	private void processExportRecipes(World world, ICommandSender sender) {
		RecyclingManager rm = RecyclingManager.getInstance();
		RecyclingRecipe cr;
		List<String> list = new ArrayList<String>();
		for (int i = 0 ; i < rm.getRecipesCount() ; i++) {
			cr = rm.getRecipe(i);
			if (!cr.isAllowed() || (cr.isUnbalanced() && !ConfigurationHandler.unbalancedRecipes)) { continue; }
			list.add(cr.getItemRecipe().getItem().getRegistryName() + ":" + cr.getMeta() + ":" + (cr.isUserDefined()?"User Defined":"Default"));
		}
		EntityPlayer player = (EntityPlayer) sender;
		boolean success = rm.saveAsJson(new File(ConfigurationHandler.getConfigDir(), "export_recipes.json"), list);
		if (player != null) {
			//TODO translate
			if (success) {
				Helper.sendMessage("Exportation réussie", player, false);
			} else {
				Helper.sendMessage("Impossible d'exporter les données", player, false);
			}
		}
	}
	
	private void processAddRecipe(World world, ICommandSender sender) {
		EntityPlayer player = (EntityPlayer) sender;
		if (player != null && player.getActiveItemStack() != null) {
			ItemStack stack = player.getActiveItemStack();
			List<IRecipe> craftingList = CraftingManager.getInstance().getRecipeList();
			for (int i = 0 ; i < craftingList.size() ; i++) {
				if (craftingList.get(i).getRecipeOutput().isItemEqual(stack)) {
					RecyclingRecipe recipe = new RecyclingRecipe(stack);
					if (craftingList.get(i) instanceof ShapedRecipes) {
						ShapedRecipes craftingRecipe = (ShapedRecipes) craftingList.get(i);
						for (int j = 0; j < craftingRecipe.recipeItems.length; j++) {
							if (!craftingRecipe.recipeItems[j].isEmpty()) {
								recipe.addStack(craftingRecipe.recipeItems[j]);
							}
						}
						recipe.setCanBeRepaired(stack.getItem().isRepairable());
						recipe.setUnbalanced(false);
						RecyclingManager.getInstance().addRecipe(recipe);
						break;
					} else if (craftingList.get(i) instanceof ShapelessRecipes) {
						ShapelessRecipes craftingRecipe = (ShapelessRecipes) craftingList.get(i);
						for (int j = 0; j < craftingRecipe.recipeItems.size(); j++) {
							if (!craftingRecipe.recipeItems.get(j).isEmpty()) {
								recipe.addStack(craftingRecipe.recipeItems.get(j));
							}
						}
						recipe.setCanBeRepaired(stack.getItem().isRepairable());
						recipe.setUnbalanced(false);
						RecyclingManager.getInstance().addRecipe(recipe);
						break;
					} else if (craftingList.get(i)  instanceof ShapedOreRecipe) {
						ShapedOreRecipe craftingRecipe = (ShapedOreRecipe) craftingList.get(i);
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
						recipe.setCanBeRepaired(stack.getItem().isRepairable());
						recipe.setUnbalanced(false);
						RecyclingManager.getInstance().addRecipe(recipe);
						break;
					} else if (craftingList.get(i)  instanceof ShapelessOreRecipe) {
						ShapelessOreRecipe craftingRecipe = (ShapelessOreRecipe) craftingList.get(i);
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
						recipe.setCanBeRepaired(stack.getItem().isRepairable());
						recipe.setUnbalanced(false);
						RecyclingManager.getInstance().addRecipe(recipe);
						break;
					}
				}
			}
			// TODO add to json file
		}
	}
	
	private void processRemoveRecipe(World world, ICommandSender sender) {
		EntityPlayer player = (EntityPlayer) sender;
		if (player != null && player.getHeldItemMainhand() != null) {
			boolean success = RecyclingManager.getInstance().removeRecipe(player.getHeldItemMainhand());
			if (player != null) {
				//TODO translate
				if (success) {
					Helper.sendMessage("Recette supprimée", player, false);
				} else {
					Helper.sendMessage("Impossible de suprimer la recette", player, false);
				}
			}
		}
	}

}
