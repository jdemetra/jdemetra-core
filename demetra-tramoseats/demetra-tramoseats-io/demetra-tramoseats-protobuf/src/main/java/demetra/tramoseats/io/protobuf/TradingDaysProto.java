/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.data.Parameter;
import demetra.modelling.io.protobuf.ModellingProtosUtility;
import demetra.tramo.TradingDaysSpec;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TradingDaysProto {

    public void fill(TradingDaysSpec spec, TramoSeatsProtos.TramoSpec.TradingDaysSpec.Builder builder) {

        String holidays = spec.getHolidays();
        if (holidays != null) {
            builder.setHolidays(holidays)
                    .setLp(ModellingProtosUtility.convert(spec.getLengthOfPeriodType()))
                    .setTd(ModellingProtosUtility.convert(spec.getTradingDaysType()))
                    .setAuto(TramoSeatsProtosUtility.convert(spec.getAutomaticMethod()))
                    .setTest(TramoSeatsProtosUtility.convert(spec.getRegressionTestType()));
            return;
        }

        String[] userVariables = spec.getUserVariables();
        if (userVariables != null && userVariables.length > 0) {
            for (String v : userVariables) {
                builder.addUsers(v);
            }
            builder.setTest(TramoSeatsProtosUtility.convert(spec.getRegressionTestType()));
            return;
        }
        int w = spec.getStockTradingDays();
        if (w > 0) {
            builder.setW(w)
                    .setTest(TramoSeatsProtosUtility.convert(spec.getRegressionTestType()));
            return;
        }
        if (spec.isAutomatic()) {
            builder.setLp(ModellingProtosUtility.convert(spec.getLengthOfPeriodType()))
                    .setTd(ModellingProtosUtility.convert(spec.getTradingDaysType()))
                    .setTest(TramoSeatsProtosUtility.convert(spec.getRegressionTestType()))
                    .setAuto(TramoSeatsProtosUtility.convert(spec.getAutomaticMethod()))
                    .setPtest(spec.getProbabilityForFTest());

        } else {
            builder.setLp(ModellingProtosUtility.convert(spec.getLengthOfPeriodType()))
                    .setTd(ModellingProtosUtility.convert(spec.getTradingDaysType()))
                    .setTest(TramoSeatsProtosUtility.convert(spec.getRegressionTestType()));
        }
    }

    public TramoSeatsProtos.TramoSpec.TradingDaysSpec convert(TradingDaysSpec spec) {
        TramoSeatsProtos.TramoSpec.TradingDaysSpec.Builder builder = TramoSeatsProtos.TramoSpec.TradingDaysSpec.newBuilder();
        fill(spec, builder);
        return builder
                .setLpcoefficient(ToolkitProtosUtility.convert(spec.getLpCoefficient()))
                .addAllTdcoefficients(ToolkitProtosUtility.convert(spec.getTdCoefficients()))
                .build();
    }

    public byte[] toBuffer(TradingDaysSpec spec) {
        return convert(spec).toByteArray();
    }

    private boolean isTest(TramoSeatsProtos.TramoSpec.TradingDaysSpec spec) {
        return spec.getAuto() == TramoSeatsProtos.AutomaticTradingDays.TD_AUTO_WALD
                || spec.getAuto() == TramoSeatsProtos.AutomaticTradingDays.TD_AUTO_FTEST
                || spec.getTest() == TramoSeatsProtos.TradingDaysTest.TD_TEST_JOINT_F
                || spec.getTest() == TramoSeatsProtos.TradingDaysTest.TD_TEST_SEPARATE_T;
    }

    public TradingDaysSpec convert(TramoSeatsProtos.TramoSpec.TradingDaysSpec spec) {
        String holidays = spec.getHolidays();
        TradingDaysType td = ModellingProtosUtility.convert(spec.getTd());
        LengthOfPeriodType lp = ModellingProtosUtility.convert(spec.getLp());
        Parameter lpc = ToolkitProtosUtility.convert(spec.getLpcoefficient());
        Parameter[] tdc = ToolkitProtosUtility.convert(spec.getTdcoefficientsList());
        boolean test = isTest(spec);
        if (holidays != null && holidays.length() > 0) {
            TradingDaysSpec.AutoMethod auto = TramoSeatsProtosUtility.convert(spec.getAuto());
            if (auto != TradingDaysSpec.AutoMethod.UNUSED) {
                return TradingDaysSpec.automaticHolidays(holidays, lp, auto, spec.getPtest(), spec.getAutoAdjust());
            }
            if (test) {
                return TradingDaysSpec.holidays(holidays, td, lp,
                        TramoSeatsProtosUtility.convert(spec.getTest()), spec.getAutoAdjust());
            } else {
                return TradingDaysSpec.holidays(holidays, td, lp,
                        tdc, lpc);
            }
        }
        int nusers = spec.getUsersCount();
        if (nusers > 0) {
            String[] users = new String[nusers];
            for (int i = 0; i < nusers; ++i) {
                users[i] = spec.getUsers(i);
            }
            if (test) {
                return TradingDaysSpec.userDefined(users, TramoSeatsProtosUtility.convert(spec.getTest()));
            } else {
                return TradingDaysSpec.userDefined(users, tdc);
            }
        }
        int w = spec.getW();
        if (w > 0) {
            return TradingDaysSpec.stockTradingDays(w, TramoSeatsProtosUtility.convert(spec.getTest()));
        }
        TradingDaysSpec.AutoMethod auto = TramoSeatsProtosUtility.convert(spec.getAuto());
        if (auto != TradingDaysSpec.AutoMethod.UNUSED) {
            return TradingDaysSpec.automatic(lp, auto, spec.getPtest(), spec.getAutoAdjust());
        } else if (td == TradingDaysType.NONE) {
            return TradingDaysSpec.none();
        } else {
            if (test) {
                return TradingDaysSpec.td(td, lp,
                        TramoSeatsProtosUtility.convert(spec.getTest()), spec.getAutoAdjust());
            } else {
                return TradingDaysSpec.td(td, lp,
                        tdc, lpc);
            }
        }
    }

    public TradingDaysSpec of(byte[] bytes) throws InvalidProtocolBufferException {
        TramoSeatsProtos.TramoSpec.TradingDaysSpec spec = TramoSeatsProtos.TramoSpec.TradingDaysSpec.parseFrom(bytes);
        return convert(spec);
    }

}
