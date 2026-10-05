package com.thenathe.chalkcompat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;

/** The server confirms the result; no global flag can leak across reconnects or integrated worlds. */
public final class ChalkCompatClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(NativeClients.StateRequest.TYPE, (request, context) -> {
            // Fabric has acknowledged its remap before the server queues this request.
            context.client().execute(() -> {
                var blocks = new java.util.ArrayList<NativeClients.BlockIds>();
                var mapper = net.minecraft.world.level.block.Block.BLOCK_STATE_REGISTRY;
                for (var block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
                    var states = block.getStateDefinition().getPossibleStates();
                    int[] ids = new int[states.size()];
                    for (int i = 0; i < ids.length; i++) ids[i] = mapper.getId(states.get(i));
                    blocks.add(new NativeClients.BlockIds(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block), ids));
                }
                int bits = Math.max(1, 32 - Integer.numberOfLeadingZeros(mapper.size() - 1));
                context.responseSender().sendPacket(new NativeClients.StateReply(bits, blocks));
            });
        });
    }
}
