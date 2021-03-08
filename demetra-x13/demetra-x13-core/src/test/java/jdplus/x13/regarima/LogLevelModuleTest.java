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
package jdplus.x13.regarima;

import demetra.data.Data;
import demetra.sa.ComponentType;
import demetra.sa.SaVariable;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.LengthOfPeriod;
import demetra.timeseries.regression.Variable;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.arima.PreadjustmentType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import jdplus.regarima.ami.Utility;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModelling;
import static jdplus.x13.regarima.Converter.convert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class LogLevelModuleTest {

    public LogLevelModuleTest() {
    }

    @Test
    public void testNoAdjust() {
        long t0 = System.currentTimeMillis();
        TsData[] insee = Data.insee();
        for (int i = 0; i < insee.length; ++i) {
            LogLevelModule ll = LogLevelModule.builder()
                    .preadjust(LengthOfPeriodType.None)
                    .estimationPrecision(1e-5)
                    .aiccLogCorrection(2)
                    .build();
            ModelDescription model = new ModelDescription(insee[i], null);
            model.setAirline(true);
            RegSarimaModelling m = RegSarimaModelling.of(model);
            ll.process(m);
            boolean log = m.getDescription().isLogTransformation();
            ec.tstoolkit.modelling.arima.x13.LogLevelTest oll = new ec.tstoolkit.modelling.arima.x13.LogLevelTest();
            ec.tstoolkit.timeseries.simplets.TsData s = convert(insee[i]);
            ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
            ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();
            desc.setTransformation(DefaultTransformationType.Auto);
            desc.setAirline(true);
            context.description = desc;
            context.hasseas = true;
            oll.process(context);
            boolean olog = oll.isChoosingLog();
            assertTrue(log == olog);
            assertEquals(ll.getAICcLevel(), oll.getLevel().getStatistics().AICC, 1e-1);
            assertEquals(ll.getAICcLog(), oll.getLog().getStatistics().AICC, 1e-1);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    public void testAdjust() {
        long t0 = System.currentTimeMillis();
        TsData[] insee = Data.insee();
        for (int i = 0; i < insee.length; ++i) {
            LogLevelModule ll = LogLevelModule.builder()
                    .preadjust(LengthOfPeriodType.LeapYear)
                    .estimationPrecision(1e-5)
                    .aiccLogCorrection(2)
                    .build();
            ModelDescription model = new ModelDescription(insee[i], null);
            model.setAirline(true);
            model.addVariable(Variable.builder().name("lp").core(new LengthOfPeriod(LengthOfPeriodType.LeapYear)).build());
            model.addVariable(Variable.builder().name("td").core(new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD7))).build());
            RegSarimaModelling m = RegSarimaModelling.of(model);
            ll.process(m);
            boolean log = m.getDescription().isLogTransformation();
            ec.tstoolkit.modelling.arima.x13.LogLevelTest oll = new ec.tstoolkit.modelling.arima.x13.LogLevelTest();
            ec.tstoolkit.timeseries.simplets.TsData s = convert(insee[i]);
            ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
            ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();
            desc.setAirline(true);
            desc.setTransformation(PreadjustmentType.Auto);
            desc.setTransformation(DefaultTransformationType.Auto);
            desc.addVariable(ec.tstoolkit.modelling.Variable.lpVariable(new ec.tstoolkit.timeseries.regression.LeapYearVariable(ec.tstoolkit.timeseries.calendars.LengthOfPeriodType.LeapYear), RegStatus.ToRemove));
            desc.addVariable(ec.tstoolkit.modelling.Variable.tdVariable(ec.tstoolkit.timeseries.regression.GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays), RegStatus.ToRemove));
            context.description = desc;
            context.hasseas = true;
            oll.process(context);
            boolean olog = oll.isChoosingLog();
            assertTrue(log == olog);
            assertEquals(ll.getAICcLevel(), oll.getLevel().getStatistics().AICC, 1e-1);
            assertEquals(ll.getAICcLog(), oll.getLog().getStatistics().AICC, 1e-1);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    public void testAdjustTD() {
        long t0 = System.currentTimeMillis();
        int n = 0;
        TsData[] insee = Data.insee();
        for (int i = 0; i < insee.length; ++i) {
            LogLevelModule ll = LogLevelModule.builder()
                    .preadjust(LengthOfPeriodType.LeapYear)
                    .estimationPrecision(1e-5)
                    .aiccLogCorrection(-2)
                    .build();
            ModelDescription model0 = new ModelDescription(insee[i], null);
            model0.setAirline(true);
            RegSarimaModelling m0 = RegSarimaModelling.of(model0);
            ll.process(m0);
            boolean log0 = m0.getDescription().isLogTransformation();
            ModelDescription model1 = new ModelDescription(insee[i], null);
            model1.setAirline(true);
            model1.addVariable(Variable.builder().name("lp").core(new LengthOfPeriod(LengthOfPeriodType.LeapYear)).attribute(SaVariable.REGEFFECT, ComponentType.CalendarEffect.name()).build());
            model1.addVariable(Variable.builder().name("td").core(new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD7))).attribute(SaVariable.REGEFFECT, ComponentType.CalendarEffect.name()).build());
            RegSarimaModelling m1 = RegSarimaModelling.of(model1);
            ll.process(m1);
            boolean log1 = m1.getDescription().isLogTransformation();
            if (log0 != log1) {
                ++n;
            }
        }
        assertTrue(n == 3);
        System.out.println(n);
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    public void testAO() {
        long t0 = System.currentTimeMillis();
        TsData[] insee = Data.insee();
        for (int i = 0; i < insee.length; ++i) {
            LogLevelModule ll = LogLevelModule.builder()
                    .preadjust(LengthOfPeriodType.None)
                    .estimationPrecision(1e-5)
                    .aiccLogCorrection(2)
                    .build();
            ModelDescription model = new ModelDescription(insee[i], null);
            model.setAirline(true);
            RegSarimaModelling m = RegSarimaModelling.of(model);
            ll.process(m);
            System.out.print(ll.isChoosingLog());
            System.out.print('\t');

            double[] nvals = insee[i].getValues().toArray();
            nvals[nvals.length - 3] *= 0.25;
            nvals[nvals.length - 2] *= 0.20;
            TsData s = TsData.ofInternal(insee[i].getStart(), nvals);
            model = new ModelDescription(s, null);
            model.setAirline(true);
            m = RegSarimaModelling.of(model);
            ll.process(m);
            System.out.println(ll.isChoosingLog());
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
