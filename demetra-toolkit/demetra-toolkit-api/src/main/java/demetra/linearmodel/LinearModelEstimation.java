/*
 * Copyright 2019 National Bank of Belgium
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
package demetra.linearmodel;

import demetra.maths.matrices.MatrixType;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
public class LinearModelEstimation {

    public static final LinearModelEstimation EMPTY=new LinearModelEstimation(new Coefficient[0], MatrixType.EMPTY);
    
    private @lombok.NonNull
    Coefficient[] coefficients;
    private MatrixType covariance;

    public DoubleSeq values() {
        return DoubleSeq.onMapping(coefficients.length, i -> coefficients[i].getValue());
    }

    public double ser(int var) {
        return Math.sqrt(covariance.get(var, var));
    }

    public int nx() {
        return coefficients.length;
    }
}
