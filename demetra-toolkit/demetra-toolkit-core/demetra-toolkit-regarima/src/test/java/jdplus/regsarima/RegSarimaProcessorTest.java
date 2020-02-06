/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regsarima;

import demetra.arima.SarimaSpecification;
import demetra.data.Data;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author PALATEJ
 */
public class RegSarimaProcessorTest {
    
    public RegSarimaProcessorTest() {
    }

    @Test
    public void testProd() {
        assertTrue(prodAirline() != null);
    }
    
    @Test
    public void testFunction(){
        SarimaSpecification spec=SarimaSpecification.airline(12);
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();
        RegArimaModel model = RegArimaModel.<SarimaModel>builder()
                .y(DoubleSeq.of(Data.PROD))
                .arima(arima)
                .meanCorrection(true)
                .build();
        RegArimaEstimation<SarimaModel> rslt = RegSarimaProcessor.builder()
                .minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(1e-9)
                .build()
                .process(model, null);
        
        double ll1 = rslt.getMax().getFunction().evaluate(DoubleSeq.of(rslt.getMax().getParameters())).getValue();
        double ll2 = rslt.getConcentratedLikelihood().logLikelihood();
        assertEquals(ll1, ll2, 1e-9);
    }
    
    public static RegArimaModel<SarimaModel> prodAirline(){
        SarimaSpecification spec=SarimaSpecification.airline(12);
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();
        RegArimaModel model = RegArimaModel.<SarimaModel>builder()
                .y(DoubleSeq.of(Data.PROD))
                .arima(arima)
                .meanCorrection(true)
                .build();
        RegArimaEstimation<SarimaModel> rslt = RegSarimaProcessor.builder()
                .minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(1e-9)
                .build()
                .process(model, null);
        return rslt.getModel();
    }
    
}
