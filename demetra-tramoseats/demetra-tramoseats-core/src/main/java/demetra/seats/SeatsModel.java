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
package demetra.seats;

import demetra.arima.IArimaModel;
import demetra.data.DoubleSequence;
import demetra.sa.ComponentType;
import demetra.regarima.RegArimaModel;
import demetra.sarima.SarimaModel;
import demetra.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate
 */
@lombok.Data()
public class SeatsModel {

    public static SeatsModel of(DoubleSequence series, SarimaModel originalModel, boolean meanCorrection) {
        SeatsModel model = new SeatsModel();
        model.series = series;
        model.originalModel = originalModel;
        model.currentModel = originalModel;
        model.meanCorrection = meanCorrection;
        return model;
    }

    private DoubleSequence series;
    private SarimaModel originalModel;
    private boolean meanCorrection;
    private int forecastsCount, backcastsCount;
    private SarimaModel currentModel;
    private UcarimaModel ucarimaModel;
    private ComponentType[] types;
    private double innovationVariance;
    private boolean significantSeasonality;

    public RegArimaModel<SarimaModel> asRegarima() {
        return RegArimaModel.builder(SarimaModel.class)
                .y(series)
                .arima(currentModel)
                .meanCorrection(meanCorrection)
                .build();
    }
}
