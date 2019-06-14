/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regarima.ami;

import demetra.design.Development;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
@lombok.Builder
public class TransformedSeries {

    public double transformationCorrection;
    public double[] data;
    public int[] missing;
}
