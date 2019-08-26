/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11plus;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class X11TrendOptions {
    int trendFilterLength;
    int localPolynomialDegree;
    String kernel;
    
    String asymmetricEndPoints;
    
}
