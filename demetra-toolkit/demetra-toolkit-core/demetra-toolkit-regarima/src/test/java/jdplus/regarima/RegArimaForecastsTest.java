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
package jdplus.regarima;

import demetra.data.Data;
import demetra.likelihood.LikelihoodStatistics;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.LengthOfPeriod;
import demetra.timeseries.regression.Variable;
import java.util.Arrays;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.Regression;
import jdplus.regsarima.RegSarimaComputer;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class RegArimaForecastsTest {

    public RegArimaForecastsTest() {
    }

    @Test
    public void testSomeMethod() {
        ModelDescription model = new ModelDescription(Data.TS_PROD, null);
        model.setAirline(true);
        model.setMean(true);
//        model.setLogTransformation(true);
//        model.setPreadjustment(LengthOfPeriodType.LeapYear);
        GenericTradingDaysVariable td = new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD7));
        LengthOfPeriod lp = new LengthOfPeriod(LengthOfPeriodType.LeapYear);
        model.addVariable(Variable.variable("td", td));
        EasterVariable easter = EasterVariable.builder()
                .duration(8)
                .meanCorrection(EasterVariable.Correction.PreComputed)
                .endPosition(-1)
                .build();
        model.addVariable(Variable.variable("lp", lp));
        model.addVariable(Variable.variable("easter", easter));
        RegSarimaModel rslt = RegSarimaModel.of(model, RegSarimaComputer.PROCESSOR.process(model.regarima(), model.mapping()), ProcessingLog.dummy());

        TsDomain xdom = model.getEstimationDomain().extend(0, 24);
        Variable[] variables = rslt.getDescription().getVariables(); // could contain the trend const
        FastMatrix matrix = Regression.matrix(xdom, Arrays.stream(variables).map(v -> v.getCore()).toArray(n -> new ITsVariable[n]));

        LikelihoodStatistics ll = rslt.getEstimation().getStatistics();
        double sig2 = ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount() + 1);
        RegArimaForecasts.Result f = RegArimaForecasts.calcForecast(
                rslt.arima(), rslt.getEstimation().originalY(), matrix, 
                rslt.getEstimation().getCoefficients(),
                rslt.getEstimation().getCoefficientsCovariance(), sig2);
    }

}
