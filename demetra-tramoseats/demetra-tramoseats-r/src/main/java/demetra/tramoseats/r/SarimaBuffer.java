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

import demetra.arima.SarimaSpec;
import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.util.r.Buffer;
import demetra.util.r.Buffers;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SarimaBuffer extends Buffer<SarimaSpec>{

    private static final int MAXR = 3, MAXS = 1, D = 0, BD = D + 1,
            PHI = BD + 1, JPHI = PHI + MAXR, THETA = JPHI + MAXR, JTHETA = THETA + MAXR,
            BPHI = JTHETA + MAXR, JBPHI = BPHI + MAXS, BTHETA = JBPHI + MAXS, JBTHETA = BTHETA + MAXS, SIZE = JBTHETA + MAXS;

    public static SarimaBuffer of(SarimaSpec spec) {
        double[] input = new double[SIZE];
        // fill the buffer
        input[D] = spec.getD();
        input[BD] = spec.getBd();
        Parameter[] phi = spec.getPhi();
        for (int i = 0; i < phi.length; ++i) {
            input[PHI + i] = phi[i].getValue();
            input[JPHI + i] = Buffers.parameterType(phi[i].getType());
        }
        Parameter[] th = spec.getTheta();
        for (int i = 0; i < th.length; ++i) {
            input[THETA + i] = th[i].getValue();
            input[JTHETA + i] = Buffers.parameterType(th[i].getType());
        }
        Parameter[] bphi = spec.getBphi();
        for (int i = 0; i < bphi.length; ++i) {
            input[BPHI + i] = bphi[i].getValue();
            input[JBPHI + i] = Buffers.parameterType(bphi[i].getType());
        }
        Parameter[] bth = spec.getBtheta();
        for (int i = 0; i < bth.length; ++i) {
            input[BTHETA + i] = bth[i].getValue();
            input[JBTHETA + i] = Buffers.parameterType(bth[i].getType());
        }

        return new SarimaBuffer(input);
    }

    public SarimaBuffer(double[] data) {
        super(data);
    }

    private int n(int pos0, int pos1) {
        for (int i = pos0; i < pos1; ++i) {
            if (buffer[i] == 0) {
                return i - pos0;
            }
        }
        return pos1 - pos0;
    }

    @Override
    public SarimaSpec build() {
        SarimaSpec.Builder builder = SarimaSpec.builder()
                .d((int) buffer[D])
                .bd((int) buffer[BD]);
        int n = n(JPHI, JPHI + MAXR);
        if (n > 0) {
            Parameter[] Phi = new Parameter[n];
            for (int i = 0; i < n; ++i) {
                ParameterType t = Buffers.parameterType((int) buffer[JPHI + i]);
                double val = buffer[PHI + i];
                Phi[i] = Parameter.of(val, t);
            }
            builder.phi(Phi);
        }
        n = n(JTHETA, JTHETA + MAXR);
        if (n > 0) {
            Parameter[] Th = new Parameter[n];
            for (int i = 0; i < n; ++i) {
                ParameterType t = Buffers.parameterType((int) buffer[JTHETA + i]);
                double val = buffer[THETA + i];
                Th[i] = Parameter.of(val, t);
            }
            builder.theta(Th);
        }
        n = n(JBPHI, JBPHI + MAXS);
        if (n > 0) {
            Parameter[] Bphi = new Parameter[n];
            for (int i = 0; i < n; ++i) {
                ParameterType t = Buffers.parameterType((int) buffer[JBPHI + i]);
                double val = buffer[BPHI + i];
                Bphi[i] = Parameter.of(val, t);
            }
            builder.bphi(Bphi);
        }
        n = n(JBTHETA, JBTHETA + MAXS);
        if (n > 0) {
            Parameter[] Bth = new Parameter[n];
            for (int i = 0; i < n; ++i) {
                ParameterType t = Buffers.parameterType((int) buffer[JBTHETA + i]);
                double val = buffer[BTHETA + i];
                Bth[i] = Parameter.of(val, t);
            }
            builder.btheta(Bth);
        }
        return builder.build();
    }
}
