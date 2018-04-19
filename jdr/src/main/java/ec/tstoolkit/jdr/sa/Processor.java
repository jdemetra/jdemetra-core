/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.sa;

import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.simplets.TsData;
import jdr.spec.ts.Utility;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Processor {

 
    public static TramoSeatsResults tramoseats(TsData s, TramoSeatsSpecification spec, Utility.Dictionary dic) {
        ProcessingContext context = null;
        if (dic != null) {
            context = dic.toContext();
        }
        return new TramoSeatsResults(TramoSeatsProcessingFactory.process(s, spec, context));
    }

    public static X13Results x13(TsData s,X13Specification spec, Utility.Dictionary dic) {
        ProcessingContext context = null;
        if (dic != null) {
            context = dic.toContext();
        }
        return new X13Results(X13ProcessingFactory.process(s, spec, context));
    }

}
