/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.spi;

import demetra.arima.ArimaProcess;
import demetra.arima.SarimaProcess;
import demetra.arima.UcarimaProcess;
import demetra.arima.spi.ArimaProcesses;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = ArimaProcesses.Processor.class)
public class ArimaProcessor implements ArimaProcesses.Processor {

    @Override
    public IntToDoubleFunction autoCovarianceFunction(ArimaProcess process) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DoubleUnaryOperator pseudoSpectrum(ArimaProcess process) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IntToDoubleFunction piWeights(ArimaProcess process) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IntToDoubleFunction psiWeights(ArimaProcess process) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArimaProcess plus(ArimaProcess left, ArimaProcess right) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArimaProcess minus(ArimaProcess left, ArimaProcess right) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArimaProcess plus(ArimaProcess left, double noise) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArimaProcess minus(ArimaProcess left, double noise) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArimaProcess convert(SarimaProcess sarima) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UcarimaProcess doCanonical(UcarimaProcess ucarima) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
