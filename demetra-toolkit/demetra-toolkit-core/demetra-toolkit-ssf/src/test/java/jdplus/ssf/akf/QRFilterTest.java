/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.akf;

import jdplus.arima.ssf.SsfArima;
import demetra.data.Data;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class QRFilterTest {

    static final jdplus.sarima.SarimaModel arima1, arima2;
    static final double[] data;

    static {
        SarimaOrders spec=SarimaOrders.airline(12);
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
    public void testDiffuse() {
        Ssf ssf = Ssf.of(SsfArima.of(arima1), SsfArima.defaultLoading());
        SsfData ssfData = new SsfData(data);

        QRFilter filter = new QRFilter();
        filter.process(ssf, ssfData);
        DiffuseLikelihood ll1 = filter.diffuseLikelihood(true, true);
        DiffuseLikelihood ll2 = DkToolkit.likelihood(ssf, ssfData, true, true);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-6);
    }
}
