package ovh.corail.recycler.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import ovh.corail.recycler.ModRecycler;
import ovh.corail.recycler.core.Helper;

public class ItemRecyclingBook extends Item {
	private static final String name = "recycling_book";
	
	public ItemRecyclingBook() {
		super();
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(ModRecycler.tabRecycler);
		setMaxStackSize(1);
	}
	
	@Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (player != null) {
			/** advancement read_recycling_book */
			Helper.grantAdvancement(player, "tutorial/read_recycling_book");
			player.openGui(ModRecycler.instance, 1, world, 0, 0, 0);
		}
        return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
	
	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}
	
}
