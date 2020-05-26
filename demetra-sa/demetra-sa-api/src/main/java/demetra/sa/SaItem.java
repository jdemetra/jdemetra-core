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

import demetra.processing.ProcQuality;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class SaItem {
    
    SaSpecification domainSpec, estimationSpec, pointSpec;
    EstimationPolicy policy;
    ProcQuality quality;
    int priority;

/*    private boolean dirty_ = true;
    private Ts ts_;
    private ISaSpecification pspec_, espec_, dspec_;
    private boolean cacheResults_ = true;
    private volatile CompositeResults rslts_;
    private EstimationPolicyType estimation_ = EstimationPolicyType.None;
    private Status status_ = Status.Unprocessed;
    private int priority_ = -1;
    private ProcQuality quality_ = ProcQuality.Undefined;
    private String[] warnings_;
    private InformationSet qsummary_;
    private MetaData metaData_;
    private String name = "";
    private boolean locked_;
*/
    
}
