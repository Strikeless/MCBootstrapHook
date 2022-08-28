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

    public Field getFieldByName(final Object object, final String fieldName) throws NoSuchFieldException {
        return getInheritedDeclaredField(getClassFromObject(object), fieldName);
    }

    public Field getFieldByType(final Object object, final Class<?> type) throws NoSuchFieldException {
        for (final Field field : getInheritedDeclaredFields(getClassFromObject(object))) {
            if (type.isAssignableFrom(field.getType())) return field;
        }

        throw new NoSuchFieldException("Type: " + type.getName());
    }

    public Field getFieldByTypeSuffix(final Object object, final String suffix) throws NoSuchFieldException {
        for (final Field field : getInheritedDeclaredFields(getClassFromObject(object))) {
            if (field.getType().getName().endsWith(suffix)) return field;
        }

        throw new NoSuchFieldException("Type suffix: " + suffix);
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

    private Field getInheritedDeclaredField(final Class<?> klass, final String name) throws NoSuchFieldException {
        try {
            // Try to find the field from the provided class
            return klass.getDeclaredField(name);
        } catch (final NoSuchFieldException ex) {
            // Check if we can't recurse any further
            if (klass.equals(Object.class)) throw new NoSuchFieldException(name);

            // Try to find the same field from the class's superclass with recursion
            return getInheritedDeclaredField(klass.getSuperclass(), name);
        }
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
