/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */

package io.github.strikeless.bootstraphook.plugin;

import io.github.strikeless.bootstraphook.api.BootstrapHook;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Strikeless
 * @since 04.08.2022
 */
public class BootstrapHookPlugin extends JavaPlugin implements Listener {

    @Override
    public void onLoad() {
        try {
            // Inject the acceptors here so that we can handle exceptions and that dependants won't have to.
            BootstrapHook.injectAcceptors();

            this.getLogger().info(
                    this.getDescription().getName() + " " + this.getDescription().getVersion() + " loaded."
            );

            BootstrapHook.builder()
                    .channelInitializerName("TestingInitializer")
                    .channelInitializer(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            System.out.println("Initialized channel " + String.join(", ", ch.pipeline().names()));
                        }
                    })
                    .build()
                    .inject();
        } catch (final Exception ex) {
            this.getLogger().severe("Failed to inject acceptors!");
            ex.printStackTrace();
            this.setEnabled(false);
        }
    }
}
