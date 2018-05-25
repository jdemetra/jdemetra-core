/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.regarima.regular;

import demetra.regarima.regular.RegArimaModelling;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IRegressionModule {
    ProcessingResult test(RegArimaModelling context);
}
