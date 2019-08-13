/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.spi;

import demetra.arima.ArimaModel;
import demetra.arima.ArimaType;
import demetra.arima.UcarimaModel;
import demetra.arima.spi.Arima;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(Arima.Processor.class)
public class ArimaProcessor implements Arima.Processor {

    @Override
    public IntToDoubleFunction autoCovarianceFunction(ArimaType process) {
        jdplus.arima.ArimaModel arima=ArimaProcessorUtility.convert(process);
        return arima.getAutoCovarianceFunction().asFunction();
    }

    @Override
    public DoubleUnaryOperator pseudoSpectrum(ArimaType process) {
        jdplus.arima.ArimaModel arima=ArimaProcessorUtility.convert(process);
        return arima.getSpectrum().asFunction();
    }

    @Override
    public IntToDoubleFunction piWeights(ArimaType process) {
        jdplus.arima.ArimaModel arima=ArimaProcessorUtility.convert(process);
        return arima.getPiWeights().asFunction();
    }

    @Override
    public IntToDoubleFunction psiWeights(ArimaType process) {
        jdplus.arima.ArimaModel arima=ArimaProcessorUtility.convert(process);
        return arima.getPsiWeights().asFunction();
    }

    @Override
    public ArimaModel plus(ArimaType left, ArimaType right) {
        jdplus.arima.ArimaModel l=ArimaProcessorUtility.convert(left);
        jdplus.arima.ArimaModel r=ArimaProcessorUtility.convert(right);
        return ArimaProcessorUtility.convert(l.plus(r), "sum");
    }

    @Override
    public ArimaModel minus(ArimaType left, ArimaType right) {
        jdplus.arima.ArimaModel l=ArimaProcessorUtility.convert(left);
        jdplus.arima.ArimaModel r=ArimaProcessorUtility.convert(right);
        return ArimaProcessorUtility.convert(l.minus(r), "difference");
    }

    @Override
    public ArimaModel plus(ArimaType left, double noise) {
        jdplus.arima.ArimaModel l=ArimaProcessorUtility.convert(left);
        return ArimaProcessorUtility.convert(l.plus(noise), "sum");
    }

    @Override
    public ArimaModel minus(ArimaType left, double noise) {
        jdplus.arima.ArimaModel l=ArimaProcessorUtility.convert(left);
        return ArimaProcessorUtility.convert(l.minus(noise), "difference");
    }

    @Override
    public UcarimaModel doCanonical(UcarimaModel ucarima) {
        jdplus.ucarima.UcarimaModel ucm = ArimaProcessorUtility.convert(ucarima);
        ucm=ucm.setVarianceMax(-1, false);
        String[] names=new String[ucm.getComponentsCount()];
        for (int i=0; i<names.length-1; ++i)
            names[i]=ucarima.getComponent(i).getName();
        names[names.length-1]="noise";
        return ArimaProcessorUtility.convert(ucm, names);
    }

}
