package ovh.corail.recycler.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import ovh.corail.recycler.gui.ContainerRecycler;
import ovh.corail.recycler.gui.GuiRecycler;
import ovh.corail.recycler.gui.GuiRecyclingBook;
import ovh.corail.recycler.item.ItemRecyclingBook;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class GuiHandler implements IGuiHandler {
	private static final int RECYCLER = 0;
	private static final int RECYCLING_BOOK = 1;
	
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case RECYCLER:
				TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
				if (tileEntity instanceof TileEntityRecycler) { 
					return new ContainerRecycler(player, world, x, y, z, (TileEntityRecycler) tileEntity);
				}
				break;
			case RECYCLING_BOOK:
				break;
			default:
				System.err.println("Invalid gui id, received : " + id);
		}
		return null;
	} 

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case RECYCLER:
				TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
				if (tileEntity instanceof TileEntityRecycler) {
					return new GuiRecycler(player, world, x, y, z, (TileEntityRecycler) tileEntity);
				}
				break;
			case RECYCLING_BOOK:
				ItemStack stack = player.getHeldItemMainhand();
				if (stack.getItem() instanceof ItemRecyclingBook) {
					return new GuiRecyclingBook();
				}
				break;
			default:
				System.err.println("Invalid gui id, received : " + id);
		}
		return null;
	}

}