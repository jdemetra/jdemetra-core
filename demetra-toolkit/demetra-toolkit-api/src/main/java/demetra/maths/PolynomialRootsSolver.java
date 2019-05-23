/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths;

import demetra.design.Algorithm;
import demetra.design.ServiceDefinition;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Algorithm
@ServiceDefinition
public interface PolynomialRootsSolver {
    Complex[] roots(@Nonnull PolynomialType polynomial);
    
}
