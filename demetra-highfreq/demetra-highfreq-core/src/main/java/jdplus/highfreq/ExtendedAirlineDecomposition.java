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
package jdplus.highfreq;

import demetra.data.DoubleSeq;
import demetra.highfreq.SeriesComponent;
import demetra.sa.SeriesDecomposition;
import java.util.List;
import jdplus.arima.ArimaModel;
import jdplus.ucarima.UcarimaModel;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class ExtendedAirlineDecomposition {

    @lombok.Value
    @lombok.Builder
    public static class Step {
        private double period;
        private DoubleSeq data;
        private ArimaModel model;
        private UcarimaModel ucarimaModel;
        @lombok.Singular
        List<SeriesComponent> components;

        public int getComponentsCount() {
            return components.size();
        }

        public SeriesComponent getComponent(int idx) {
            return idx >= components.size() ? null : components.get(idx);
        }
    }

    @lombok.Singular
    private List<Step> steps;
    private SeriesDecomposition finalComponents;

}
