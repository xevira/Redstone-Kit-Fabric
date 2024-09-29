package github.xevira.redstone_kit;

import github.xevira.redstone_kit.screen.RedstoneTimerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;

public class RedstoneKitClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Render Layers
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
				Registration.REDSTONE_INVERTER_BLOCK,
				Registration.REDSTONE_RSNORLATCH_BLOCK,
				Registration.REDSTONE_TICKER_BLOCK,
				Registration.REDSTONE_TIMER_BLOCK);

		// Screen Handlers
		HandledScreens.register(Registration.REDSTONE_TIMER_SCREEN_HANDLER, RedstoneTimerScreen::new);

	}
}