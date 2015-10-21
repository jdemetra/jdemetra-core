/*
 * Copyright 2015 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
/*
 */
package ec.tstoolkit.arima;

import ec.tstoolkit.maths.linearfilters.RationalBackFilter;

/**
 * Computation of the AR-distance between two Arima models. See D. Piccolo
 * (1990, 2010). "A Distance measure for classifying Arima models", journal of
 * time series analysis, 11, 2
 *
 * @author Jean Palate
 */
public class AutoRegressiveDistance {
    /**
     * @param a The first model
     * @param b The second model
     * @param npi The number of pi-weights take into account (200 is more than
     * enough in most cases.
     * @return The distance between the models
     */
    public static double compute(IArimaModel a, IArimaModel b, int npi){
        RationalBackFilter pia = a.getPiWeights();
        RationalBackFilter pib = b.getPiWeights();
        double[] wa = pia.getWeights(npi+1);
        double[] wb = pib.getWeights(npi+1);
        double d=0;
        for (int i=1; i<=npi; ++i){
            double di=wa[i]-wb[i];
            d+=di*di;            
        }
        return Math.sqrt(d);
    }

//    /**
//     * Compute the AR-distance between two Arima models.
//     * See D. Piccolo (1990, 2010). "A Distance measure for classifying 
//     * Arima models", journal of time series analysis, 11, 2
//     * @param a The first model
//     * @param b The second model
//     * @param npi The number of pi-weights take into account
//     * @return The distance between the models
//     */
//    public static double compute2(IArimaModel a, IArimaModel b, int npi){
//        RationalBackFilter pia = a.getPiWeights();
//        BackFilter na = pia.getNumerator();
//        BackFilter da = pia.getDenominator();
//        RationalBackFilter pib = b.getPiWeights();
//        BackFilter nb = pib.getNumerator();
//        BackFilter db = pib.getDenominator();
//        BackFilter n = na.times(db).minus(nb.times(da));
//        double[] c = n.getCoefficients();
//        if (c.length == 1)
//            return 0;
//        double[] cc=new double[c.length-1];
//        System.arraycopy(c, 1, cc, 0, cc.length);
//        n=BackFilter.of(cc);
//        BackFilter.SimplifyingTool smp=new BackFilter.SimplifyingTool(true);
//        if (smp.simplify(n, da)){
//            n=smp.getLeft();
//            da=smp.getRight();
//        }
//        if (smp.simplify(n, db)){
//            n=smp.getLeft();
//            db=smp.getRight();
//        }
//        RationalBackFilter dpi=new RationalBackFilter(n, da.times(db));
//       double[] w = dpi.getWeights(npi);
//        double d=0;
//        for (int i=0; i<npi; ++i){
//            double di=w[i];
//            d+=di*di;            
//        }
//        return Math.sqrt(d);
//    }

}
