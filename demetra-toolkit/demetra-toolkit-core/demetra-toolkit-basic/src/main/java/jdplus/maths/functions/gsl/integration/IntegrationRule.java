/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.functions.gsl.integration;

import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
public interface IntegrationRule {
    IntegrationResult integrate(DoubleUnaryOperator fn, double a, double b);
}
