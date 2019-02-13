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
package demetra.tempdisagg.univariate;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.normalizer.IDataNormalizer;
import demetra.design.Development;
import demetra.maths.matrices.Matrix;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
class DisaggregationModel {

    @Nonnull
    double[] hO;

    @Nonnull
    double[] hY;
    /**
     * Y expanded to the high frequency (with missing values). hY corresponds to
     * hDom (domain of the set of the indicators or pre-specified domain when
     * indicators are missing). If necessary it is expanded with missing values.
     */
    @Nonnull
    double[] hEY;
    /**
     * Regression variables. Defined on the high level domain. Could be null
     */
    Matrix hX;
    /**
     * Regression variables transformed to match the aggregation mode
     * (cumulative variables). Defined on the high level domain.
     */
    Matrix hEX;
    /**
     * High level domain. Same length as hX, The results correspond to that
     * domain.
     */
    TsDomain hDom;
    /**
     * High level estimation domain. Same length as hEX. Corresponds to the
     * domain taken into account in the estimation procedure.
     */
    TsDomain hEDom;
    /**
     * Ratio between the high and the low frequencies (Conversion ratio)
     */
    int frequencyRatio;
    /**
     * Scaling factor for hY
     */
    double yfactor;
    /**
     * Scaling factors for hX
     */
    double[] xfactor;
    
    DisaggregationModel(DisaggregationModelBuilder builder){
        this.hO=builder.hO;
        this.hY=builder.hY;
        this.hEY=builder.hEY;
        this.hX=builder.hX;
        this.hEX=builder.hEX;
        this.hDom=builder.hDom;
        this.hEDom=builder.hEDom;
        this.frequencyRatio=builder.frequencyRatio;
        this.yfactor=builder.yfactor;
        this.xfactor=builder.xfactor;
    }

    int nx() {
        return hX == null ? 0 : hX.getColumnsCount();
    }

    int n() {
        return hEY.length;
    }
    
    

}
