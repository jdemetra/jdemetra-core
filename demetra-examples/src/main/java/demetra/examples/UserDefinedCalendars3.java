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
package demetra.examples;

import demetra.data.Data;
import demetra.math.matrices.Matrix;
import demetra.timeseries.StaticTsDataSupplier;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.TsDataSuppliers;
import demetra.tramo.CalendarSpec;
import demetra.tramo.RegressionSpec;
import demetra.tramo.RegressionTestType;
import demetra.tramo.TradingDaysSpec;
import demetra.tramoseats.TramoSeatsSpec;
import jdplus.modelling.regression.Regression;
import jdplus.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.TramoSeatsResults;

/**
 * User-defined calendar variables.
 * For this example, we use the default routine to generate the variables, but
 * they could of course come from any other piece of code
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class UserDefinedCalendars3 {

    Matrix td(TsDomain domain) {
        // Mondays-Thursdays, Fridays, Saturdays, Sundays
        DayClustering dc = DayClustering.TD4;
        // usual contrasts
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);

        // we extend the variables on 5 years
        return Regression.matrix(domain.extend(0, 60), new GenericTradingDaysVariable(gtd));
    }

    public void main(String[] args) {
        TsData ts = Data.TS_ABS_RETAIL;
        Matrix td = td(ts.getDomain());

        // We create a modelling context and put the regression variables in it
        // We could also use the "activeContext=ModellingContext.getActiveContext()
        // In that case, we can omit it in the kernel (put the context to null)
        ModellingContext context = new ModellingContext();
        TsDataSuppliers vars = new TsDataSuppliers();
        TsPeriod start = ts.getStart();
        for (int i = 0; i < td.getColumnsCount(); ++i) {
            vars.set("td" + (i + 1), new StaticTsDataSupplier(TsData.of(start, td.column(i))));
        }
        context.getTsVariableManagers().set("mytd", vars);

        // user-defined td spec (with F-test)
        TradingDaysSpec tdspec = TradingDaysSpec.userDefined(new String[]{"mytd.td1", "mytd.td2", "mytd.td3"}, RegressionTestType.Joint_F);
        // modifying an existing spec (sometimestedious but safe)
        // to be noted that we only rebuild what is needed
        TramoSeatsSpec spec = TramoSeatsSpec.RSAfull;

        CalendarSpec ncal = spec.getTramo().getRegression().getCalendar().toBuilder()
                .tradingDays(tdspec)
                .build();
        
        RegressionSpec reg=spec.getTramo().getRegression().toBuilder()
                .calendar(ncal)
                .build();
        
        TramoSeatsSpec nspec=spec.toBuilder()
                .tramo(spec.getTramo().toBuilder()
                        .regression(reg)
                        .build())
                .build();
        
        // Could also be written that way
//        TramoSeatsSpec nspec=spec.toBuilder()
//                .tramo(spec.getTramo().toBuilder()
//                        .regression(spec.getTramo().getRegression().toBuilder()
//                                .calendar(spec.getTramo().getRegression().getCalendar().toBuilder()
//                                        .tradingDays(tdspec)
//                                        .build())
//                                .build())
//                        .build())
//                .build();
        

        TramoSeatsResults rslt = TramoSeatsKernel.of(nspec, context).process(ts, null);
        
        System.out.println(rslt.getPreprocessing().deterministicEffect(ts.getDomain().extend(0, 60), var->var.getCore() instanceof ITradingDaysVariable));
        
    }

}
