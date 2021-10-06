/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.data;

/**
 *
 * @author Jean Palate
 */
public class DataSets {

    public static class PerfectFit {

        public static final double[] y = new double[]{11, 12, 13, 14, 15, 16};
        public static final double[] x1 = new double[]{0, 2, 0, 0, 0, 0};
        public static final double[] x2 = new double[]{0, 0, 3, 0, 0, 0};
        public static final double[] x3 = new double[]{0, 0, 0, 4, 0, 0};
        public static final double[] x4 = new double[]{0, 0, 0, 0, 5, 0};
        public static final double[] x5 = new double[]{0, 0, 0, 0, 0, 6};

        public static final double[] expectedBeta = new double[]{11.0, 0.5, 0.666666666666667, 0.75, 0.8, 0.8333333333333333};
        public static final double[] expectedResiduals = new double[]{0, 0, 0, 0, 0, 0};
        public static final double RSquare = 1;
    }

//# Data Source: J. DataSets (1967) "An Appraisal of Least Squares Programs for the
//# Electronic Computer from the Point of View of the User",
//# Journal of the American Statistical Association,
//# vol. 62. September, pp. 819-841.
//#
//# Certified values (and data) are from NIST:
//# http://www.itl.nist.gov/div898/strd/lls/data/LINKS/DATA/Longley.dat
    public static final class Norris {

        public static final double[] y = new double[]{
            0.1, 338.8, 118.1, 888, 9.2, 228.1, 668.5, 998.5, 449.1, 778.9, 559.2, 0.3, 0.1, 778.1, 668.8, 339.3,
            448.9, 10.8, 557.7, 228.3, 998, 888.8, 119.6, 0.3, 0.6, 557.6, 339.3, 888, 998.5, 778.9, 10.2, 117.6,
            228.9, 668.4, 449.2, 0.2
        };
        public static final double[] x = new double[]{
            0.2, 337.4, 118.2, 884.6, 10.1, 226.5, 666.3, 996.3, 448.6, 777, 558.2, 0.4, 0.6, 775.5, 666.9, 338, 447.5, 11.6, 556,
            228.1, 995.8, 887.6, 120.2, 0.3, 0.3, 556.8, 339.1, 887.2, 999, 779, 11.1, 118.3, 229.2, 669.1, 448.9, 0.5
        };

        public static final double[] expectedBeta = new double[]{-0.262323073774029, 1.00211681802045};
        public static final double[] expectedErrors = new double[]{0.232818234301152, 0.429796848199937E-03};
    }

    public static final class Longley {

        public static final double[] y = new double[]{60323, 61122, 60171, 61187, 63221, 63639, 64989, 63761, 66019, 67857, 68169, 66513, 68655, 69564, 69331, 70551};
        public static final double[] x1 = new double[]{83, 88.5, 88.2, 89.5, 96.2, 98.1, 99, 100, 101.2, 104.6, 108.4, 110.8, 112.6, 114.2, 115.7, 116.9};
        public static final double[] x2 = new double[]{234289, 259426, 258054, 284599, 328975, 346999, 365385, 363112, 397469, 419180, 442769, 444546, 482704, 502601, 518173, 554894};
        public static final double[] x3 = new double[]{2356, 2325, 3682, 3351, 2099, 1932, 1870, 3578, 2904, 2822, 2936, 4681, 3813, 3931, 4806, 4007};
        public static final double[] x4 = new double[]{1590, 1456, 1616, 1650, 3099, 3594, 3547, 3350, 3048, 2857, 2798, 2637, 2552, 2514, 2572, 2827};
        public static final double[] x5 = new double[]{107608, 108632, 109773, 110929, 112075, 113270, 115094, 116219, 117388, 118734, 120445, 121950, 123366, 125368, 127852, 130081};
        public static final double[] x6 = new double[]{1947, 1948, 1949, 1950, 1951, 1952, 1953, 1954, 1955, 1956, 1957, 1958, 1959, 1960, 1961, 1962};

        public static final double[] expectedBeta = new double[]{-3482258.63459582, 15.0618722713733, -0.358191792925910E-01, -2.02022980381683, -1.03322686717359, -0.511041056535807E-01, 1829.15146461355};
        public static final double[] expectedErrors = new double[]{890420.383607373, 84.9149257747669, 0.334910077722432E-01, 0.488399681651699, 0.214274163161675, 0.226073200069370, 455.478499142212};
        public static final double[] expectedBetaNoC = new double[]{-52.99357013868291, 0.07107319907358, -0.42346585566399, -0.57256866841929, -0.41420358884978, 48.41786562001326};
        public static final double FTest = 330.285339234588;
    }

