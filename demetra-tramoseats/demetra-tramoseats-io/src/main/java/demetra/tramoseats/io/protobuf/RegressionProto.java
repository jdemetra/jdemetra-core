/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import demetra.data.Parameter;
import demetra.tramo.RegressionSpec;
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
import demetra.tramo.CalendarSpec;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegressionProto {

    public RegressionSpec convert(TramoSeatsProtos.TramoSpec.RegressionSpec spec, double tc) {
        CalendarSpec.Builder cbuilder = CalendarSpec.builder();
        if (spec.hasEaster()) {
            cbuilder.easter(EasterProto.convert(spec.getEaster()));
        }
        if (spec.hasTd()) {
            cbuilder.tradingDays(TradingDaysProto.convert(spec.getTd()));
        }
        RegressionSpec.Builder builder = RegressionSpec.builder()
                .mean(ToolkitProtosUtility.convert(spec.getMean()))
                .calendar(cbuilder.build());
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

    public TramoSeatsProtos.TramoSpec.RegressionSpec convert(RegressionSpec spec) {
        TramoSeatsProtos.TramoSpec.RegressionSpec.Builder builder = TramoSeatsProtos.TramoSpec.RegressionSpec.newBuilder()
                .setMean(ToolkitProtosUtility.convert(spec.getMean()))
                .setEaster(EasterProto.convert(spec.getCalendar().getEaster()))
                .setTd(TradingDaysProto.convert(spec.getCalendar().getTradingDays()));
        
        List<Variable<IOutlier>> outliers = spec.getOutliers();
        outliers.forEach(outlier -> {
            builder.addOutliers(convert(outlier));
        });
        List<Variable<TsContextVariable>> users = spec.getUserDefinedVariables();
        users.forEach(user -> {
            builder.addUsers(RegArimaProtosUtility.convertTsContextVariable(user));
        });
        List<Variable<InterventionVariable>> ivs = spec.getInterventionVariables();
        ivs.forEach(iv -> {
            builder.addInterventions(RegArimaProtosUtility.convertInterventionVariable(iv));
        });
        List<Variable<Ramp>> ramps = spec.getRamps();
        ramps.forEach(ramp -> {
            builder.addRamps(RegArimaProtosUtility.convertRamp(ramp));
        });
        
        return builder.build();
    }

    public Variable<IOutlier> convert(RegArimaProtos.Outlier outlier, double tc) {
        LocalDate ldt = ToolkitProtosUtility.convert(outlier.getPosition());
        IOutlier o;
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
