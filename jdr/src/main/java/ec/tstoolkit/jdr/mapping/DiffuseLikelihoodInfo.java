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
package ec.tstoolkit.jdr.mapping;

import demetra.information.InformationMapping;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class DiffuseLikelihoodInfo {

    static final InformationMapping<DiffuseConcentratedLikelihood> MAPPING = new InformationMapping<>(DiffuseConcentratedLikelihood.class);

    static {
        MAPPING.set("ll", Double.class, dll -> dll.getLogLikelihood());
        MAPPING.set("dll", Double.class, dll -> dll.getDiffuseLogDeterminant());
        MAPPING.set("ssq", Double.class, dll -> dll.getSsqErr());
        MAPPING.set("sigma", Double.class, dll -> dll.getSigma());
    }

    public InformationMapping<DiffuseConcentratedLikelihood> getMapping() {
        return MAPPING;
    }

}
