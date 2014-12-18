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
package ec.satoolkit.special;

import ec.satoolkit.IDefaultSeriesDecomposer;
import ec.satoolkit.IPreprocessingFilter;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.structural.BsmMonitor;
import ec.tstoolkit.structural.BsmSpecification;
import ec.tstoolkit.structural.ModelSpecification;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author pcuser
 */
public class StmDecomposer implements IDefaultSeriesDecomposer<StmResults> {

    private BsmSpecification spec_;
    private StmResults results_;

    public StmDecomposer(BsmSpecification spec) {
        spec_ = spec;
    }

    @Override
    public boolean decompose(PreprocessingModel model, IPreprocessingFilter filter) {
        TsData y = filter.getCorrectedSeries(true);
        BsmMonitor monitor = new BsmMonitor();
        monitor.setSpecification(spec_);
        RegArimaModel<SarimaModel> regArima = model.estimation.getRegArima();
        DataBlock ry = regArima.getY();
        double[] yo = new double[ry.getLength()];
        ry.copyTo(yo, 0);
        int[] missings = regArima.getMissings();
        if (missings != null) {
            for (int i = 0; i < missings.length; ++i) {
                yo[i] = Double.NaN;
            }
        }

        Matrix M;
        if (regArima.getXCount() > 0) {
            M = new Matrix(yo.length, regArima.getXCount());
            for (int i = 0; i < M.getColumnsCount(); ++i) {
                M.column(i).copy(regArima.X(i));
            }
        } else {
            M = null;
        }
        if (!monitor.process(yo,M== null ? null : M.subMatrix(), y.getFrequency().intValue())) {
            //if (!monitor.process(y.getValues().internalStorage(), y.getFrequency().intValue())) {
            return false;
        } else {
            results_ = new StmResults(y, model == null ? new TsVariableList(): model.description.buildRegressionVariables(), monitor,
                    model.description.getTransformation() == DefaultTransformationType.Log);
            return true;
        }
    }

    @Override
    public boolean decompose(TsData y) {
        BsmMonitor monitor = new BsmMonitor();
        monitor.setSpecification(spec_);
        if (!monitor.process(y.getValues().internalStorage(), y.getFrequency().intValue())) {
            return false;
        } else {
            results_ = new StmResults(y,new TsVariableList(), monitor, false);
            return true;
        }
    }

    @Override
    public StmResults getDecomposition() {
        return results_;
    }

}
