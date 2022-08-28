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

/**
 * An exception thrown by a failed attempt at injecting to the server's bootstrap.
 *
 * @author Strikeless
 * @since 04.08.2022
 */
public class BootstrapHookException extends Exception {

    public BootstrapHookException(final String s) {
        super(s);
    }

    public BootstrapHookException(final Throwable throwable) {
        super(throwable);
    }

    public BootstrapHookException(final String s, final Throwable throwable) {
        super(s, throwable);
    }
}
