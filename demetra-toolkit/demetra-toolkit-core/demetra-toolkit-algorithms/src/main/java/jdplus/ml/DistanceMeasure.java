/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ml;

/**
 *
 * @author palatej
 */
public interface DistanceMeasure<Z> {

    /**
     * Compute the distance between two objects.
     *
     * @param a the first vector
     * @param b the second vector
     * @return the distance between the two vectors
     */
    double compute(Z a, Z b);
}
