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
package demetra.x13.r;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.data.DoubleSeq;
import demetra.math.matrices.Matrix;
import demetra.regarima.RegArimaSpec;
import demetra.regarima.io.protobuf.RegArimaEstimationProto;
import demetra.sa.EstimationPolicyType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ModellingContext;
import demetra.util.r.Dictionary;
import demetra.x13.io.protobuf.RegArimaProto;
import demetra.x13.io.protobuf.X13Protos;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.StationaryTransformation;
import jdplus.regsarima.regular.Forecast;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.x13.regarima.DifferencingModule;
import jdplus.x13.regarima.RegArimaFactory;
import jdplus.x13.regarima.RegArimaKernel;
import jdplus.x13.regarima.RegArimaOutput;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class RegArima {

    public byte[] toBuffer(RegSarimaModel core) {
        return RegArimaEstimationProto.convert(core).toByteArray();
    }

    public RegSarimaModel process(TsData series, String defSpec) {
        RegArimaSpec spec = RegArimaSpec.fromString(defSpec);
        RegArimaKernel regarima = RegArimaKernel.of(spec, null);
        return regarima.process(series.cleanExtremities(), null);
    }

    public RegSarimaModel process(TsData series, RegArimaSpec spec, Dictionary dic) {
        ModellingContext context = dic == null ? null : dic.toContext();
        RegArimaKernel regarima = RegArimaKernel.of(spec, context);
        return regarima.process(series.cleanExtremities(), null);
    }

    public RegArimaSpec refreshSpec(RegArimaSpec currentSpec, RegArimaSpec domainSpec, TsDomain domain, String policy) {
        return RegArimaFactory.INSTANCE.refreshSpec(currentSpec, domainSpec, EstimationPolicyType.valueOf(policy), domain);
    }

    public Matrix forecast(TsData series, String defSpec, int nf) {
        RegArimaSpec spec = RegArimaSpec.fromString(defSpec);
        return forecast(series, spec, null, nf);
    }

    public Matrix forecast(TsData series, RegArimaSpec spec, Dictionary dic, int nf) {
        ModellingContext context = dic == null ? null : dic.toContext();
        RegArimaKernel kernel = RegArimaKernel.of(spec, context);
        Forecast f = new Forecast(kernel, nf);
        if (!f.process(series.cleanExtremities())) {
            return null;
        }
        FastMatrix R = FastMatrix.make(nf, 4);
        R.column(0).copy(f.getForecasts());
        R.column(1).copy(f.getForecastsStdev());
        R.column(2).copy(f.getRawForecasts());
        R.column(3).copy(f.getRawForecastsStdev());
        return R;
    }

    public byte[] toBuffer(RegArimaSpec spec) {
        return RegArimaProto.convert(spec).toByteArray();
    }

    public RegArimaSpec specOf(byte[] buffer) {
        try {
            X13Protos.RegArimaSpec spec = X13Protos.RegArimaSpec.parseFrom(buffer);
            return RegArimaProto.convert(spec);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

    public RegArimaOutput fullProcess(TsData series, RegArimaSpec spec, Dictionary dic) {
        ModellingContext context = dic == null ? null : dic.toContext();
        RegArimaKernel tramo = RegArimaKernel.of(spec, context);
        RegSarimaModel estimation = tramo.process(series.cleanExtremities(), null);

        return RegArimaOutput.builder()
                .estimationSpec(spec)
                .result(estimation)
                .resultSpec(estimation == null ? null : RegArimaFactory.INSTANCE.generateSpec(spec, estimation.getDescription()))
                .build();
    }

    public RegArimaOutput fullProcess(TsData series, String defSpec) {
        RegArimaSpec spec = RegArimaSpec.fromString(defSpec);
        return fullProcess(series, spec, null);
    }

    public byte[] toBuffer(RegArimaOutput output) {
        return RegArimaProto.convert(output).toByteArray();
    }

    public StationaryTransformation doStationary(double[] data, int period) {
        DifferencingModule diff = DifferencingModule.builder()
                .build();

        DoubleSeq s = DoubleSeq.of(data);
        diff.process(s, period);

        return StationaryTransformation.builder()
                .meanCorrection(diff.isMeanCorrection())
                .difference(new StationaryTransformation.Differencing(1, diff.getD()))
                .difference(new StationaryTransformation.Differencing(period, diff.getBd()))
                .build();
    }

}