    public static final class Wampler1 {

        public static final double[] y = new double[]{
            1, 6, 63, 364, 1365, 3906, 9331, 19608, 37449, 66430, 111111, 177156, 271453, 402234, 579195, 813616, 1118481, 1508598, 2000719, 2613660, 3368421

        };
        public static final double[] x = new double[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        };

        public static final double[] expectedBeta = new double[]{1, 1, 1, 1, 1, 1};
        public static final double[] expectedErrors = new double[]{0, 0, 0, 0, 0, 0};
    }

    public static final class Wampler2 {

        public static final double[] y = new double[]{
            1, 1.11111, 1.24992, 1.42753, 1.65984, 1.96875, 2.38336, 2.94117, 3.68928, 4.68559, 6, 7.71561, 9.92992, 12.75603, 16.32384,
            20.78125, 26.29536, 33.05367, 41.26528, 51.16209, 63

        };
        public static final double[] x = new double[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        };

        public static final double[] expectedBeta = new double[]{1, 0.1, 0.01, 0.001, 0.0001, 0.00001};
        public static final double[] expectedErrors = new double[]{0, 0, 0, 0, 0, 0};
    }

    public static final class Wampler3 {

        public static final double[] y = new double[]{
            760, -2042, 2111, -1684, 3888, 1858, 11379, 17560, 39287, 64382, 113159, 175108, 273291, 400186, 581243, 811568,
            1121004, 1506550, 2002767, 2611612, 3369180
        };
        public static final double[] x = new double[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        };

        public static final double[] expectedBeta = new double[]{1, 1, 1, 1, 1, 1};
        public static final double[] expectedErrors = new double[]{2152.32624678170, 2363.55173469681, 779.343524331583, 101.475507550350, 5.64566512170752, 0.112324854679312
        };
    }

    public static final class Wampler4 {

        public static final double[] y = new double[]{
            75901, -204794, 204863, -204436, 253665, -200894, 214131, -185192, 221249, -138370, 315911, -27644, 455253,
            197434, 783995, 608816, 1370781, 1303798, 2205519, 2408860, 3444321};
        public static final double[] x = new double[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        };

        public static final double[] expectedBeta = new double[]{1, 1, 1, 1, 1, 1};
        public static final double[] expectedErrors = new double[]{215232.624678170, 236355.173469681, 77934.3524331583, 10147.5507550350, 564.566512170752, 11.2324854679312
        };
    }

    public static final class Wampler5 {

        public static final double[] y = new double[]{
            7590001, -20479994, 20480063, -20479636, 25231365, -20476094, 20489331, -20460392, 18417449,
            -20413570, 20591111, -20302844, 18651453, -20077766, 21059195, -19666384, 26348481, -18971402,
            22480719, -17866340, 10958421

        };
        public static final double[] x = new double[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        };

        public static final double[] expectedBeta = new double[]{1, 1, 1, 1, 1, 1};
        public static final double[] expectedErrors = new double[]{21523262.4678170, 23635517.3469681, 7793435.24331583, 1014755.07550350, 56456.6512170752, 1123.24854679312};
    }

    public static final class NoInt1 {

        public static final double[] y = new double[]{
            130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140
        };
        public static final double[] x = new double[]{
            60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70
        };

        public static final double[] expectedBeta = new double[]{2.07438016528926};
        public static final double[] expectedErrors = new double[]{0.165289256198347E-01};
    }

    public static final class NoInt2 {

        public static final double[] y = new double[]{3, 4, 4};

        public static final double[] x = new double[]{4, 5, 6};
        public static final double[] expectedBeta = new double[]{0.727272727272727};
        public static final double[] expectedErrors = new double[]{0.420827318078432E-01};
    }

    public static final class Pontius {

        public static final double[] y = new double[]{
            0.11019, 0.21956, 0.32949, 0.43899, 0.54803, 0.65694, 0.76562, 0.87487, 0.98292, 1.09146, 1.20001, 1.30822, 1.41599, 1.52399, 1.63194, 1.73947, 1.84646, 1.95392, 2.06128, 2.16844,
            0.11052, 0.22018, 0.32939, 0.43886, 0.54798, 0.65739, 0.76596, 0.87474, 0.983, 1.0915, 1.20004, 1.30818, 1.41613, 1.52408, 1.63159, 1.73965, 1.84696, 1.95445, 2.06177, 2.16829
        };

