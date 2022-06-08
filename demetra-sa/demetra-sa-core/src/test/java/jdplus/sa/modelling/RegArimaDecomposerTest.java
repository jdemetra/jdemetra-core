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
package jdplus.sa.modelling;

import demetra.data.Data;
import demetra.processing.ProcessingLog;
import demetra.sa.ComponentType;
import demetra.sa.SaVariable;
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
import jdplus.regarima.RegArimaEstimation;
import jdplus.regsarima.RegSarimaComputer;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.sarima.SarimaModel;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class RegArimaDecomposerTest {
    
    public RegArimaDecomposerTest() {
    }

    @Test
    public void testFullEstimation() {
        ModelDescription model = new ModelDescription(Data.TS_PROD, null);
        model.setAirline(true);
        model.setLogTransformation(true);
        model.setPreadjustment(LengthOfPeriodType.LeapYear);
        GenericTradingDaysVariable td = new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD3));
        model.addVariable(Variable.variable("td", td).addAttribute(SaVariable.REGEFFECT, ComponentType.CalendarEffect.name()));
        EasterVariable easter = EasterVariable.builder()
                .duration(6)
                .meanCorrection(EasterVariable.Correction.Theoretical)
                .build();
        model.addVariable(Variable.builder().name("easter").core(easter).attribute(SaVariable.REGEFFECT, ComponentType.CalendarEffect.name()).build());
        RegArimaEstimation<SarimaModel> estimation = RegSarimaComputer.PROCESSOR.process(model.regarima(), model.mapping());
        RegSarimaModel rslt=RegSarimaModel.of(model, estimation, ProcessingLog.dummy());
 
        List<TsData> all = new ArrayList<>();
        all.add(RegArimaDecomposer.deterministicEffect(rslt, model.getDomain(), ComponentType.Trend, false));
        all.add(RegArimaDecomposer.deterministicEffect(rslt, model.getDomain(), ComponentType.CalendarEffect, false));
        all.add(RegArimaDecomposer.deterministicEffect(rslt, model.getDomain(), ComponentType.Seasonal, false));
        all.add(RegArimaDecomposer.deterministicEffect(rslt, model.getDomain(), ComponentType.Irregular, false));
        all.add(RegArimaDecomposer.deterministicEffect(rslt, model.getDomain(), ComponentType.Undefined, false));
        all.add(model.getSeries());
        all.add(rslt.backTransform(rslt.linearizedSeries(), false));
        TsDataTable ts = TsDataTable.of(all);
//        System.out.println(ts);
    }
    
}
