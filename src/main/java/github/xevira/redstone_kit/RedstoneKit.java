package github.xevira.redstone_kit;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedstoneKit implements ModInitializer {
	public static final String MOD_ID = "redstone_kit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Registration.load();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}