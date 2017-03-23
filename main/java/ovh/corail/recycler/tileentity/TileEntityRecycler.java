package ovh.corail.recycler.tileentity;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.core.RecyclingRecipe;
import ovh.corail.recycler.handler.ConfigurationHandler;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.packet.ServerProgressMessage;
import ovh.corail.recycler.packet.SoundMessage;
import ovh.corail.recycler.packet.WorkingMessage;

public class TileEntityRecycler extends TileEntityInventory implements ITickable {

	public RecyclingManager recyclingManager;
	private int countTicks = 0;
	private final int maxTicks = 100;
	private boolean isWorking = false;
	private int progress = 0;
	private int cantRecycleTicks = 0;
    public int numPlayersUsing; // TODO only one access
    private int ticksSinceSync;

	public TileEntityRecycler() {
		super();
		recyclingManager = RecyclingManager.getInstance();
	}

	public boolean canRecycle(EntityPlayer currentPlayer) {
		/** item input slot empty */
		if (getStackInSlot(0).isEmpty()) {
			Helper.sendMessage("tile.recycler.message.emptySlot", currentPlayer, true);
			return false;
		} else if (getStackInSlot(0).getCount() <= 0) {
			Helper.sendMessage("tile.recycler.message.emptySlot", currentPlayer, true);
			setInventorySlotContents(0, ItemStack.EMPTY);
			return false;
		}
		/** disk input slot empty */
		ItemStack diskStack = getStackInSlot(1);
		if (diskStack.isEmpty()) {
			Helper.sendMessage("tile.recycler.message.noDisk", currentPlayer, true);
			return false;
		} else if (diskStack.getCount() <= 0) {
			Helper.sendMessage("tile.recycler.message.noDisk", currentPlayer, true);
			setInventorySlotContents(1, ItemStack.EMPTY);
			return false;
		} else if (getStackInSlot(1).getItemDamage() >= getStackInSlot(1).getMaxDamage()) {
			setInventorySlotContents(1, ItemStack.EMPTY);
			Helper.sendMessage("tile.recycler.message.noDisk", currentPlayer, true);
			return false;
		}
		// TODO test space here
		return true;
	}

	private boolean transferSlotInput() {
		int emptySlot = this.getEmptySlot();
		if (emptySlot == -1 || getStackInSlot(0).isEmpty()) { return false; }
		ItemStack stack = getStackInSlot(0).copy();
		this.setInventorySlotContents(0, ItemStack.EMPTY);
		this.setInventorySlotContents(emptySlot, stack);
		return true;
	}
	
	public boolean hasSpaceInInventory(List<ItemStack> itemsList, boolean simulate) {
		Helper.sendLog("hasSpaceInInventory with simulate = " + simulate);
		/** only slots output */
		List<ItemStack> resultList = Lists.newArrayList(inventory.subList(firstOutput, count));
		/** list of empty slots */
		List<Integer> emptySlots = Lists.newArrayList();
		for (int i = 0 ; i < resultList.size() ; i++) {
			if (resultList.get(i).isEmpty()) {
				emptySlots.add(i);
			}
		}
		Helper.sendLog(emptySlots.size() + " empty slots found");
		/** simulate : enough empty slots */
		if (simulate && emptySlots.size() >= itemsList.size()) {
			Helper.sendLog("there's enough empty slots");
			return true; 
		}
		/** simulate : try to fill at least minCount stacks depending of empty slots */
		int minCount = simulate ? itemsList.size() - emptySlots.size() : 0;
		int space, maxSize, add, left, emptySlot;
		ItemStack stackCopy;
		if (simulate) {
			Helper.sendLog("try to fill at least " + minCount + " same stacks");
		} else {
			Helper.sendLog("try to fill same stacks");
		}
		/** each stack of the input List */
		for (ItemStack stackIn : itemsList) {
			/** input stack empty or max stacksize */
			if (stackIn.isEmpty()) {
				if (simulate) {	minCount--;	}
				Helper.sendLog("SKIP - current input stack is empty");
				continue;
			}
			if (stackIn.getCount()==stackIn.getMaxStackSize()) {
				Helper.sendLog("SKIP - current input stack is fullstack : "+stackIn.getDisplayName());
				continue;
			}
			Helper.sendLog("current stack : " + stackIn.getCount() + " " + stackIn.getDisplayName());
			/** try to fill same stacks not full */
			left = stackIn.getCount();
			maxSize = stackIn.getMaxStackSize();
			/** each stack of the output List */
			for (ItemStack stackOut : resultList) {
				/** output stack empty or max stacksize */
				if (stackOut.isEmpty() || stackOut.getCount()==stackOut.getMaxStackSize()) { continue; }
				/** stacks equal and same meta/nbt */
				if (Helper.areItemEqual(stackIn, stackOut)) {
					space = maxSize - stackOut.getCount();
					add = Math.min(space, left);
					if (add > 0) {
						stackOut.grow(add);
						left -= add;
						Helper.sendLog("found stack with space for " + space + ", left : " + left);
						if (left <= 0) { break; }
					}
				}
			}
			/** stack completely filled */
			if (left <= 0) {
				Helper.sendLog("stack completely filled");
				if (simulate) {
					minCount--;
					Helper.sendLog("try to fill at least " + minCount + " same stacks");
				}
			}
			/** place the stack left in an empty stack */
			if (left > 0) {
				if (emptySlots.size() > 0) {
					Helper.sendLog("place the stack left in an empty stack");
					emptySlot = emptySlots.get(0);
					emptySlots.remove(0);
					stackCopy = stackIn.copy();
					stackCopy.setCount(left);
					resultList.set(emptySlot, stackCopy);
					if (simulate) { minCount++; }
				/** no empty stack */
				} else {
					Helper.sendLog("no empty stack to place the stack left");
					Helper.sendLog("FAILED");
					return false;
				}
			}
			if (simulate && minCount <= 0) {
				Helper.sendLog("SUCCESS");
				return true;
			}
		}
		/** overwrite the output slots */
		if (!simulate) {
			Helper.sendLog("overwriting output slots");
			for (int i = firstOutput ; i < inventory.size() ; i++) {
				this.setInventorySlotContents(i, resultList.get(i-firstOutput));
			}
		}
		Helper.sendLog("SUCCESS");
		return true;
	}
	
