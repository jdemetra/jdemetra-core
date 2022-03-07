/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.modelling.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.toolkit.dictionaries.ArimaDictionaries;
import demetra.arima.SarimaSpec;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class SarimaSpecExtractor extends InformationMapping<SarimaSpec> {

    public SarimaSpecExtractor() {
        set(ArimaDictionaries.P, Integer.class, source -> source.getP());
        set(ArimaDictionaries.D, Integer.class, source -> source.getD());
        set(ArimaDictionaries.Q, Integer.class, source -> source.getQ());
        set(ArimaDictionaries.BP, Integer.class, source -> source.getBp());
        set(ArimaDictionaries.BD, Integer.class, source -> source.getBd());
        set(ArimaDictionaries.BQ, Integer.class, source -> source.getBq());
    }

    @Override
    public Class<SarimaSpec> getSourceClass() {
        return SarimaSpec.class;
    }

}
