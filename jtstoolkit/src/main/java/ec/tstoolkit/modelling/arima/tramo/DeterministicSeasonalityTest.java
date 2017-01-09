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

import ec.tstoolkit.arima.estimation.IRegArimaProcessor;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.IOutliersDetectionModule;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;

/**
 *   Implements the test in the paper:
 *   Determining Seasonality: A comparison diagnostic from X12-Arima
 *   Demetra P. Lytras, Roxanne M. Feldpausch, William R. Bell
 * @author Jean Palate, caporellogl
 */
public class DeterministicSeasonalityTest {

    private ModellingContext ds;
    private JointRegressionTest ftest;
    private final IOutliersDetectionModule outliers;
    private final IRegArimaProcessor<SarimaModel> processor;
    
    public DeterministicSeasonalityTest(IRegArimaProcessor<SarimaModel> processor, IOutliersDetectionModule outliers){
        this.processor=processor;
        this.outliers=outliers;
    }
    
    public JointRegressionTest getTest(){
        return ftest;
    }
    
    public ModellingContext getTestedModellingContext(){
        return ds;
    }

    public boolean test(ModellingContext context) {

        PreprocessingModel model = context.current(true);
        ModelDescription md = model.description.clone();
        //
        // Add Seasonal dummy variables
//        
        SeasonalDummies sd = new SeasonalDummies(context.description.getEstimationDomain().getFrequency());
        Variable tvar = Variable.userVariable(sd, ComponentType.Seasonal, RegStatus.Prespecified);
        md.addVariable(tvar);
        SarimaSpecification spec = md.getSpecification();
        spec.setBP(0);
        spec.setBD(0);
        spec.setBQ(0);
        md.setSpecification(spec);
        // Model Estimation
        ds = new ModellingContext();
        ds.description = md;
        if (outliers != null) {
            outliers.process(ds);
        }
        
        RegArimaModel<SarimaModel> regarima = md.buildRegArima();
        ModelEstimation estim = new ModelEstimation(regarima, md.getLikelihoodCorrection());
        if (!estim.compute(processor, spec.getParametersCount())) {
            return false;
        }
        ds.estimation = estim;
        //
        // Retrieve the Seasonal dummy coefficient + Innovation Variance Matrix
        //        
        TsVariableList ts = md.buildRegressionVariables();
        TsVariableSelection sel = ts.select(SeasonalDummies.class);
        ftest = new JointRegressionTest(.05);
        ftest.accept(estim.getLikelihood(), ds.description.getArimaComponent().getParametersCount(), sel.get(0).position, sd.getDim(), null);
        return true;
    }
}
