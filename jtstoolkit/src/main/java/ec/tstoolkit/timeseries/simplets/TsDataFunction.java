/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.timeseries.simplets;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@FunctionalInterface
public interface TsDataFunction {
    double apply(TsData s, int pos);
}
