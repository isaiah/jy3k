package org.python.io.util;

import org.python.core.Py;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * A selector pool for each jylang runtime, so we don't have to create a new one each time
 */
public class SelectorPool {
    private Map<SelectorProvider, Queue<Selector>> pool = new HashMap<>();
    private List<Selector> openSelectors = new ArrayList<>();

    public Selector get() {
        return get(SelectorProvider.provider());
    }

    public Selector get(SelectorProvider provider) {
        Queue<Selector> selectorPool = pool.get(provider);
        Selector selector;
        if (selectorPool == null || selectorPool.isEmpty()) {
            try {
                selector = provider.openSelector();
            } catch (IOException e) {
                throw Py.IOError(e);
            }
        } else {
            selector = selectorPool.remove();
        }
        openSelectors.add(selector);
        return selector;
    }

    public void put(Selector selector) {
        for (SelectionKey key : selector.keys()) {
            if (key != null) {
                key.cancel();
            }
        }
        try {
            selector.selectNow();
        } catch (IOException e) {
            // ignore
        }
        returnToPool(selector);
    }

    private void returnToPool(Selector selector) {
        openSelectors.remove(selector);
        if (selector.isOpen()) {
            SelectorProvider provider = selector.provider();
            Queue<Selector> selectorPool = pool.get(provider);
            if (selectorPool == null) {
                selectorPool = new LinkedList<>();
                pool.put(provider, selectorPool);
            }
            selectorPool.add(selector);
        }
    }
}
