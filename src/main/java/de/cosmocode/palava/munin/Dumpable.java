package de.cosmocode.palava.munin;

/**
 * {@link Dumpable} can be represented in {@link String}s.
 *
 * @since 1.3
 * @author Willi Schoenborn
 */
interface Dumpable {

    /**
     * Dumps this object into a string.
     *
     * @since 1.3
     * @return a string representation of this object
     */
    String dump();
    
}
