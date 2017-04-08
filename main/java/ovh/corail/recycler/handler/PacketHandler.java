package ovh.corail.recycler.handler;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import ovh.corail.recycler.core.ModProps;
import ovh.corail.recycler.packet.ClientProgressMessage;
import ovh.corail.recycler.packet.ClientWorkingMessage;
import ovh.corail.recycler.packet.RecycleMessage;
import ovh.corail.recycler.packet.ServerProgressMessage;
import ovh.corail.recycler.packet.ServerWorkingMessage;
import ovh.corail.recycler.packet.SoundMessage;
import ovh.corail.recycler.packet.TakeAllMessage;

public class PacketHandler {

	public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(ModProps.MOD_ID);

	public static void init() {
		int id = 0;
		INSTANCE.registerMessage(RecycleMessage.Handler.class, RecycleMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(ClientProgressMessage.Handler.class, ClientProgressMessage.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(ServerProgressMessage.Handler.class, ServerProgressMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(ClientWorkingMessage.Handler.class, ClientWorkingMessage.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(ServerWorkingMessage.Handler.class, ServerWorkingMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(TakeAllMessage.Handler.class, TakeAllMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(SoundMessage.Handler.class, SoundMessage.class, id++, Side.CLIENT);
	}
}
