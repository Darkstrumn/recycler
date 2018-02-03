package ovh.corail.recycler.item;

import net.minecraft.item.Item;
import ovh.corail.recycler.ModRecycler;

public class ItemDiamondFragment extends Item {
	private static final String name = "diamond_fragment";
	
	public ItemDiamondFragment() {
		super();
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(ModRecycler.tabRecycler);
		setMaxStackSize(64);
	}
}
