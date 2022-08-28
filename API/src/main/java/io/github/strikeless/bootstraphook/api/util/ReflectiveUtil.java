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

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * A utility class providing methods to do reflective operations.
 *
 * @author Strikeless
 * @since 27.08.2022
 */
@UtilityClass
public class ReflectiveUtil {

    public Field getFieldByType(final Object object, final Class<?> type) throws NoSuchFieldException {
        for (final Field field : getInheritedDeclaredFields(getClassFromObject(object))) {
            if (type.isAssignableFrom(field.getType())) return field;
        }

        throw new NoSuchFieldException("Type: " + type.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> T getFieldValue(final Object object, final Field field) throws IllegalAccessException {
        field.setAccessible(true);
        return (T) field.get(object);
    }

    private Class<?> getClassFromObject(final Object object) {
        if (object instanceof Class<?>) return (Class<?>) object;
        return object.getClass();
    }

    private Field[] getInheritedDeclaredFields(final Class<?> klass) {
        // Get all the inherited fields with recursion
        final Field[] inheritedFields;
        if (klass.equals(Object.class)) inheritedFields = new Field[0];
        else inheritedFields = getInheritedDeclaredFields(klass.getSuperclass());

        // Get all the fields specific to the provided class
        final Field[] ownFields = klass.getDeclaredFields();

        // Concentrate both our arrays
        final Field[] allFields = Arrays.copyOf(ownFields, ownFields.length + inheritedFields.length);
        System.arraycopy(inheritedFields, 0, allFields, ownFields.length, inheritedFields.length);

        return allFields;
    }
}
