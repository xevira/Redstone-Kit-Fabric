package github.xevira.redstone_kit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import github.xevira.redstone_kit.config.ServerConfig;
import github.xevira.redstone_kit.poi.POILoader;
import github.xevira.redstone_kit.poi.WirelessNetwork;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedstoneKit implements ModInitializer {
	public static final String MOD_ID = "redstone_kit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	@Override
	public void onInitialize() {
		Registration.load();


		ServerLifecycleEvents.SERVER_STARTED.register(ServerConfig::onServerLoad);
		ServerLifecycleEvents.SERVER_STARTED.register(WirelessNetwork::onServerStarted);
		ServerLifecycleEvents.SERVER_STOPPED.register(ServerConfig::onServerSave);
		ServerLifecycleEvents.SERVER_STOPPED.register(WirelessNetwork::onServerStopped);
		ServerLifecycleEvents.AFTER_SAVE.register(WirelessNetwork::onAfterSave);

		ServerWorldEvents.LOAD.register(POILoader::onServerWorldLoad);
		ServerWorldEvents.UNLOAD.register(POILoader::onServerWorldSave);

		//PlayerBlockBreakEvents.BEFORE.register(OwnerBlockBreaker.INSTANCE);
		//PlayerBlockBreakEvents.CANCELED.register(OwnerBlockBreaker.INSTANCE);
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static String textPath(String prefix, String path)
	{
		return prefix + "." + MOD_ID + "." + path;
	}
}