	public boolean recycle(EntityPlayer currentPlayer) {
		/** test if diamond disk and recycled item are not empty */
		if (!canRecycle(currentPlayer)) {
			return false;
		}
		ItemStack diskStack = getStackInSlot(1);
		/** find the recipe */
		int num_recipe = recyclingManager.hasRecipe(getStackInSlot(0));
		if (num_recipe < 0) { /** no recipe */
			//Helper.sendMessage("tile.recycler.message.noRecipe", currentPlayer, true);
			transferSlotInput();
			return false;
		}
		RecyclingRecipe currentRecipe = recyclingManager.getRecipe(num_recipe);
		/** number of times that the recipe can be used with this input */
		int nb_input = (int) Math.floor((double) getStackInSlot(0).getCount() / (double) currentRecipe.getItemRecipe().getCount());
		if (nb_input == 0) { /** not enough input for at least one recipe */
			//Helper.sendMessage("tile.recycler.message.noEnoughInput", currentPlayer, true);
			return false;
		}
		/** by unit in auto recycle */
		if (isWorking) { nb_input = 1; }
		/** max uses of the disk */
		int maxDiskUse = (int) Math.floor((double) (diskStack.getMaxDamage() - diskStack.getItemDamage()) / 10.0D);
		if (maxDiskUse < nb_input) {
			nb_input = maxDiskUse;
		}
		/** calculation of the result */
		List<ItemStack> itemsList = recyclingManager.getResultStack(getStackInSlot(0), nb_input);
		/** simule the space needed */
		if (!hasSpaceInInventory(itemsList, true)) {
			Helper.sendMessage("tile.recycler.message.notEnoughOutputSlots", currentPlayer, true);
			return false;
		}
		/** Loss chance */
		int loss = 0;
		if (ConfigurationHandler.chanceLoss > 0) {
			for (int i = 0 ; i < nb_input ; i++) {
				if (Helper.getRandom(1, 100) <= ConfigurationHandler.chanceLoss) {
					loss++;
				}
			}
			if (loss > 0) {
				Helper.sendMessage("tile.recycler.message.loss", currentPlayer, true);
			}
		}
		List<ItemStack> stackList;
		if (nb_input-loss > 0) {
			stackList = recyclingManager.getResultStack(getStackInSlot(0), nb_input-loss);
		} else {
			stackList = Lists.newArrayList();
		}
		if (loss > 0) {
			List<ItemStack> halfstackList = recyclingManager.getResultStack(getStackInSlot(0), loss, true);
			stackList.addAll(halfstackList);
		}
		/** transfer stacks */
		hasSpaceInInventory(stackList, false);
		/** empty the input slot */
		if (currentRecipe.getItemRecipe().getCount() * nb_input == getStackInSlot(0).getCount()) {
			setInventorySlotContents(0, ItemStack.EMPTY);
		} else {
			ItemStack stack = getStackInSlot(0).copy();
			stack.setCount(getStackInSlot(0).getCount() - (nb_input * currentRecipe.getItemRecipe().getCount()));
			setInventorySlotContents(0, stack);
		}
		/** damage the disk */
		diskStack.setItemDamage(diskStack.getItemDamage() + (10 * nb_input));
		if (diskStack.getItemDamage() >= diskStack.getMaxDamage()) {
			Helper.sendMessage("tile.recycler.message.brokenDisk", currentPlayer, true);
			this.setInventorySlotContents(1, ItemStack.EMPTY);
		} else {
			this.setInventorySlotContents(1, diskStack);
		}
		/** play sound */
		if (!ConfigurationHandler.soundOff) {
			PacketHandler.INSTANCE.sendToAllAround(new SoundMessage(getPos(), 0),
					new TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 20));
		}
		return true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("countTicks", countTicks);
		compound.setBoolean("isWorking", isWorking);
		compound.setInteger("progress", progress);
		compound.setInteger("cantRecycleTicks", cantRecycleTicks);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		countTicks = compound.getInteger("countTicks");
		isWorking = compound.getBoolean("isWorking");
		progress = compound.getInteger("progress");
		cantRecycleTicks = compound.getInteger("cantRecycleTicks");
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		/** output slots */
		if (index > 1) {
			return false;
		}
		/** disk input slot */
		if (index == 1) {
			if (stack.getItem()==Main.diamond_disk) {
				return true;
			} else {
				return false;
			}
		}
		if (stack.getItem()==Main.diamond_disk) {
			return false;
		}
		return true;
	}



	@Override
	public void update() {
		if (world.isRemote || !isWorking) { return; }
		countTicks--;
		
		/** each tick */
		if (!canRecycle((EntityPlayer) null)) {
			cantRecycleTicks++;
			countTicks = maxTicks;
		} else {
			/** corresponding recipe */
			int num_recipe = recyclingManager.hasRecipe(getStackInSlot(0));
			if (num_recipe >= 0) {
				int neededStacksize = recyclingManager.getRecipe(num_recipe).getItemRecipe().getCount();
				/** enough stacksize for the input slot */
				if (getStackInSlot(0).getCount() < neededStacksize) {
					cantRecycleTicks++;
					countTicks = maxTicks;
				}
			/** no recipe */
			} else {
				cantRecycleTicks++;
				countTicks = maxTicks;
			}
		}
		
		/** can't recycle since 4 seconds */
		if (cantRecycleTicks > 40) {
			/** no input item or no disk */
			if (getStackInSlot(0).isEmpty() || getStackInSlot(1).isEmpty()) {
				isWorking = false;
			}
			/** no output slot */
			if (!transferSlotInput()) {
				isWorking = false;
			}
			cantRecycleTicks = 0;
			countTicks = maxTicks;
		}
		
		/** try to recycle */
		if (countTicks <= 0) {
			if (!recycle((EntityPlayer) null)) {
				cantRecycleTicks++;
			}
			countTicks = maxTicks;
			/** play sound */
		} else if (!ConfigurationHandler.soundOff && cantRecycleTicks<=1 && countTicks%15==0) {
			PacketHandler.INSTANCE.sendToAllAround(new SoundMessage(getPos(), 1),
				new TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 20));
		}

		progress = (int) Math.floor(((double) (maxTicks-countTicks) / (double) maxTicks) * 100.0);
		PacketHandler.INSTANCE.sendToServer(new ServerProgressMessage(getPos(), progress));
		if (!isWorking) {
			PacketHandler.INSTANCE.sendToServer(new WorkingMessage(getPos(), false));
		}
	}

	public int getPercentWorking() {
		return progress;
	}
	
	public boolean isWorking() {
		return isWorking;
	}
	
	public int getCountTicks() {
		return countTicks;
	}
	
	public void setWorking(boolean isWorking) {
		this.setProgress(0);
		this.isWorking = isWorking;

	}
	
	public void setProgress(int progress) {
		this.progress = progress;
		if (progress == 0) {
			countTicks = maxTicks;
		}
	}
	
	public String getName() {
        return this.hasCustomName() ? this.customName : "container.recycler";
    }
	
}
