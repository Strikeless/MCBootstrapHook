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

package io.github.strikeless.bootstraphook.api.util;

import io.netty.channel.ChannelFuture;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.List;

/**
 * A utility class providing methods to get parts of NMS
 *
 * @author Strikeless
 * @since 27.08.2022
 */
@UtilityClass
public class NMSUtil {

    // TODO: Make this package thing simpler and a bit less messy

    /**
     * The package name used by CraftBukkit
     */
    public static final String OBC_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    /**
     * The version string used by CraftBukkit
     */
    public static final String OBC_VERSION_STRING = OBC_PACKAGE.split("\\.")[3];

    /**
     * Whether to use class names found in minor server versions 17 and above.
     */
    // Minor server versions 17 and above do not contain the version string in the NMS package,
    // however the OBC package is still providing us a version string, thus we need the integer check.
    // The isEmpty check is just there for potential future-proofing.
    public static final boolean USE_MODERN_NMS_NAMES = OBC_VERSION_STRING.isEmpty()
            || Integer.parseInt(OBC_VERSION_STRING.split("_")[1]) >= 17;

    /**
     * The package name used by NMS. Note that this will not work in case the package name has been changed by
     * fork obfuscation or such. This also relies on OBC's version string.
     */
    // "Modern" versions do not contain the version string in the NMS package.
    public static final String NMS_PACKAGE = "net.minecraft.server"
            + (USE_MODERN_NMS_NAMES ? "" : "." + OBC_VERSION_STRING);

    /**
     * Tries to find and returns a class of NMS with the given name.
     *
     * @param legacyName The class name used for server versions below 1.17
     * @param modernName The class name/path used for server versions above 1.17
     */
    public Class<?> getNMSClass(final String legacyName, final String modernName) throws ClassNotFoundException {
        return Class.forName(NMS_PACKAGE + "." + (USE_MODERN_NMS_NAMES ? modernName : legacyName));
    }

    /**
     * Tries to find and returns the NMS server instance.
     */
    public Object getServerInstance() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        final Class<?> klass = getNMSClass("MinecraftServer", "MinecraftServer");
        final Field field = ReflectiveUtil.getFieldByType(klass, klass);
        return ReflectiveUtil.getFieldValue(null, field);
    }

    /**
     * Returns a {@link List<ChannelFuture>} contained by NMS's ServerConnection instance.
     *
     * @param server The NMS server
     */
    public List<ChannelFuture> getServerChannelFutures(final Object server) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        final Class<?> serverConnectionClass = getNMSClass("ServerConnection", "network.ServerConnection");
        final Field serverConnectionField = ReflectiveUtil.getFieldByType(server, serverConnectionClass);
        final Object serverConnection = ReflectiveUtil.getFieldValue(server, serverConnectionField);

        // Get the list of ChannelFutures in the ServerConnection instance.
        // For now, we'll just assume that the list we want is the first list in the class.
        final Field channelFuturesField = ReflectiveUtil.getFieldByType(serverConnection, List.class);
        return ReflectiveUtil.getFieldValue(serverConnection, channelFuturesField);
    }
}
