/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.modelling.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.toolkit.dictionaries.ArimaDictionaries;
import jdplus.sarima.SarimaModel;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class SarimaExtractor extends InformationMapping<SarimaModel> {

    public SarimaExtractor() {
        set(ArimaDictionaries.P, Integer.class, source -> source.getP());
        set(ArimaDictionaries.D, Integer.class, source -> source.getD());
        set(ArimaDictionaries.Q, Integer.class, source -> source.getQ());
        set(ArimaDictionaries.BP, Integer.class, source -> source.getBp());
        set(ArimaDictionaries.BD, Integer.class, source -> source.getBd());
        set(ArimaDictionaries.BQ, Integer.class, source -> source.getBq());
        set(ArimaDictionaries.PHI, double[].class, source -> source.getPhi().toArray());
        set(ArimaDictionaries.BPHI, double[].class, source -> source.getBphi().toArray());
        set(ArimaDictionaries.THETA, double[].class, source -> source.getTheta().toArray());
        set(ArimaDictionaries.BTHETA, double[].class, source -> source.getBtheta().toArray());
        setArray(ArimaDictionaries.PHI, 1, 12, Double.class, (source, i) -> {
            if (i <= 0 || i > source.getP()) {
                return null;
            }
            return source.phi(i);
        });
        setArray(ArimaDictionaries.BPHI, 1, 2, Double.class, (source, i) -> {
            if (i <= 0 || i > source.getBp()) {
                return null;
            }
            return source.bphi(i);
        });
        setArray(ArimaDictionaries.THETA, 1, 12, Double.class, (source, i) -> {
            if (i <= 0 || i > source.getQ()) {
                return null;
            }
            return source.theta(i);
        });
        setArray(ArimaDictionaries.BTHETA, 1, 2, Double.class, (source, i) -> {
            if (i <= 0 || i > source.getBq()) {
                return null;
            }
            return source.btheta(i);
        });
    }

    @Override
    public Class<SarimaModel> getSourceClass() {
        return SarimaModel.class;
    }

}
