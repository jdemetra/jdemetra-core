/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import demetra.data.Parameter;
import demetra.tramo.RegressionSpec;
import demetra.regarima.io.protobuf.RegArimaProtos;
import demetra.regarima.io.protobuf.RegArimaProtos.Variable;
import demetra.regarima.io.protobuf.RegArimaProtosUtility;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TransitoryChange;
import demetra.timeseries.regression.TsContextVariable;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import demetra.tramo.CalendarSpec;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
                .mean(spec.getMean())
                .calendar(cbuilder.build());
        int n = spec.getOutliersCount();
        for (int i = 0; i < n; ++i) {
            RegArimaProtos.Outlier outlier = spec.getOutliers(i);
            builder.outlier(convert(outlier, tc));
        }
//        n = spec.getUsersCount();
//        for (int i = 0; i < n; ++i) {
//            RegArimaProtos.Variable var = spec.getUsers(i);
//            builder.userDefinedVariable(RegArimaProtosUtility.convert(var));
//        }
//        n = spec.getInterventionsCount();
//        for (int i = 0; i < n; ++i) {
//            RegArimaProtos.InterventionVariable var = spec.getInterventions(i);
//            builder.interventionVariable(RegArimaProtosUtility.convert(var));
//        }
//        n = spec.getRampsCount();
//        for (int i = 0; i < n; ++i) {
//            RegArimaProtos.Ramp var = spec.getRamps(i);
//            builder.ramp(RegArimaProtosUtility.convert(var));
//        }
        
        n=spec.getPreadujstmentsCount();
        for (int i=0; i<n; ++i){
            TramoSeatsProtos.TramoSpec.RegressionSpec.PrespecifiedVariable p = spec.getPreadujstments(i);
            builder.coefficient(p.getName(), ToolkitProtosUtility.convert(p.getParametersList()));
        }
        return builder.build();
    }

    public TramoSeatsProtos.TramoSpec.RegressionSpec convert(RegressionSpec spec) {
        TramoSeatsProtos.TramoSpec.RegressionSpec.Builder builder = TramoSeatsProtos.TramoSpec.RegressionSpec.newBuilder()
                .setMean(spec.isMean())
                .setEaster(EasterProto.convert(spec.getCalendar().getEaster()))
                .setTd(TradingDaysProto.convert(spec.getCalendar().getTradingDays()));
        
        List<IOutlier> outliers = spec.getOutliers();
        for (IOutlier outlier : outliers) {
           builder.addOutliers(convert(outlier));
        }
//        List<Variable<TsContextVariable>> users = spec.getUserDefinedVariables();
//        for (TsContextVariable user:users) {
//            builder.addUsers(RegArimaProtosUtility.convert(user));
//        }
//        List<InterventionVariable> ivs = spec.getInterventionVariables();
//        for (InterventionVariable iv : ivs) {
//            builder.addInterventions(RegArimaProtosUtility.convert(iv));
//        }
//        List<Ramp> ramps = spec.getRamps();
//        for (Ramp ramp : ramps) {
//            builder.addRamps(RegArimaProtosUtility.convert(ramp));
//        }
        
        Map<String, Parameter[]> map = spec.getCoefficients();
        for (Entry<String, Parameter[]> entry : map.entrySet()){
            TramoSeatsProtos.TramoSpec.RegressionSpec.PrespecifiedVariable.Builder b = TramoSeatsProtos.TramoSpec.RegressionSpec.PrespecifiedVariable.newBuilder()
                    .setName(entry.getKey());
            for (int i=0; i<entry.getValue().length; ++i){
                b.addParameters(ToolkitProtosUtility.convert(entry.getValue()[i]));
            }        
            builder.addPreadujstments(b.build());
        }
        return builder.build();
    }

    public IOutlier convert(RegArimaProtos.Outlier outlier, double tc) {
        LocalDate ldt = LocalDate.parse(outlier.getPosition(), DateTimeFormatter.ISO_DATE);
        switch (outlier.getCode()) {
            case "ao":
            case "AO":
                return new AdditiveOutlier(ldt.atStartOfDay());
            case "ls":
            case "LS":
                return new LevelShift(ldt.atStartOfDay(), false);
            case "tc":
            case "TC":
                return new TransitoryChange(ldt.atStartOfDay(), tc);

            case "so":
            case "SO":
                return new PeriodicOutlier(ldt.atStartOfDay(), 0, false);

            default:
                return null;
        }
    }
    
    public RegArimaProtos.Outlier convert(IOutlier outlier){
        return RegArimaProtos.Outlier.newBuilder()
                .setCode(outlier.getCode())
                .setPosition(outlier.getPosition().toLocalDate().format(DateTimeFormatter.ISO_DATE))
                .build();
    }
}
