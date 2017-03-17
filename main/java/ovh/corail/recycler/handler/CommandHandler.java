package ovh.corail.recycler.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.JsonRecyclingRecipe;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.core.RecyclingRecipe;

public class CommandHandler implements ICommand {
	private final List<String> aliases = new ArrayList<String>();
	private final List<String> commands = new ArrayList<String>();

	public CommandHandler() {
		aliases.add("recycler");
		aliases.add("corail");
		commands.add("exportRecyclingRecipes");
		commands.add("exportCraftingRecipes");
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
		return "recycler ExportRecyclingRecipe|exportCraftingRecipes|addRecipe|removeRecipe\n" + 
		"exportRecyclingRecipes   - create a json file in the config directory with the list of all items that are allowed to recycle\n" +
		"exportCraftingRecipes    - create a json file in the config directory with the list of all reverse crafting recipes" + 
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
		if (world.isRemote) {
			Helper.sendMessage("message.command.denyClient", (EntityPlayer) sender, true);
			return;
		}
		if (args.length != 1) {
			Helper.sendMessage("message.command.invalidArgument", (EntityPlayer) sender, true);
			return;
		}
		if (args[0].equals("exportRecyclingRecipes")) {
			processExportRecyclingRecipes(world, sender);
		} else if (args[0].equals("exportCraftingRecipes")) {
			processExportCraftingRecipes(world, sender);	
		} else if (args[0].equals("addRecipe")) {
			processAddRecipe(world, sender);
		} else if (args[0].equals("removeRecipe")) {
			processRemoveRecipe(world, sender);
		} else {
			Helper.sendMessage("message.command.invalidArgument", (EntityPlayer) sender, true);
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
	
	private void processExportCraftingRecipes(World world, ICommandSender sender) {
		CraftingManager cm = CraftingManager.getInstance();
		RecyclingManager rm = RecyclingManager.getInstance();
		List<IRecipe> craftingList = cm.getRecipeList();
		List<JsonRecyclingRecipe> list = new ArrayList<JsonRecyclingRecipe>();
		for (int i = 0 ; i < craftingList.size() ; i++) {
			/* only recipes not in the recycler */
			if (!craftingList.get(i).getRecipeOutput().isEmpty() && rm.hasRecipe(craftingList.get(i).getRecipeOutput()) == -1) {
				list.add(rm.convertRecipeToJson(rm.convertCraftingRecipe(craftingList.get(i))));
			}

		}
		File exportFile = new File(ConfigurationHandler.getConfigDir(), "export_crafting_recipes.json");
		EntityPlayer player = (EntityPlayer) sender;
		Helper.sendMessage(list.size() + " " + Helper.getTranslation("message.command.recipesFound"), player, false);
		boolean success = rm.saveAsJson(exportFile, list);
		if (success) {
			Helper.sendMessage("message.command.exportSucceeded", player, true);
		} else {
			Helper.sendMessage("message.command.exportFailed", player, true);
		}
	}
	
	private void processExportRecyclingRecipes(World world, ICommandSender sender) {
		RecyclingManager rm = RecyclingManager.getInstance();
		RecyclingRecipe cr;
		List<JsonRecyclingRecipe> list = new ArrayList<JsonRecyclingRecipe>();
		for (int i = 0 ; i < rm.getRecipesCount() ; i++) {
			cr = rm.getRecipe(i);
			if (!cr.isAllowed() || (cr.isUnbalanced() && !ConfigurationHandler.unbalancedRecipes)) { continue; }
			list.add(rm.convertRecipeToJson(cr));
		}
		File exportFile = new File(ConfigurationHandler.getConfigDir(), "export_recycling_recipes.json");
		EntityPlayer player = (EntityPlayer) sender;
		Helper.sendMessage(list.size() + " " + Helper.getTranslation("message.command.recipesFound"), player, false);
		boolean success = rm.saveAsJson(exportFile, list);
		if (success) {
			Helper.sendMessage("message.command.exportSucceeded", player, true);
		} else {
			Helper.sendMessage("message.command.exportFailed", player, true);
		}
	}
	
	private void processAddRecipe(World world, ICommandSender sender) {
		EntityPlayer player = (EntityPlayer) sender;
		RecyclingManager rm = RecyclingManager.getInstance();
		if (player != null && player.getActiveItemStack() != null) {
			ItemStack stack = player.getActiveItemStack();
			int hasRecipe = Helper.indexOfList(stack, rm.recipes);
			/** Recipe already in recycler */
			if (hasRecipe > -1) {
				/** isn't blacklist */
				if (rm.getRecipe(hasRecipe).isAllowed()) {
					Helper.sendMessage("message.command.addRecipeFailed", player, true);
				} else {
					rm.getRecipe(hasRecipe).setAllowed(true);
					rm.saveBlacklist();
					Helper.sendMessage("message.command.addRecipeSuceeded", player, true);
				}
			} else {
				/** new recipe added */
				List<IRecipe> craftingList = CraftingManager.getInstance().getRecipeList();
				for (int i = 0 ; i < craftingList.size() ; i++) {
					if (craftingList.get(i).getRecipeOutput().isItemEqual(stack)) {
						RecyclingRecipe recipe = rm.convertCraftingRecipe(craftingList.get(i));
						rm.addRecipe(recipe);
						break;
					}
				}
				/** save user defined recipes to json */
				if (rm.saveUserDefinedRecipes()) {
					Helper.sendMessage("message.command.addRecipeSuceeded", player, true);
				} else {
					Helper.sendMessage("message.command.addRecipeFailed", player, true);
				}
			}
		}
	}
	
	private void processRemoveRecipe(World world, ICommandSender sender) {
		EntityPlayer player = (EntityPlayer) sender;
		if (player != null && player.getHeldItemMainhand() != null) {
			boolean success = RecyclingManager.getInstance().removeRecipe(player.getHeldItemMainhand());
			if (success) {
				Helper.sendMessage("message.command.removeRecipeSucceeded", player, true);
			} else {
				Helper.sendMessage("message.command.removeRecipeFailed", player, true);
			}
		}
	}

}
