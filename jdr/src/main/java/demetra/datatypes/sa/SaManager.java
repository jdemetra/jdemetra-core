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
package demetra.datatypes.sa;

import ec.satoolkit.ISaSpecification;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.information.InformationSet;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SaManager {

    public ISaSpecification createSpecification(InformationSet info) {
        AlgorithmDescriptor desc = info.get(ISaSpecification.ALGORITHM, AlgorithmDescriptor.class);
        if (desc.isCompatible(TramoSeatsProcessingFactory.DESCRIPTOR)) {
            TramoSeatsSpecification spec = new TramoSeatsSpecification();
            if (spec.read(info)) {
                return spec;
            } else {
                return null;
            }
        } else if (desc.isCompatible(X13ProcessingFactory.DESCRIPTOR)) {
            X13Specification spec = new X13Specification();
            if (spec.read(info)) {
                return spec;
            } else {
                return null;
            }
        } else {
            return null;
        }

    }

}
