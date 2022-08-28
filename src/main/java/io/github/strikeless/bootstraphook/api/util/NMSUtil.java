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
import lombok.Getter;
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
     * Returns the name of the package used by NMS
     */
    public String getNMSPackage() {
        return Bukkit.getServer().getClass().getPackage().getName()
                .replace("org.bukkit.craftbukkit", "net.minecraft.server");
    }

    /**
     * Returns a concentration of NMS's package name and the given string
     */
    public String getNMSClassName(final String name) {
        return getNMSPackage() + "." + name;
    }

    /**
     * Tries to find and returns the NMS server instance.
     */
    public Object getServerInstance() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        final Class<?> klass = Class.forName(getNMSClassName("MinecraftServer"));
        final Field field = ReflectiveUtil.getFieldByType(klass, klass);
        return ReflectiveUtil.getFieldValue(null, field);
    }

    /**
     * Returns a {@link List<ChannelFuture>} contained by NMS's ServerConnection instance.
     *
     * @param server The NMS server
     */
    public List<ChannelFuture> getServerChannelFutures(final Object server) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        // On newer server versions, the ServerConnection class is located in a network package, thus we cannot simply
        // use Class.forName() alongside ReflectiveUtil.getFieldByType(). We could technically check the server version
        // and get the class's path based on that, but this'll do.
        final Field serverConnectionField = ReflectiveUtil.getFieldByTypeSuffix(server, "ServerConnection");
        final Object serverConnection = ReflectiveUtil.getFieldValue(server, serverConnectionField);

        // Get the list of ChannelFutures in the ServerConnection instance.
        // We'll just assume that the list we want is the first list in the class.
        final Field channelFuturesField = ReflectiveUtil.getFieldByType(serverConnection, List.class);
        return ReflectiveUtil.getFieldValue(serverConnection, channelFuturesField);
    }
}
