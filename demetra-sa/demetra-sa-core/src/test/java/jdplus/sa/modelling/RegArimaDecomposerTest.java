/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.modelling;

import demetra.data.Data;
import demetra.sa.ComponentType;
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
import jdplus.regsarima.RegSarimaProcessor;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ModelEstimation;
import org.junit.Test;

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
        model.addVariable(Variable.variable("td", td, ComponentType.CalendarEffect.name()));
        EasterVariable easter = EasterVariable.builder()
                .duration(6)
                .meanCorrection(EasterVariable.Correction.Theoretical)
                .build();
        model.addVariable(Variable.builder().name("easter").core(easter).attribute("prespecified").attribute(ComponentType.CalendarEffect.name()).build());
        ModelEstimation rslt = ModelEstimation.of(model, RegSarimaProcessor.PROCESSOR);

 
        List<TsData> all = new ArrayList<>();
        all.add(RegArimaDecomposer.deterministicEffect(rslt, model.getDomain(), ComponentType.Trend, false));
        all.add(RegArimaDecomposer.deterministicEffect(rslt, model.getDomain(), ComponentType.CalendarEffect, false));
        all.add(RegArimaDecomposer.deterministicEffect(rslt, model.getDomain(), ComponentType.Seasonal, false));
        all.add(RegArimaDecomposer.deterministicEffect(rslt, model.getDomain(), ComponentType.Irregular, false));
        all.add(RegArimaDecomposer.deterministicEffect(rslt, model.getDomain(), ComponentType.Undefined, false));
        all.add(rslt.getOriginalSeries());
        all.add(rslt.backTransform(rslt.linearizedSeries(), false));
        TsDataTable ts = TsDataTable.of(all);
//        System.out.println(ts);
    }
    
}