        public static final double[] x = new double[]{
            150000, 300000, 450000, 600000, 750000, 900000, 1050000, 1200000, 1350000, 1500000, 1650000, 1800000, 1950000, 2100000, 2250000, 2400000, 2550000, 2700000, 2850000, 3000000,
            150000, 300000, 450000, 600000, 750000, 900000, 1050000, 1200000, 1350000, 1500000, 1650000, 1800000, 1950000, 2100000, 2250000, 2400000, 2550000, 2700000, 2850000, 3000000
        };
        public static final double[] expectedBeta = new double[]{0.673565789473684E-03, 0.732059160401003E-06, -0.316081871345029E-14};
        public static final double[] expectedErrors = new double[]{0.107938612033077E-03, 0.157817399981659E-09, 0.486652849992036E-16};
    }

    public static final class Filip {

        public static final double[] y = new double[]{
            0.8116, 0.9072, 0.9052, 0.9039, 0.8053, 0.8377, 0.8667, 0.8809, 0.7975, 0.8162, 0.8515, 0.8766,
            0.8885, 0.8859, 0.8959, 0.8913, 0.8959, 0.8971, 0.9021, 0.909, 0.9139, 0.9199,
            0.8692, 0.8872, 0.89, 0.891, 0.8977, 0.9035, 0.9078, 0.7675, 0.7705, 0.7713,
            0.7736, 0.7775, 0.7841, 0.7971, 0.8329, 0.8641, 0.8804, 0.7668, 0.7633, 0.7678,
            0.7697, 0.77, 0.7749, 0.7796, 0.7897, 0.8131, 0.8498, 0.8741, 0.8061, 0.846, 0.8751,
            0.8856, 0.8919, 0.8934, 0.894, 0.8957, 0.9047, 0.9129, 0.9209, 0.9219, 0.7739, 0.7681,
            0.7665, 0.7703, 0.7702, 0.7761, 0.7809, 0.7961, 0.8253, 0.8602, 0.8809, 0.8301,
            0.8664, 0.8834, 0.8898, 0.8964, 0.8963, 0.9074, 0.9119, 0.9228
        };

        public static final double[] x = new double[]{
            -6.860120914, -4.324130045, -4.358625055, -4.358426747, -6.955852379, -6.661145254, -6.355462942,
            -6.118102026, -7.115148017, -6.815308569, -6.519993057, -6.204119983, -5.853871964,
            -6.109523091, -5.79832982, -5.482672118, -5.171791386, -4.851705903, -4.517126416,
            -4.143573228, -3.709075441, -3.499489089, -6.300769497, -5.953504836, -5.642065153,
            -5.031376979, -4.680685696, -4.329846955, -3.928486195, -8.56735134, -8.363211311,
            -8.107682739, -7.823908741, -7.522878745, -7.218819279, -6.920818754, -6.628932138,
            -6.323946875, -5.991399828, -8.781464495, -8.663140179, -8.473531488, -8.247337057,
            -7.971428747, -7.676129393, -7.352812702, -7.072065318, -6.774174009, -6.478861916,
            -6.159517513, -6.835647144, -6.53165267, -6.224098421, -5.910094889, -5.598599459,
            -5.290645224, -4.974284616, -4.64454848, -4.290560426, -3.885055584, -3.408378962,
            -3.13200249, -8.726767166, -8.66695597, -8.511026475, -8.165388579, -7.886056648,
            -7.588043762, -7.283412422, -6.995678626, -6.691862621, -6.392544977, -6.067374056,
            -6.684029655, -6.378719832, -6.065855188, -5.752272167, -5.132414673, -4.811352704,
            -4.098269308, -3.66174277, -3.2644011
        };

        public static final double[] expectedBeta = new double[]{
            -1467.48961422980, -2772.17959193342, -2316.37108160893, -1127.97394098372, -354.478233703349,
            -75.1242017393757, -10.8753180355343, -1.06221498588947, -0.670191154593408E-01, -0.246781078275479E-02, -0.402962525080404E-04
        };
        public static final double[] expectedErrors = new double[]{
            298.084530995537, 559.779865474950, 466.477572127796, 227.204274477751, 71.6478660875927,
            15.2897178747400, 2.23691159816033, 0.221624321934227, 0.142363763154724E-01, 0.535617408889821E-03, 0.896632837373868E-05
        };

    }

    public static double lre(double val, double refval) {
        double c = Math.abs((val - refval) / refval);
        return c == 0 ? 15 : -Math.log10(c);
    }
}
