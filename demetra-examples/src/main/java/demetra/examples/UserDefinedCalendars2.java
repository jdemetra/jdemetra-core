/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.examples;

import demetra.data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.sa.processors.TramoSeatsProcessor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.RegressionTestType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec;
import ec.tstoolkit.timeseries.DayClustering;
import ec.tstoolkit.timeseries.calendars.GenericTradingDays;
import ec.tstoolkit.timeseries.regression.GenericTradingDaysVariables;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.RegressionUtilities;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import jdplus.regsarima.regular.RegSarimaModel;

/**
 * User-defined calendar variables.
 * For this example, we use the default routine to generate the variables, but
 * they could of course come from any other piece of code
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class UserDefinedCalendars2 {

    Matrix td(TsDomain domain) {
        // Mondays-Thursdays, Fridays, Saturdays, Sundays
        DayClustering dc = DayClustering.TD4;
        // usual contrasts
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);

        // we extend the variables on 5 years
        
        return  RegressionUtilities.matrix(new GenericTradingDaysVariables(gtd), domain.extend(0, 60));
    }

    public void main(String[] args) {
        TsData ts = new TsData(TsFrequency.Monthly, 1982, 3, Data.ABS_RETAIL, true);
        Matrix td = td(ts.getDomain());

        // We create a processing context and put the regression variables in it
        // We could also use the "activeContext=ModellingContext.getActiveContext()
        // In that case, we can omit it in the kernel (put the context to null)
        ProcessingContext context = new ProcessingContext();
        TsVariables vars = new TsVariables();
        TsPeriod start = ts.getStart();
        for (int i = 0; i < td.getColumnsCount(); ++i) {
            vars.set("td" + (i + 1), new TsVariable(new TsData(start, td.column(i))));
        }
        context.getTsVariableManagers().set("mytd", vars);

        // user-defined td spec (with F-test)
        TramoSeatsSpecification nspec = TramoSeatsSpecification.RSAfull.clone();
        TradingDaysSpec tdspec = nspec.getTramoSpecification().getRegression().getCalendar().getTradingDays();
        
        tdspec.setUserVariables(new String[]{"mytd.td1", "mytd.td2", "mytd.td3"});
        tdspec.setRegressionTestType(RegressionTestType.Joint_F);

        CompositeResults rslt = TramoSeatsProcessingFactory.process(ts, nspec, context);
        PreprocessingModel preprocessing = rslt.get("preprocessing", PreprocessingModel.class);
         
        System.out.println(preprocessing.deterministicEffect(ts.getDomain().extend(0, 60), var->var instanceof ITradingDaysVariable));
        
    }

}
