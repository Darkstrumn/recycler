package ovh.corail.recycler.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class ServerWorkingMessage implements IMessage {
	private BlockPos currentPos; 
	private boolean isWorking;

	public ServerWorkingMessage() {
	}

	public ServerWorkingMessage(BlockPos currentPos, boolean isWorking) {
		this.currentPos = currentPos;
		this.isWorking = isWorking;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.currentPos = BlockPos.fromLong(buf.readLong());
		this.isWorking = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(this.currentPos.toLong());
		buf.writeBoolean(this.isWorking);
	}
	
	public static class Handler implements IMessageHandler<ServerWorkingMessage, IMessage> {
		@Override
		public IMessage onMessage(final ServerWorkingMessage message, final MessageContext ctx) {
			IThreadListener mainThread = (IThreadListener) ctx.getServerHandler().player.world;
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					World worldIn = ctx.getServerHandler().player.world;
					TileEntity tile = worldIn.getTileEntity(message.currentPos);
					if (tile == null || !(tile instanceof TileEntityRecycler)) { return ; }
					TileEntityRecycler recycler = (TileEntityRecycler) worldIn.getTileEntity(message.currentPos);
   					recycler.updateWorking(message.isWorking);
 				}
			});
			return null;
		}
	}
}
