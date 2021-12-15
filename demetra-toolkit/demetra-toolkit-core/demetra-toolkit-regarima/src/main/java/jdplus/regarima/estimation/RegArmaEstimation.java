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
package jdplus.regarima.estimation;

import demetra.data.DoubleSeq;
import jdplus.arima.IArimaModel;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
@lombok.Value
public class RegArmaEstimation<S extends IArimaModel> {

    private DoubleSeq parameters;
    private DoubleSeq score;
    private FastMatrix information;
//    private int degreesOfFreedom;
}
