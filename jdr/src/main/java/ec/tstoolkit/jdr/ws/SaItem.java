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
package ec.tstoolkit.jdr.ws;

import demetra.algorithm.IProcResults;
import demetra.datatypes.sa.SaItemType;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.jdr.sa.Processor;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
@lombok.Data
public class SaItem {

    private final SaItemType saDefinition;
    private IProcResults results;

    public boolean compute(ProcessingContext context) {
        if (results != null)
            return true;
        TsData data = saDefinition.getTs().getData();
        if (data == null) {
            return false;
        }
        ISaSpecification curSpec = saDefinition.getPointSpec();
        if (curSpec == null) {
            curSpec = saDefinition.getEstimationSpec();
        }
        if (curSpec == null) {
            curSpec = saDefinition.getDomainSpec();
        }
        if (curSpec instanceof TramoSeatsSpecification) {
            results=Processor.tramoseatsWithContext(data, (TramoSeatsSpecification)curSpec, context);
            
            return true;
        }
        if (curSpec instanceof X13Specification) {
            results=Processor.x13WithContext(data, (X13Specification)curSpec, context);
            return true;
        }
        return true;
    }
}
