package ovh.corail.recycler.tileentity;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import ovh.corail.recycler.ModItems;
import ovh.corail.recycler.block.BlockRecycler;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.RecyclingManager;
import ovh.corail.recycler.core.RecyclingRecipe;
import ovh.corail.recycler.handler.ConfigurationHandler;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.handler.SoundHandler;
import ovh.corail.recycler.packet.ClientProgressMessage;
import ovh.corail.recycler.packet.ClientWorkingMessage;
import ovh.corail.recycler.packet.SoundMessage;

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
		ItemStack inputStack = getStackInSlot(0);
		/** item input slot empty */
		if (inputStack.isEmpty()) {
			Helper.sendMessage("tile.recycler.message.emptySlot", currentPlayer, true);
			return false;
		} else if (inputStack.getCount() <= 0) {
			Helper.sendMessage("tile.recycler.message.emptySlot", currentPlayer, true);
			setInventorySlotContents(0, ItemStack.EMPTY);
			return false;
		}
		ItemStack diskStack = getStackInSlot(1);
		/** disk input slot empty */
		if (diskStack.isEmpty()) {
			Helper.sendMessage("tile.recycler.message.noDisk", currentPlayer, true);
			return false;
		} else if (diskStack.getCount() <= 0) {
			Helper.sendMessage("tile.recycler.message.noDisk", currentPlayer, true);
			setInventorySlotContents(1, ItemStack.EMPTY);
			return false;
		} else if (diskStack.getItemDamage() >= diskStack.getMaxDamage()) {
			setInventorySlotContents(1, ItemStack.EMPTY);
			Helper.sendMessage("tile.recycler.message.noDisk", currentPlayer, true);
			return false;
		}
		return true;
	}

	private boolean transferSlotInput() {
		int emptySlot = this.getEmptySlot();
		if (emptySlot == -1 || getStackInSlot(0).isEmpty()) { return false; }
		ItemStack stack = getStackInSlot(0).copy();
		setInventorySlotContents(0, ItemStack.EMPTY);
		setInventorySlotContents(emptySlot, stack);
		return true;
	}
	
	public boolean hasSpaceInInventory(List<ItemStack> itemsList, boolean simulate) {
		/** only slots output */
		List<ItemStack> resultList = Lists.newArrayList(inventory.subList(firstOutput, count));
		/** list of empty slots */
		List<Integer> emptySlots = Lists.newArrayList();
		for (int i = 0 ; i < resultList.size() ; i++) {
			if (resultList.get(i).isEmpty()) {
				emptySlots.add(i);
			}
		}
		/** simulate : enough empty slots */
		if (simulate && emptySlots.size() >= itemsList.size()) {
			return true; 
		}
		/** simulate : try to fill at least minCount stacks depending of empty slots */
		int minCount = simulate ? itemsList.size() - emptySlots.size() : 0;
		int space, maxSize, add, left, emptySlot;
		ItemStack stackCopy;
		/** each stack of the input List */
		ItemStack stackIn, stackOut;
		for (int i = 0 ; i < itemsList.size() ; i++) {
			stackIn = itemsList.get(i);
			/** input stack empty or max stacksize */
			if (stackIn.isEmpty()) {
				if (simulate) {	minCount--;	}
				continue;
			}
			if (stackIn.getCount()==stackIn.getMaxStackSize()) {
				continue;
			}
			/** try to fill same stacks not full */
			left = stackIn.getCount();
			maxSize = stackIn.getMaxStackSize();
			/** each stack of the output List */
			for (int j = 0 ;  j < resultList.size() ; j++) {
				stackOut = resultList.get(j);
				/** output stack empty or max stacksize */
				if (stackOut.isEmpty() || stackOut.getCount()==stackOut.getMaxStackSize()) { continue; }
				/** stacks equal and same meta/nbt */
				if (Helper.areItemEqual(stackIn, stackOut)) {
					space = maxSize - stackOut.getCount();
					add = Math.min(space, left);
					if (add > 0) {
						stackOut.grow(add);
						left -= add;
						if (left <= 0) { break; }
					}
				}
			}
			/** stack completely filled */
			if (left <= 0) {
				if (simulate) {
					minCount--;
				}
			}
			/** place the stack left in an empty stack */
			if (left > 0) {
				if (emptySlots.size() > 0) {
					emptySlot = emptySlots.get(0);
					emptySlots.remove(0);
					stackCopy = stackIn.copy();
					stackCopy.setCount(left);
					resultList.set(emptySlot, stackCopy);
					if (simulate) {
						minCount++;
					}
				/** no empty stack */
				} else {
					return false;
				}
			}
			if (simulate && minCount <= 0) {
				return true;
			}
			itemsList.set(i, ItemStack.EMPTY);
		}
		/** add the fullstack left in input */
		for (ItemStack stack : itemsList) {
			if (!stack.isEmpty() && emptySlots.size() > 0) {
				emptySlot = emptySlots.get(0);
				emptySlots.remove(0);
				resultList.set(emptySlot, stack.copy());
			}
		}
		/** overwrite the output slots */
		if (!simulate) {
			for (int i = firstOutput ; i < inventory.size() ; i++) {
				this.setInventorySlotContents(i, resultList.get(i-firstOutput));
			}
		}
		return true;
	}
	
	public boolean recycle(@Nullable EntityPlayer currentPlayer) {
		/** test if diamond disk and recycled item are not empty */
		if (!canRecycle(currentPlayer)) {
			return false;
		}
		ItemStack inputStack = getStackInSlot(0);
		ItemStack diskStack = getStackInSlot(1);
		/** find the recipe */
		int num_recipe = recyclingManager.hasRecipe(inputStack);
		if (num_recipe < 0) { /** no recipe */
			//Helper.sendMessage("tile.recycler.message.noRecipe", currentPlayer, true);
			transferSlotInput();
			return false;
		}
		RecyclingRecipe currentRecipe = recyclingManager.getRecipe(num_recipe);
		/** number of times that the recipe can be used with this input */
		int nb_input = (int) Math.floor((double) inputStack.getCount() / (double) currentRecipe.getItemRecipe().getCount());
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
		List<ItemStack> itemsList = recyclingManager.getResultStack(inputStack, nb_input);
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
			stackList = recyclingManager.getResultStack(inputStack, nb_input-loss);
		} else {
			stackList = Lists.newArrayList();
		}
		if (loss > 0) {
			List<ItemStack> halfstackList = recyclingManager.getResultStack(inputStack, loss, true);
			stackList.addAll(halfstackList);
		}
		/** transfer stacks */
		hasSpaceInInventory(stackList, false);
		/** empty the input slot */
		if (currentRecipe.getItemRecipe().getCount() * nb_input == inputStack.getCount()) {
			setInventorySlotContents(0, ItemStack.EMPTY);
		} else {
			ItemStack stack = inputStack.copy();
			stack.setCount(inputStack.getCount() - (nb_input * currentRecipe.getItemRecipe().getCount()));
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
		// TODO remove the nullable for player
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && ConfigurationHandler.allowSound) {
			world.playSound(null, getPos(), SoundHandler.recycler, SoundCategory.NEUTRAL, 1f, 1f);
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
			if (stack.getItem()==ModItems.diamond_disk) {
				return true;
			} else {
				return false;
			}
		}
		if (stack.getItem()==ModItems.diamond_disk) {
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
			ItemStack inputStack = getStackInSlot(0);
			int num_recipe = recyclingManager.hasRecipe(inputStack);
			if (num_recipe >= 0) {
				int neededStacksize = recyclingManager.getRecipe(num_recipe).getItemRecipe().getCount();
				/** enough stacksize for the input slot */
				if (inputStack.getCount() < neededStacksize) {
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
				this.updateWorking(false);
			}
			/** no output slot */
			if (!transferSlotInput()) {
				this.updateWorking(false);
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
		} else if (ConfigurationHandler.allowSound && cantRecycleTicks<=1 && countTicks%15==0) {
			PacketHandler.INSTANCE.sendToAllAround(new SoundMessage(getPos(), 1),
				new TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 20));
		}

		progress = (int) Math.floor(((double) (maxTicks-countTicks) / (double) maxTicks) * 100.0);
		/** TODO less packets */
		PacketHandler.INSTANCE.sendToAllAround(new ClientProgressMessage(getPos(), progress),
					new TargetPoint(world.provider.getDimension(), (double) getPos().getX(), (double) getPos().getY(), (double) getPos().getZ(), 12.0d));
		if (!isWorking) {
			PacketHandler.INSTANCE.sendToAllAround(new ClientWorkingMessage(getPos(), false),
					new TargetPoint(world.provider.getDimension(), (double) getPos().getX(), (double) getPos().getY(), (double) getPos().getZ(), 12.0d));
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
	
	public void updateWorking(boolean isWorking) {
		this.setProgress(0);
		this.isWorking = isWorking;
		IBlockState state = world.getBlockState(pos);
		world.setBlockState(pos, state.withProperty(BlockRecycler.ENABLED, isWorking));
		//world.getBlockState(pos).getBlock().setLightLevel(isWorking?0.5f:0f);
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
