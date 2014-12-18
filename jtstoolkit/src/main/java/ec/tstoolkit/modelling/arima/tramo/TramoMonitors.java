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

package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.EasterVariable;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.LeapYearVariable;
import ec.tstoolkit.timeseries.simplets.AverageInterpolator;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TramoMonitors {

    static void addEaster(ModelDescription desc) {
        EasterVariable easter = new EasterVariable();
        easter.setDuration(6);
        easter.includeEaster(true);
        easter.setType(EasterVariable.Type.Tramo);
        desc.getMovingHolidays().add(new Variable(easter, ComponentType.CalendarEffect, RegStatus.ToRemove));
    }

    static void addTradingDays(ModelDescription desc, int ntd) {
        if (ntd == 1 || ntd == 2) {
            GregorianCalendarVariables td = GregorianCalendarVariables.getDefault(TradingDaysType.WorkingDays);
            desc.getCalendars().add(new Variable(td, ComponentType.CalendarEffect, RegStatus.ToRemove));
        } else if (ntd == 6 || ntd == 7) {
            GregorianCalendarVariables td = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
            desc.getCalendars().add(new Variable(td, ComponentType.CalendarEffect, RegStatus.ToRemove));
        }
        if (ntd == 2 || ntd == 7) {
            LeapYearVariable lp = new LeapYearVariable(LengthOfPeriodType.LeapYear);
            desc.getCalendars().add(new Variable(lp, ComponentType.CalendarEffect, RegStatus.ToRemove));
        }
    }

    static IPreprocessingModule buildOutliers() {
        OutliersDetector outliers = new OutliersDetector();
        outliers.setDefault();
        return outliers;
    }

    static class Rsa0 extends AbstractTramoModule implements IPreprocessor {

        @Override
        public PreprocessingModel process(TsData originalTs, ModellingContext context) {
            context.description = new ModelDescription(originalTs, null);
            context.automodelling = false;
            context.outliers = false;
           initContext(context);
            // missings
            if (!context.description.updateMissing(new AverageInterpolator())) {
                return null;
            }
            IParametricMapping<SarimaModel> mapping =context.description.defaultMapping();
            TramoModelEstimator estimator = new TramoModelEstimator(mapping);
            estimator.setPrecision(1e-7);
            context.estimation = new ModelEstimation(context.description.buildRegArima());
            context.estimation.compute(estimator, mapping.getDim());
            return context.current(true);
        }

        private void initContext(ModellingContext context) {
            context.description.setTransformation(DefaultTransformationType.Auto);
            context.description.setAirline(context.hasseas);
        }
    }

    static class Rsa1 extends AbstractTramoModule implements IPreprocessor {

        IPreprocessingModule loglevel;
        IPreprocessingModule outliers;

        Rsa1() {
            loglevel = new LogLevelTest();
            outliers = TramoMonitors.buildOutliers();
        }

        @Override
        public PreprocessingModel process(TsData originalTs, ModellingContext context) {
            context.description = new ModelDescription(originalTs, null);
            context.automodelling = false;
            context.outliers = true;
           initContext(context);

            // missings
            if (!context.description.updateMissing(new AverageInterpolator())) {
                return null;
            }
            // log-level
            loglevel.process(context);
            // outliers
            outliers.process(context);
            //estimation
            IParametricMapping<SarimaModel> mapping = context.description.defaultMapping();
            TramoModelEstimator estimator = new TramoModelEstimator(mapping);
            estimator.setPrecision(1e-7);
            context.estimation = new ModelEstimation(context.description.buildRegArima(), context.description.getLikelihoodCorrection());
            context.estimation.compute(estimator, mapping.getDim());
            return context.current(true);
        }

        private void initContext(ModellingContext context) {
            context.description.setTransformation(DefaultTransformationType.Auto);
            context.description.setAirline(context.hasseas);
        }
    }

    static class Rsa2 extends AbstractTramoModule implements IPreprocessor {

        IPreprocessingModule loglevel;
        IPreprocessingModule regression;
        IPreprocessingModule outliers;

        Rsa2() {
            loglevel = new LogLevelTest();
            regression = new RegressionVariablesTest(false);
            outliers = TramoMonitors.buildOutliers();
        }

        @Override
        public PreprocessingModel process(TsData originalTs, ModellingContext context) {
            context.description = new ModelDescription(originalTs, null);
            if (!check(context)) {
                return null;
            }
            context.automodelling = false;
            context.outliers = true;
            initContext(context);
            // missing value...

            if (!context.description.updateMissing(new AverageInterpolator())) {
                return null;
            }


            // log-level
            loglevel.process(context);
            // calendars
            regression.process(context);
            // outliers
            outliers.process(context);
            //estimation
            IParametricMapping<SarimaModel> mapping = context.description.defaultMapping();
            TramoModelEstimator estimator = new TramoModelEstimator(mapping);
            estimator.setPrecision(1e-7);
            context.estimation = new ModelEstimation(context.description.buildRegArima(),
                    context.description.getLikelihoodCorrection());
            context.estimation.compute(estimator, mapping.getDim());
            return context.current(true);
        }

        private void initContext(ModellingContext context) {
            context.description.setTransformation(DefaultTransformationType.Auto);
            context.description.setAirline(context.hasseas);
            // add easter and td
            TramoMonitors.addTradingDays(context.description, 2);
            TramoMonitors.addEaster(context.description);
        }

        private boolean check(ModellingContext context) {
            double[] y = context.description.getY();
            if (y == null || y.length < 3 * context.description.getFrequency()) {
                return false;
            }
            return true;
        }
    }
}
