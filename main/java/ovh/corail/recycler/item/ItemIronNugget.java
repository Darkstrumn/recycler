package ovh.corail.recycler.item;

import net.minecraft.item.Item;
import ovh.corail.recycler.core.Main;

public class ItemIronNugget extends Item {
	private static final String name = "iron_nugget";
	
	public ItemIronNugget() {
		super();
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(Main.tabRecycler);
		setMaxStackSize(64);
	}
}
