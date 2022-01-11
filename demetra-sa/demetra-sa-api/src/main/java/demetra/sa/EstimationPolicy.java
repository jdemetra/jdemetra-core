/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.sa;

import demetra.timeseries.TsDomain;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class EstimationPolicy {
    
    public static final EstimationPolicy COMPLETE=new EstimationPolicy(EstimationPolicyType.Complete, null);
    public static final EstimationPolicy FIXED=new EstimationPolicy(EstimationPolicyType.Fixed, null);
    public static final EstimationPolicy FIXEDAUTOREGRESSIVEPARAMETERS=new EstimationPolicy(EstimationPolicyType.FixedAutoRegressiveParameters, null);
    public static final EstimationPolicy FIXEDPARAMETERS=new EstimationPolicy(EstimationPolicyType.FixedParameters, null);
    public static final EstimationPolicy FREEPARAMETERS=new EstimationPolicy(EstimationPolicyType.FreeParameters, null);
    public static final EstimationPolicy INTERACTIVE=new EstimationPolicy(EstimationPolicyType.Interactive, null);
    public static final EstimationPolicy OUTLIERS=new EstimationPolicy(EstimationPolicyType.Outliers, null);
    public static final EstimationPolicy ARIMA=new EstimationPolicy(EstimationPolicyType.Outliers_StochasticComponent, null);
    
    EstimationPolicyType policy;
    TsDomain frozenSpan;
}
