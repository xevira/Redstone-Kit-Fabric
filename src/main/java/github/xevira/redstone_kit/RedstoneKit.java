package github.xevira.redstone_kit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import github.xevira.redstone_kit.config.ServerConfig;
import net.fabricmc.api.ModInitializer;

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

		ServerWorldEvents.LOAD.register((server, world) -> ServerConfig.onServerLoad(server));
		ServerWorldEvents.UNLOAD.register((server, world) -> ServerConfig.onServerSave(server));

	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static String textPath(String prefix, String path)
	{
		return prefix + "." + MOD_ID + "." + path;
	}
}