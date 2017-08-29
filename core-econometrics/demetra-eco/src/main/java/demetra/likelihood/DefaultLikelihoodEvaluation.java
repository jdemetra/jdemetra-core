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

import demetra.data.DoubleSequence;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 * @param <L>
 */
public class DefaultLikelihoodEvaluation {
    
    public static ToDoubleFunction<ILikelihood> ml(){
        return likelihood->likelihood.logLikelihood();
    }

    public static ToDoubleFunction<ILikelihood> deviance(){
        return likelihood->likelihood.ssq() * likelihood.factor();
    }

    public static ToDoubleFunction<ILikelihood> ssq(){
        return likelihood->likelihood.ssq();
    }

    public static ToDoubleFunction<ILikelihood> logSsq(){
        return likelihood->Math.log(likelihood.ssq());
    }

    public static Function<ILikelihood, DoubleSequence> errors(){
        return likelihood->likelihood.e();
    }
    
    public static Function<ILikelihood, DoubleSequence> v(){
        return likelihood->likelihood.v();
    }
    
}