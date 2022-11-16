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
package demetra.tramoseats.r;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.data.DoubleSeq;
import demetra.math.matrices.Matrix;
import jdplus.modelling.StationaryTransformation;
import demetra.regarima.io.protobuf.RegArimaEstimationProto;
import demetra.sa.EstimationPolicyType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ModellingContext;
import demetra.tramo.TramoSpec;
import jdplus.tramo.TramoOutput;
import demetra.tramoseats.io.protobuf.TramoProto;
import demetra.tramoseats.io.protobuf.TramoSeatsProtos;
import jdplus.math.matrices.FastMatrix;
import jdplus.regsarima.regular.Forecast;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.tramo.TramoFactory;
import jdplus.tramo.TramoKernel;
import jdplus.tramo.internal.DifferencingModule;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Tramo {

    public byte[] toBuffer(RegSarimaModel core) {
        return RegArimaEstimationProto.convert(core).toByteArray();
    }

    public RegSarimaModel process(TsData series, String defSpec) {
        TramoSpec spec = TramoSpec.fromString(defSpec);
        TramoKernel tramo = TramoKernel.of(spec, null);
        return tramo.process(series.cleanExtremities(), null);
    }

    public RegSarimaModel process(TsData series, TramoSpec spec, ModellingContext context) {
        TramoKernel tramo = TramoKernel.of(spec, context);
        return tramo.process(series.cleanExtremities(), null);
    }

    public TramoSpec refreshSpec(TramoSpec currentSpec, TramoSpec domainSpec, TsDomain domain, String policy) {
        return TramoFactory.getInstance().refreshSpec(currentSpec, domainSpec, EstimationPolicyType.valueOf(policy), domain);
    }

    public Matrix forecast(TsData series, String defSpec, int nf) {
        TramoSpec spec = TramoSpec.fromString(defSpec);
        return forecast(series, spec, null, nf);
    }

    public Matrix forecast(TsData series, TramoSpec spec, ModellingContext context, int nf) {
        TramoKernel kernel = TramoKernel.of(spec, context);
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

    public TramoOutput fullProcess(TsData series, TramoSpec spec, ModellingContext context) {
        TramoKernel tramo = TramoKernel.of(spec, context);
        RegSarimaModel estimation = tramo.process(series.cleanExtremities(), null);

        return TramoOutput.builder()
                .estimationSpec(spec)
                .result(estimation)
                .resultSpec(estimation == null ? null : TramoFactory.getInstance().generateSpec(spec, estimation.getDescription()))
                .build();
    }

    public TramoOutput fullProcess(TsData series, String defSpec) {
        TramoSpec spec = TramoSpec.fromString(defSpec);
        return fullProcess(series, spec, null);
    }

    public byte[] toBuffer(TramoSpec spec) {
        return TramoProto.convert(spec).toByteArray();
    }

    public TramoSpec specOf(byte[] buffer) {
        try {
            TramoSeatsProtos.TramoSpec spec = TramoSeatsProtos.TramoSpec.parseFrom(buffer);
            return TramoProto.convert(spec);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

    public byte[] toBuffer(TramoOutput output) {
        return TramoProto.convert(output).toByteArray();
    }

    public StationaryTransformation doStationary(double[] data, int period) {
        DifferencingModule diff = DifferencingModule.builder()
                .build();

        DoubleSeq s = DoubleSeq.of(data);
        diff.process(s, period, 0, 0, true);

        return StationaryTransformation.builder()
                .meanCorrection(diff.isMeanCorrection())
                .difference(new StationaryTransformation.Differencing(1, diff.getD()))
                .difference(new StationaryTransformation.Differencing(period, diff.getBd()))
                .build();
    }

}
