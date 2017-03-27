package ovh.corail.recycler.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ovh.corail.recycler.core.Main;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class RecycleMessage implements IMessage {
	private int id;
	private BlockPos currentPos;
	private UUID playerId;

	public RecycleMessage() {
	}

	public RecycleMessage(int id, BlockPos currentPos, UUID playerId) {
		this.id = id;
		this.currentPos = currentPos;
		this.playerId = playerId;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.id = buf.readInt();
		this.currentPos = BlockPos.fromLong(buf.readLong());
		this.playerId = UUID.fromString(ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.id);
		buf.writeLong(this.currentPos.toLong());
		ByteBufUtils.writeUTF8String(buf, playerId.toString());
	}
	
	public static class Handler implements IMessageHandler<RecycleMessage, IMessage> {
		@Override
		public IMessage onMessage(final RecycleMessage message, final MessageContext ctx) {
			IThreadListener mainThread = (IThreadListener) ctx.getServerHandler().player.world;
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					World worldIn = ctx.getServerHandler().player.world;
					TileEntity tile = worldIn.getTileEntity(message.currentPos);
					if (tile == null || !(tile instanceof TileEntityRecycler)) { return ; }
					TileEntityRecycler recycler = (TileEntityRecycler) worldIn.getTileEntity(message.currentPos);
					switch (message.id) {
					case 0: // Recycle
						recycler.recycle(null);
						EntityPlayer currentPlayer = worldIn.getPlayerEntityByUUID(message.playerId);
						if (currentPlayer != null) {
							currentPlayer.addStat(Main.achievementFirstRecycle, 1);
						}
						break;
					}
				}
			});
			return null;
		}
	}
}
