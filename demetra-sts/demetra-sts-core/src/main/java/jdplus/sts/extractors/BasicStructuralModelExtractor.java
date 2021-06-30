/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.extractors;

import demetra.information.InformationExtractor;
import nbbrd.design.Development;
import demetra.information.InformationMapping;
import demetra.sts.Component;
import jdplus.sts.BsmData;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class BasicStructuralModelExtractor extends InformationMapping<BsmData> {

    public final static String LVAR = "levelvar", SVAR = "slopevar", SEASVAR = "seasvar", CVAR = "cyclevar", NVAR = "noisevar",
            CDUMP = "cycledumpingfactor", CLENGTH = "cycleLength";

    public BasicStructuralModelExtractor() {
        set(LVAR, Double.class, source -> source.getLevelVar());
        set(SVAR, Double.class, source -> source.getSlopeVar());
        set(CVAR, Double.class, source -> source.getCycleVar());
        set(SEASVAR, Double.class, source -> source.getSeasonalVar());
        set(NVAR, Double.class, source -> source.getNoiseVar());
        set(CDUMP, Double.class, source -> source.getCycleDumpingFactor());
        set(CLENGTH, Double.class, source -> source.getCycleLength());
    }

    @Override
    public Class getSourceClass() {
        return BsmData.class;
    }

}
