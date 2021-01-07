/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.SigmaVecOption;
import demetra.x11.X11Spec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X11Proto {
    public void fill(X11Spec spec, X13Protos.X11Spec.Builder builder) {
        builder
                .setMode(X13ProtosUtility.convert(spec.getMode()))
                .setSeasonal(spec.isSeasonal())
                .setLsig(spec.getLowerSigma())
                .setUsig(spec.getUpperSigma())
                .setHenderson(spec.getHendersonFilterLength())
                .setNfcasts(spec.getForecastHorizon())
                .setNbcasts(spec.getForecastHorizon())
                .setSigma(X13ProtosUtility.convert(spec.getCalendarSigma()))
                .setExcudefcasts(spec.isExcludeForecast())
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

    public X13Protos.X11Spec convert(X11Spec spec) {
        X13Protos.X11Spec.Builder builder = X13Protos.X11Spec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public byte[] toBuffer(X11Spec spec) {
        return convert(spec).toByteArray();
    }

    public X11Spec convert(X13Protos.X11Spec x11) {
        X11Spec.Builder builder = X11Spec.builder()
                .mode(X13ProtosUtility.convert(x11.getMode()))
                .seasonal(x11.getSeasonal())
                .lowerSigma(x11.getLsig())
                .upperSigma(x11.getUsig())
                .hendersonFilterLength(x11.getHenderson())
                .forecastHorizon(x11.getNfcasts())
                .backcastHorizon(x11.getNbcasts())
                .calendarSigma(X13ProtosUtility.convert(x11.getSigma()))
                .excludeForecast(x11.getExcudefcasts())
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

    public X11Spec of(byte[] bytes) throws InvalidProtocolBufferException {
        X13Protos.X11Spec x11 = X13Protos.X11Spec.parseFrom(bytes);
        return convert(x11);
    }
    
}
