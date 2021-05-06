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
import demetra.sts.BsmEstimation;
import demetra.sts.BsmSpec;
import demetra.sts.Component;
import demetra.sts.SeasonalModel;
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
    
    public StsProtos.Bsm.Estimation convert(BsmEstimation e){
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

//    public StsProtos.BsmData convert(BsmData bsm) {
//        BsmSpec spec = bsm.specification();
//        return StsProtos.BsmData.newBuilder()
//                .setPeriod(bsm.getPeriod())
//                .setNvar(spec.hasNoise() ? bsm.getVariance(Component.Noise) : 0)
//                .setLevel(spec.hasLevel())
//                .setNvar(spec.hasLevel() ? bsm.getVariance(Component.Level) : 0)
//                .setSlope(spec.hasSlope())
//                .setSvar(spec.hasSlope() ? bsm.getVariance(Component.Slope) : 0)
//                .setCycle(spec.hasCycle())
//                .setCyclePeriod(spec.hasCycle() ? bsm.getCyclicalPeriod() : 0)
//                .setCycleFactor(spec.hasCycle() ? bsm.getCyclicalDumpingFactor() : 0)
//                .setCvar(spec.hasCycle()? bsm.getVariance(Component.Cycle) : 0)
//                .setSeasonalModel(convert(spec.getSeasonalModel()))
//                .setSeasvar(spec.hasSeasonal() ? bsm.getVariance(Component.Seasonal) : 0)
//                .build();
//    }
}
