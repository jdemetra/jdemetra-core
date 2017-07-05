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
package demetra.data;

import demetra.design.AlgorithmImplementation;
import static demetra.design.AlgorithmImplementation.Feature.Robust;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 *
 * @author Jean Palate
 */
@AlgorithmImplementation(algorithm=IDataNormalizer.class, feature=Robust)
public strictfp class BigDecimalAccumulator implements DoubleAccumulator {

    private BigDecimal sum;
    private final MathContext context=MathContext.DECIMAL128;

    public BigDecimalAccumulator() {
        sum = new BigDecimal(0, context);
    }

    @Override
    public void add(double term) {
        sum=sum.add(new BigDecimal(term), context);
    }

    @Override
    public void reset() {
        sum = new BigDecimal(0, context);
    }

    @Override
    public double sum() {
        return sum.doubleValue();
    }

    @Override
    public void set(double val) {
        sum = new BigDecimal(val, context);
    }

}
