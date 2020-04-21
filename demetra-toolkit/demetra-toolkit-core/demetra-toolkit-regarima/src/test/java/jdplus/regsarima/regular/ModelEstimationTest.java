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
package jdplus.regsarima.regular;

import demetra.data.Data;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.Variable;
import java.util.ArrayList;
import java.util.List;
import jdplus.data.interpolation.AverageInterpolator;
import jdplus.regsarima.RegSarimaProcessor;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class ModelEstimationTest {

    public ModelEstimationTest() {
    }

    @Test
    public void testFullEstimation() {
        ModelDescription model = new ModelDescription(Data.TS_PROD, null);
        model.setAirline(true);
        model.setLogTransformation(true);
        model.setPreadjustment(LengthOfPeriodType.LeapYear);
        GenericTradingDaysVariable td = new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD3));
        model.addVariable(Variable.variable("td", td));
        EasterVariable easter = EasterVariable.builder()
                .duration(6)
                .meanCorrection(EasterVariable.Correction.Theoretical)
                .build();
        model.addVariable(Variable.variable("easter", easter));
        ModelEstimation rslt = ModelEstimation.of(model, RegSarimaProcessor.PROCESSOR);
        List<TsData> all = new ArrayList<>();
        all.add(rslt.getOriginalSeries());
        all.add(rslt.getTransformedSeries());
        all.add(rslt.linearizedSeries());
        TsDataTable ts = TsDataTable.of(all);
//        System.out.println(ts);
    }

    @Test
    public void testPartialEstimation() {
        ModelDescription model = new ModelDescription(Data.TS_PROD, Data.TS_PROD.getDomain().drop(25, 33));
        model.setAirline(true);
        model.setLogTransformation(true);
        model.setPreadjustment(LengthOfPeriodType.LeapYear);
        GenericTradingDaysVariable td = new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD3));
        model.addVariable(Variable.variable("td", td));
        EasterVariable easter = EasterVariable.builder()
                .duration(6)
                .meanCorrection(EasterVariable.Correction.Theoretical)
                .build();
        model.addVariable(Variable.variable("easter", easter));
        ModelEstimation rslt = ModelEstimation.of(model, RegSarimaProcessor.PROCESSOR);
        List<TsData> all = new ArrayList<>();
        all.add(rslt.getOriginalSeries());
        all.add(rslt.getTransformedSeries());
        all.add(rslt.linearizedSeries());
        TsDataTable ts = TsDataTable.of(all);
//        System.out.println(ts);
    }

    @Test
    public void testFullWithMissing() {
        double[] data = Data.PROD.clone();
        data[5] = data[25] = data[40] = data[100] = data[101] = data[102] = Double.NaN;
        ModelDescription model = new ModelDescription(TsData.ofInternal(Data.TS_PROD.getStart(), data), null);
        model.interpolate(AverageInterpolator.interpolator());
        model.setAirline(true);
        model.setLogTransformation(true);
        model.setPreadjustment(LengthOfPeriodType.LeapYear);
        GenericTradingDaysVariable td = new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD3));
        model.addVariable(Variable.variable("td", td));
        EasterVariable easter = EasterVariable.builder()
                .duration(6)
                .meanCorrection(EasterVariable.Correction.Theoretical)
                .build();
        model.addVariable(Variable.variable("easter", easter));
        ModelEstimation rslt = ModelEstimation.of(model, RegSarimaProcessor.PROCESSOR);
        List<TsData> all = new ArrayList<>();
        all.add(rslt.getOriginalSeries());
        all.add(rslt.interpolatedSeries(true));
        all.add(rslt.linearizedSeries());
        TsDataTable ts = TsDataTable.of(all);
//        System.out.println(ts);
    }

    @Test
    public void testPartialWithMissing() {
        double[] data = Data.PROD.clone();
        data[105] = data[125] = data[40] = data[100] = data[101] = data[102] = Double.NaN;
        ModelDescription model = new ModelDescription(TsData.ofInternal(Data.TS_PROD.getStart(), data), Data.TS_PROD.getDomain().drop(25, 33));
        model.interpolate(AverageInterpolator.interpolator());
        model.setAirline(true);
        model.setLogTransformation(true);
        model.setPreadjustment(LengthOfPeriodType.LeapYear);
        GenericTradingDaysVariable td = new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD3));
        model.addVariable(Variable.variable("td", td));
        EasterVariable easter = EasterVariable.builder()
                .duration(6)
                .meanCorrection(EasterVariable.Correction.Theoretical)
                .build();
        model.addVariable(Variable.variable("easter", easter));
        ModelEstimation rslt = ModelEstimation.of(model, RegSarimaProcessor.PROCESSOR);
        List<TsData> all = new ArrayList<>();
        all.add(rslt.getOriginalSeries());
        all.add(rslt.interpolatedSeries(false));
        all.add(rslt.linearizedSeries());
        // Test regression effects
        TsData tde = rslt.regressionEffect(rslt.getEstimationDomain().drop(-50, -100), var -> var.isTradingDays());
        TsData ee = rslt.regressionEffect(rslt.getEstimationDomain().drop(-50, -100), var -> var.isMovingHolidays());
        TsData rege = rslt.regressionEffect(rslt.getEstimationDomain().drop(-50, -100), var -> true);
        all.add(tde);
        all.add(ee);
        all.add(rege);
        assertTrue(TsData.add(tde, ee).distance(rege)<1e-9);
        TsDataTable ts = TsDataTable.of(all);
//        System.out.println(ts);
    }

}
