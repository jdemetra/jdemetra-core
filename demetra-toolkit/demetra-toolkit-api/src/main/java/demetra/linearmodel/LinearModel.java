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

import demetra.design.Development;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.Matrix;

/**
 * Describes the linear model: y = a + b * X
 * The constant should be defined explicitly (not included in X)
 * 
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class LinearModel {
    @lombok.NonNull
    /**
     * Exogenous variable
     */
    private DoubleSeq y;

    /**
     * Mean correction
     */
    private boolean meanCorrection;

    /**
     * 
     */
    private Matrix X;
    
}
