package ovh.corail.recycler.item;

import net.minecraft.item.Item;
import ovh.corail.recycler.core.Main;

public class ItemDiamondNugget extends Item {
	private static final String name = "diamond_nugget";
	
	public ItemDiamondNugget() {
		super();
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(Main.tabRecycler);
		setMaxStackSize(64);
	}
}
