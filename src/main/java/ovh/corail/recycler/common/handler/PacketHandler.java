package ovh.corail.recycler.common.handler;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import ovh.corail.recycler.common.Main;
import ovh.corail.recycler.common.packets.ButtonMessage;
import ovh.corail.recycler.common.packets.VisualMessage;

public class PacketHandler {

	public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(Main.MODID);

	public static void init() {
		int id = 0;
		INSTANCE.registerMessage(ButtonMessage.class, ButtonMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(VisualMessage.class, VisualMessage.class, id++, Side.SERVER);
		/*
		 * INSTANCE.registerMessage(StacksMessage.class, StacksMessage.class,
		 * id++, Side.CLIENT); INSTANCE.registerMessage(RequestMessage.class,
		 * RequestMessage.class, id++, Side.SERVER);
		 * INSTANCE.registerMessage(ClearMessage.class, ClearMessage.class,
		 * id++, Side.SERVER); INSTANCE.registerMessage(SortMessage.class,
		 * SortMessage.class, id++, Side.SERVER);
		 * INSTANCE.registerMessage(SyncMessage.class, SyncMessage.class, id++,
		 * Side.CLIENT); INSTANCE.registerMessage(RecipeMessage.class,
		 * RecipeMessage.class, id++, Side.SERVER);
		 * INSTANCE.registerMessage(LimitMessage.class, LimitMessage.class,
		 * id++, Side.SERVER); INSTANCE.registerMessage(RemoteMessage.class,
		 * RemoteMessage.class, id++, Side.SERVER);
		 * INSTANCE.registerMessage(InsertMessage.class, InsertMessage.class,
		 * id++, Side.SERVER); INSTANCE.registerMessage(StackMessage.class,
		 * StackMessage.class, id++, Side.CLIENT);
		 * INSTANCE.registerMessage(FilterMessage.class, FilterMessage.class,
		 * id++, Side.SERVER); INSTANCE.registerMessage(TemplateMessage.class,
		 * TemplateMessage.class, id++, Side.SERVER);
		 * INSTANCE.registerMessage(FaceMessage.class, FaceMessage.class, id++,
		 * Side.SERVER);
		 */
	}
}
