package ovh.corail.recycler.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ovh.corail.recycler.ModItems;
import ovh.corail.recycler.ModRecycler;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class BlockRecycler<TE extends TileEntityRecycler> extends Block {
	private static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool ENABLED = PropertyBool.create("enabled");
	private static String name = "recycler";

	public BlockRecycler() {
		super(Material.ROCK);
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(ModRecycler.tabRecycler);
		setHardness(5f);
		setResistance(20f);
		setLightLevel(0f);
		setLightOpacity(255);
		setHarvestLevel("pickaxe", 0);
		blockSoundType = SoundType.STONE;
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ENABLED, false));
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			player.openGui(ModRecycler.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}
    
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TE tile = getTileEntity(world, pos);
		if (tile != null) {
			InventoryHelper.dropInventoryItems(world, pos, (IInventory) tile);
			world.removeTileEntity(pos);
		}
		super.breakBlock(world, pos, state);
	}
	
	@Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, entity.getHorizontalFacing().getOpposite()), 3);
		EntityPlayer player = (EntityPlayer) entity;
		/* place a recycling book in the recycler */
		TileEntity tile = world.getTileEntity(pos);
		if (world.getTileEntity(pos) != null && tile instanceof TileEntityRecycler) {
			TileEntityRecycler recycler = (TileEntityRecycler) world.getTileEntity(pos);
			recycler.setInventorySlotContents(2, new ItemStack(ModItems.recycling_book, 1, 0));
		}
		
    }
	
	/*@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing facing) {
		return facing == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.DOWN;
	}*/
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return true;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, ENABLED);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.HORIZONTALS[meta & 3]).withProperty(ENABLED, (meta & 8) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex() + (state.getValue(ENABLED) ? 8 : 0);
	}

	@Override
	public TE createTileEntity(World world, IBlockState state) {
		return (TE) new TileEntityRecycler();
	}
	
	
	public TE getTileEntity(World world, BlockPos pos) {
		return (TE)world.getTileEntity(pos);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
}
