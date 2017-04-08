package ovh.corail.recycler.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.handler.AchievementHandler;

public class ItemRecyclingBook extends Item {
	private static final String name = "recycling_book";
	
	public ItemRecyclingBook() {
		super();
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(Main.tabRecycler);
		setMaxStackSize(1);
	}
	
	@Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (playerIn != null) {
			playerIn.addStat(AchievementHandler.getAchievement("readRecyclingBook"), 1);
			playerIn.openGui(Main.instance, 1, worldIn, 0, 0, 0);
		}
        return new ActionResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
	
	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}
	
}
