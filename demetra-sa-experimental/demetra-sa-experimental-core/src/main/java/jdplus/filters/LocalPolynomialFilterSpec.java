/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import jdplus.math.linearfilters.AsymmetricFiltersFactory;

/**
 *
 * @author Jean Palate
 */
@lombok.Data
public class LocalPolynomialFilterSpec {

    private int filterLength=6;
    private KernelOption kernel=KernelOption.Henderson;
    private int polynomialDegree=2;
    private AsymmetricFiltersFactory.Option asymmetricFilters=AsymmetricFiltersFactory.Option.MMSRE;
    private int asymmetricPolynomialDegree=0;
    private double[] linearModelCoefficients=new double[]{2/(Math.sqrt(Math.PI)*3.5)};
    private double timelinessWeight;
    private double passBand=Math.PI/18;
}
