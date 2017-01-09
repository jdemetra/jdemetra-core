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
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.LeapYearVariable;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class ModellingContextTest {
    
    private final ModellingContext reference=new ModellingContext();
    
    public ModellingContextTest() {
        // create a (Tramo)preprocessor corresponding to RSA5
        IPreprocessor processor = TramoSpecification.TR5.build(null);
        processor.process(Data.X, reference);
    }
    
    public void computeTramo(ModellingContext context){
        
        // the easy way... use an existing estimator with default options
        IModelEstimator tramo=
                new ec.tstoolkit.modelling.arima.tramo.FinalEstimator();
        
        tramo.estimate(context);
    }
    
    /**
     * Similar to the CALCULA_MODEL routine (at least, I hope so)
     * Outliers are re-estimated
     * @param context 
     */
    public void calcModel(ModellingContext context){
        
        ec.tstoolkit.modelling.arima.tramo.ModelEstimator calmodel=
                new ec.tstoolkit.modelling.arima.tramo.ModelEstimator();
        ec.tstoolkit.modelling.arima.tramo.OutliersDetector outliers=
                new ec.tstoolkit.modelling.arima.tramo.OutliersDetector();
        outliers.setDefault();
        // Usually, you will use the outliers detection module of the TramoProcessor itself
        calmodel.setOutliersDetectionModule(outliers);
        // Set eps
        calmodel.setPrecision(1e-5);
        calmodel.estimate(context);
    }

    public void computeRegArima(ModellingContext context){
        
        // the easy way... use an existing estimator with default options
        IModelEstimator regarima=
                new ec.tstoolkit.modelling.arima.x13.IGlsFinalEstimator();
        
        regarima.estimate(context);
    }

    //@Test
    public void demoTramo(){
        System.out.println("Tramo");
        ModellingContext context=new ModellingContext();
        context.description=reference.description.clone();
        computeTramo(context);
        displayStatistics(context);
        System.out.println();
    }

    //@Test
    public void demoX13(){
        System.out.println("RegArima (IGLS)");
        ModellingContext context=new ModellingContext();
        context.description=reference.description.clone();
        computeRegArima(context);
        displayStatistics(context);
        System.out.println();
    }

    //@Test
    public void demoCalModel(){
        System.out.println("Tramo-CalModel");
        ModellingContext context=new ModellingContext();
        context.description=reference.description.clone();
        // remove outliers...
        context.description.setOutliers(null);
        calcModel(context);
        displayStatistics(context);
        System.out.println();
    }
    
    //@Test
    public void demoNoTradingDays(){
        System.out.println("No trading days");
        ModellingContext context=new ModellingContext();
        context.description=reference.description.clone();
        context.description.removeVariable(var->var.isCalendar());
        computeTramo(context);
        displayStatistics(context);
        // or
        // System.out.println(context.estimation.getStatistics());
        System.out.println();
    }
    
    //@Test
    public void demoTradingDays(){
        System.out.println("Trading days");
        ModellingContext context=new ModellingContext();
        context.description=reference.description.clone();
        context.description.removeVariable(var->var.isCalendar());
        // add usual trading days (without testing)
        GregorianCalendarVariables td=GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        context.description.addVariable(Variable.calendarVariable(td, RegStatus.Prespecified));
        LeapYearVariable lp=new LeapYearVariable(LengthOfPeriodType.LeapYear);
        context.description.addVariable(Variable.calendarVariable(lp, RegStatus.Prespecified));
        computeTramo(context);
        displayStatistics(context);
        // or
        // System.out.println(context.estimation.getStatistics());
        System.out.println();
    }
    
    //@Test
    public void demoWorkingDays(){
        System.out.println("Working days");
        ModellingContext context=new ModellingContext();
        context.description=reference.description.clone();
        List<Variable> calendars = context.description.getCalendars();
        // add usual trading days (without testing)
        calendars.clear();
        GregorianCalendarVariables td=GregorianCalendarVariables.getDefault(TradingDaysType.WorkingDays);
        calendars.add(Variable.calendarVariable(td, RegStatus.Prespecified));
        LeapYearVariable lp=new LeapYearVariable(LengthOfPeriodType.LeapYear);
        calendars.add(Variable.calendarVariable(lp, RegStatus.Prespecified));
        computeTramo(context);
        displayStatistics(context);
        // or
        // System.out.println(context.estimation.getStatistics());
        System.out.println();
    }

    public void displayContext(ModellingContext context) {
        
    }

    private void displayStatistics(ModellingContext context) {
        ec.tstoolkit.modelling.arima.ModelStatistics stats=
                new ec.tstoolkit.modelling.arima.ModelStatistics(context.tmpModel());
        System.out.println(stats);
    }
}
