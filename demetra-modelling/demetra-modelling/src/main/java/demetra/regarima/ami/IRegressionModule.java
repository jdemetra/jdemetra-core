/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.regarima.ami;

import demetra.regarima.regular.RegArimaContext;
import demetra.information.InformationSet;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IRegressionModule {
    ProcessingResult test(RegArimaContext context);
}
