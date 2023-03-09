/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.x13.io.protobuf;

import demetra.data.Parameter;
import demetra.modelling.io.protobuf.ModellingProtos;
import demetra.modelling.io.protobuf.ModellingProtosUtility;
import demetra.regarima.MeanSpec;
import demetra.regarima.RegressionSpec;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TransitoryChange;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegressionProto {
    public RegressionSpec convert(RegArimaSpec.RegressionSpec spec, double tc) {

        MeanSpec mean = MeanSpec.none();
        if (spec.hasMean()) {
            boolean check = spec.getCheckMean();
            mean=MeanSpec.builder()
                    .trendConstant(true)
                    .test(check)
                    .coefficient(ToolkitProtosUtility.convert(spec.getMean()))
                    .build();
        }

        RegressionSpec.Builder builder = RegressionSpec.builder()
                .mean(mean)
                .easter(EasterProto.convert(spec.getEaster()))
                .tradingDays(TradingDaysProto.convert(spec.getTd()));
        int n = spec.getOutliersCount();
        for (int i = 0; i < n; ++i) {
            ModellingProtos.Outlier outlier = spec.getOutliers(i);
            builder.outlier(convert(outlier, tc));
        }
        n = spec.getUsersCount();
        for (int i = 0; i < n; ++i) {
            ModellingProtos.TsVariable var = spec.getUsers(i);
            builder.userDefinedVariable(ModellingProtosUtility.convert(var));
        }
        n = spec.getInterventionsCount();
        for (int i = 0; i < n; ++i) {
            ModellingProtos.InterventionVariable var = spec.getInterventions(i);
            builder.interventionVariable(ModellingProtosUtility.convert(var));
        }
        n = spec.getRampsCount();
        for (int i = 0; i < n; ++i) {
            ModellingProtos.Ramp var = spec.getRamps(i);
            builder.ramp(ModellingProtosUtility.convert(var));
        }
        
        return builder.build();
    }

    public RegArimaSpec.RegressionSpec convert(RegressionSpec spec) {
        RegArimaSpec.RegressionSpec.Builder builder = RegArimaSpec.RegressionSpec.newBuilder()
                .setEaster(EasterProto.convert(spec.getEaster()))
                .setTd(TradingDaysProto.convert(spec.getTradingDays()));
        MeanSpec mean = spec.getMean();
        if (mean.isUsed()) {
            builder.setMean(ToolkitProtosUtility.convert(mean.getCoefficient()))
                    .setCheckMean(mean.isTest());
        }else
            builder.clearMean();
        
        List<Variable<IOutlier>> outliers = spec.getOutliers();
        for (Variable<IOutlier> outlier : outliers) {
           builder.addOutliers(convert(outlier));
        }
        List<Variable<TsContextVariable>> users = spec.getUserDefinedVariables();
        for (Variable<TsContextVariable> user:users) {
            builder.addUsers(ModellingProtosUtility.convertTsContextVariable(user));
        }
        List<Variable<InterventionVariable>> ivs = spec.getInterventionVariables();
        for (Variable<InterventionVariable> iv : ivs) {
            builder.addInterventions(ModellingProtosUtility.convertInterventionVariable(iv));
        }
        List<Variable<Ramp>> ramps = spec.getRamps();
        for (Variable<Ramp> ramp : ramps) {
            builder.addRamps(ModellingProtosUtility.convertRamp(ramp));
        }
        
        return builder.build();
    }

    public Variable<IOutlier> convert(ModellingProtos.Outlier outlier, double tc) {
        LocalDate ldt = ToolkitProtosUtility.convert(outlier.getPosition());
        IOutlier o=null;
        switch (outlier.getCode()) {
            case "ao":
            case "AO":
                o= new AdditiveOutlier(ldt.atStartOfDay());
                break;
            case "ls":
            case "LS":
                o= new LevelShift(ldt.atStartOfDay(), true);
                break;
            case "tc":
            case "TC":
                o= new TransitoryChange(ldt.atStartOfDay(), tc);
                break;
            case "so":
            case "SO":
                o=new PeriodicOutlier(ldt.atStartOfDay(), 0, true);
                break;

            default:
                return null;
        }
        Parameter c = ToolkitProtosUtility.convert(outlier.getCoefficient());
        return Variable.<IOutlier>builder()
                .core(o)
                .name(outlier.getName())
                .coefficients(c == null ? null : new Parameter[]{c})
                .attributes(outlier.getMetadataMap())
                .build();        
    }
    
    public ModellingProtos.Outlier convert(Variable<IOutlier> v){
        IOutlier outlier = v.getCore();
        return ModellingProtos.Outlier.newBuilder()
                .setName(v.getName())
                .setCode(outlier.getCode())
                .setPosition(ToolkitProtosUtility.convert(outlier.getPosition().toLocalDate()))
                .setCoefficient(ToolkitProtosUtility.convert(v.getCoefficient(0)))
                .build();
    }
}
