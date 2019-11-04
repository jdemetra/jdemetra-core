/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.filter;

import demetra.x11.SeasonalFilterOption;
import demetra.x11.filter.endpoints.AsymmetricEndPoints;
import demetra.x11.filter.endpoints.IEndPointsProcessor;
import jdplus.data.DataBlock;
import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class X11SeasonalFiltersFactory {

    public X11SeasonalFilterProcessor filter(int period, SeasonalFilterOption[] option) {
        IFiltering[] result = new IFiltering[period];

        for (int i = 0; i < period; i++) {
            switch (option[i]) {
                case S3X1:
                    result[i] = new DefaultFilter(period, S3X1, new AsymmetricEndPoints(FC1, 0));
                    break;
                case S3X3:
                    result[i] = new DefaultFilter(period, S3X3, new AsymmetricEndPoints(FC3, 0));
                    break;
                case S3X5:
                    result[i] = new DefaultFilter(period, S3X5, new AsymmetricEndPoints(FC5, 0));
                    break;
                case S3X9:
                    result[i] = new DefaultFilter(period, S3X9, new AsymmetricEndPoints(FC9, 0));
                    break;
                case S3X15:
                    result[i] = new DefaultFilter(period, S3X15, new AsymmetricEndPoints(FC15, 0));
                    break;
                case Stable:
                    result[i] = new StableFilter(period);
            }
        }
        return new X11SeasonalFilterProcessor(result);
    }

    static class DefaultFilter implements IFiltering {

        private final SymmetricFilter sfilter;
        private final IEndPointsProcessor endpoints;
        private final int period;

        public SymmetricFilter getSfilter() {
            return sfilter;
        }

        public IEndPointsProcessor getEndpoints() {
            return endpoints;
        }

        DefaultFilter(final int period, final SymmetricFilter sfilter, final IEndPointsProcessor endpoints) {
            this.period = period;
            this.sfilter = sfilter;
            this.endpoints = endpoints;
        }

        @Override
        public DataBlock process(DataBlock cin) {

            DataBlock cout = DataBlock.of(new double[cin.length()]);
            int n = sfilter.length() / 2;
            if (2 * n < cin.length()) {
                sfilter.apply(cin, cout.drop(n, n));
            }
            if (endpoints != null) {
                endpoints.process(cin, cout);
            }
            return cout;
        }
    }

    static class StableFilter implements IFiltering {

        private final int period;

        StableFilter(int period) {
            this.period = period;
        }

        @Override
        public DataBlock process(DataBlock cin) {

            DataBlock cout = DataBlock.of(new double[cin.length()]);
            cout.set(cin.average());
            return cout;

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

    private final double[] MA1X0 = {0.39, 0.61},
            MA2X1 = {3.0 / 27, 7.0 / 27, 10.0 / 27, 7.0 / 27},
            MA2X0 = {5.0 / 27, 11.0 / 27, 11.0 / 27},
            MA3X2 = {4.0 / 60, 8.0 / 60, 13.0 / 60, 13.0 / 60, 13.0 / 60, 9.0 / 60},
            MA3X1 = {4.0 / 60, 11.0 / 60, 15.0 / 60, 15.0 / 60, 15.0 / 60},
            MA3X0 = {9.0 / 60, 17.0 / 60, 17.0 / 60, 17.0 / 60},
            // { 35.0/1026, 75.0/1026, 114.0, 75.0/1026, 116.0, 75.0/1026, 75.0/1026,
            // 117.0, 75.0/1026, 119.0, 75.0/1026, 120.0, 75.0/1026, 121.0, 75.0/1026,
            // 123.0, 75.0/1026, 86.0, 75.0/1026 };
            MA5X4 = {0.034, 0.073, 0.111, 0.113, 0.114, 0.116, 0.117, 0.118, 0.12, 0.084},
            // { 35.0/1026, 77.0/1026, 116.0/1026, 120.0/1026, 126.0/1026, 131.0/1026, 135.0/1026, 141.0/1026, 145.0/1026 }
            MA5X3 = {0.034, 0.075, 0.113, 0.117, 0.123, 0.128, 0.132, 0.137, 0.141},
            // { 33.0/1026, 81.0/1026, 126.0/1026, 136.0/1026, 147.0/1026, 158.0/1026, 167.0/1026, 177.0/1026 }
            MA5X2 = {0.032, 0.079, 0.123, 0.133, 0.143, 0.154, 0.163, 0.173},
            // { 29.0/1026, 94.0/1026, 148.0/1026, 164.0/1026, 181.0/1026, 197.0/1026, 213.0/1026 }
            MA5X1 = {0.028, 0.092, 0.144, 0.160, 0.176, 0.192, 0.208},
            // { 52.0/1026, 115.0/1026, 177.0/1026, 202.0/1026, 227.0/1026, 252.0/1026 }
            MA5X0 = {0.051, 0.112, 0.173, 0.197, 0.221, 0.246},
            MA8X0 = {0.02222, 0.04444, 0.06667, 0.06667, 0.16, 0.16, 0.16, 0.16, 0.16},
            MA8X1 = {0.0222, 0.04444, 0.06667, 0.06667, 0.06667, 0.14667, 0.14667, 0.14667, 0.14667, 0.14667},
            MA8X2 = {0.02223, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667, 0.13333, 0.13333, 0.13333, 0.13333, 0.13333},
            MA8X3 = {0.02221, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.12, 0.12, 0.12, 0.12, 0.12},
            MA8X4 = {0.02219, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.10667, 0.10667, 0.10667, 0.10667, 0.10667},
            MA8X5 = {0.02222, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.09333, 0.09333, 0.09333, 0.09333, 0.09333},
            MA8X6 = {0.0222, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.08, 0.08, 0.08, 0.08, 0.08},
            MA8X7 = {0.0222, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.07111, 0.07111, 0.07111, 0.07111, 0.04889};

    final FiniteFilter M_1X0 = FiniteFilter.ofInternal(MA1X0, -1);

    final FiniteFilter M_2X1 = FiniteFilter.ofInternal(MA2X1, -2);

    final FiniteFilter M_2X0 = FiniteFilter.ofInternal(MA2X0, -2);

    final FiniteFilter M_3X2 = FiniteFilter.ofInternal(MA3X2, -3);
    final FiniteFilter M_3X1 = FiniteFilter.ofInternal(MA3X1, -3);
    final FiniteFilter M_3X0 = FiniteFilter.ofInternal(MA3X0, -3);
    final FiniteFilter M_5X4 = FiniteFilter.ofInternal(MA5X4, -5);
    final FiniteFilter M_5X3 = FiniteFilter.ofInternal(MA5X3, -5);
    final FiniteFilter M_5X2 = FiniteFilter.ofInternal(MA5X2, -5);
    final FiniteFilter M_5X1 = FiniteFilter.ofInternal(MA5X1, -5);
    final FiniteFilter M_5X0 = FiniteFilter.ofInternal(MA5X0, -5);
    final FiniteFilter M_8X0 = FiniteFilter.ofInternal(MA8X0, -8);
    final FiniteFilter M_8X1 = FiniteFilter.ofInternal(MA8X1, -8);
    final FiniteFilter M_8X2 = FiniteFilter.ofInternal(MA8X2, -8);
    final FiniteFilter M_8X3 = FiniteFilter.ofInternal(MA8X3, -8);
    final FiniteFilter M_8X4 = FiniteFilter.ofInternal(MA8X4, -8);
    final FiniteFilter M_8X5 = FiniteFilter.ofInternal(MA8X5, -8);
    final FiniteFilter M_8X6 = FiniteFilter.ofInternal(MA8X6, -8);
    final FiniteFilter M_8X7 = FiniteFilter.ofInternal(MA8X7, -8);
    final FiniteFilter[] FC1 = new FiniteFilter[]{M_1X0};
    final FiniteFilter[] FC3 = new FiniteFilter[]{M_2X1, M_2X0};
    final FiniteFilter[] FC5 = new FiniteFilter[]{M_3X2, M_3X1, M_3X0};
    final FiniteFilter[] FC9 = new FiniteFilter[]{M_5X4, M_5X3, M_5X2,
        M_5X1, M_5X0};
    final FiniteFilter[] FC15 = new FiniteFilter[]{M_8X7, M_8X6,
        M_8X5, M_8X4, M_8X3, M_8X2, M_8X1, M_8X0};

}
