/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.seats;

import demetra.arima.SarimaModel;
import demetra.arima.UcarimaModel;
import demetra.sa.SeriesDecomposition;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class SeatsResults {

    private SarimaModel initialModel, finalModel;
    private SeriesDecomposition initialComponents, finalComponents;
    private UcarimaModel decomposition;
    
    @lombok.Singular
    private Map<String, Object> addtionalResults;
}
