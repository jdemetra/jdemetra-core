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

import demetra.timeseries.Ts;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder", toBuilder = true)
public class SaDefinition {
    

    /**
     * Initial specification. Reference for any relaxing of some elements of the
     * specification
     */
    @lombok.NonNull
    SaSpecification domainSpec;

    /**
     * Specification used for the current estimation
     */
    SaSpecification estimationSpec;

    /**
     * Specification corresponding to the result
     */
    @lombok.experimental.NonFinal
    @lombok.EqualsAndHashCode.Exclude
    volatile SaSpecification pointSpec;

    /**
     * Way the current estimation specification has been achieved
     */
    @lombok.Builder.Default
    EstimationPolicyType policy=EstimationPolicyType.None;

    /**
     * Time series
     */
    Ts ts;

    public SaSpecification activeSpecification() {
        return estimationSpec == null ? domainSpec : estimationSpec;
    }

    public void setPointSpecification(SaSpecification pspec) {
        synchronized (this) {
            this.pointSpec = pspec;
        }
    }
}
