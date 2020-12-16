/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.r;

import demetra.arima.SarimaSpec;
import demetra.data.Parameter;
import demetra.data.ParameterType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SarimaBuffer {

    private static final int MAXR = 6, MAXS = 1, D = 0, BD = D + 1,
            PHI = BD + 1, JPHI = PHI + MAXR, THETA = JPHI + MAXR, JTHETA = THETA + MAXR,
            BPHI = JTHETA + MAXR, JBPHI = BPHI + MAXS, BTHETA = JBPHI + MAXS, JBTHETA = BTHETA + MAXS, SIZE = JBTHETA + MAXS;
    private final double[] buffer;

    public SarimaBuffer(SarimaSpec spec) {
        buffer = new double[SIZE];
        // fill the buffer
        buffer[D] = spec.getD();
        buffer[BD] = spec.getBd();
        Parameter[] phi = spec.getPhi();
        for (int i = 0; i < phi.length; ++i) {
            buffer[PHI + i] = phi[i].getValue();
            buffer[JPHI + i] = parameterType(phi[i].getType());
        }
        Parameter[] th = spec.getTheta();
        for (int i = 0; i < th.length; ++i) {
            buffer[THETA + i] = th[i].getValue();
            buffer[JTHETA + i] = parameterType(th[i].getType());
        }
        Parameter[] bphi = spec.getBphi();
        for (int i = 0; i < bphi.length; ++i) {
            buffer[BPHI + i] = bphi[i].getValue();
            buffer[JBPHI + i] = parameterType(bphi[i].getType());
        }
        Parameter[] bth = spec.getBtheta();
        for (int i = 0; i < bth.length; ++i) {
            buffer[BTHETA + i] = bth[i].getValue();
            buffer[JBTHETA + i] = parameterType(bth[i].getType());
        }

    }

    public SarimaBuffer(double[] data) {
        buffer = new double[SIZE];
        System.arraycopy(data, 0, buffer, 0, data.length);
    }

    public static int parameterType(ParameterType type) {
        switch (type) {
            case Undefined:
                return 1;
            case Initial:
                return 2;
            case Fixed:
                return 3;
            case Estimated:
                return 4;
            default:
                return 0;
        }
    }

    public static ParameterType parameterType(int type) {
        switch (type) {
            case 1:
                return ParameterType.Undefined;
            case 2:
                return ParameterType.Initial;
            case 3:
                return ParameterType.Fixed;
            case 4:
                return ParameterType.Estimated;
            default:
                return null;
        }
    }

    private int n(int pos0, int pos1) {
        for (int i = pos0; i < pos1; ++i) {
            if (buffer[i] == 0) {
                return i - pos0;
            }
        }
        return pos1 - pos0;
    }

    public SarimaSpec build() {
        SarimaSpec.Builder builder = SarimaSpec.builder()
                .d((int) buffer[D])
                .bd((int) buffer[BD]);
        int n = n(JPHI, JPHI + MAXR);
        if (n > 0) {
            Parameter[] Phi = new Parameter[n];
            for (int i = 0; i < n; ++i) {
                ParameterType t = parameterType((int) buffer[JPHI + i]);
                double val = buffer[PHI + i];
                Phi[i] = Parameter.of(val, t);
            }
            builder.phi(Phi);
        }
        n = n(JTHETA, JTHETA + MAXR);
        if (n > 0) {
            Parameter[] Th = new Parameter[n];
            for (int i = 0; i < n; ++i) {
                ParameterType t = parameterType((int) buffer[JTHETA + i]);
                double val = buffer[THETA + i];
                Th[i] = Parameter.of(val, t);
            }
            builder.theta(Th);
        }
        n = n(JBPHI, JBPHI + MAXS);
        if (n > 0) {
            Parameter[] Bphi = new Parameter[n];
            for (int i = 0; i < n; ++i) {
                ParameterType t = parameterType((int) buffer[JBPHI + i]);
                double val = buffer[BPHI + i];
                Bphi[i] = Parameter.of(val, t);
            }
            builder.bphi(Bphi);
        }
        n = n(JBTHETA, JBTHETA + MAXS);
        if (n > 0) {
            Parameter[] Bth = new Parameter[n];
            for (int i = 0; i < n; ++i) {
                ParameterType t = parameterType((int) buffer[JBTHETA + i]);
                double val = buffer[BTHETA + i];
                Bth[i] = Parameter.of(val, t);
            }
            builder.btheta(Bth);
        }
        return builder.build();
    }
}
