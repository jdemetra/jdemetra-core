/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.examples;

import demetra.data.Data;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.tramoseats.TramoSeatsSpec;
import jdplus.seats.SeatsResults;
import jdplus.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.TramoSeatsResults;
import nbbrd.service.ServiceProvider;

/**
 * This example shows how to extend the output of a high-level algorithm, which
 * can be accessed by means of the generic approach (getData on an Explorable).
 * Such information can also be used - for instance - in the cruncher
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class AdditionalOutput {

    @ServiceProvider(InformationExtractor.class)
    public static class MyExtractor extends InformationMapping<SeatsResults> {

        public MyExtractor() {
            set("test", String.class, source->"Hello world");
            set("modelchanged", Boolean.class, source->source.isModelChanged());
            set("cutoff", Boolean.class, source->source.isParametersCutOff());
        }

        @Override
        public Class<SeatsResults> getSourceClass() {
            return SeatsResults.class;
        }
    }
    
    public void main(String[] arg){
        
        TramoSeatsSpec dspec=TramoSeatsSpec.RSA4;
        TsData data = TsData.ofInternal(TsPeriod.monthly(1967, 1), Data.PROD);
        TramoSeatsResults rslt = TramoSeatsKernel.of(dspec, null).process(data, null);
        String test = rslt.getData("decomposition.test", String.class);
        System.out.println(test);
        System.out.println(rslt.getData("decomposition.cutoff", Boolean.class));
        System.out.println(rslt.getData("decomposition.modelchanged", Boolean.class));
    }

}
