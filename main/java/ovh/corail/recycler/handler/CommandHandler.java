package ovh.corail.recycler.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.WorldServer;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.JsonRecyclingRecipe;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.core.RecyclingRecipe;

public class CommandHandler extends CommandBase {
	private final String name = "recycler";
	private final List<String> commands = new ArrayList<String>();

	public CommandHandler() {
		commands.add("exportCraftingRecipes");
		commands.add("addRecipe");
		commands.add("removeRecipe");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO translate
		return "recycler <command>\n" + 
		"exportCraftingRecipes - save the list of all crafting recipes in \"recycling\" format in the config directory" + 
		"addRecipe - add the recycling recipe of the crafting result of the item hold in main hand\n" + 
		"removeRecipe - remove the recycling recipe of the item hold in main hand";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!(sender instanceof EntityPlayerMP)) {
			throw new WrongUsageException("message.command.onlyInGame");
		}
		EntityPlayerMP player = (EntityPlayerMP) sender;
		WorldServer world = player.getServerWorld();
		if (args.length != 1) {
			throw new WrongUsageException(getUsage(sender));
		}
		if (args[0].equals("exportCraftingRecipes")) {
			processExportCraftingRecipes(world, player);	
		} else if (args[0].equals("addRecipe")) {
			processAddRecipe(world, player);
		} else if (args[0].equals("removeRecipe")) {
			processRemoveRecipe(world, player);
		} else {
			throw new WrongUsageException(getUsage(sender));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		return args.length == 1 ? commands : Collections.<String>emptyList();
	}
	
	private void processExportCraftingRecipes(WorldServer world, EntityPlayerMP player) {
		try {
			RecyclingManager rm = RecyclingManager.getInstance();
			RegistryNamespaced<ResourceLocation, IRecipe> craftingList = CraftingManager.REGISTRY;
			List<JsonRecyclingRecipe> list = new ArrayList<JsonRecyclingRecipe>();
			Iterator<IRecipe> it = craftingList.iterator();
			while (it.hasNext()) {
				/* only recipes not in the recycler */
				IRecipe crafting_recipe = (IRecipe) it.next();
				if (!crafting_recipe.getRecipeOutput().isEmpty() && rm.hasRecipe(crafting_recipe.getRecipeOutput()) == -1) {
					list.add(rm.convertRecipeToJson(rm.convertCraftingRecipe(crafting_recipe)));
				}
	
			}
			File exportFile = new File(ConfigurationHandler.getConfigDir(), "export_crafting_recipes.json");
			Helper.sendMessage(list.size() + " " + Helper.getTranslation("message.command.recipesFound"), player, false);
			boolean success = rm.saveAsJson(exportFile, list);
			if (success) {
				Helper.sendMessage("message.command.exportSucceeded", player, true);
			} else {
				Helper.sendMessage("message.command.exportFailed", player, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processAddRecipe(WorldServer world, EntityPlayerMP player) {
		try {
			RecyclingManager rm = RecyclingManager.getInstance();
			if (!player.getHeldItemMainhand().isEmpty()) {
				ItemStack stack = player.getHeldItemMainhand();
				int hasRecipe = Helper.indexOfList(stack, rm.recipes);
				/** Recipe already in recycler */
				if (hasRecipe > -1) {
					/** isn't blacklist */
					if (rm.getRecipe(hasRecipe).isAllowed()) {
						Helper.sendMessage("message.command.addRecipeFailed", player, true);
					} else {
						rm.getRecipe(hasRecipe).setAllowed(true);
						rm.saveBlacklist();
						Helper.sendMessage("message.command.addRecipeSucceeded", player, true);
					}
				} else {
					/** new recipe added */
					boolean valid = false;
					RecyclingRecipe recipe = null;
					RegistryNamespaced<ResourceLocation, IRecipe> craftingList = CraftingManager.REGISTRY;
					Iterator<IRecipe> it = craftingList.iterator();
					while (it.hasNext()) {
						IRecipe crafting_recipe = (IRecipe) it.next();
						ItemStack o = crafting_recipe.getRecipeOutput();
						/** TODO damaged items ! */
						if (Helper.areItemEqual(o, stack)) {
							recipe = rm.convertCraftingRecipe(crafting_recipe);
							if (recipe.getCount() > 0 && !recipe.getItemRecipe().isEmpty()) {
								valid = true;
							}
							break;
						}
					}
					/** add recipe and save user defined recipes to json */
					if (valid) {
						rm.addRecipe(recipe);
						if (rm.saveUserDefinedRecipes()) {
							Helper.sendMessage("message.command.addRecipeSucceeded", player, true);
						} else {
							Helper.sendMessage("message.command.addRecipeFailed", player, true);
						}
					} else {
						Helper.sendMessage("message.command.addRecipeFailed", player, true);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processRemoveRecipe(WorldServer world, EntityPlayerMP player) {
		if (!player.getHeldItemMainhand().isEmpty()) {
			boolean success = RecyclingManager.getInstance().removeRecipe(player.getHeldItemMainhand());
			if (success) {
				Helper.sendMessage("message.command.removeRecipeSucceeded", player, true);
			} else {
				Helper.sendMessage("message.command.removeRecipeFailed", player, true);
			}
		}
	}
}
