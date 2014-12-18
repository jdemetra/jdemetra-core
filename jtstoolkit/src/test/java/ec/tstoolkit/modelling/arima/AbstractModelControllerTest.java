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

package ec.tstoolkit.modelling.arima;

import data.Data;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.tramo.ModelEstimator;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.LeapYearVariable;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class AbstractModelControllerTest {

    public AbstractModelControllerTest() {
    }

    @Test
    public void testSomeController() {
        
        ModellingContext context=new ModellingContext();
        TramoSpecification.TR0.build().process(Data.X, context);
        SomeController controller = new SomeController();
        // use CALCULA_MODEL
        controller.setEstimator(new ModelEstimator());
        controller.process(context);
    }

    static class SomeController extends AbstractModelController {

        @Override
        public ProcessingResult process(ModellingContext context) {
            // model without td. See ModellingContextTest for further explanations
            // See also RegressionVariablesTest for further examples.

            ModelDescription test0=createTestModel(context, TradingDaysType.None, LengthOfPeriodType.None);
            ModellingContext cxt0=new ModellingContext();
            cxt0.description=test0;
            estimate(cxt0, false);
            
            ModelDescription test1=createTestModel(context, TradingDaysType.TradingDays, LengthOfPeriodType.LeapYear);
            ModellingContext cxt1=new ModellingContext();
            cxt1.description=test1;
            // estimate the model, perhaps with test on mean
            // Corresponds to the CalModel routine
            // If the controller has an outliers detection routine, it will be used in the estimation procedure
            estimate(cxt1, false);
            
            ModelDescription test2=createTestModel(context, TradingDaysType.WorkingDays, LengthOfPeriodType.LeapYear);
            ModellingContext cxt2=new ModellingContext();
            cxt2.description=test2;
            estimate(cxt2, false);
            
            // compute usual (Tramo) statistics on the models
            // you can also retrieve the necessary information in the estimation part of the context
            // double ll0=cxt0.estimation.getLikelihood().getLogLikelihood();
            ModelStatistics stat0=new ModelStatistics(cxt0.tmpModel());
            ModelStatistics stat1=new ModelStatistics(cxt1.tmpModel());
            ModelStatistics stat2=new ModelStatistics(cxt2.tmpModel());
            // Other more general statistics
            LikelihoodStatistics lstat0 = cxt0.estimation.getStatistics();
            LikelihoodStatistics lstat1 = cxt1.estimation.getStatistics();
            LikelihoodStatistics lstat2 = cxt2.estimation.getStatistics();
            
            System.out.println("No TD");
            System.out.println(stat0);
            System.out.println("TD");
            System.out.println(stat1);
            System.out.println("WD");
            System.out.println(stat2);
            System.out.println("No TD");
            System.out.println(lstat0);
            System.out.println("TD");
            System.out.println(lstat1);
            System.out.println("WD");
            System.out.println(lstat2);
            
            // choose the right description
            //context.description=best.description;
            //context.estimation=best.estimation;
            return ProcessingResult.Changed;
        }

        private ModelDescription createTestModel(ModellingContext context, TradingDaysType td, LengthOfPeriodType lp) {
            ModelDescription model = context.description.clone();
            // for instance, we use airline with mean
            model.setAirline(context.hasseas);
            model.setMean(true);

            // remove any outliers
            // of course, we could keep (part of) them
            model.setOutliers(null);
            model.setPrespecifiedOutliers(null);

            // remove previous calendar effects 
            model.getCalendars().clear();
            if (td != TradingDaysType.None) // we create a variable, using the usual calendar, with the "Accepted" status (so that it could be removed later).
            // use the prespecified status if you don't want to test it later.
            {
                model.getCalendars().add(new Variable(GregorianCalendarVariables.getDefault(td), ComponentType.CalendarEffect, RegStatus.Accepted));
            }
            if (lp != LengthOfPeriodType.None) // same as td
            {
                model.getCalendars().add(new Variable(new LeapYearVariable(lp), ComponentType.CalendarEffect, RegStatus.Accepted));
            }
            return model;
        }
    }
}
