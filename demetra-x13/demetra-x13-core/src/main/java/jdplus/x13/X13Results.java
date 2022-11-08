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
package jdplus.x13;

import jdplus.x11.X11Results;
import jdplus.sa.modelling.HasRegSarimaPreprocessing;
import jdplus.x11.HasX11Decomposition;
import jdplus.regsarima.regular.RegSarimaModel;
import demetra.information.GenericExplorable;
import demetra.processing.HasLog;
import demetra.processing.ProcessingLog;
import jdplus.sa.SaBenchmarkingResults;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class X13Results implements GenericExplorable , HasLog, HasRegSarimaPreprocessing, HasX11Decomposition{

    private RegSarimaModel preprocessing;
    private X13Preadjustment preadjustment;
    private X11Results decomposition;
    private X13Finals finals;
    private SaBenchmarkingResults benchmarking;
    private X13Diagnostics diagnostics;
    private ProcessingLog log;
}
