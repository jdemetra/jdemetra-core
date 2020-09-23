/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.extractors;

import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.sts.Component;
import jdplus.sts.BasicStructuralModel;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class BasicStructuralModelExtractor {

    public final static String 
            LVAR = "levelvar", SVAR = "slopevar", SEASVAR = "seasvar", CVAR = "cyclevar", NVAR = "noisevar",
                CDUMP = "cycledumpingfactor", CLENGTH = "cycleLength";


    private final InformationMapping<BasicStructuralModel> MAPPING = new InformationMapping<>(BasicStructuralModel.class);

    static {
            MAPPING.set(LVAR, Double.class, source -> source.getVariance(Component.Level));
            MAPPING.set(SVAR, Double.class, source -> source.getVariance(Component.Slope));
            MAPPING.set(CVAR, Double.class, source -> source.getVariance(Component.Cycle));
            MAPPING.set(SEASVAR, Double.class, source -> source.getVariance(Component.Seasonal));
            MAPPING.set(NVAR, Double.class, source -> source.getVariance(Component.Noise));
            MAPPING.set(CDUMP, Double.class, source -> source.getCyclicalDumpingFactor());
            MAPPING.set(CLENGTH, Double.class, source -> source.getCyclicalPeriod() / (6 * source.getPeriod()));
    }

    public InformationMapping<BasicStructuralModel> getMapping() {
        return MAPPING;
    }


}
