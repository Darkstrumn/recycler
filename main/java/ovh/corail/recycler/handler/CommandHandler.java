package ovh.corail.recycler.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
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
		commands.add("viewRecipes");
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
		return "recycler viewRecipes\nShow the list of all items that are allowed to recycle in the console";
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
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
			processViewRecipes(world, sender);
		/*} else if (args[0].equals("command2")) {
			processGraveDigger(world, sender);
			/*} else if (args[0].equals("command3")) {
			processDeleteGraves(world, sender);*/
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
	
	private void processViewRecipes(World world, ICommandSender sender) {
		RecyclingManager rm = RecyclingManager.getInstance();
		RecyclingRecipe cr;
		for (int i = 0 ; i < rm.getRecipesCount() ; i++) {
			cr = rm.getRecipe(i);
			if (!cr.isAllowed() || (cr.isUnbalanced() && !ConfigurationHandler.unbalancedRecipes)) { continue; }
			System.out.println(cr.getItemRecipe().getItem().getRegistryName() + ":" + cr.getMeta());
			//Helper.addChatMessage(cr.getItemRecipe().getItem().getUnlocalizedName() + ":" + cr.getMeta(), (EntityPlayer) sender, false);
		}
	}

}
