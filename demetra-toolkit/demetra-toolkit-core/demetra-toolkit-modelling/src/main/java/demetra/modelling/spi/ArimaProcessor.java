/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.spi;

import demetra.arima.ArimaModel;
import demetra.arima.UcarimaModel;
import demetra.arima.spi.Arima;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = Arima.Processor.class)
public class ArimaProcessor implements Arima.Processor {

    @Override
    public IntToDoubleFunction autoCovarianceFunction(ArimaModel process) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DoubleUnaryOperator pseudoSpectrum(ArimaModel process) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IntToDoubleFunction piWeights(ArimaModel process) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IntToDoubleFunction psiWeights(ArimaModel process) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArimaModel plus(ArimaModel left, ArimaModel right) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArimaModel minus(ArimaModel left, ArimaModel right) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArimaModel plus(ArimaModel left, double noise) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArimaModel minus(ArimaModel left, double noise) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UcarimaModel doCanonical(UcarimaModel ucarima) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
