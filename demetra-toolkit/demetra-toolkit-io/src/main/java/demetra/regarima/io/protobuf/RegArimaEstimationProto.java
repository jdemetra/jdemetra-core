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
import demetra.data.Iterables;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.MissingValueEstimation;
import demetra.likelihood.ParametersEstimation;
import demetra.math.matrices.MatrixType;
import demetra.modelling.implementations.SarimaSpec;
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
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import jdplus.dstats.T;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaEstimationProto {
    
    RegArimaResultsProtos.Sarima arima(RegSarimaModel model) {
        SarimaModel arima = model.arima();
        SarimaOrders orders = arima.orders();
        ParametersEstimation parameters = model.getEstimation().getParameters();
        RegArimaResultsProtos.Sarima.Builder builder = RegArimaResultsProtos.Sarima.newBuilder()
                .setPeriod(orders.getPeriod())
                .setP(orders.getP())
                .setD(orders.getD())
                .setQ(orders.getQ())
                .setBp(orders.getBp())
                .setBd(orders.getBd())
                .setBq(orders.getBq())
                .addAllParameters(Iterables.of(parameters.getValues()))
                .setCovariance(ToolkitProtosUtility.convert(parameters.getCovariance()))
                .addAllScore(Iterables.of(parameters.getScores()));
        return builder.build();
    }
    
    public RegArimaResultsProtos.RegArimaModel.Description convert(GeneralLinearModel.Description<SarimaSpec> description) {
        
        RegArimaResultsProtos.RegArimaModel.Description.Builder builder = RegArimaResultsProtos.RegArimaModel.Description.newBuilder();
        
        TsDomain domain = description.getSeries().getDomain();
        Variable[] vars = description.getVariables();
        for (int i = 0; i < vars.length; ++i) {
            Variable vari = vars[i];
            int m = vari.dim();
            ITsVariable core = vari.getCore();
            RegArimaResultsProtos.VariableType type = type(core);
            RegArimaResultsProtos.RegressionVariable.Builder vbuilder = RegArimaResultsProtos.RegressionVariable.newBuilder()
                    .setName(vari.getName())
                    .setVarType(type)
                    .putAllMetadata(vars[i].getAttributes());
            for (int k = 0; k < m; ++k) {
                String pname = m == 1 ? vari.getName() : vari.getCore().description(k, domain);
                vbuilder.addCoefficients(ToolkitProtosUtility.convert(vari.getCoefficient(k), pname));
            }
            builder.addVariables(vbuilder.build());
        }
        
        return builder.setSeries(ToolkitProtosUtility.convert(description.getSeries()))
                .setMean(description.isMean())
                .setLog(description.isLogTransformation())
                .setArima(RegArimaProtosUtility.convert(description.getStochasticComponent()))
                .build();
    }
    
    public RegArimaResultsProtos.RegArimaModel.Estimation convert(GeneralLinearModel.Estimation estimation) {
        RegArimaResultsProtos.RegArimaModel.Estimation.Builder builder = RegArimaResultsProtos.RegArimaModel.Estimation.newBuilder();
        
        MatrixType cov = estimation.getCoefficientsCovariance();
        LikelihoodStatistics statistics = estimation.getStatistics();
        
        builder.addAllY(Iterables.of(estimation.getY()))
                .setX(ToolkitProtosUtility.convert(estimation.getX()))
                .setParameters(ToolkitProtosUtility.convert(estimation.getParameters()))
                .setLikelihood(ToolkitProtosUtility.convert(statistics))
                .addAllResiduals(Iterables.of(estimation.getResiduals()))
                .addAllB(Iterables.of(estimation.getCoefficients()))
                .setBcovariance(ToolkitProtosUtility.convert(cov));

        // missing
        MissingValueEstimation[] missing = estimation.getMissing();
        if (missing.length > 0) {
            for (int i = 0; i < missing.length; ++i) {
                builder.addMissings(convert(missing[i]));
            }
        }
        return builder.build();
    }
    
    public RegArimaResultsProtos.RegArimaModel convert(RegSarimaModel model) {
        return RegArimaResultsProtos.RegArimaModel.newBuilder()
                .setDescription(convert(model.getDescription()))
                .setEstimation(convert(model.getEstimation()))
                .setDiagnostics(RegArimaProtosUtility.diagnosticsOf(model))
                .build();
        
    }
    
    public RegArimaResultsProtos.MissingEstimation convert(MissingValueEstimation missing) {
        return RegArimaResultsProtos.MissingEstimation.newBuilder()
                .setPosition(missing.getPosition())
                .setValue(missing.getValue())
                .setStde(missing.getStandardError())
                .build();
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
