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
package jdplus.tramoseats.spi;

import demetra.data.DoubleSeq;
import demetra.processing.ProcessingLog;
import demetra.seats.SeatsProcessor;
import demetra.seats.SeatsResults;
import demetra.seats.SeatsSpec;
import java.util.List;
import jdplus.seats.SeatsKernel;
import jdplus.seats.SeatsToolkit;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(SeatsProcessor.Computer.class)
public class SeatsComputer implements SeatsProcessor.Computer{

    @Override
    public SeatsResults compute(SeatsSpec spec, List<String> addtionalItems) {
        // TODO Handling of additional items
        SeatsToolkit toolkit=SeatsToolkit.of(spec.getDecompositionSpec());
        SeatsKernel kernel=new SeatsKernel(toolkit);
        ProcessingLog log =new ProcessingLog();
        jdplus.seats.SeatsResults rslts = kernel.process(spec.getModelSpec(), log);
        return ApiUtility.toApi(rslts);
    }

}
