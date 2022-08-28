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
    public List<ChannelFuture> getServerChannelFutures(final Object server)
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        final Class<?> serverConnectionClass = getNMSClass("ServerConnection", "network.ServerConnection");
        final Field serverConnectionField = ReflectiveUtil.getFieldByType(server, serverConnectionClass);
        final Object serverConnection = ReflectiveUtil.getFieldValue(server, serverConnectionField);

        // Get the list of ChannelFutures in the ServerConnection instance.
        // For now, we'll just assume that the list we want is the first list in the class.
        final Field channelFuturesField = ReflectiveUtil.getFieldByType(serverConnection, List.class);
        return ReflectiveUtil.getFieldValue(serverConnection, channelFuturesField);
    }
}
