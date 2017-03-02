package ovh.corail.recycler.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ovh.corail.recycler.handler.PacketHandler;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class ServerProgressMessage implements IMessage {
	private BlockPos currentPos; 
	private int progress;

	public ServerProgressMessage() {
	}

	public ServerProgressMessage(BlockPos currentPos, int progress) {
		this.currentPos = currentPos;
		this.progress = progress;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.currentPos = BlockPos.fromLong(buf.readLong());
		this.progress = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(this.currentPos.toLong());
		buf.writeInt(this.progress);
	}
	
	public static class Handler implements IMessageHandler<ServerProgressMessage, IMessage> {
		@Override
		public IMessage onMessage(final ServerProgressMessage message, final MessageContext ctx) {
			IThreadListener mainThread = (IThreadListener) ctx.getServerHandler().playerEntity.world;
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					World worldIn = ctx.getServerHandler().playerEntity.world;
					TileEntity tile = worldIn.getTileEntity(message.currentPos);
					if (tile == null || !(tile instanceof TileEntityRecycler)) { return ; }
					TileEntityRecycler recycler = (TileEntityRecycler) worldIn.getTileEntity(message.currentPos);
   					recycler.setProgress(message.progress);
   					/*if (message.isReset) {
   						IBlockState state = worldIn.getBlockState(message.currentPos);
   						worldIn.setBlockState(message.currentPos, state.withProperty(BlockRecycler.ENABLED, message.isWorking), 3);
   					}*/
   					PacketHandler.INSTANCE.sendToAllAround(new ClientProgressMessage(message.currentPos, message.progress),
   							new TargetPoint(worldIn.provider.getDimension(), (double) message.currentPos.getX(), (double) message.currentPos.getY(), (double) message.currentPos.getZ(), 12.0d));
				}
			});
			return null;
		}
	}
}
