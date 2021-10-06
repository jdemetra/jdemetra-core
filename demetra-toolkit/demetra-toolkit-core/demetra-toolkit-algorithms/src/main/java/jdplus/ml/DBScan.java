/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ml;

import demetra.util.IntList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

/**
 *
 * @author palatej
 */
public class DBScan<Z> extends Clusterer<Z, DBScan.Results> {

    @lombok.Value
    public static class Results {

        final List<IntList> clusters;
        final IntList noises;
    }

    /**
     * Maximum radius of the neighbourhood to be considered.
     */
    private final double eps;

    /**
     * Minimum number of points needed for a cluster.
     */
    private final int minPts;

    /**
     * Status of a point during the clustering process.
     */
    private enum PointStatus {
        /**
         * The point has is considered to be noise.
         */
        NOISE,
        /**
         * The point is part of a cluster.
         */
        PART_OF_CLUSTER
    }

    /**
     * Creates a new instance of a DBSCANClusterer.
     * <p>
     * The euclidean distance will be used as default distance measure.
     *
     * @param eps maximum radius of the neighborhood to be considered
     * @param minPts minimum number of points needed for a cluster
     * @param distance
     */
    public DBScan(final double eps, final int minPts, DistanceMeasure<Z> distance) {
        super(distance);
        this.eps = eps;
        this.minPts = minPts;
    }

    /**
     * Returns the maximum radius of the neighborhood to be considered.
     *
     * @return maximum radius of the neighborhood
     */
    public double getEps() {
        return eps;
    }

    /**
     * Returns the minimum number of points needed for a cluster.
     *
     * @return minimum number of points needed for a cluster
     */
    public int getMinPts() {
        return minPts;
    }

    @Override
    public Results cluster(IntFunction<Z> points, int size) {

        final List<IntList> clusters = new ArrayList<>();
        final Map<Integer, PointStatus> visited = new HashMap<>();

        for (int i = 0; i < size; ++i) {
            if (visited.get(i) != null) {
                continue;
            }
            final IntList neighbors = neighbors(i, points, size);
            if (neighbors.size() >= minPts) {
                // DBSCAN does not care about center points
                final IntList cluster = new IntList();
                clusters.add(expandCluster(cluster, i, neighbors, points, size, visited));
            } else {
                visited.put(i, PointStatus.NOISE);
            }
        }

        IntList noises = new IntList();
        visited.entrySet().stream().filter(entry -> entry.getValue() == PointStatus.NOISE).mapToInt(entry -> entry.getKey()).forEach(i -> noises.add(i));

        return new Results(clusters, noises);
    }

    /**
     * Performs DBSCAN cluster analysis.
     *
     * @param points the points to cluster
     * @param size
     * @return the list of clusters
     */
    /**
     * Expands the cluster to include density-reachable items.
     *
     * @param cluster Cluster to expand
     * @param point Point to add to cluster
     * @param neighbors List of neighbors
     * @param points the data set
     * @param visited the set of already visited points
     * @return the expanded cluster
     */
    private IntList expandCluster(final IntList cluster,
            final int point,
            final IntList neighbors,
            final IntFunction<Z> points,
            final int size,
            final Map<Integer, PointStatus> visited) {
        cluster.add(point);
        visited.put(point, PointStatus.PART_OF_CLUSTER);

        IntList seeds = new IntList(neighbors);
        int index = 0;
        while (index < seeds.size()) {
            int sidx = seeds.get(index);
            PointStatus pStatus = visited.get(sidx);
            // only check non-visited points
            if (pStatus == null) {
                final IntList currentNeighbors = neighbors(sidx, points, size);
                if (currentNeighbors.size() >= minPts) {
                    seeds = merge(seeds, currentNeighbors);
                }
            }
            if (pStatus != PointStatus.PART_OF_CLUSTER) {
                visited.put(sidx, PointStatus.PART_OF_CLUSTER);
                cluster.add(sidx);
            }

            index++;
        }
        return cluster;
    }

    /**
     * Returns a list of density-reachable neighbors of a {@code point}.
     *
     * @param point the point to look for
     * @param points possible neighbors
     * @return the List of neighbors
     */
    private IntList neighbors(final int point, final IntFunction<Z> points, final int size) {
        final IntList neighbors = new IntList();
        Z cur = points.apply(point);
        for (int j = 0; j < size; ++j) {
            if (point != j && distance(points.apply(j), cur) <= eps) {
                neighbors.add(j);
            }
        }
        return neighbors;
    }

    /**
     * Merges two lists together. The lists are ordered !
     *
     * @param one first list
     * @param two second list
     * @return merged lists
     */
    private IntList merge(final IntList one, final IntList two) {
        if (one.isEmpty()) {
            return two;
        }
        if (two.isEmpty()) {
            return one;
        }
        final IntList all = new IntList();
        int sone = one.size(), stwo = two.size(), ione = 0, itwo = 0;
        int cone = one.get(ione), ctwo = two.get(itwo);
        do {
            if (cone == ctwo) {
                all.add(cone);
                ++ione;
                ++itwo;
                if (ione < sone) {
                    cone = one.get(ione);
                } else {
                    break;
                }
                if (itwo < stwo) {
                    ctwo = two.get(itwo);
                } else {
                    break;
                }
            } else if (cone < ctwo) {
                all.add(cone);
                ++ione;
                if (ione < sone) {
                    cone = one.get(ione);
                } else {
                    break;
                }
            } else {
                all.add(ctwo);
                ++itwo;
                if (itwo < stwo) {
                    ctwo = two.get(itwo);
                } else {
                    break;
                }
            }
        } while (true);
        if (ione < sone) {
            for (int i = ione; i < sone; ++i) {
                all.add(one.get(i));
            }
        }
        if (itwo < stwo) {
            for (int i = itwo; i < stwo; ++i) {
                all.add(two.get(i));
            }
        }

        return one;
    }

}
