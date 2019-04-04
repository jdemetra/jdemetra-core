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
import demetra.maths.MatrixType;
import demetra.data.DoubleSeq;

/**
 * Describes the linear model: y = a + b * X
 * 
 * 
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
public interface LinearModelType {
    
    public static LinearModelType of(DoubleSeq y, boolean mean, MatrixType x){
        return new LightLinearModel(y, mean, x);
    }

    DoubleSeq getY();

    /**
     * Mean correction
     * @return 
     */
    boolean isMeanCorrection();

    /**
     * 
     * @return 
     */
    MatrixType getX();

}
