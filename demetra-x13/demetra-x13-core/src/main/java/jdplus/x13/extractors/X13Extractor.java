/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.modelling.SeriesInfo;
import demetra.sa.SaDictionaries;
import demetra.timeseries.TsData;
import demetra.toolkit.dictionaries.Dictionary;
import demetra.x11.X11Dictionaries;
import demetra.x13.X13Dictionaries;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.x11.X11Results;
import jdplus.x13.X13Diagnostics;
import jdplus.x13.X13Results;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class X13Extractor extends InformationMapping<X13Results> {

    public static final String FINAL = "";
    
    private String decompositionItem(String key){
        return Dictionary.concatenate(SaDictionaries.DECOMPOSITION, key);
    }

    private String preadjustItem(String key){
        return Dictionary.concatenate(X13Dictionaries.PREADJUST, key);
    }
    
    private String finalItem(String key){
        return Dictionary.concatenate(X13Dictionaries.FINAL, key);
    }
    public X13Extractor() {
//         MAPPING.set(FINAL + ModellingDictionary.Y, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Forecast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Backcast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));
//
//        MAPPING.set(FINAL + SaDictionaries.T, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));
//
    

        set(preadjustItem(X13Dictionaries.A1), TsData.class, source
                -> source.getPreadjustment().getA1());
        set(preadjustItem(X13Dictionaries.A1A), TsData.class, source
                -> source.getPreadjustment().getA1a());
        set(preadjustItem(X13Dictionaries.A1B), TsData.class, source
                -> source.getPreadjustment().getA1b());
        set(preadjustItem(X13Dictionaries.A6), TsData.class, source
                -> source.getPreadjustment().getA6());
        set(preadjustItem(X13Dictionaries.A7), TsData.class, source
                -> source.getPreadjustment().getA7());
        set(preadjustItem(X13Dictionaries.A8), TsData.class, source
                -> source.getPreadjustment().getA8());
        set(preadjustItem(X13Dictionaries.A8I), TsData.class, source
                -> source.getPreadjustment().getA8i());
        set(preadjustItem(X13Dictionaries.A8S), TsData.class, source
                -> source.getPreadjustment().getA8s());
        set(preadjustItem(X13Dictionaries.A8T), TsData.class, source
                -> source.getPreadjustment().getA8t());
        set(preadjustItem(X13Dictionaries.A9), TsData.class, source
                -> source.getPreadjustment().getA9());
        set(preadjustItem(X13Dictionaries.A9SA), TsData.class, source
                -> source.getPreadjustment().getA9sa());
        set(preadjustItem(X13Dictionaries.A9U), TsData.class, source
                -> source.getPreadjustment().getA9u());
        set(preadjustItem(X13Dictionaries.A9SER), TsData.class, source
                -> source.getPreadjustment().getA9ser());

        set(SaDictionaries.S, TsData.class, source
                -> source.getFinals().getD16());
        set(SaDictionaries.S + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getD16a());
        set(SaDictionaries.SA, TsData.class, source
                -> source.getFinals().getD11final());
        set(SaDictionaries.SA + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getD11a());
        set(SaDictionaries.T, TsData.class, source
                -> source.getFinals().getD12final());
        set(SaDictionaries.T + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getD12a());
        set(SaDictionaries.I, TsData.class, source
                -> source.getFinals().getD13final());
        
        set(decompositionItem(SaDictionaries.Y_CMP), TsData.class, source
                -> source.getDecomposition().getB1());
        set(decompositionItem(SaDictionaries.Y_CMP_F), TsData.class, source
                -> source.getDecomposition().getB1());
        set(decompositionItem(SaDictionaries.Y_CMP_B), TsData.class, source
                -> source.getDecomposition().getB1());
        set(decompositionItem(SaDictionaries.S_CMP), TsData.class, source
                -> source.getDecomposition().getD10());
        set(decompositionItem(SaDictionaries.SA_CMP), TsData.class, source
                -> source.getDecomposition().getD11());
        set(decompositionItem(SaDictionaries.T_CMP), TsData.class, source
                -> source.getDecomposition().getD12());
        set(decompositionItem(SaDictionaries.I_CMP), TsData.class, source
                -> source.getDecomposition().getD13());

        set(finalItem(X13Dictionaries.D11), TsData.class, source
                -> source.getFinals().getD11final());
        set(finalItem(X13Dictionaries.D12), TsData.class, source
                -> source.getFinals().getD12final());
        set(finalItem(X13Dictionaries.D13), TsData.class, source
                -> source.getFinals().getD13final());
        set(finalItem(X13Dictionaries.D16), TsData.class, source
                -> source.getFinals().getD16());
        set(finalItem(X13Dictionaries.D18), TsData.class, source
                -> source.getFinals().getD18());
        set(finalItem(X13Dictionaries.D11A), TsData.class, source
                -> source.getFinals().getD11a());
        set(finalItem(X13Dictionaries.D12A), TsData.class, source
                -> source.getFinals().getD12a());
        set(finalItem(X13Dictionaries.D16A), TsData.class, source
                -> source.getFinals().getD16a());
        set(finalItem(X13Dictionaries.D18A), TsData.class, source
                -> source.getFinals().getD18a());

        set(finalItem(X13Dictionaries.E1), TsData.class, source
                -> source.getFinals().getE1());
        set(finalItem(X13Dictionaries.E2), TsData.class, source
                -> source.getFinals().getE2());
        set(finalItem(X13Dictionaries.E3), TsData.class, source
                -> source.getFinals().getE3());
        set(finalItem(X13Dictionaries.E11), TsData.class, source
                -> source.getFinals().getE11());
        

        delegate(null, RegSarimaModel.class, source -> source.getPreprocessing());
         
        delegate(SaDictionaries.DECOMPOSITION, X11Results.class, source -> source.getDecomposition());
        
        delegate(null, X13Diagnostics.class, source -> source.getDiagnostics());
    }

    @Override
    public Class getSourceClass() {
        return X13Results.class;
    }
}
