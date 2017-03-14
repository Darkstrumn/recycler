package ovh.corail.recycler.core;

public class JsonRecyclingRecipe {
	public String inputItem;
	public String[] outputItems;
	
	public JsonRecyclingRecipe(String inputItem, String[] outputItems) {
		this.inputItem = inputItem;
		this.outputItems = outputItems;
	}
	
}
