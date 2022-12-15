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
import demetra.x11.SeasonalFilterOption;
import demetra.x11.SigmaVecOption;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X11Proto {
    public void fill(demetra.x11.X11Spec spec, X11Spec.Builder builder) {
        builder
                .setMode(X13ProtosUtility.convert(spec.getMode()))
                .setSeasonal(spec.isSeasonal())
                .setLsig(spec.getLowerSigma())
                .setUsig(spec.getUpperSigma())
                .setHenderson(spec.getHendersonFilterLength())
                .setNfcasts(spec.getForecastHorizon())
                .setNbcasts(spec.getBackcastHorizon())
                .setSigma(X13ProtosUtility.convert(spec.getCalendarSigma()))
                .setExcludeFcasts(spec.isExcludeForecast())
                .setBias(X13ProtosUtility.convert(spec.getBias()));
        SeasonalFilterOption[] filters = spec.getFilters();
        for (int i = 0; i < filters.length; ++i) {
            builder.addSfilters(X13ProtosUtility.convert(filters[i]));
        }
        SigmaVecOption[] vs = spec.getSigmaVec();
        if (vs != null) {
            for (int i = 0; i < vs.length; ++i) {
                builder.addVsigmas(vs[i] == SigmaVecOption.Group1 ? 1 : 2);
            }
        }
    }

    public X11Spec convert(demetra.x11.X11Spec spec) {
        X11Spec.Builder builder = X11Spec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public demetra.x11.X11Spec convert(X11Spec x11) {
        demetra.x11.X11Spec.Builder builder = demetra.x11.X11Spec.builder()
                .mode(X13ProtosUtility.convert(x11.getMode()))
                .seasonal(x11.getSeasonal())
                .lowerSigma(x11.getLsig())
                .upperSigma(x11.getUsig())
                .hendersonFilterLength(x11.getHenderson())
                .forecastHorizon(x11.getNfcasts())
                .backcastHorizon(x11.getNbcasts())
                .calendarSigma(X13ProtosUtility.convert(x11.getSigma()))
                .excludeForecast(x11.getExcludeFcasts())
                .bias(X13ProtosUtility.convert(x11.getBias()));
        int n = x11.getSfiltersCount();
        if (n > 0) {
            SeasonalFilterOption[] sf = new SeasonalFilterOption[n];
            for (int i = 0; i < n; ++i) {
                sf[i] = X13ProtosUtility.convert(x11.getSfilters(i));
            }
            builder.filters(sf);
        }
        n = x11.getVsigmasCount();
        if (n > 0) {
            SigmaVecOption[] sv = new SigmaVecOption[n];
            for (int i = 0; i < n; ++i) {
                sv[i] = x11.getVsigmas(i) == 1 ? SigmaVecOption.Group1 : SigmaVecOption.Group2;
            }
            builder.sigmaVec(sv);
        }

        return builder.build();
    }

   
}
