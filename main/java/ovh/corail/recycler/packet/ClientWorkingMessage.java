package ovh.corail.recycler.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class ClientWorkingMessage implements IMessage {
	
	private BlockPos currentPos;
	private boolean isWorking;

	public ClientWorkingMessage() {
		
	}
	
	public ClientWorkingMessage(BlockPos currentPos, boolean isWorking) {
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
	
	public static class Handler implements IMessageHandler<ClientWorkingMessage, IMessage> {
		@Override
		public IMessage onMessage(final ClientWorkingMessage message, final MessageContext ctx) {
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					WorldClient worldIn = Minecraft.getMinecraft().world;
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
