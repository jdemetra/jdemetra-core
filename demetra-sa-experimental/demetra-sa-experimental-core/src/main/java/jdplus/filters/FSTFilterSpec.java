/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

/**
 *
 * @author Jean Palate
 */
@lombok.Data
public class FSTFilterSpec {

    private int polynomialPreservationDegree = 2;
    private int smoothnessDegree = 3;
    private int lags = 6, leads = 6;
    private double w0 = 0, w1 = Math.PI / 8;
    private boolean antiphase = true;

    private double smoothnessWeight, TimelinessWeight;
}
