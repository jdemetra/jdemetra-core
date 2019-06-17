/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.spi;

import demetra.arima.ArimaModel;
import demetra.maths.RealPolynomial;
import jdplus.maths.linearfilters.BackFilter;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class ArimaProcessorUtility {

    public demetra.arima.SarimaModel convert(jdplus.sarima.SarimaModel sarima) {
        return convert(sarima, null);
    }

    public demetra.arima.UcarimaModel convert(jdplus.ucarima.UcarimaModel ucm) {
        return convert(ucm, null);
    }

    public demetra.arima.SarimaModel convert(jdplus.sarima.SarimaModel sarima, String name) {
        return demetra.arima.SarimaModel.builder()
                .period(sarima.getFrequency())
                .d(sarima.getRegularDifferenceOrder())
                .bd(sarima.getSeasonalDifferenceOrder())
                .phi(sarima.phi())
                .bphi(sarima.bphi())
                .theta(sarima.theta())
                .btheta(sarima.btheta())
                .name(name)
                .build();
    }

    public demetra.arima.ArimaModel convert(jdplus.arima.IArimaModel arima, String name) {
        if (arima == null)
            return null;
        return demetra.arima.ArimaModel.builder()
                .ar(RealPolynomial.ofInternal(arima.getStationaryAr().asPolynomial().toArray()))
                .delta(RealPolynomial.ofInternal(arima.getNonStationaryAr().asPolynomial().toArray()))
                .ma(RealPolynomial.ofInternal(arima.getMa().asPolynomial().toArray()))
                .innovationVariance(arima.getInnovationVariance())
                .name(name)
                .build();
    }

    public jdplus.sarima.SarimaModel convert(demetra.arima.SarimaModel sarima) {
        return jdplus.sarima.SarimaModel.builder(sarima.specification())
                .differencing(sarima.getD(), sarima.getBd())
                .phi(sarima.getPhi())
                .bphi(sarima.getBphi())
                .theta(sarima.getTheta())
                .btheta(sarima.getBtheta())
                .build();
    }

    public jdplus.arima.ArimaModel convert(demetra.arima.ArimaType arima) {
        BackFilter ar=BackFilter.ofInternal(arima.getAr().toArray());
        BackFilter ma=BackFilter.ofInternal(arima.getMa().toArray());
        BackFilter delta=BackFilter.ofInternal(arima.getDelta().toArray());
        return new jdplus.arima.ArimaModel(ar, delta, ma, arima.getInnovationVariance());
    }

    public demetra.arima.UcarimaModel convert(jdplus.ucarima.UcarimaModel ucm, String[] names) {
        demetra.arima.ArimaModel sum=convert(ucm.getModel(), "sum");
        demetra.arima.ArimaModel[] cmps=new demetra.arima.ArimaModel[ucm.getComponentsCount()];
        for (int i=0; i<cmps.length; ++i){
            cmps[i]=convert(ucm.getComponent(i), names != null ? names[i] : null);
        }
        return new demetra.arima.UcarimaModel(sum, cmps);
    }
    
     public jdplus.ucarima.UcarimaModel convert(demetra.arima.UcarimaModel ucm) {
        jdplus.arima.ArimaModel sum=convert(ucm.getSum());
        demetra.arima.ArimaModel[] scmps = ucm.getComponents();
        jdplus.arima.ArimaModel[] tcmps=new jdplus.arima.ArimaModel[scmps.length];
        for (int i=0; i<tcmps.length; ++i){
            tcmps[i]=convert(scmps[i]);
        }
        return jdplus.ucarima.UcarimaModel.builder()
                .verify(false)
                .model(sum)
                .add(tcmps)
                .build();
    }
   
}
