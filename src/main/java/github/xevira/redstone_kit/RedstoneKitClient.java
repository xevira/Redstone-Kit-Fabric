package github.xevira.redstone_kit;

import github.xevira.redstone_kit.block.TeleporterBlock;
import github.xevira.redstone_kit.renderer.TeleportBlockEntityRenderer;
import github.xevira.redstone_kit.screen.PlayerDetectorScreen;
import github.xevira.redstone_kit.screen.RedstoneTimerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class RedstoneKitClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(TeleporterBlock.TeleportHandler::handleInput);

		// Render Layers
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
				Registration.REDSTONE_AND_BLOCK,
				Registration.REDSTONE_INVERTER_BLOCK,
				Registration.REDSTONE_AND_BLOCK,
				Registration.REDSTONE_OR_BLOCK,
				Registration.REDSTONE_XOR_BLOCK,
				Registration.REDSTONE_RSNORLATCH_BLOCK,
				Registration.REDSTONE_TICKER_BLOCK,
				Registration.REDSTONE_TIMER_BLOCK,
				Registration.REDSTONE_XOR_BLOCK);

		// Screen Handlers
		HandledScreens.register(Registration.REDSTONE_TIMER_SCREEN_HANDLER, RedstoneTimerScreen::new);
		HandledScreens.register(Registration.PLAYER_DETECTOR_SCREEN_HANDLER, PlayerDetectorScreen::new);

		// Block Entity Renderers
		BlockEntityRendererFactories.register(Registration.TELEPORTER_BLOCK_ENTITY, TeleportBlockEntityRenderer::new);
	}
}