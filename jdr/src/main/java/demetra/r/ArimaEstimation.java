/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.r;

import demetra.arima.regarima.RegArimaEstimation;
import demetra.arima.regarima.RegArimaModel;
import demetra.data.DoubleSequence;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.estimation.GlsSarimaMonitor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
@lombok.Setter
public class ArimaEstimation {
    private double[] phi, theta, bphi, btheta;
    private int[] order, seasonalOrder;
    private int frequency;
    private double[] y;
    private final List<double[]> xreg=new ArrayList<>();
    private boolean mean;
    
    public void addX(double[] x){
        xreg.add(x);
    }
    
    public Results process(){
        SarimaSpecification spec=new SarimaSpecification(frequency);
        if (order != null){
            spec.setP(order[0]);
            spec.setD(order[1]);
            spec.setQ(order[2]);
        }
        if (seasonalOrder != null){
            spec.setBP(seasonalOrder[0]);
            spec.setBD(seasonalOrder[1]);
            spec.setBQ(seasonalOrder[2]);
        }
        SarimaModel.Builder builder = SarimaModel.builder(spec);
        //
        SarimaModel arima = builder.setDefault().build();
        GlsSarimaMonitor monitor=GlsSarimaMonitor.builder().precision(1e-9).build();
        
        RegArimaModel.Builder<SarimaModel> rbuilder = RegArimaModel.builder(DoubleSequence.of(y), arima);
        rbuilder = rbuilder.meanCorrection(mean);
        for (double[] x : xreg){
            rbuilder=rbuilder.addX(DoubleSequence.of(x));
        }
        RegArimaEstimation<SarimaModel> rslt = monitor.compute(rbuilder.build());
        return new Results(rslt.getModel().arima(), rslt.getEstimation().getLikelihood());
    }
    
    @lombok.Value
    public static class Results{
        SarimaModel arima;
        ConcentratedLikelihood ll;
    }
}
