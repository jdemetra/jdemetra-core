/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.spi;


/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class ArimaProcessorUtility {
    
    public demetra.arima.SarimaModel convert(jdplus.sarima.SarimaModel sarima) {
        return demetra.arima.SarimaModel.builder()
                .period(sarima.getFrequency())
                .d(sarima.getRegularDifferenceOrder())
                .bd(sarima.getSeasonalDifferenceOrder())
                .phi(sarima.phi())
                .bphi(sarima.bphi())
                .theta(sarima.theta())
                .btheta(sarima.btheta())
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

}
