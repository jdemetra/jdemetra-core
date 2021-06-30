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

import demetra.data.Iterables;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.MissingValueEstimation;
import demetra.math.matrices.MatrixType;
import demetra.modelling.implementations.SarimaSpec;
import demetra.modelling.io.protobuf.ModellingProtos;
import demetra.stats.StatisticalTest;
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

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaEstimationProto {

    public RegArimaProtos.RegArimaModel.Description convert(GeneralLinearModel.Description<SarimaSpec> description) {

        RegArimaProtos.RegArimaModel.Description.Builder builder = RegArimaProtos.RegArimaModel.Description.newBuilder();

        TsDomain domain = description.getSeries().getDomain();
        Variable[] vars = description.getVariables();
        for (int i = 0; i < vars.length; ++i) {
            Variable vari = vars[i];
            int m = vari.dim();
            ITsVariable core = vari.getCore();
            ModellingProtos.VariableType type = type(core);
            ModellingProtos.RegressionVariable.Builder vbuilder = ModellingProtos.RegressionVariable.newBuilder()
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
                .setLog(description.isLogTransformation())
                .setArima(RegArimaProtosUtility.convert(description.getStochasticComponent()))
                .build();
    }

    public RegArimaProtos.RegArimaModel.Estimation convert(GeneralLinearModel.Estimation estimation) {
        RegArimaProtos.RegArimaModel.Estimation.Builder builder = RegArimaProtos.RegArimaModel.Estimation.newBuilder();

        MatrixType cov = estimation.getCoefficientsCovariance();
        LikelihoodStatistics statistics = estimation.getStatistics();

        builder.addAllY(Iterables.of(estimation.getY()))
                .setX(ToolkitProtosUtility.convert(estimation.getX()))
                .setParameters(ToolkitProtosUtility.convert(estimation.getParameters()))
                .setLikelihood(ToolkitProtosUtility.convert(statistics))
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

    public RegArimaProtos.RegArimaModel convert(GeneralLinearModel<SarimaSpec> model) {
        return RegArimaProtos.RegArimaModel.newBuilder()
                .setDescription(convert(model.getDescription()))
                .setEstimation(convert(model.getEstimation()))
                .setDiagnostics(diagnosticsOf(model))
                .build();

    }

    public ModellingProtos.MissingEstimation convert(MissingValueEstimation missing) {
        return ModellingProtos.MissingEstimation.newBuilder()
                .setPosition(missing.getPosition())
                .setValue(missing.getValue())
                .setStde(missing.getStandardError())
                .build();
    }

    public ModellingProtos.Diagnostics diagnosticsOf(GeneralLinearModel<SarimaSpec> model) {

        ModellingProtos.Diagnostics.Builder builder = ModellingProtos.Diagnostics.newBuilder();
        model.getAdditionalResults().forEach((k, v)
                -> {
            if (v instanceof StatisticalTest) {
                builder.putResidualsTests(k, ToolkitProtosUtility.convert((StatisticalTest) v));
            }
        });
        return builder.build();
    }

    public ModellingProtos.VariableType type(ITsVariable var) {
        if (var instanceof TrendConstant) {
            return ModellingProtos.VariableType.VAR_MEAN;
        }
        if (var instanceof ITradingDaysVariable) {
            return ModellingProtos.VariableType.VAR_TD;
        }
        if (var instanceof ILengthOfPeriodVariable) {
            return ModellingProtos.VariableType.VAR_LP;
        }
        if (var instanceof IEasterVariable) {
            return ModellingProtos.VariableType.VAR_EASTER;
        }
        if (var instanceof IOutlier) {
            switch (((IOutlier) var).getCode()) {
                case AdditiveOutlier.CODE:
                    return ModellingProtos.VariableType.VAR_AO;
                case LevelShift.CODE:
                    return ModellingProtos.VariableType.VAR_LS;
                case TransitoryChange.CODE:
                    return ModellingProtos.VariableType.VAR_TC;
                case PeriodicOutlier.CODE:
                case PeriodicOutlier.PO:
                    return ModellingProtos.VariableType.VAR_SO;
                default:
                    return ModellingProtos.VariableType.VAR_OUTLIER;
            }
        }
        if (var instanceof InterventionVariable) {
            return ModellingProtos.VariableType.VAR_IV;
        }
        if (var instanceof Ramp) {
            return ModellingProtos.VariableType.VAR_RAMP;
        }
        return ModellingProtos.VariableType.VAR_UNSPECIFIED;
    }

}
