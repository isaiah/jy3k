package org.python.core;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class PropertyMap implements Iterable<Object> {

    /** Map status flags. */
    private final int flags;

    private transient PropertyHashMap properties;

    /** Number of fields in use. */
    private final int fieldCount;

    /** History of maps, used to limit map duplication. */
    private transient WeakHashMap<Property, Reference<PropertyMap>> history;

    private PropertyMap(PropertyHashMap properties, int flags, int fieldCount) {
        this.properties = properties;
        this.flags = flags;
        this.fieldCount = fieldCount;
    }

    public final synchronized PropertyMap addProperty(final Property property) {
        propertyAdded(property, true);
        PropertyMap newMap = checkHistory(property);
        if (newMap == null) {
            newMap = addPropertyInternal(property);
            addToHistory(property, newMap);
        }
        return newMap;
    }

    /**
     * Find a property in the map.
     * @param key Key to search for
     * @return {@link Property} matching key
     */
    public final Property find(final Object key) {
        return properties.get(key);
    }

    /**
     * Adds all map properties from another map.
     * @param other The source of properties.
     * @return New {@link PropertyMap} with added properties.
     */
    public final PropertyMap addAll(final PropertyMap other) {
        assert this != other;
        final Property[] otherProperties = other.properties.getProperties();
        final PropertyHashMap newProperties = properties.immutableAdd(otherProperties);
        final PropertyMap newMap = deriveMap(newProperties, flags, fieldCount);
        return newMap;
    }

    /**
     * Return an array of all properties
     * @return
     */
    public final Property[] getProperties() {
        return properties.getProperties();
    }

    private PropertyMap deriveMap(final PropertyHashMap newProperties, final int newFlags, final int newFieldCount) {
        return new PropertyMap(newProperties, newFlags, newFieldCount);
    }

    private int newFieldCount(final Property property) {
        return 0;
    }

    private int newFlags(final Property property) {
        return 0;
    }

    private PropertyMap addPropertyInternal(final Property property) {
        final PropertyHashMap newProperties = properties.immutableAdd(property);
        final PropertyMap newMap = deriveMap(newProperties, newFlags(property), newFieldCount(property));
//        newMap.updateFreeSlots(null, property);
        return newMap;
    }

    private void addToHistory(final Property property, final PropertyMap newMap) {
        if (history == null) {
            history = new WeakHashMap<>();
        }
        history.put(property, new WeakReference<>(newMap));
    }

    private PropertyMap checkHistory(final Property property) {
        if (history == null) {
            final Reference<PropertyMap> ref = history.get(property);
            final PropertyMap historicMap = ref == null ? null : ref.get();
            if (historicMap != null) {
                return historicMap;
            }
        }
        return null;
    }

    public void propertyAdded(final Property property, final boolean isSelf) {
    }

    public void propertyDeleted(final Property property, final boolean isSelf) {
    }

    public void propertyModified(final Property oldProperty, final Property newProperty, final boolean isSelf) {
    }

    @Override
    public Iterator<Object> iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer<? super Object> action) {

    }

    @Override
    public Spliterator<Object> spliterator() {
        return null;
    }
}
