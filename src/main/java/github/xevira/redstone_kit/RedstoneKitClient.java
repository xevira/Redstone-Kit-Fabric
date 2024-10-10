package github.xevira.redstone_kit;

import github.xevira.redstone_kit.block.TeleportInhibitorBlock;
import github.xevira.redstone_kit.block.TeleporterBlock;
import github.xevira.redstone_kit.events.OwnerBlockBreaker;
import github.xevira.redstone_kit.renderer.TeleportBlockEntityRenderer;
import github.xevira.redstone_kit.renderer.TeleportInhibitorBlockEntityRenderer;
import github.xevira.redstone_kit.screen.PlayerDetectorScreen;
import github.xevira.redstone_kit.screen.RedstoneTimerScreen;
import github.xevira.redstone_kit.screen.TeleportInhibitorScreen;
import github.xevira.redstone_kit.screen.TeleporterScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class RedstoneKitClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Client-side Event handlers
		ClientTickEvents.END_CLIENT_TICK.register(TeleporterBlock.TeleportHandler::handleInput);
		AttackBlockCallback.EVENT.register(OwnerBlockBreaker.INSTANCE);

		// Render Layers
		// Cutout - Where parts of the textures need to be transparent, but NOT translucent (use Translucent for that)
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
		HandledScreens.register(Registration.TELEPORTER_SCREEN_HANDLER, TeleporterScreen::new);
		HandledScreens.register(Registration.TELEPORT_INHIBITOR_SCREEN_HANDLER, TeleportInhibitorScreen::new);

		// Block Entity Renderers
		BlockEntityRendererFactories.register(Registration.TELEPORT_INHIBITOR_BLOCK_ENTITY, TeleportInhibitorBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Registration.TELEPORTER_BLOCK_ENTITY, TeleportBlockEntityRenderer::new);
	}
}