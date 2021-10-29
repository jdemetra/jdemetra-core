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
package jdplus.eco.discrete;

import demetra.data.DoubleSeq;
import jdplus.data.DataBlockIterator;
import jdplus.data.normalizer.SafeNormalizer;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.math.functions.FunctionMinimizer;
import jdplus.math.functions.bfgs.Bfgs;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class DiscreteModelKernel {

    DiscreteModelEvaluation process(DiscreteModel model, FunctionMinimizer minimizer) {

        // first of all, we normalize the columns of the model
        SafeNormalizer normalizer = new SafeNormalizer();
        FastMatrix Xnorm = model.X.deepClone();
        double[] xnorm = new double[Xnorm.getColumnsCount()];
        DataBlockIterator columns = Xnorm.columnsIterator();
        int pos = 0;
        while (columns.hasNext()) {
//            columns.next();
//            xnorm[pos++]=1;
            xnorm[pos++] = normalizer.normalize(columns.next());
        }
        DiscreteModel modelc = new DiscreteModel(model.y, Xnorm, model.cdf);
        // initialization 
        DoubleSeq c = initialize(model.y, Xnorm);
        // create the function 
        llFn fn = new llFn(modelc);
        // optimization...
        if (minimizer == null){
            minimizer=Bfgs.builder().build();
        }
        if (!minimizer.minimize(fn.evaluate(c))) 
            return null;
        llFn.Point rslt=(llFn.Point) minimizer.getResult();
        
        double[] coeff = rslt.getParameters().toArray();
        for (int i=0; i<coeff.length; ++i){
            coeff[i]*=xnorm[i];
        }
        DoubleSeq b = DoubleSeq.of(coeff);
        
        llFn ll=new llFn(model);
        double[] grad = ll.loglikelihoodGradient(b);
        double[] probabilities = ll.probabilities(b);
       
         return DiscreteModelEvaluation.builder()
                .model(model)
                .coefficients(b)
                .probabilities(DoubleSeq.of(probabilities))
                .gradient(DoubleSeq.of(grad))
                .build();
                
    }

    private DoubleSeq initialize(int[] y, FastMatrix X) {
        double[] dy = new double[y.length];
        for (int i = 0; i < y.length; ++i) {
            dy[i] = y[i];
        }
        LinearModel model = LinearModel.builder()
                .y(DoubleSeq.of(dy))
                .addX(X)
                .build();
        LeastSquaresResults ls = Ols.compute(model);
        return ls.getCoefficients();
    }

}
