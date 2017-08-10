package org.python.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Immutable hash map implementation for properties. Properties are keyed on string.
 * Copying and cloning is avoided by replying on immutability.
 */
public class PropertyHashMap implements Map<Object, Property> {
    public static final PropertyHashMap EMPTY_HASHMAP = new PropertyHashMap();

    private static final int INITIAL_BINS = 32;
    private static final int LIST_THRESHOLD = 8;
    /** Number of properties in the map. */
    private final int size;

    /** Threshold before growing the bins */
    private final int threshold;

    /** Reverse list of all properties. */
    private final Element list;

    /** Hash map bins */
    private final Element bins[];

    /** All properties as an array */
    private Property[] properties;

    private PropertyHashMap() {
        this.size = 0;
        this.threshold = 0;
        this.list = null;
        this.bins = null;
    }

    /**
     * Clone constructor
     * @param map Original {@link PropertyHashMap}
     */
    private PropertyHashMap(PropertyHashMap map) {
        this.size = map.size;
        this.threshold = map.threshold;
        this.list = map.list;
        this.bins = map.bins;
    }

    private PropertyHashMap(final int size, final Element[] bins, final Element list) {
        this.size = size;
        this.threshold = bins == null ? 0 : threeQuarters(bins.length);
        this.bins = bins;
        this.list = list;
    }

    /**
     * Clone a {@link PropertyHashMap} and replace a property with a new one in same place.
     * @param property old property
     * @param newProperty new property
     * @return New {@link PropertyHashMap}.
     */
    public PropertyHashMap immutableReplace(final Property property, final Property newProperty) {
        return cloneMap().replaceNoClone(property.getKey(), newProperty);
    }

    /**
     * Clone a {@link PropertyHashMap} and add a {@link Property}.
     * @param property {@link Property} to add.
     * @return New {@link PropertyHashMap}.
     */
    public PropertyHashMap immutableAdd(final Property property) {
        final int newSize = size + 1;
        PropertyHashMap newMap = cloneMap(newSize);
        newMap = newMap.addNoClone(property);
        return newMap;
    }

    /**
     * Clone a {@link PropertyHashMap} and add an array of properties.
     * @param newProperties Properties to add.
     * @return New {@link PropertyHashMap}.
     */
    public PropertyHashMap immutableAdd(final Property... newProperties) {
        final int newSize = size + newProperties.length;
        PropertyHashMap newMap = cloneMap(newSize);
        for (final Property property : newProperties) {
            newMap = newMap.addNoClone(property);
        }
        return newMap;
    }

    /**
     * Clone a {@link PropertyHashMap} and add a collection of properties.
     * @param newProperties Properties to add.
     * @return New {@link PropertyHashMap}.
     */
    public PropertyHashMap immutableAdd(final Collection<Property> newProperties) {
        if (newProperties != null) {
            final int newSize = size + newProperties.size();
            PropertyHashMap newMap = cloneMap(newSize);
            for (final Property property : newProperties) {
                newMap = newMap.addNoClone(property);
            }
            return newMap;
        }
        return this;
    }

    /**
     * Clone a {@link PropertyHashMap} and remove a {@link Property} based on its key.
     * @param key Key of {@link Property} to remove.
     * @return New {@link PropertyHashMap}.
     */
    public PropertyHashMap immutableRemove(final Object key) {
        if (bins != null) {
            final int binIndex = binIndex(bins, key);
            final Element bin = bins[binIndex];
            if (findElement(bin, key) != null) {
                final int newSize = size - 1;
                Element[] newBins = null;
                if (newSize > LIST_THRESHOLD) {
                    newBins = bins.clone();
                    newBins[binIndex] = removeFromList(bin, key);
                }
                final Element newList = removeFromList(list, key);
                return new PropertyHashMap(newSize, newBins, newList);
            }
        } else if (findElement(list, key) != null) {
            final int newSize = size - 1;
            return newSize != 0 ? new PropertyHashMap(newSize, null, removeFromList(list, key)) : EMPTY_HASHMAP;
        }
        return this;
    }

    private static int binsNeeded(final int n) {
        return 1 << 32 - Integer.numberOfLeadingZeros(n + (n >>> 1) | INITIAL_BINS - 1);
    }

    private static int threeQuarters(final int n) {
        return (n >>> 1) + (n >>> 2);
    }

    private static int binIndex(final Element[] bins, final Object key) {
        return key.hashCode() & bins.length - 1;
    }

    private Element findElement(final Object key) {
        if (bins != null) {
            final int binIndex = binIndex(bins, key);
            return findElement(bins[binIndex], key);
        }
        return findElement(list, key);
    }

    private static Element findElement(final Element list, final Object key) {
        final int hashCode = key.hashCode();
        for (Element element = list; element != null; element = list.link) {
            if (element.match(key, hashCode)) {
                return element;
            }
        }
        return null;
    }

    private PropertyHashMap cloneMap() {
        return new PropertyHashMap(size, bins == null ? null : bins.clone(), list);
    }

    /**
     * Clone {@link PropertyHashMap} to accomodate new size.
     * @param newSize New size of {@link PropertyHashMap}.
     * @return Cloned {@link PropertyHashMap} with new size.
     */
    private PropertyHashMap cloneMap(final int newSize) {
        Element[] newBins;
        if (bins == null && newSize <= LIST_THRESHOLD) {
            newBins = null;
        } else if (newSize > LIST_THRESHOLD) {
            newBins = rehash(list, binsNeeded(newSize));
        } else {
            newBins = bins.clone();
        }
        return new PropertyHashMap(newSize, newBins, list);
    }

