/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
@lombok.ToString
public class Population {

    public static final int UNKNOWN_SIZE = -1;
    public static final Population UNKNOWN = new Population(UNKNOWN_SIZE, Double.NaN, Double.NaN, false);

    private final int size;
    private final double mean, variance;
    private final boolean normal;

    public static class PopulationBuilder{
    private int size=UNKNOWN_SIZE;
    private double mean, variance=Double.NaN;
    private boolean normal=true;
        
    }
}
