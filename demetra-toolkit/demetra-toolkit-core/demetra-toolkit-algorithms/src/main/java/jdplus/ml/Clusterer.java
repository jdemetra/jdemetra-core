/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ml;

import demetra.util.IntList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;

/**
 *
 * @author palatej
 * @param <Z>
 * @param <T>
 */
public abstract class Clusterer<Z, R> {

    /** The distance measure to use. */
    private final DistanceMeasure<Z> measure;

    /**
     * Build a new clusterer with the given {@link DistanceMeasure}.
     *
     * @param measure the distance measure to use
     */
    protected Clusterer(final DistanceMeasure<Z> measure) {
        this.measure = measure;
    }

    /**
     * Perform a cluster analysis on the given set of {@link Clusterable} instances.
     *
     * @param points the set of {@link Clusterable} instances
     * @return a {@link List} of clusters
     */
    public abstract R cluster(IntFunction<Z> points, int size);

    /**
     * Returns the {@link DistanceMeasure} instance used by this clusterer.
     *
     * @return the distance measure
     */
    public DistanceMeasure getDistanceMeasure() {
        return measure;
    }

    /**
     * Calculates the distance between two {@link Clusterable} instances
     * with the configured {@link DistanceMeasure}.
     *
     * @param p1
     * @param p2
      * @return the distance between the two items
     */
    protected double distance(final Z p1, final Z p2) {
        return measure.compute(p1, p2);
    }
    
}
