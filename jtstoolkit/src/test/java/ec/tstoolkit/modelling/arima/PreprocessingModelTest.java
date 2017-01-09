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
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.IterativeGlsSarimaMonitor;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class PreprocessingModelTest {

    public PreprocessingModelTest() {
    }

    //@Test
    public void demoDictionary() {
        Map<String, Class> dic = PreprocessingModel.dictionary();
        for (Entry<String, Class> o : dic.entrySet()) {
            System.out.print(o.getKey());
            System.out.print('\t');
            System.out.println(o.getValue().getSimpleName());
        }
    }

    //@Test
    public void demoEstimation() {
        // Create a model for the series Data.X. The entire time domain of X is
        // modelled (second parameter setto null).
        ModelDescription model = new ModelDescription(Data.X, null);
        // Add log transformation and preliminary correction for leap year.
        model.setTransformation(DefaultTransformationType.Log, PreadjustmentType.LengthOfPeriod);
        // Use a (0 1 1) (without parameters)
        model.setAirline(false);
        // Add trading days (without leap year) and seasonal dummies
        GregorianCalendarVariables td = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        ITsVariable sd = new SeasonalDummies(Data.X.getFrequency());
        model.addVariable(Variable.calendarVariable(td, RegStatus.Prespecified));
        model.addVariable(Variable.userVariable(sd, ComponentType.Seasonal, RegStatus.Prespecified));
        // Generate the low-level regression model
        RegArimaModel<SarimaModel> regarima = model.buildRegArima();

        // Tramo-like
        GlsSarimaMonitor gls = new GlsSarimaMonitor();
        RegArimaEstimation<SarimaModel> glsEstimation = gls.process(regarima);

        // X13-like
        IterativeGlsSarimaMonitor igls = new IterativeGlsSarimaMonitor();
        RegArimaEstimation<SarimaModel> iglsEstimation = igls.process(regarima);

        System.out.println();
        System.out.println("Gls");
        System.out.println(glsEstimation.model.getArima());
        System.out.println(glsEstimation.statistics(1, 0));
        System.out.println();
        System.out.println("IGls");
        System.out.println(iglsEstimation.model.getArima());
        System.out.println(iglsEstimation.statistics(1, 0));
    }

//   @Test
    public void demoLikelihoodFunction() {
        ModelDescription model = new ModelDescription(Data.P, null);
        model.setTransformation(DefaultTransformationType.Log, PreadjustmentType.LengthOfPeriod);
        model.setAirline(true);
        GregorianCalendarVariables td = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        model.getCalendars().add(Variable.calendarVariable(td, RegStatus.Prespecified));
        RegArimaModel<SarimaModel> regarima = model.buildRegArima();
        GlsSarimaMonitor gls = new GlsSarimaMonitor();
        gls.setPrecision(1e-9);
        ModelEstimation estimate = new ModelEstimation(regarima, 0);
        estimate.compute(gls, 0);
        PreprocessingModel pmodel = new PreprocessingModel(model, estimate);
        pmodel.updateModel();
        IFunction fn = pmodel.likelihoodFunction();
        IFunctionInstance ml = pmodel.maxLikelihoodFunction();
        Matrix L = new Matrix(100, 100);
        IReadDataBlock parameters = ml.getParameters();
        DataBlock p = new DataBlock(parameters);

        for (int i = 0; i < 101; ++i) {
            p.set(0,parameters.get(0) + .002 * i - .1);
            for (int j = 0; j < 101; ++j) {
                p.set(1,parameters.get(1) + .002 * j - .1);
                try {
                    L.set(i, j, fn.evaluate(p).getValue());
                } catch (Exception err) {
                }
            }
        }
        System.out.println(L);
    }

}
