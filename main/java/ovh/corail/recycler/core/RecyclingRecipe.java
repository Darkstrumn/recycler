package ovh.corail.recycler.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RecyclingRecipe {
	private ItemStack itemRecipe;
	private boolean isUnbalanced=false;
	private boolean isUserDefined=false;
	private boolean isAllowed=true;
	private List<ItemStack> itemsList=new ArrayList<ItemStack>();
	
	/** TODO code not used
	public RecyclingRecipe(Item item, int count, int meta) {
		this.itemRecipe=new ItemStack(item, count, meta);
	}*/
	
	public RecyclingRecipe(ItemStack itemStack) {
		this.itemRecipe=itemStack;
	}
	
	public RecyclingRecipe(ItemStack stackIn, Object... recipeComponents) {
		this.itemRecipe=stackIn.copy();
		for (Object object : recipeComponents) {
			if (object instanceof ItemStack) {
				itemsList.add(((ItemStack) object).copy());
			} 
		}
	}
	
	public RecyclingRecipe(ItemStack stackIn, ItemStack stackOut) {
		this.itemRecipe=stackIn.copy();
		itemsList.add(stackOut.copy());
	}

	public ItemStack getItemRecipe() {
		return itemRecipe;
	}
	
	public boolean canBeRepaired() {
		return getItemRecipe().getItem().isRepairable();
	}
	
	public void setUnbalanced(boolean state) {
		isUnbalanced=state;
	}
	
	public boolean isUnbalanced() {
		return isUnbalanced;
	}
	
	public void setUserDefined(boolean state) {
		isUserDefined=state;
	}
	
	public boolean isUserDefined() {
		return isUserDefined;
	}
	
	public void setAllowed(boolean state) {
		isAllowed=state;
	}
	
	public boolean isAllowed() {
		return isAllowed;
	}
	
	public int getMeta() {
		return itemRecipe.getItemDamage();
	}
	
	public Integer getCount() {
		return itemsList.size();
	}
	
	public void addStack(Item item, int count, int meta) {
		itemsList.add(new ItemStack(item, count, meta));
	}
	
	public void addStack(Block block, int count, int meta) {
		addStack(Item.getItemFromBlock(block), count, meta);
	}
	
	public void addStack(ItemStack stack) {
		itemsList.add(stack);
	}
	
	public ItemStack getStack(int index) {
		return itemsList.get(index);
	}
	
	public void setStack(int index, ItemStack stack) {
		itemsList.set(index, stack);
	}
}
