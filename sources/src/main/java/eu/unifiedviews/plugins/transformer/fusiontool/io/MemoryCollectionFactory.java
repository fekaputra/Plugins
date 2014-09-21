package eu.unifiedviews.plugins.transformer.fusiontool.io;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper factory class for memory-backed collections.
 * Current implementation uses HashSet.
 * @author Jan Michelfeit
 */
@Deprecated
public class MemoryCollectionFactory implements LargeCollectionFactory {
    @Override
    public <T> Set<T> createSet() {
        return new HashSet<T>();
    }

    @Override
    public void close() {
        // do nothing
    }
}
