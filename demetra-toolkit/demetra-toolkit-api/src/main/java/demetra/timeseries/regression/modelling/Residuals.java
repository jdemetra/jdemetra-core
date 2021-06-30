/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.timeseries.regression.modelling;

import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsPeriod;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class Residuals {
     public static enum Type {
         /**
          * no information
          */
        Undefined,
        /**
         * One step ahead forecast error
         */
        OneStepAHead,
        /**
         * Maximum likelihood estimates
         */
        MLEstimate,
        /**
         * Independent residuals obtained through a QR estimation of 
         * the regression model (see for instance TRAMO)
         */
        QR_Transformed,
        /**
         * L^-1(y-Xb)
         */        
        FullResiduals;
    }
     
    /**
     * Default tests
     */ 
    public static final String MEAN = "mean", SKEW = "skewness", KURT = "kurtosis", DH = "doornikhansen", LB = "lb", LB2 = "lb2",
            SEASLB = "seaslb", BP = "bp", BP2 = "bp2", SEASBP = "seasbp", NRUNS= "nruns", LRUNS = "lruns",
            NUDRUNS = "nudruns", LUDRUNS = "ludruns" ; 
     
     
    @lombok.NonNull
    private Type type;
    
    @lombok.NonNull 
    private DoubleSeq res;
    
    private TsPeriod start;
    
    private double ser;
    
    @lombok.Singular
    private Map<String, StatisticalTest> tests;
   
}
