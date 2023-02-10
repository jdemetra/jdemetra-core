/*
 * Copyright 2013 National Bank of Belgium
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
package jdplus.highfreq.extendedairline;

import jdplus.highfreq.regarima.ArimaComputer;
import jdplus.highfreq.regarima.ModelDescription;
import jdplus.highfreq.regarima.HighFreqRegArimaModel;
import nbbrd.design.Development;
import demetra.processing.ProcessingLog;
import jdplus.arima.ArimaModel;
import jdplus.regarima.RegArimaEstimation;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Getter
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ExtendedRegAirlineModelling {

    private ModelDescription<ArimaModel, ExtendedAirlineDescription> description;
    private RegArimaEstimation<ArimaModel> estimation;
    private final ProcessingLog log;

    public static ExtendedRegAirlineModelling of(ModelDescription desc) {
        return new ExtendedRegAirlineModelling(desc, null, ProcessingLog.dummy());
    }

    public static ExtendedRegAirlineModelling of(ModelDescription desc, ProcessingLog log) {
        return new ExtendedRegAirlineModelling(desc, null, log);
    }

    public static ExtendedRegAirlineModelling copyOf(ExtendedRegAirlineModelling modelling) {
        return new ExtendedRegAirlineModelling(ModelDescription.copyOf(modelling.description),
                modelling.estimation, ProcessingLog.dummy());
    }

    public void estimate(double precision) {

        ArimaComputer computer = new ArimaComputer(precision, false);
        estimation = description.estimate(computer);
    }

    public HighFreqRegArimaModel build() {
        return HighFreqRegArimaModel.of(description, estimation, log);
    }

    public boolean needEstimation() {
        return estimation == null;
    }

    public void setDescription(ModelDescription desc) {
        this.description = desc;
        this.estimation = null;
    }

    public void set(ModelDescription desc, RegArimaEstimation<ArimaModel> est) {
        this.description = desc;
        this.estimation = est;
    }

    public void clearEstimation() {
        this.estimation = null;
    }
}
