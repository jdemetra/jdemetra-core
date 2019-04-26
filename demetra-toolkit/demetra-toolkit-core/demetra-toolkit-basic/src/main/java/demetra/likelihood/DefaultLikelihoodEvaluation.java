/*
 * Copyright 2013 National Bank of Belgium
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
package demetra.likelihood;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nonnull;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 * @param <L>
 */
public class DefaultLikelihoodEvaluation {
    
    public static ToDoubleFunction<Likelihood> ml(){
        return likelihood->likelihood.logLikelihood();
    }

    public static ToDoubleFunction<Likelihood> deviance(){
        return likelihood->likelihood.ssq() * likelihood.factor();
    }

    public static ToDoubleFunction<Likelihood> ssq(){
        return likelihood->likelihood.ssq();
    }

    public static ToDoubleFunction<Likelihood> logSsq(){
        return likelihood->Math.log(likelihood.ssq());
    }

    public static Function<Likelihood, DoubleSeq> errors(){
        return likelihood->likelihood.e();
    }
    
    public static Function<Likelihood, DoubleSeq> v(){
        return likelihood->likelihood.deviances();
    }
    
}