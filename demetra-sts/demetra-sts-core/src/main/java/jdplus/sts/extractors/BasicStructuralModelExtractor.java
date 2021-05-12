/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.extractors;

import nbbrd.design.Development;
import demetra.information.InformationMapping;
import demetra.sts.Component;
import jdplus.sts.BsmData;

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


    private final InformationMapping<BsmData> MAPPING = new InformationMapping<>(BsmData.class);

    static {
            MAPPING.set(LVAR, Double.class, source -> source.getLevelVar());
            MAPPING.set(SVAR, Double.class, source -> source.getSlopeVar());
            MAPPING.set(CVAR, Double.class, source -> source.getCycleVar());
            MAPPING.set(SEASVAR, Double.class, source -> source.getSeasonalVar());
            MAPPING.set(NVAR, Double.class, source -> source.getNoiseVar());
            MAPPING.set(CDUMP, Double.class, source -> source.getCycleDumpingFactor());
            MAPPING.set(CLENGTH, Double.class, source -> source.getCycleLength());
    }

    public InformationMapping<BsmData> getMapping() {
        return MAPPING;
    }


}
