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

public class WorkingMessage implements IMessage {
	private BlockPos currentPos; 
	private boolean isWorking;

	public WorkingMessage() {
	}

	public WorkingMessage(BlockPos currentPos, boolean isWorking) {
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
	
	public static class Handler implements IMessageHandler<WorkingMessage, IMessage> {
		@Override
		public IMessage onMessage(final WorkingMessage message, final MessageContext ctx) {
			IThreadListener mainThread = (IThreadListener) ctx.getServerHandler().playerEntity.worldObj;
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					World worldIn = ctx.getServerHandler().playerEntity.worldObj;
					TileEntity tile = worldIn.getTileEntity(message.currentPos);
					if (tile == null || !(tile instanceof TileEntityRecycler)) { return ; }
					TileEntityRecycler recycler = (TileEntityRecycler) worldIn.getTileEntity(message.currentPos);
   					recycler.setWorking(message.isWorking);
 				}
			});
			return null;
		}
	}
}
