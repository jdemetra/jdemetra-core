/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sarima.estimation;

import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import demetra.design.Development;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.data.DataBlock;
import jdplus.math.functions.ParamValidation;
import jdplus.math.linearfilters.FilterUtility;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class SarimaMapping2 implements IArimaMapping<SarimaModel> {

    public static final double ARMAX = 0.99999;
    public static final double MAMAX = 1;
    public static final double STEP = Math.sqrt(2.220446e-16);
    private double arLimit;
    private double maLimit;
    private boolean checkAll;
    private double epsilon;
    private final SarimaOrders orders;

    public static Builder builder(SarimaOrders orders) {
        return new Builder()
                .orders(orders)
                .arLimit(ARMAX)
                .maLimit(MAMAX)
                .checkAll(true)
                .epsilon(STEP);
    }

    /**
     *
     * @param m
     * @return
     */
    public static SarimaModel stabilize(SarimaModel m) {
        DataBlock np = DataBlock.of(m.parameters());
        SarimaOrders mspec = m.orders();
        SarimaMapping2 mapping = SarimaMapping2.of(mspec);
        if (mapping.stabilize(np)) {
            return SarimaModel.builder(mspec).parameters(np).build();
        } else {
            return m;
        }
    }

    public static SarimaMapping2 of(SarimaOrders spec) {
        return SarimaMapping2
                .builder(spec)
                .arLimit(1)
                .maLimit(1)
                .build();
    }

    public static SarimaMapping2 ofStationary(final SarimaOrders spec) {
        SarimaOrders nspec = spec.clone();
        nspec.setD(0);
        nspec.setBd(0);
        return of(nspec);
    }

    public boolean stabilize(DoubleSeq.Mutable p) {
        boolean rslt = false;
        int start = 0, len = orders.getP();
        if (len > 0 && FilterUtility.stabilize(p.extract(start, len), arLimit)) {
            rslt = true;
        }
        start += len;
        len = orders.getBp();
        if (len > 0 && FilterUtility.stabilize(p.extract(start, len), arLimit)) {
            rslt = true;
        }
        if (checkAll) {
            start += len;
            len = orders.getQ();
            if (len > 0 && FilterUtility.stabilize(p.extract(start, len), maLimit)) {
                rslt = true;
            }
            start += len;
            len = orders.getBq();
            if (len > 0 && FilterUtility.stabilize(p.extract(start, len), maLimit)) {
                rslt = true;
            }
        }
        return rslt;
    }

    /**
     *
     * @param p
     * @return
     */
    @Override
    public boolean checkBoundaries(DoubleSeq p) {
        int start = 0, len = orders.getP();
        if (len > 0 && !FilterUtility.checkRoots(p.extract(start, len), arLimit)) {
            return false;
        }
        start += len;
        len = orders.getBp();
        if (len > 0 && !FilterUtility.checkRoots(p.extract(start, len), arLimit)) {
            return false;
        }
        if (checkAll) {
            start += len;
            len = orders.getQ();
            if (len > 0 && !FilterUtility.checkRoots(p.extract(start, len), maLimit)) {
                return false;
            }
            start += len;
            len = orders.getBq();
            if (len > 0 && !FilterUtility.checkRoots(p.extract(start, len), maLimit)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {
        double p = inparams.get(idx);
        if (p < 0) {
            return epsilon * Math.max(1, -p);
        } else {
            return -epsilon * Math.max(1, p);
        }
    }

    @Override
    public int getDim() {
        return orders.getParametersCount();
    }

    /**
     *
     * @return
     */
    public boolean isCheckingAll() {
        return checkAll;
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public double lbound(int idx) {
        if (orders.getP() > 0) {
            if (idx < orders.getP()) {
                if (orders.getP() == 1) {
                    return -arLimit;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= orders.getP();
        }
        if (orders.getBp() > 0) {
            if (idx < orders.getBp()) {
                if (orders.getBp() == 1) {
                    return -arLimit;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= orders.getBp();
        }
        if (orders.getQ() > 0) {
            if (idx < orders.getQ()) {
                if (orders.getQ() == 1) {
                    return -maLimit;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
        }
        if (orders.getBq() == 1) {
            return -maLimit;
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    @Override
    public SarimaModel map(DoubleSeq p) {
        if (p.length() != orders.getParametersCount()) {
            return null;
        }
        return SarimaModel.builder(orders)
                .parameters(p)
                .build();
    }

    @Override
    public double ubound(int idx) {
        if (orders.getP() > 0) {
            if (idx < orders.getP()) {
                if (orders.getP() == 1) {
                    return arLimit;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= orders.getP();
        }
        if (orders.getBp() > 0) {
            if (idx < orders.getBp()) {
                if (orders.getBp() == 1) {
                    return arLimit;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= orders.getBp();
        }
        if (orders.getQ() > 0) {
            if (idx < orders.getQ()) {
                if (orders.getQ() == 1) {
                    return maLimit;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
        if (orders.getBq() == 1) {
            return maLimit;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    /**
     *
     * @param value
     * @return
     */
    @Override
    public ParamValidation validate(DataBlock value) {
        if (value.length() != orders.getParametersCount()) {
            return ParamValidation.Invalid;
        }
        if (stabilize(value)) {
            return ParamValidation.Changed;
        } else {
            return ParamValidation.Valid;
        }
    }

    @Override
    public DoubleSeq parametersOf(SarimaModel m) {
        return m.parameters();
    }

    @Override
    public IArimaMapping<SarimaModel> stationaryMapping() {
        if (orders.isStationary()) {
            return this;
        }
        SarimaOrders norders = orders.clone();
        norders.setD(0);
        norders.setBd(0);
        return toBuilder()
                .orders(norders)
                .build();
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        double[] p = new double[orders.getParametersCount()];
        int nar = orders.getP() + orders.getBp();
        for (int i = 0; i < nar; ++i) {
            p[i] = -.1;
        }
        for (int i = nar; i < p.length; ++i) {
            p[i] = -.2;
        }
        return DoubleSeq.of(p);
    }

    @Override
    public String getDescription(final int idx) {
        return getDescription(orders, idx);
    }

    static String getDescription(final SarimaOrders orders, final int idx) {
        int i = idx;
        if (i < orders.getP()) {
            return desc(PHI, i);
        } else {
            i -= orders.getP();
        }
        if (i < orders.getBp()) {
            return desc(BPHI, i);
        } else {
            i -= orders.getBp();
        }
        if (i < orders.getQ()) {
            return desc(TH, i);
        } else {
            i -= orders.getQ();
        }
        if (i < orders.getBq()) {
            return desc(BTH, i);
        } else {
            return EMPTY;
        }
    }

    static String desc(String prefix, int idx) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix).append('(').append(idx + 1).append(')');
        return builder.toString();
    }

    public static final String PHI = "phi", BPHI = "bphi", TH = "th", BTH = "bth";

}
