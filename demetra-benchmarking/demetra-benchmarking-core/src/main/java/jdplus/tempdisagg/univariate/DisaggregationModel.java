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
package jdplus.tempdisagg.univariate;

import nbbrd.design.Development;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.Variable;
import org.checkerframework.checker.nullness.qual.NonNull;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
class DisaggregationModel {
    
    @NonNull TsData originalSeries;
    
    Variable[] indicators;
    
    @NonNull
    double[] hO;

    @NonNull
    double[] hY;
    /**
     * Y expanded to the high frequency (with missing values). hY corresponds to
     * hDom (domain of the set of the indicators or pre-specified domain when
     * indicators are missing). If necessary it is expanded with missing values.
     */
    @NonNull
    double[] hEY;
    /**
     * Regression variables. Defined on the high level domain. Could be null
     */
    FastMatrix hX;
    /**
     * Regression variables transformed to match the aggregation mode
     * (cumulative variables). Defined on the high level domain.
     */
    FastMatrix hEX;
    /**
     * low-frequency domain. Domain of y
     */
    TsDomain lDom;
    /**
     * High frequency domain. Same length as hX, The results correspond to that
     * domain.
     */
    TsDomain hDom;
    /**
     * Low-level estimation domain. Corresponds to the low-frequency
     * domain taken into account in the estimation procedure.
     */
    TsDomain lEDom;
    /**
     * High frequency estimation domain. Same length as hEX. Corresponds to the
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
    
    int start;
    
    DisaggregationModel(DisaggregationModelBuilder builder){
        this.originalSeries=builder.y;
        this.indicators=builder.regressors.toArray(new Variable[builder.regressors.size()]);
        this.hO=builder.hO;
        this.hY=builder.hY;
        this.hEY=builder.hEY;
        this.hX=builder.hX;
        this.hEX=builder.hEX;
        this.lDom=builder.y.getDomain();
        this.lEDom=builder.lEDom;
        this.hDom=builder.hDom;
        this.hEDom=builder.hEDom;
        this.frequencyRatio=builder.frequencyRatio;
        this.yfactor=builder.yfactor;
        this.xfactor=builder.xfactor;
        this.start=builder.start;
    }

    int nx() {
        return hX == null ? 0 : hX.getColumnsCount();
    }

    int n() {
        return hEY.length;
    }
    
    

}
