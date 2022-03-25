/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.highfreq;

import jdplus.arima.ArimaModel;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;

/**
 *
 * @author PALATEJ
 */
public class ExtendedAirlineComputer {
    
    private final double eps;
    private final boolean exactDerivatives;
    
    public ExtendedAirlineComputer(double eps, boolean exactDerivatives){
        this.eps=eps;
        this.exactDerivatives=exactDerivatives;
    }
    
    public RegArimaEstimation<ArimaModel> process(final RegArimaModel<ArimaModel> regarima, ExtendedAirlineMapping mapping){
        
        GlsArimaProcessor<ArimaModel> finalProcessor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(eps)
                .computeExactFinalDerivatives(exactDerivatives)
                .build();
        return finalProcessor.process(regarima, mapping);
    }
}
