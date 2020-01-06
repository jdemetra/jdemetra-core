/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.akf;

import jdplus.ssf.likelihood.MarginalLikelihood;
import jdplus.arima.ssf.SsfArima;
import demetra.data.Data;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class QRFilterTest {

    static final jdplus.sarima.SarimaModel arima1, arima2;
    static final double[] data;

    static {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        arima1 = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).build();
        arima2 = SarimaModel.builder(spec).theta(1, .3).btheta(1, -.4).build();
        data = Data.PROD.clone();
//        data[3]=Double.NaN;
//        data[7]=Double.NaN;
//        data[8]=Double.NaN;
    }

    public QRFilterTest() {
    }

    @Test
    public void testMarginal() {
        Ssf ssf1 = Ssf.of(SsfArima.of(arima1), SsfArima.defaultLoading());
        Ssf ssf2 = Ssf.of(SsfArima.of(arima2), SsfArima.defaultLoading());
        SsfData ssfData = new SsfData(data);

        QRFilter filter = new QRFilter();
        filter.process(ssf1, ssfData);
        MarginalLikelihood ml11 = filter.getMarginalLikelihood();
        MarginalLikelihood ml12 = QRFilter.ml(ssf1, ssfData, true);
        filter.process(ssf2, ssfData);
        MarginalLikelihood ml21 = filter.getMarginalLikelihood();
        MarginalLikelihood ml22 = QRFilter.ml(ssf2, ssfData, true);
        assertEquals(ml11.logLikelihood() - ml12.logLikelihood(), ml21.logLikelihood() - ml22.logLikelihood(), 1e-6);
    }

}
