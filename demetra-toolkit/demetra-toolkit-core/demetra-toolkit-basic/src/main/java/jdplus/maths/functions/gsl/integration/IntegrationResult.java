/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.functions.gsl.integration;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
public class IntegrationResult {
    private double result;
    private double absError;
    private double resultAbs;
    private double resultAsc;
}
