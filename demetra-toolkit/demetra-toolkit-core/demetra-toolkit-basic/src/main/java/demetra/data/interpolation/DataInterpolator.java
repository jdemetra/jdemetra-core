/*
 * Copyright 2019 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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

package demetra.data.interpolation;

import demetra.design.Development;
import demetra.util.IntList;
import demetra.data.DoubleSeq;


/**
 * Interface for interpolation methods of time series
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@FunctionalInterface
public interface DataInterpolator
{

    /**
     * Interpolates a time series.   
     * @param input The series being interpolated. On entry, the missing values are
     * identified by Double.Nan values. On exit, the missing values are replace 
     * by their interpolations.  
     * @param missingpos On exit, the list contains the 0-based positions of the 
     * missing values (which have been replaced)
     * @return The interpolated series
     */
    double[] interpolate(DoubleSeq input, IntList missingpos);
}
