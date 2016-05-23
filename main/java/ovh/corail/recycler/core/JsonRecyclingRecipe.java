package ovh.corail.recycler.core;

public class JsonRecyclingRecipe {
	public String inputItem;
	public String[] outputItems;
	public boolean canBeRepaired, isUnbalanced;
	public JsonRecyclingRecipe(String inputItem, String[] outputItems, boolean canBeRepaired, boolean isUnbalanced) {
		this.inputItem = inputItem;
		this.outputItems = outputItems;
		this.canBeRepaired = canBeRepaired;
		this.isUnbalanced = isUnbalanced;
	}
	public JsonRecyclingRecipe(String inputItem, String[] outputItems, boolean canBeRepaired) {
		this(inputItem, outputItems, canBeRepaired, false);
	}
	public JsonRecyclingRecipe(String inputItem, String[] outputItems) {
		this(inputItem, outputItems, false, false);
	}
}
