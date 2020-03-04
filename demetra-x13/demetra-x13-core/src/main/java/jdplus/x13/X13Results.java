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

import jdplus.regsarima.regular.ModelEstimation;
import demetra.sa.SeriesDecomposition;
import demetra.x11.X11Results;

/**
 *
 * @author palatej
 */
@lombok.Value
public class X13Results {
    
    private ModelEstimation preprocessing;
    private X11Results results;
    private SeriesDecomposition seriesDecomposition;
    
}
