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
package jdplus.x13.spi;

import demetra.processing.DefaultProcessingLog;
import demetra.processing.GenericResults;
import demetra.processing.ProcResults;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.x11.X11;
import demetra.x11.X11Spec;
import java.util.List;
import jdplus.x11.X11Kernel;
import jdplus.x11.X11Results;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(X11.Processor.class)
public class X11Computer implements X11.Processor {

    @Override
    public ProcResults process(TsData timeSeries, X11Spec spec, List<String> items) {
        X11Kernel x11 = new X11Kernel();
        DefaultProcessingLog log = new DefaultProcessingLog();
        X11Results rslt = x11.process(timeSeries, spec);
        return GenericResults.of(rslt, items, ProcessingLog.dummy()) ; 
    }
    
}
