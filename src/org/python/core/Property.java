package org.python.core;

public class Property {
    /** Property key. */
    private final Object key;

    /** Property flags. */
    private int flags;

    /** Property field number. */
    private final int slot;

    Property(Object key, int flags, int slots) {
        this.key = key;
        this.flags = flags;
        this.slot = slots;
    }

    Property(Property property, final int flags) {
        this.key = property.key;
        this.slot = property.slot;
        this.flags = flags;
    }

    public Object getKey() {
        return key;
    }
}
