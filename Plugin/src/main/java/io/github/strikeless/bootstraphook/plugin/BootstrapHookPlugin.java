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

package io.github.strikeless.bootstraphook.plugin;

import io.github.strikeless.bootstraphook.api.BootstrapHook;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Strikeless
 * @since 04.08.2022
 */
public class BootstrapHookPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        try {
            // Inject the acceptors here so that we can handle exceptions and
            // that dependants won't have to worry about failure.
            BootstrapHook.injectAcceptors();

            this.getLogger().info(
                    this.getDescription().getName() + " " + this.getDescription().getVersion() + " loaded."
            );
        } catch (final Exception ex) {
            this.getLogger().severe("Failed to inject acceptors!");
            ex.printStackTrace();
            this.setEnabled(false);
        }
    }
}
