/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11plus;

import demetra.data.DataBlock;
import demetra.maths.linearfilters.FiniteFilter;
import demetra.maths.linearfilters.IFiniteFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class X11SeasonalFiltersFactory {

    IFiltering filter(Number period, SeasonalFilterOption option) {

        SymmetricFilter sfilter = null;
        IFiniteFilter[] efilters = null;

        switch (option) {
            case S3X1:
                sfilter = S3X1;
                efilters = FC1;
                break;
            case S3X3:
                sfilter = S3X3;
                efilters = FC3;
                break;
            case S3X5:
                sfilter = S3X5;
                efilters = FC5;
                break;
            case S3X9:
                sfilter = S3X9;
                efilters = FC9;
                break;
            case S3X15:
                sfilter = S3X15;
                efilters = FC15;
                break;
        }

        if (period instanceof Integer) {
            return new DefaultFilter(period.intValue(), sfilter, new AsymmetricEndPoints(efilters, 0));
        } else {
            return new AnyFilter(period.doubleValue(), sfilter, efilters);
        }
    }

    static class DefaultFilter implements IFiltering {

        private final SymmetricFilter sfilter;
        private final IEndPointsProcessor endpoints;
        private int period;

        DefaultFilter(final int period, final SymmetricFilter sfilter, final IEndPointsProcessor endpoints) {
            this.period = period;
            this.sfilter = sfilter;
            this.endpoints = endpoints;
        }

        @Override
        public DoubleSeq process(DoubleSeq in) {
            double[] x = new double[in.length()];
            DataBlock out = DataBlock.ofInternal(x);
            DataBlock input = DataBlock.of(in);
            int n = sfilter.length() / 2;
            for (int i = 0; i < period; ++i) {
                DataBlock cin = input.extract(i, -1, period);
                DataBlock cout = out.extract(i, -1, period);
                sfilter.apply(cin, cout.drop(n, n));
                if (endpoints != null) {
                    endpoints.process(cin, cout);
                }
            }
            return DoubleSeq.of(x);
        }

    }

    static class AnyFilter implements IFiltering {

        private final SymmetricFilter sfilter;
        private final IFiniteFilter[] endpoints;
        private double period;

        AnyFilter(final double period, final SymmetricFilter sfilter, final IFiniteFilter[] endpoints) {
            this.period = period;
            this.sfilter = sfilter;
            this.endpoints = endpoints;
        }

        @Override
        public DoubleSeq process(DoubleSeq in) {
            double[] x = new double[in.length()];
            int n = sfilter.length(), m = n / 2;
            DataBlock cin = DataBlock.make(n);
            double t = m * period;
            int l;
            if ((t - (int) t) < 1e-9) {
                l = m;
            } else {
                l = 1 + m;
            }
            for (int i = 0; i < x.length; ++i) {
                cin.set(0);
                int c = (int) (i / period);
                int q = (int) ((x.length - 1 - i) / period); // last possible item
                if (c >= l && q >= l) {
                    // fill the input buffer;
                    cin.set(m, in.get(i));
                    double dcur = period;
                    for (int j = m + 1; j < n; ++j, dcur += period) {
                        int jcur = (int) dcur;
                        double p0 = dcur - jcur, p1 = 1 - p0;
                        if (p0 > 1e-9) {
                            cin.set(j, p1 * in.get(i + jcur) + p0 * in.get(i + jcur + 1));
                        } else {
                            cin.set(j, in.get(i + jcur));
                        }
                    }
                    dcur = period;
                    for (int j = m - 1; j >= 0; --j, dcur += period) {
                        int jcur = (int) dcur;
                        double p0 = dcur - jcur, p1 = 1 - p0;
                        if (p0 > 1e-9) {
                            cin.set(j, p1 * in.get(i - jcur) + p0 * in.get(i - jcur - 1));
                        } else {
                            cin.set(j, in.get(i + jcur));
                        }
                    }
                    x[i] = sfilter.apply(cin);
                } else if (c < l) {
                    int w = c;
                    // fill the input buffer;
                    cin.set(w, in.get(i));
                    double dcur = period;
                    for (int j = w + 1; j < n; ++j, dcur += period) {
                        int jcur = (int) dcur;
                        double p0 = dcur - jcur, p1 = 1 - p0;
                        if (p0 > 1e-9) {
                            cin.set(j, p1 * in.get(i + jcur) + p0 * in.get(i + jcur + 1));
                        } else {
                            cin.set(j, in.get(i + jcur));
                        }
                    }
                    dcur = period;
                    for (int j = w - 1; j >= 0; --j, dcur += period) {
                        int jcur = (int) dcur;
                        double p0 = dcur - jcur, p1 = 1 - p0;
                        if (p0 > 1e-9 && i - jcur - 1 >= 0) {
                            cin.set(j, p1 * in.get(i - jcur) + p0 * in.get(i - jcur - 1));
                        } else {
                            cin.set(j, in.get(i - jcur));
                        }
                    }
                    if (c < m) {
                        int k = c + 1;
                        x[i] = endpoints[m - k].apply(cin.reverse().range(m-c, n));
                    } else {
                        x[i] = sfilter.apply(cin);
                    }
                } else {
                    int w = n - q - 1;
                    // fill the input buffer;
                    cin.set(w, in.get(i));
                    double dcur = period;
                    for (int j = w + 1; j < n; ++j, dcur += period) {
                        int jcur = (int) dcur;
                        double p0 = dcur - jcur, p1 = 1 - p0;
                        if (p0 > 1e-9 && i + jcur + 1 < in.length()) {
                            cin.set(j, p1 * in.get(i + jcur) + p0 * in.get(i + jcur + 1));
                        } else {
                            cin.set(j, p1 * in.get(i + jcur));
                        }
                    }
                    dcur = period;
                    for (int j = w - 1; j >= 0; --j, dcur += period) {
                        int jcur = (int) dcur;
                        double p0 = dcur - jcur, p1 = 1 - p0;
                        if (p0 > 1e-9) {
                            cin.set(j, p1 * in.get(i - jcur) + p0 * in.get(i - jcur - 1));
                        } else {
                            cin.set(j, in.get(i - jcur));
                        }
                    }
                    if (q < m) {
                        int k = q + 1;
                        x[i] = endpoints[m - k].apply(cin.range(m-q, n));
                    } else {
                        x[i] = sfilter.apply(cin);
                    }

                }
            }
            return DoubleSeq.of(x);
        }

    }

    /**
     *
     */
    public final SymmetricFilter S3X1 = X11FilterFactory
            .makeSymmetricFilter(3, 1);
    /**
     *
     */
    public final SymmetricFilter S3X3 = X11FilterFactory
            .makeSymmetricFilter(3, 3);
    /**
     *
     */
    public final SymmetricFilter S3X5 = X11FilterFactory
            .makeSymmetricFilter(3, 5);
    /**
     *
     */
    public final SymmetricFilter S3X9 = X11FilterFactory
            .makeSymmetricFilter(3, 9);
    /**
     *
     */
    public final SymmetricFilter S3X15 = X11FilterFactory
            .makeSymmetricFilter(3, 15);

    private final double[] ma1x0 = {0.39, 0.61},
            ma2x1 = {3.0 / 27, 7.0 / 27, 10.0 / 27, 7.0 / 27},
            ma2x0 = {5.0 / 27, 11.0 / 27, 11.0 / 27},
            ma3x2 = {4.0 / 60, 8.0 / 60, 13.0 / 60, 13.0 / 60, 13.0 / 60,
                9.0 / 60},
            ma3x1 = {4.0 / 60, 11.0 / 60, 15.0 / 60, 15.0 / 60, 15.0 / 60},
            ma3x0 = {9.0 / 60, 17.0 / 60, 17.0 / 60, 17.0 / 60},
            // { 35.0/1026, 75.0/1026, 114.0, 75.0/1026, 116.0, 75.0/1026,
            // 75.0/1026,
            // 117.0, 75.0/1026, 119.0, 75.0/1026, 120.0, 75.0/1026, 121.0,
            // 75.0/1026,
            // 123.0, 75.0/1026, 86.0, 75.0/1026 };
            ma5x4 = {0.034, 0.073, 0.111, 0.113, 0.114, 0.116, 0.117, 0.118,
                0.12, 0.084},
            // { 35.0/1026, 77.0/1026, 116.0/1026, 120.0/1026, 126.0/1026,
            // 131.0/1026, 135.0/1026, 141.0/1026, 145.0/1026 }
            ma5x3 = {0.034, 0.075, 0.113, 0.117, 0.123, 0.128, 0.132, 0.137,
                0.141},
            // { 33.0/1026, 81.0/1026, 126.0/1026, 136.0/1026, 147.0/1026,
            // 158.0/1026, 167.0/1026, 177.0/1026 }
            ma5x2 = {0.032, 0.079, 0.123, 0.133, 0.143, 0.154, 0.163, 0.173},
            // { 29.0/1026, 94.0/1026, 148.0/1026, 164.0/1026, 181.0/1026,
            // 197.0/1026, 213.0/1026 }
            ma5x1 = {0.028, 0.092, 0.144, 0.160, 0.176, 0.192, 0.208},
            // { 52.0/1026, 115.0/1026, 177.0/1026, 202.0/1026, 227.0/1026,
            // 252.0/1026 }
            ma5x0 = {0.051, 0.112, 0.173, 0.197, 0.221, 0.246}, ma8x0 = {
                0.02222, 0.04444, 0.06667, 0.06667, 0.16, 0.16, 0.16, 0.16,
                0.16}, ma8x1 = {0.0222, 0.04444, 0.06667, 0.06667,
                0.06667, 0.14667, 0.14667, 0.14667, 0.14667, 0.14667},
            ma8x2 = {0.02223, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667,
                0.13333, 0.13333, 0.13333, 0.13333, 0.13333}, ma8x3 = {
                0.02221, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667,
                0.06667, 0.12, 0.12, 0.12, 0.12, 0.12}, ma8x4 = {0.02219,
                0.04444, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667,
                0.06667, 0.10667, 0.10667, 0.10667, 0.10667, 0.10667},
            ma8x5 = {0.02222, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667,
                0.06667, 0.06667, 0.06667, 0.09333, 0.09333, 0.09333,
                0.09333, 0.09333}, ma8x6 = {0.0222, 0.04444, 0.06667,
                0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667,
                0.06667, 0.08, 0.08, 0.08, 0.08, 0.08}, ma8x7 = {0.0222,
                0.04444, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667,
                0.06667, 0.06667, 0.06667, 0.06667, 0.07111, 0.07111,
                0.07111, 0.07111, 0.04889};

    final FiniteFilter M_1X0 = new FiniteFilter(ma1x0, -1);

    final FiniteFilter M_2X1 = new FiniteFilter(ma2x1, -2);

    final FiniteFilter M_2X0 = new FiniteFilter(ma2x0, -2);

    final FiniteFilter M_3X2 = new FiniteFilter(ma3x2, -3);
    final FiniteFilter M_3X1 = new FiniteFilter(ma3x1, -3);
    final FiniteFilter M_3X0 = new FiniteFilter(ma3x0, -3);
    final FiniteFilter M_5X4 = new FiniteFilter(ma5x4, -5);
    final FiniteFilter M_5X3 = new FiniteFilter(ma5x3, -5);
    final FiniteFilter M_5X2 = new FiniteFilter(ma5x2, -5);
    final FiniteFilter M_5X1 = new FiniteFilter(ma5x1, -5);
    final FiniteFilter M_5X0 = new FiniteFilter(ma5x0, -5);
    final FiniteFilter M_8X0 = new FiniteFilter(ma8x0, -8);
    final FiniteFilter M_8X1 = new FiniteFilter(ma8x1, -8);
    final FiniteFilter M_8X2 = new FiniteFilter(ma8x2, -8);
    final FiniteFilter M_8X3 = new FiniteFilter(ma8x3, -8);
    final FiniteFilter M_8X4 = new FiniteFilter(ma8x4, -8);
    final FiniteFilter M_8X5 = new FiniteFilter(ma8x5, -8);
    final FiniteFilter M_8X6 = new FiniteFilter(ma8x6, -8);
    final FiniteFilter M_8X7 = new FiniteFilter(ma8x7, -8);
    final FiniteFilter[] FC1 = new FiniteFilter[]{M_1X0};
    final FiniteFilter[] FC3 = new FiniteFilter[]{M_2X1, M_2X0};
    final FiniteFilter[] FC5 = new FiniteFilter[]{M_3X2, M_3X1, M_3X0};
    final FiniteFilter[] FC9 = new FiniteFilter[]{M_5X4, M_5X3, M_5X2,
        M_5X1, M_5X0};
    final FiniteFilter[] FC15 = new FiniteFilter[]{M_8X7, M_8X6,
        M_8X5, M_8X4, M_8X3, M_8X2, M_8X1, M_8X0};

}
