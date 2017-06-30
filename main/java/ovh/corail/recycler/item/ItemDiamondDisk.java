package ovh.corail.recycler.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ovh.corail.recycler.core.Helper;
import ovh.corail.recycler.core.Main;

public class ItemDiamondDisk extends Item {
	private static final String name = "diamond_disk";
	
	public ItemDiamondDisk() {
		super();
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(Main.tabRecycler);
		setMaxStackSize(1);
		setMaxDamage(5000);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List list, ITooltipFlag flagIn) {
		list.add(TextFormatting.WHITE + Helper.getTranslation("item." + name + ".desc"));
	}
	
	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		// TODO Advancement
		//playerIn.addStat(AchievementHandler.getAchievement("buildDisk"), 1);
	}
}