    /**
     * Regenerate the bin table after changing the number of bins.
     * @param list List of all properties.
     * @param binSize new size of bins
     * @return the populated bins
     */
    private static Element[] rehash(final Element list, final int binSize) {
        final Element[] newBins = new Element[binSize];
        for (Element element = list; element != null; element = element.getLink()) {
            final Property property = element.getProperty();
            final Object key = property.getKey();
            final int binIndex = binIndex(newBins, key);
            newBins[binIndex] = new Element(newBins[binIndex], property);
        }
        return newBins;
    }

    private static Element replaceInList(final Element list, final Object key, final Property property) {
        final int hashCode = key.hashCode();
        if (list.match(key, hashCode)) {
            return new Element(list.link, property);
        }
        final Element head = new Element(null, property);
        Element previous = head;
        for (Element element = list.link; element != null; element = element.link) {
            if (element.match(key, hashCode)) {
                previous.setLink(new Element(element.link, property));
                return head;
            }
            final Element next = new Element(null, element.property);
            previous.setLink(next);
            previous = next;
        }
        return list;
    }

    /**
     * Remove an {@link Element} from a specific list, avoiding duplication.
     * @param list List to remove from.
     * @param key Key of the {@link Element} to remove.
     * @return
     */
    private static Element removeFromList(final Element list, final Object key) {
        if (list == null) {
            return null;
        }
        final int hashCode = key.hashCode();
        if (list.match(key, hashCode)) {
            return list.getLink();
        }
        final Element head = new Element(null, list.getProperty());
        Element previous = head;
        for (Element element = list.getLink(); element != null; element = element.getLink()) {
            if (element.match(key, hashCode)) {
                previous.setLink(element.getLink());
                return head;
            }
            final Element next = new Element(null, element.getProperty());
            previous.setLink(next);
            previous = next;
        }
        return list;
    }

    /**
     * Add a {@link Property} to a temporary {@link PropertyHashMap}, which has been already cloned.
     * Removes duplicates if necessary.
     * @param property {@link Property} to add.
     * @return New {@link PropertyHashMap}.
     */
    private PropertyHashMap addNoClone(final Property property) {
        int newSize = size;
        final Object key = property.getKey();
        Element newList = list;
        if (bins != null) {
            final int binIndex = binIndex(bins, key);
            Element bin = bins[binIndex];
            if (findElement(bin, key) != null) {
                newSize--;
                bin = removeFromList(bin, key);
                newList = removeFromList(newList, key);
            }
            bins[binIndex] = new Element(bin, property);
        } else {
            if (findElement(list, key) != null) {
                newSize--;
                newList = removeFromList(newList, key);
            }
        }
        newList = new Element(newList, property);
        return new PropertyHashMap(newSize, bins, newList);
    }

    private PropertyHashMap replaceNoClone(final Object key, final Property property) {
        if (bins != null) {
            final int binIndex = binIndex(bins, key);
            Element bin = bins[binIndex];
            bin = replaceInList(bin, key, property);
            bins[binIndex] = bin;
        }
        Element newList = list;
        newList = replaceInList(newList, key, property);
        return new PropertyHashMap(size, bins, newList);
    }

    Property[] getProperties() {
        if (properties == null) {
            final Property[] array = new Property[size];
            int i = size;
            for (Element element = list; element != null; element = element.link) {
                array[--i] = element.property;
            }
            properties = array;
        }
        return properties;
    }

    /**
     *  Map implementation
     */

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return findElement(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value instanceof Property) {
            final Property property = (Property) value;
            final Element element = findElement(property.getKey());
            return element != null && element.property.equals(value);
        }
        return false;
    }

    @Override
    public Property get(Object key) {
        final Element element = findElement(key);
        return element == null ? null : element.property;
    }

    @Override
    public Property put(Object key, Property value) {
        throw new UnsupportedOperationException("Immutalbe map.");
    }

    @Override
    public Property remove(Object key) {
        throw new UnsupportedOperationException("Immutalbe map.");
    }

    @Override
    public void putAll(Map<? extends Object, ? extends Property> m) {
        throw new UnsupportedOperationException("Immutalbe map.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Immutalbe map.");
    }

    @Override
    public Set<Object> keySet() {
        final HashSet<Object> set = new HashSet<>();
        for (Element element = list; element != null; element = element.link) {
            set.add(element.getKey());
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Collection<Property> values() {
        return Collections.unmodifiableList(Arrays.asList(getProperties()));
    }

    @Override
    public Set<Entry<Object, Property>> entrySet() {
        final HashSet<Entry<Object, Property>> set = new HashSet<>();
        for (Element element = list; element != null; element = element.link) {
            set.add(element);
        }
        return Collections.unmodifiableSet(set);
    }

    static final class Element implements Entry<Object, Property> {
        /** Linked element reference. */
        private Element link;

        /** Element property. */
        private final Property property;

        /** Element key. */
        private final Object key;

        /** Element key hash code. */
        private final int hashCode;

        Element(final Element link, final Property property) {
            this.link = link;
            this.property = property;
            this.key = property.getKey();
            this.hashCode = this.key.hashCode();
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Property getValue() {
            return property;
        }

        @Override
        public Property setValue(Property value) {
            throw new UnsupportedOperationException("Immutable map.");
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("[");
            Element element = this;
            do {
                sb.append(element.getValue());
                element = element.link;
                if (element != null) {
                    sb.append(" -> ");
                }
            } while (element != null);
            sb.append("]");
            return sb.toString();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        boolean match(final Object otherKey, final int otherHashCode) {
            return this.hashCode == otherHashCode && this.key.equals(otherKey);
        }

        public Element getLink() {
            return link;
        }

        public void setLink(Element link) {
            this.link = link;
        }

        public Property getProperty() {
            return property;
        }
    }
}
