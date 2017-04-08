package ovh.corail.recycler.handler;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import ovh.corail.recycler.core.ModProps;

public class SoundHandler {
	public static SoundEvent recycler, recycler_working;

	public static void registerSounds() {
		recycler = registerSound("recycler");
		recycler_working = registerSound("recycler_working");
	}

	private static SoundEvent registerSound(String soundName) {
		final ResourceLocation soundID = new ResourceLocation(ModProps.MOD_ID, soundName);
		return GameRegistry.register(new SoundEvent(soundID).setRegistryName(soundID));
	}
}
