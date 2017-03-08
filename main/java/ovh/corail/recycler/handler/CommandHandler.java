package ovh.corail.recycler.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.core.RecyclingRecipe;

public class CommandHandler implements ICommand {
	private final List<String> aliases = new ArrayList<String>();
	private final List<String> commands = new ArrayList<String>();

	public CommandHandler() {
		aliases.add("recycler");
		aliases.add("corail");
		commands.add("viewRecipe");
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
		return "recycler viewRecipe|addRecipe|removeRecipe\n" + 
		"viewRecipe   - show the list of all items that are allowed to recycle in the console\n" + 
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
		if (args[0].equals("viewRecipes")) {
			processViewRecipe(world, sender);
		} else if (args[0].equals("addRecipe")) {
			processAddRecipe(world, sender);
		} else if (args[0].equals("removeRecipe")) {
			try {
				processRemoveRecipe(world, sender);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	
	private void processViewRecipe(World world, ICommandSender sender) {
		RecyclingManager rm = RecyclingManager.getInstance();
		RecyclingRecipe cr;
		for (int i = 0 ; i < rm.getRecipesCount() ; i++) {
			cr = rm.getRecipe(i);
			if (!cr.isAllowed() || (cr.isUnbalanced() && !ConfigurationHandler.unbalancedRecipes)) { continue; }
			System.out.println(cr.getItemRecipe().getItem().getRegistryName() + ":" + cr.getMeta());
		}
	}
	
	private void processAddRecipe(World world, ICommandSender sender) {
		EntityPlayer player = (EntityPlayer) sender;
		ItemStack stack = player.getActiveItemStack();
		if (stack != null) {
			//TODO
		}
	}
	
	private void processRemoveRecipe(World world, ICommandSender sender) throws IOException {
		EntityPlayer player = (EntityPlayer) sender;
		ItemStack stack = player.getHeldItemMainhand();
		if (stack != null) {
			RecyclingManager.getInstance().removeRecipe(stack);
		}
	}

}
