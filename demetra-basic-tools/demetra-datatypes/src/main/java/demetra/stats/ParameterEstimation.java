/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats;

import demetra.design.Development;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@Development(status = Development.Status.Beta)
public class ParameterEstimation {
    private double value;
    private double standardError;
    private String description;
   
}
