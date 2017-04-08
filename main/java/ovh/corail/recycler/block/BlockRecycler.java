package ovh.corail.recycler.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.handler.AchievementHandler;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class BlockRecycler extends Block implements ITileEntityProvider {
	private static final PropertyDirection FACING = PropertyDirection.create("facing");
	public static final PropertyBool ENABLED = PropertyBool.create("enabled");
	private static String name = "recycler";

	public BlockRecycler() {
		super(Material.ROCK);
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(Main.tabRecycler);
		isBlockContainer = true;
		setHardness(2.0f);
		setResistance(10.0f);
		setLightLevel(0f);
		blockSoundType = SoundType.STONE;
		setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ENABLED, false));
	}
	
	@Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()));
		EntityPlayer player = (EntityPlayer) placer;
		player.addStat(AchievementHandler.getAchievement("placeRecycler"), 1);
		/** place a recycling book in the recycler */
		TileEntity tile = world.getTileEntity(pos);
		if (world.getTileEntity(pos) != null && tile instanceof TileEntityRecycler) {
			TileEntityRecycler recycler = (TileEntityRecycler) world.getTileEntity(pos);
			recycler.setInventorySlotContents(2, new ItemStack(Main.recycling_book, 1, 0));
		}
		
    }
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING, ENABLED });
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex() + (state.getValue(ENABLED) ? 8 : 0);
	}
	
	@Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7)).withProperty(ENABLED, (meta & 8) != 0);
    }
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState iBlockState) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
		if (!world.isRemote) {
			if (player instanceof EntityPlayer) {
				player.openGui(Main.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
		}

		return true;
	}

	@Override
	public int tickRate(World world) {
		return 1;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityRecycler();
	}   
    
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof TileEntityRecycler) {
			InventoryHelper.dropInventoryItems(world, pos, (IInventory) tileentity);
			world.updateComparatorOutputLevel(pos, this);
			world.removeTileEntity(pos);
		}
		super.breakBlock(world, pos, state);
	}
	
	/** block container implement */
	@Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
        super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
    }
}
