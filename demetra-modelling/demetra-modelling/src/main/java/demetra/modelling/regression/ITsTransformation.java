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
package demetra.modelling.regression;

import demetra.data.DoubleSequence;
import demetra.data.transformation.DataTransformation.LogJacobian;
import demetra.design.Development;
import demetra.timeseries.TsData;
import demetra.data.transformation.DataTransformation;

/**
 * Interface for transformation of a time series
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ITsTransformation {

    /**
     * Gives the converse transformation. Applying a transformation and its
     * converse should not change the initial series
     *
     * @return The converse transformation.
     */
    ITsTransformation converse();

    /**
     * Transforms a time series.
     *
     * @param data The data being transformed.
     * @param logjacobian I/O parameter. The log of the Jacobian of this
     * transformation
     * @return The transformed data. Null if the transformation was not
     * successful
     */
    TsData transform(TsData data, LogJacobian logjacobian);

    public static ITsTransformation of(final DataTransformation dataTransformation) {
        return new GenericTsTransformation(dataTransformation);
    }

}

class GenericTsTransformation implements ITsTransformation {

    private final DataTransformation dataTransformation;

    GenericTsTransformation(final DataTransformation dataTransformation) {
        this.dataTransformation = dataTransformation;
    }

    @Override
    public ITsTransformation converse() {
        return new GenericTsTransformation(dataTransformation.converse());
    }

    @Override
    public TsData transform(TsData data, LogJacobian logjacobian) {
        return TsData.of(data.getStart(), dataTransformation.transform(data.getValues(), logjacobian));
    }
}
