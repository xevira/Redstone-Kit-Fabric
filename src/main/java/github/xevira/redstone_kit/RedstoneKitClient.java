package github.xevira.redstone_kit;

import github.xevira.redstone_kit.block.TeleporterBlock;
import github.xevira.redstone_kit.client.particle.ModFlameParticle;
import github.xevira.redstone_kit.events.OwnerBlockBreaker;
import github.xevira.redstone_kit.renderer.RedstoneTransmitterBlockEntityRenderer;
import github.xevira.redstone_kit.renderer.TeleportBlockEntityRenderer;
import github.xevira.redstone_kit.renderer.TeleportInhibitorBlockEntityRenderer;
import github.xevira.redstone_kit.screen.PlayerDetectorScreen;
import github.xevira.redstone_kit.screen.RedstoneTimerScreen;
import github.xevira.redstone_kit.screen.TeleportInhibitorScreen;
import github.xevira.redstone_kit.screen.TeleporterScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class RedstoneKitClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Client-side Event handlers
		// - Handle the jump activation on teleporters
		ClientTickEvents.END_CLIENT_TICK.register(TeleporterBlock.TeleportHandler::handleInput);
		// - Handle owned block break protection
		AttackBlockCallback.EVENT.register(OwnerBlockBreaker.INSTANCE);

		// Render Layers
		// Cutout - Where parts of the textures need to be transparent, but NOT translucent (use Translucent for that)
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
				Registration.ENDER_TORCH_BLOCK,
				Registration.ENDER_WALL_TORCH_BLOCK,
				Registration.EQUATOR_BLOCK,
				Registration.REDSTONE_AND_BLOCK,
				Registration.REDSTONE_INVERTER_BLOCK,
				Registration.REDSTONE_OR_BLOCK,
				Registration.REDSTONE_RSNORLATCH_BLOCK,
				Registration.REDSTONE_TICKER_BLOCK,
				Registration.REDSTONE_TIMER_BLOCK,
				Registration.REDSTONE_TRANSMITTER_BLOCK,
				Registration.REDSTONE_XOR_BLOCK);

		// Translucent

		// Screen Handlers
		HandledScreens.register(Registration.PLAYER_DETECTOR_SCREEN_HANDLER, PlayerDetectorScreen::new);
		HandledScreens.register(Registration.REDSTONE_TIMER_SCREEN_HANDLER, RedstoneTimerScreen::new);
		HandledScreens.register(Registration.TELEPORTER_SCREEN_HANDLER, TeleporterScreen::new);
		HandledScreens.register(Registration.TELEPORT_INHIBITOR_SCREEN_HANDLER, TeleportInhibitorScreen::new);

		// Block Entity Renderers
		BlockEntityRendererFactories.register(Registration.REDSTONE_TRANSMITTER_BLOCK_ENTITY, RedstoneTransmitterBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Registration.TELEPORT_INHIBITOR_BLOCK_ENTITY, TeleportInhibitorBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Registration.TELEPORTER_BLOCK_ENTITY, TeleportBlockEntityRenderer::new);

		// Particles
		ParticleFactoryRegistry.getInstance().register(Registration.ENDER_FLAME_PARTICLE, ModFlameParticle.Factory::new);
	}
}
