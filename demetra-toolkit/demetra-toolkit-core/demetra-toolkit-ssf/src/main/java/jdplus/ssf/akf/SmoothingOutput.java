/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.ssf.akf;

import jdplus.ssf.univariate.DefaultSmoothingResults;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class SmoothingOutput {
    private DefaultAugmentedFilteringResults filtering;
    private double sig2;
    private DefaultSmoothingResults smoothing;
    
}
