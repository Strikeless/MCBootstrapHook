/*
 * MCBootstrapHook: Inject to a Minecraft server's Netty bootstrap with ease
 * Copyright (C) 2022  Strikeless
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.strikeless.bootstraphook.api;

import io.github.strikeless.bootstraphook.api.util.NMSUtil;
import io.netty.channel.*;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main entry for the public API.
 *
 * @author Strikeless
 * @since 04.08.2022
 */
@Data
@Builder
public class BootstrapHook {

    private static final Map<String, ChannelInitializer<Channel>> CHANNEL_INITIALIZER_MAP = new HashMap<>();

    private static boolean injectedAcceptors;

    /**
     * The {@link ChannelInitializer<Channel>} that will be injected to the bootstrap upon initialization.
     */
    @NonNull
    private final ChannelInitializer<Channel> channelInitializer;

    /**
     * The name for the {@link ChannelInitializer<Channel>} that MCBootstrapHook will inject.
     * If null, we'll let Netty generate the name automatically.
     */
    private final String channelInitializerName;

    /**
     * Injects BootstrapHook's internal "acceptors" to the bootstrap.
     * This'll allow BootstrapHook to inject the {@link ChannelInitializer<Channel>}s provided by dependants.
     * <p>
     * This does not need to be called manually, as {@link #inject()} automatically does it.
     */
    public static void injectAcceptors() throws BootstrapHookException {
        try {
            if (!injectedAcceptors) {
                final Object nmsServer = NMSUtil.getServerInstance();
                final List<ChannelFuture> channelFutures = NMSUtil.getServerChannelFutures(nmsServer);

                for (final ChannelFuture channelFuture : channelFutures) {
                    injectAcceptor(channelFuture.channel().pipeline());
                }

                injectedAcceptors = true;
            }
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException ex) {
            throw new BootstrapHookException(ex);
        }
    }

    /**
     * Injects the {@link ChannelInitializer<Channel>} to the given server's {@link ChannelPipeline}.
     *
     * @param serverChannelPipeline The {@link ChannelPipeline} which the {@link ChannelInitializer<Channel>}
     *                              will be added to.
     */
    private static void injectAcceptor(final ChannelPipeline serverChannelPipeline) {
        // We must add to the first position as ServerBootstrapAcceptor doesn't forward the event.
        serverChannelPipeline.addFirst("MCBootstrapHookAcceptor", new ChannelInboundHandlerAdapter() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                // Forward the event to other handlers before processing this ourselves.
                super.channelRead(ctx, msg);

                final Channel childChannel = (Channel) msg;

                // Add the ChannelInitializers to the client's/child's pipeline.
                CHANNEL_INITIALIZER_MAP.forEach((name, initializer) -> {
                    if (name == null) childChannel.pipeline().addLast(initializer);
                    else childChannel.pipeline().addLast(name, initializer);
                });
            }
        });
    }

    /**
     * Injects the {@link ChannelInitializer<Channel>} to the server's bootstrap.
     *
     * @throws BootstrapHookException if the injection was unsuccessful.
     */
    public void inject() throws BootstrapHookException {
        CHANNEL_INITIALIZER_MAP.put(this.getChannelInitializerName(), this.getChannelInitializer());

        try {
            injectAcceptors();
        } catch (final Exception ex) {
            throw new BootstrapHookException("Failed to initialize", ex);
        }
    }

    /**
     * Ejects the {@link ChannelInitializer<Channel>} from the server's bootstrap.
     * Note that this does not undo anything done by the initializer.
     */
    public void eject() {
        CHANNEL_INITIALIZER_MAP.remove(this.getChannelInitializerName());
    }
}
