/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.Doubles;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
class OrderedSampleWithMean implements OrderedSample {

    private final Doubles data;
    private final double mean;

    public OrderedSampleWithMean(Doubles data, double mean) {
        this.data = data;
        this.mean = mean;
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double autoCovariance(int lag) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
