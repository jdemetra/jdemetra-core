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
package demetra.regarima.io.protobuf;

import demetra.arima.SarimaOrders;
import demetra.data.Parameter;
import demetra.data.Utility;
import demetra.stats.ProbabilityType;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TransitoryChange;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.Variable;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import jdplus.data.DataBlock;
import jdplus.dstats.T;
import jdplus.math.matrices.Matrix;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaEstimationProto {

    RegArimaResultsProtos.Sarima arima(ModelEstimation model) {
        SarimaModel arima = model.getModel().arima();
        SarimaOrders orders = arima.orders();
        RegArimaResultsProtos.Sarima.Builder builder = RegArimaResultsProtos.Sarima.newBuilder()
                .setPeriod(orders.getPeriod())
                .setP(orders.getP())
                .setD(orders.getD())
                .setQ(orders.getQ())
                .setBp(orders.getBp())
                .setBd(orders.getBd())
                .setBq(orders.getBq())
                .addAllParameters(Utility.asIterable(model.getArimaParameters()))
                .setCovariance(ToolkitProtosUtility.convert(model.getArimaCovariance()));
        return builder.build();
    }

    public RegArimaResultsProtos.RegArimaEstimation convert(ModelEstimation model) {
        RegArimaResultsProtos.RegArimaEstimation.Builder builder = RegArimaResultsProtos.RegArimaEstimation.newBuilder();

        Variable[] vars = model.getVariables();
        Matrix cov = model.getConcentratedLikelihood().covariance(model.getFreeArimaParametersCount(), true);
        TsDomain domain = model.getOriginalSeries().getDomain();

        builder.setTransformation(model.isLogTransformation() ? RegArimaProtos.Transformation.FN_LOG : RegArimaProtos.Transformation.FN_LEVEL)
                .setPreadjustment(RegArimaProtosUtility.convert(model.getLpTransformation()))
                .setCovariance(ToolkitProtosUtility.convert(model.getConcentratedLikelihood().covariance(model.getFreeArimaParametersCount(), true)))
                .setSarima(arima(model))
                .setLikelihood(ToolkitProtosUtility.convert(model.getStatistics()))
                .addAllCoefficients(Utility.asIterable(model.getConcentratedLikelihood().coefficients()))
                .setCovariance(ToolkitProtosUtility.convert(cov));

        DataBlock diag = cov.diagonal();
        T tstat = new T(model.getConcentratedLikelihood().degreesOfFreedom() - model.getFreeArimaParametersCount());
        for (int i = 0, j = 0; i < vars.length; ++i) {
            int m = vars[i].dim();
            ITsVariable core = vars[i].getCore();
            RegArimaResultsProtos.VariableType type = type(core);
            for (int k = 0; k < m; ++k) {
                String name = m == 1 ? vars[i].getName() : vars[i].getCore().description(k, domain);
                Parameter c = vars[i].getCoefficient(k);
                double val = c.getValue(), e = 0;
                if (!c.isFixed()) {
                    e = Math.sqrt(diag.get(j++));
                }
                RegArimaResultsProtos.RegressionVariable v = RegArimaResultsProtos.RegressionVariable.newBuilder()
                        .setName(name)
                        .setVarType(type)
                        .setCoefficient(val)
                        .setStde(e)
                        .setPvalue(e == 0 ? Double.NaN : 2 * tstat.getProbability(Math.abs(val / e), ProbabilityType.Upper))
                        .addAllMetadata(vars[i].getAttributes())
                        .build();
                builder.addVariables(v);
            }

        }
        return builder.build();
    }

    public RegArimaResultsProtos.VariableType type(ITsVariable var) {
        if (var instanceof TrendConstant) {
            return RegArimaResultsProtos.VariableType.VAR_MEAN;
        }
        if (var instanceof ITradingDaysVariable) {
            return RegArimaResultsProtos.VariableType.VAR_TD;
        }
        if (var instanceof ILengthOfPeriodVariable) {
            return RegArimaResultsProtos.VariableType.VAR_LP;
        }
        if (var instanceof IEasterVariable) {
            return RegArimaResultsProtos.VariableType.VAR_EASTER;
        }
        if (var instanceof IOutlier) {
            switch (((IOutlier) var).getCode()) {
                case AdditiveOutlier.CODE:
                    return RegArimaResultsProtos.VariableType.VAR_AO;
                case LevelShift.CODE:
                    return RegArimaResultsProtos.VariableType.VAR_LS;
                case TransitoryChange.CODE:
                    return RegArimaResultsProtos.VariableType.VAR_TC;
                case PeriodicOutlier.CODE:
                case PeriodicOutlier.PO:
                    return RegArimaResultsProtos.VariableType.VAR_SO;
                default:
                    return RegArimaResultsProtos.VariableType.VAR_OUTLIER;
            }
        }
        if (var instanceof InterventionVariable) {
            return RegArimaResultsProtos.VariableType.VAR_IV;
        }
        if (var instanceof Ramp) {
            return RegArimaResultsProtos.VariableType.VAR_RAMP;
        }
        return RegArimaResultsProtos.VariableType.VAR_UNSPECIFIED;
    }

}

