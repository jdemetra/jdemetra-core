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
package demetra.sts.io.protobuf;

import demetra.data.Iterables;
import demetra.modelling.io.protobuf.ModellingProtos;
import demetra.modelling.io.protobuf.ModellingProtosUtility;
import static demetra.regarima.io.protobuf.RegArimaEstimationProto.type;
import demetra.sts.BasicStructuralModel;
import demetra.sts.BsmDescription;
import demetra.sts.BsmEstimation;
import demetra.sts.BsmSpec;
import demetra.sts.Component;
import demetra.sts.SeasonalModel;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.Variable;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import jdplus.sts.BsmData;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class StsProtosUtility {

    public StsProtos.SeasonalModel convert(SeasonalModel m) {
        switch (m) {
            case Dummy:
                return StsProtos.SeasonalModel.SEAS_DUMMY;
            case Trigonometric:
                return StsProtos.SeasonalModel.SEAS_TRIGONOMETRIC;
            case HarrisonStevens:
                return StsProtos.SeasonalModel.SEAS_HARRISONSTEVENS;
            case Crude:
                return StsProtos.SeasonalModel.SEAS_CRUDE;
            default:
                return StsProtos.SeasonalModel.SEAS_NONE;
        }
    }

    public SeasonalModel convert(StsProtos.SeasonalModel m) {
        switch (m) {
            case SEAS_DUMMY:
                return SeasonalModel.Dummy;
            case SEAS_TRIGONOMETRIC:
                return SeasonalModel.Trigonometric;
            case SEAS_HARRISONSTEVENS:
                return SeasonalModel.HarrisonStevens;
            case SEAS_CRUDE:
                return SeasonalModel.Crude;
            default:
                return null;
        }
    }

    public BsmSpec convert(StsProtos.BsmSpec spec) {
        return BsmSpec.builder()
                .level(ToolkitProtosUtility.convert(spec.getLevel()), ToolkitProtosUtility.convert(spec.getSlope()))
                .seasonal(convert(spec.getSeasonalModel()), ToolkitProtosUtility.convert(spec.getSeas()))
                .noise(ToolkitProtosUtility.convert(spec.getNoise()))
                .cycle(ToolkitProtosUtility.convert(spec.getCycle()), ToolkitProtosUtility.convert(spec.getCycleFactor()), ToolkitProtosUtility.convert(spec.getCyclePeriod()))
                .build();
    }

    public StsProtos.Bsm.Estimation convert(BsmEstimation e) {
        return StsProtos.Bsm.Estimation.newBuilder()
                .addAllY(Iterables.of(e.getY()))
                .setX(ToolkitProtosUtility.convert(e.getX()))
                .addAllB(Iterables.of(e.getCoefficients()))
                .addAllResiduals(Iterables.of(e.getResiduals()))
                .setBcovariance(ToolkitProtosUtility.convert(e.getCoefficientsCovariance()))
                .setParameters(ToolkitProtosUtility.convert(e.getParameters()))
                .setLikelihood(ToolkitProtosUtility.convert(e.getStatistics()))
                .build();
    }

    public StsProtos.BsmSpec convert(BsmSpec spec) {
        return StsProtos.BsmSpec.newBuilder()
                .setLevel(ToolkitProtosUtility.convert(spec.getLevelVar()))
                .setSlope(ToolkitProtosUtility.convert(spec.getSlopeVar()))
                .setSeas(ToolkitProtosUtility.convert(spec.getSeasonalVar()))
                .setSeasonalModel(convert(spec.getSeasonalModel()))
                .setCycle(ToolkitProtosUtility.convert(spec.getCycleVar()))
                .setCycleFactor(ToolkitProtosUtility.convert(spec.getCycleDumpingFactor()))
                .setCyclePeriod(ToolkitProtosUtility.convert(spec.getCycleLength()))
                .setNoise(ToolkitProtosUtility.convert(spec.getNoiseVar()))
                .build();
    }

    public StsProtos.Bsm.Description convert(BsmDescription desc) {
        StsProtos.Bsm.Description.Builder builder = StsProtos.Bsm.Description.newBuilder()
                .setSeries(ToolkitProtosUtility.convert(desc.getSeries()))
                .setLog(desc.isLogTransformation())
                .setPreadjustment(ModellingProtosUtility.convert(desc.getLengthOfPeriodTransformation()))
                .setBsm(convert(desc.getSpecification()));
        TsDomain domain = desc.getSeries().getDomain();
        Variable[] vars = desc.getVariables();
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
        return builder.build();
    }

    public StsProtos.Bsm convert(BasicStructuralModel bsm) {
        return StsProtos.Bsm.newBuilder()
                .setDescription(convert(bsm.getDescription()))
                .setEstimation(convert(bsm.getEstimation()))
                .build();
    }

}
