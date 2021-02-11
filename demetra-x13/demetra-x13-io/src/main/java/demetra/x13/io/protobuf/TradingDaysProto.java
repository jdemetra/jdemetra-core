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

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.regarima.TradingDaysSpec;
import demetra.regarima.io.protobuf.RegArimaProtosUtility;
import demetra.timeseries.calendars.TradingDaysType;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TradingDaysProto {

    public void fill(TradingDaysSpec spec, X13Protos.RegArimaSpec.TradingDaysSpec.Builder builder) {

        String holidays = spec.getHolidays();
        if (holidays != null) {
            builder.setHolidays(holidays)
                    .setLp(RegArimaProtosUtility.convert(spec.getLengthOfPeriodType()))
                    .setTd(RegArimaProtosUtility.convert(spec.getTradingDaysType()))
                    .setTest(X13ProtosUtility.convert(spec.getRegressionTestType()))
                    .setAutoAdjust(spec.isAutoAdjust());
            return;
        }

        String[] userVariables = spec.getUserVariables();
        if (userVariables != null && userVariables.length > 0) {
            for (String v : userVariables) {
                builder.addUsers(v);
            }
            builder.setTest(X13ProtosUtility.convert(spec.getRegressionTestType()));
            return;
        }
        int w = spec.getStockTradingDays();
        if (w > 0) {
            builder.setW(w)
                    .setTest(X13ProtosUtility.convert(spec.getRegressionTestType()));
            return;
        }
        builder.setLp(RegArimaProtosUtility.convert(spec.getLengthOfPeriodType()))
                .setTd(RegArimaProtosUtility.convert(spec.getTradingDaysType()))
                .setTest(X13ProtosUtility.convert(spec.getRegressionTestType()))
                .setAutoAdjust(spec.isAutoAdjust());
    }

    public X13Protos.RegArimaSpec.TradingDaysSpec convert(TradingDaysSpec spec) {
        X13Protos.RegArimaSpec.TradingDaysSpec.Builder builder = X13Protos.RegArimaSpec.TradingDaysSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public byte[] toBuffer(TradingDaysSpec spec) {
        return convert(spec).toByteArray();
    }

    public TradingDaysSpec convert(X13Protos.RegArimaSpec.TradingDaysSpec spec) {
        String holidays = spec.getHolidays();
        if (holidays != null && holidays.length() > 0) {
            return TradingDaysSpec.holidays(holidays,
                    RegArimaProtosUtility.convert(spec.getTd()),
                    RegArimaProtosUtility.convert(spec.getLp()),
                    X13ProtosUtility.convert(spec.getTest()),
                    spec.getAutoAdjust());
        }
        int nusers = spec.getUsersCount();
        if (nusers > 0) {
            String[] users = new String[nusers];
            for (int i = 0; i < nusers; ++i) {
                users[i] = spec.getUsers(i);
            }
            return TradingDaysSpec.userDefined(users, X13ProtosUtility.convert(spec.getTest()));
        }
        int w = spec.getW();
        if (w > 0) {
            return TradingDaysSpec.stockTradingDays(w, X13ProtosUtility.convert(spec.getTest()));
        }
        TradingDaysType td = RegArimaProtosUtility.convert(spec.getTd());
        if (td == TradingDaysType.None) {
            return TradingDaysSpec.none();
        } else {
            return TradingDaysSpec.td(td,
                    RegArimaProtosUtility.convert(spec.getLp()),
                    X13ProtosUtility.convert(spec.getTest()),
                    spec.getAutoAdjust());
        }

    }

    public TradingDaysSpec of(byte[] bytes) throws InvalidProtocolBufferException {
        X13Protos.RegArimaSpec.TradingDaysSpec spec = X13Protos.RegArimaSpec.TradingDaysSpec.parseFrom(bytes);
        return convert(spec);
    }

}
