/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.extractors;

import nbbrd.design.Development;
import demetra.information.InformationMapping;
import demetra.modelling.ComponentInformation;
import demetra.modelling.ModellingDictionary;
import demetra.modelling.SeriesInfo;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.sa.SaDictionary;
import demetra.timeseries.TsData;
import demetra.information.BasicInformationExtractor;
import demetra.information.InformationExtractor;
import jdplus.arima.IArimaModel;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.sarima.SarimaModel;
import jdplus.x13.X13Results;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class X13Extractor extends InformationMapping<X13Results> {

    private final String DECOMP = "decomposition" + BasicInformationExtractor.SEP, FINAL = "";

    public X13Extractor() {
        delegate("preprocessing", RegSarimaModel.class, source -> source.getPreprocessing());
    }

    @Override
    public Class<X13Results> getSourceClass() {
        return X13Results.class;
    }
}
