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
import demetra.regarima.RegressionSpec;
import demetra.regarima.io.protobuf.RegArimaProtos;
import demetra.regarima.io.protobuf.RegArimaProtosUtility;
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
    public RegressionSpec convert(X13Protos.RegArimaSpec.RegressionSpec spec, double tc) {
        RegressionSpec.Builder builder = RegressionSpec.builder()
                .mean(ToolkitProtosUtility.convert(spec.getMean()))
                .easter(EasterProto.convert(spec.getEaster()))
                .tradingDays(TradingDaysProto.convert(spec.getTd()));
        int n = spec.getOutliersCount();
        for (int i = 0; i < n; ++i) {
            RegArimaProtos.Outlier outlier = spec.getOutliers(i);
            builder.outlier(convert(outlier, tc));
        }
        n = spec.getUsersCount();
        for (int i = 0; i < n; ++i) {
            RegArimaProtos.TsVariable var = spec.getUsers(i);
            builder.userDefinedVariable(RegArimaProtosUtility.convert(var));
        }
        n = spec.getInterventionsCount();
        for (int i = 0; i < n; ++i) {
            RegArimaProtos.InterventionVariable var = spec.getInterventions(i);
            builder.interventionVariable(RegArimaProtosUtility.convert(var));
        }
        n = spec.getRampsCount();
        for (int i = 0; i < n; ++i) {
            RegArimaProtos.Ramp var = spec.getRamps(i);
            builder.ramp(RegArimaProtosUtility.convert(var));
        }
        
        return builder.build();
    }

    public X13Protos.RegArimaSpec.RegressionSpec convert(RegressionSpec spec) {
        X13Protos.RegArimaSpec.RegressionSpec.Builder builder = X13Protos.RegArimaSpec.RegressionSpec.newBuilder()
                .setMean(ToolkitProtosUtility.convert(spec.getMean()))
                .setEaster(EasterProto.convert(spec.getEaster()))
                .setTd(TradingDaysProto.convert(spec.getTradingDays()));
        
        List<Variable<IOutlier>> outliers = spec.getOutliers();
        for (Variable<IOutlier> outlier : outliers) {
           builder.addOutliers(convert(outlier));
        }
        List<Variable<TsContextVariable>> users = spec.getUserDefinedVariables();
        for (Variable<TsContextVariable> user:users) {
            builder.addUsers(RegArimaProtosUtility.convertTsContextVariable(user));
        }
        List<Variable<InterventionVariable>> ivs = spec.getInterventionVariables();
        for (Variable<InterventionVariable> iv : ivs) {
            builder.addInterventions(RegArimaProtosUtility.convertInterventionVariable(iv));
        }
        List<Variable<Ramp>> ramps = spec.getRamps();
        for (Variable<Ramp> ramp : ramps) {
            builder.addRamps(RegArimaProtosUtility.convertRamp(ramp));
        }
        
        return builder.build();
    }

    public Variable<IOutlier> convert(RegArimaProtos.Outlier outlier, double tc) {
        LocalDate ldt = ToolkitProtosUtility.convert(outlier.getPosition());
        IOutlier o=null;
        switch (outlier.getCode()) {
            case "ao":
            case "AO":
                o= new AdditiveOutlier(ldt.atStartOfDay());
                break;
            case "ls":
            case "LS":
                o= new LevelShift(ldt.atStartOfDay(), false);
                break;
            case "tc":
            case "TC":
                o= new TransitoryChange(ldt.atStartOfDay(), tc);
                break;
            case "so":
            case "SO":
                o=new PeriodicOutlier(ldt.atStartOfDay(), 0, false);
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
    
    public RegArimaProtos.Outlier convert(Variable<IOutlier> v){
        IOutlier outlier = v.getCore();
        return RegArimaProtos.Outlier.newBuilder()
                .setName(v.getName())
                .setCode(outlier.getCode())
                .setPosition(ToolkitProtosUtility.convert(outlier.getPosition().toLocalDate()))
                .setCoefficient(ToolkitProtosUtility.convert(v.getCoefficient(0)))
                .build();
    }
}
