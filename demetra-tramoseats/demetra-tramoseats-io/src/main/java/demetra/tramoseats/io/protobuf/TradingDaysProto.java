/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.tramo.TradingDaysSpec;
import demetra.regarima.io.protobuf.RegArimaProtosUtility;
import demetra.timeseries.calendars.TradingDaysType;

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
                    .setLp(RegArimaProtosUtility.convert(spec.getLengthOfPeriodType()))
                    .setTd(RegArimaProtosUtility.convert(spec.getTradingDaysType()))
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
            builder.setLp(RegArimaProtosUtility.convert(spec.getLengthOfPeriodType()))
                    .setTd(RegArimaProtosUtility.convert(spec.getTradingDaysType()))
                    .setTest(TramoSeatsProtosUtility.convert(spec.getRegressionTestType()))
                    .setAuto(TramoSeatsProtosUtility.convert(spec.getAutomaticMethod()))
                    .setPtest(spec.getProbabilityForFTest());

        } else {
            builder.setLp(RegArimaProtosUtility.convert(spec.getLengthOfPeriodType()))
                    .setTd(RegArimaProtosUtility.convert(spec.getTradingDaysType()))
                    .setTest(TramoSeatsProtosUtility.convert(spec.getRegressionTestType()));
        }
    }

    public TramoSeatsProtos.TramoSpec.TradingDaysSpec convert(TradingDaysSpec spec) {
        TramoSeatsProtos.TramoSpec.TradingDaysSpec.Builder builder = TramoSeatsProtos.TramoSpec.TradingDaysSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public byte[] toBuffer(TradingDaysSpec spec) {
        return convert(spec).toByteArray();
    }

    public TradingDaysSpec convert(TramoSeatsProtos.TramoSpec.TradingDaysSpec spec) {
        String holidays = spec.getHolidays();
        if (holidays != null && holidays.length() > 0) {
            return TradingDaysSpec.holidays(holidays,
                    RegArimaProtosUtility.convert(spec.getTd()),
                    RegArimaProtosUtility.convert(spec.getLp()),
                    TramoSeatsProtosUtility.convert(spec.getTest()));
        }
        int nusers = spec.getUsersCount();
        if (nusers > 0) {
            String[] users = new String[nusers];
            for (int i = 0; i < nusers; ++i) {
                users[i] = spec.getUsers(i);
            }
            return TradingDaysSpec.userDefined(users, TramoSeatsProtosUtility.convert(spec.getTest()));
        }
        int w = spec.getW();
        if (w > 0) {
            return TradingDaysSpec.stockTradingDays(w, TramoSeatsProtosUtility.convert(spec.getTest()));
        }
        TradingDaysType td = RegArimaProtosUtility.convert(spec.getTd());
        TradingDaysSpec.AutoMethod auto = TramoSeatsProtosUtility.convert(spec.getAuto());
        if (auto != TradingDaysSpec.AutoMethod.Unused) {
            return TradingDaysSpec.automatic(auto, spec.getPtest());
        } else if (td == TradingDaysType.None) {
            return TradingDaysSpec.none();
        } else {
            return TradingDaysSpec.td(td,
                    RegArimaProtosUtility.convert(spec.getLp()),
                    TramoSeatsProtosUtility.convert(spec.getTest()));
        }

    }

    public TradingDaysSpec of(byte[] bytes) throws InvalidProtocolBufferException {
        TramoSeatsProtos.TramoSpec.TradingDaysSpec spec = TramoSeatsProtos.TramoSpec.TradingDaysSpec.parseFrom(bytes);
        return convert(spec);
    }

}
