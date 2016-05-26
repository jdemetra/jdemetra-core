/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x13;

import data.DataCalendarSigmaX11;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.SigmavecOption;
import ec.satoolkit.x11.X11Kernel;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x11.X11Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.x13.AutoModelSpec;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * with this test the effect of excludefcast=yes in the x11 Kernel is tested the
 * differnt choices of calendarsimgma are differntly effected
 *
 * @author Christiane Hofer
 */
public class X13SpecExcludefcastTest {

    public X13SpecExcludefcastTest() {

    }

    private static TsData tsInput;

    private X11Specification x11spec;
    private X13Specification x13spec;
    private CompositeResults comprest;

    @Test
    @Ignore
    public void CalendarSigmaSelect4Test() {
        setInputData4();

        setSpec();
        x13spec.getX11Specification().setCalendarSigma(CalendarSigma.Select);
        SigmavecOption[] sigmavecOption = new SigmavecOption[4];
        sigmavecOption[0] = SigmavecOption.Group1;
        sigmavecOption[1] = SigmavecOption.Group2;
        sigmavecOption[2] = SigmavecOption.Group2;
        sigmavecOption[3] = SigmavecOption.Group2;
        x13spec.getX11Specification().setSigmavec(sigmavecOption);
        comprest = X13ProcessingFactory.process(tsInput, x13spec);
        
        double[] d9s4
                = {101.24854, Double.NaN, 99.56373, Double.NaN, 100.67665, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.75421, 99.98503, 99.01279, Double.NaN, 101.28807, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.73893, Double.NaN, Double.NaN, 99.943, 99.18735, 99.16893, Double.NaN, 100.71319, 99.79349, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.4726, 99.75503, Double.NaN, 99.63666, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.24218, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.25458, Double.NaN, Double.NaN, 99.45497, 99.57245, Double.NaN, 100.0898, 100.00716, 99.63573, 100.35026, Double.NaN, Double.NaN, Double.NaN, 100.39815, 98.96781, Double.NaN, 100.81696, 100.94803, Double.NaN, Double.NaN, 100.07831, Double.NaN, Double.NaN, Double.NaN, 100.70552, 100.86139, Double.NaN, 99.35791, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.30196, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        TsData tsd9s4 = new TsData(TsFrequency.Quarterly, 2002, 1, d9s4, false);
        for (int i = 0; i < 92; i++) {
            Assert.assertEquals("For Calendarsigma None quarterly time series in Table D9 the  " + i + ". value ", tsd9s4.get(i) / 100, comprest.getData("d-tables.d9", TsData.class).get(i), 0.00001);
        }

    }

    @Test
    public void CalendarsigmaNoneForcast18() {
        setInputData();
        setSpec();
        x13spec.getX11Specification().setCalendarSigma(CalendarSigma.None);
        x13spec.getX11Specification().setForecastHorizon(18);
        comprest = X13ProcessingFactory.process(tsInput, x13spec);
        double[] d9 = {Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,100.50122,Double.NaN,Double.NaN,Double.NaN,Double.NaN,99.39306,Double.NaN,Double.NaN,Double.NaN,95.71319,Double.NaN,102.48386,Double.NaN,98.79776,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,95.56255,96.47674,Double.NaN,103.42272,101.96267,Double.NaN,105.4637,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,98.511,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,97.89241,Double.NaN,Double.NaN,103.42196,101.23931,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,98.7917,96.70064,Double.NaN,Double.NaN,108.65172,103.24147,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,105.56223,100.74004,Double.NaN,100.2846,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN};
            
            TsData tsd9 = new TsData(TsFrequency.Monthly, 2002, 1, d9, false);
        for (int i = 0; i < 92; i++) {
            Assert.assertEquals("Monthly time series in Table D9 the  " + i + ". value ", tsd9.get(i) / 100, comprest.getData("d-tables.d9", TsData.class).get(i), 0.00001);
        }

    }

    @Test
    public void CalendarsigmaNoneForcast48() {
        setInputData();
        setSpec();
        x13spec.getX11Specification().setCalendarSigma(CalendarSigma.None);
        x13spec.getX11Specification().setForecastHorizon(48);
        comprest = X13ProcessingFactory.process(tsInput, x13spec);
        double[] d9 = {Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,100.5012,Double.NaN,Double.NaN,Double.NaN,Double.NaN,99.39304,Double.NaN,Double.NaN,Double.NaN,95.71305,Double.NaN,102.48395,Double.NaN,98.79772,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,95.56241,96.4761,Double.NaN,103.42306,101.96289,Double.NaN,105.4637,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,98.51075,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,97.89271,Double.NaN,Double.NaN,103.42383,101.24031,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,98.78939,96.70114,Double.NaN,Double.NaN,108.66088,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,105.60628,100.77026,Double.NaN,100.25917,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN};
        TsData tsd9 = new TsData(TsFrequency.Monthly, 2002, 1, d9, false);
        for (int i = 0; i < 92; i++) {
            Assert.assertEquals("Monthly time series in Table D9 the  " + i + ". value ", tsd9.get(i) / 100, comprest.getData("d-tables.d9", TsData.class).get(i), 0.00001);
        }

    }

    @Test
    public void CalendarsigmaAllForcast18() {
        setInputData();
        setSpec();
        x13spec.getX11Specification().setCalendarSigma(CalendarSigma.All);
        x13spec.getX11Specification().setForecastHorizon(18);
        comprest = X13ProcessingFactory.process(tsInput, x13spec);

        double[] d9 = {Double.NaN, Double.NaN, 100.23005, Double.NaN, 97.76915, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.75984, Double.NaN, Double.NaN, 99.82095, Double.NaN, Double.NaN, 97.05933, 97.80986, Double.NaN, 100.64766, Double.NaN, 98.90049, Double.NaN, Double.NaN, Double.NaN, 100.18205, Double.NaN, Double.NaN, 97.08876, 97.78395, 98.99919, Double.NaN, 100.81137, 99.54005, 99.75984, 107.01656, Double.NaN, Double.NaN, Double.NaN, 97.87428, Double.NaN, 98.36615, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.49137, 98.09601, 97.45741, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.75984, Double.NaN, 102.37229, 100.18767, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.0273, 99.01654, Double.NaN, Double.NaN, 107.66968, 102.33676, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.75984, 107.04276, 101.8417, Double.NaN, 98.69433, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};

        TsData tsd9 = new TsData(TsFrequency.Quarterly, 2002, 1, d9, false);
        for (int i = 0; i < 92; i++) {
            Assert.assertEquals("For Calendarsigma all monthly time series with forcasthorizont = 18 in Table D9 the  " + i + ". value ", tsd9.get(i) / 100, comprest.getData("d-tables.d9", TsData.class).get(i), 0.00001);
        }

    }

    @Test
    public void CalendarsigmaAllForcast18ExcludefcNo() {
        setInputData();
        setSpec();
        x13spec.getX11Specification().setCalendarSigma(CalendarSigma.All);
        x13spec.getX11Specification().setForecastHorizon(18);
        x13spec.getX11Specification().setExcludefcst(false);
        x13spec.getRegArimaSpecification().setUsingAutoModel(false);

        ec.tstoolkit.modelling.arima.x13.ArimaSpec arimaSpec = new ec.tstoolkit.modelling.arima.x13.ArimaSpec();
        arimaSpec.setP(1);
        arimaSpec.setD(1);
        arimaSpec.setQ(0);
        arimaSpec.setBP(0);
        arimaSpec.setBD(1);
        arimaSpec.setBQ(1);
        arimaSpec.setMean(false);

        Parameter[] paraPhi = Parameter.create(1);
        paraPhi[0].setType(ParameterType.Fixed);
        paraPhi[0].setValue(-0.54689);
        arimaSpec.setPhi(paraPhi);

        Parameter[] paraBTheta = Parameter.create(1);
        paraBTheta[0].setType(ParameterType.Fixed);
        paraBTheta[0].setValue(-0.99704);
        arimaSpec.setBTheta(paraBTheta);

        x13spec.getRegArimaSpecification().setArima(arimaSpec);
        x13spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);

        comprest = X13ProcessingFactory.process(tsInput, x13spec);

        double[] d9 = {Double.NaN, Double.NaN, 100.22913, Double.NaN, 97.77024, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.76007, Double.NaN, Double.NaN, 99.8211, Double.NaN, Double.NaN, 97.19896, 97.81213, Double.NaN, 100.64428, Double.NaN, 98.89925, Double.NaN, Double.NaN, Double.NaN, 100.18141, Double.NaN, Double.NaN, 97.22789, 97.78588, 99.00487, Double.NaN, 100.80654, 99.53319, 99.76007, 107.01992, Double.NaN, Double.NaN, Double.NaN, 97.87469, Double.NaN, 98.36659, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.49178, 98.09582, 97.59593, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.76007, Double.NaN, 102.37117, 100.18688, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.02985, 99.01923, Double.NaN, Double.NaN, 107.66782, 102.33657, Double.NaN, Double.NaN, Double.NaN, 97.4712, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.76007, 107.04353, 101.84359, Double.NaN, 98.69271, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        TsData tsd9 = new TsData(TsFrequency.Monthly, 2002, 1, d9, false);

        for (int i = 0; i < 92; i++) {
            Assert.assertEquals("For CS all monthly with fho = 18 excludefcast=no  in Table D9 the  " + i + ". value ", tsd9.get(i) / 100, comprest.getData("d-tables.d9", TsData.class).get(i), 0.00001);
        }

    }

    @Test
    public void CalendarSigmaSelectTest() {
        setInputData();

        setSpec();
        x13spec.getX11Specification().setCalendarSigma(CalendarSigma.Select);
        SigmavecOption[] sigmavecOption = new SigmavecOption[12];
        sigmavecOption[0] = SigmavecOption.Group2;
        sigmavecOption[1] = SigmavecOption.Group2;
        sigmavecOption[11] = SigmavecOption.Group2;
        for (int i = 2; i < 11; i++) {
            sigmavecOption[i] = SigmavecOption.Group1;
        }
        x13spec.getX11Specification().setSigmavec(sigmavecOption);
        comprest = X13ProcessingFactory.process(tsInput, x13spec);
        double[] d9s
                = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.78689, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.1394, Double.NaN, Double.NaN, Double.NaN, 97.81539, Double.NaN, 100.65038, Double.NaN, 98.92192, Double.NaN, Double.NaN, Double.NaN, 100.50353, Double.NaN, Double.NaN, Double.NaN, 97.78831, 99.02479, Double.NaN, 100.36148, 99.55591, Double.NaN, 107.0224, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.37218, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.74573, Double.NaN, Double.NaN, 102.37523, 100.17912, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.91481, 99.02989, Double.NaN, Double.NaN, 107.65994, 102.33634, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.35045, Double.NaN, Double.NaN, Double.NaN, 107.02557, 101.82703, Double.NaN, 98.86157, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        TsData tsd9s = new TsData(TsFrequency.Monthly, 2002, 11, d9s, false);
        for (int i = 0; i < 92; i++) {
            Assert.assertEquals("For Calendarsigma select in Table D9 the  " + i + ". value ", tsd9s.get(i) / 100, comprest.getData("d-tables.d9", TsData.class).get(i), 0.000001);
        }

    }

    @Test
    public void CalendarSigmaNoneTest() {
        setInputData();

        setSpec();
        System.out.println(new Date());
        x13spec.getX11Specification().setCalendarSigma(CalendarSigma.None);
        comprest = X13ProcessingFactory.process(tsInput, x13spec);

        double[] b4n
                = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.0205, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.56817, 97.9913, 98.93496, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 97.45488, 96.77308, 98.59868, Double.NaN, 100.7254, 99.47413, 99.87808, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.02826, 99.82662, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.17562, 97.22097, 98.94551, 99.55054, 98.45429, Double.NaN, Double.NaN, 107.52477, 102.86003, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.32633, 100.27003, 101.07068, Double.NaN, Double.NaN, Double.NaN, 106.21069, 101.5458, 100.09615, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        TsData tsb4n = new TsData(TsFrequency.Monthly, 2002, 5, b4n, false);

        for (int i = 0; i < 92; i++) {
            Assert.assertEquals("For Calendarsigma None in Table B4 the  " + i + ". value ", tsb4n.get(i) / 100, comprest.getData("b-tables.b4", TsData.class).get(i), 0.000001);
        }

        double[] d9n
                = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.5013, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.39311, Double.NaN, Double.NaN, Double.NaN, 95.71332, Double.NaN, 102.4837, Double.NaN, 98.79778, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 95.56263, 96.47733, Double.NaN, 103.42252, 101.96227, Double.NaN, 105.46373, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.51098, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 103.42164, 101.23968, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.79396, 96.68582, Double.NaN, Double.NaN, 108.63969, 103.22344, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 105.47968, 100.67833, Double.NaN, 100.32644, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,};
        TsData tsd9n = new TsData(TsFrequency.Monthly, 2002, 11, d9n, false);
        for (int i = 0; i < 92; i++) {
            Assert.assertEquals("For Calendarsigma None in Table D9 the  " + i + ". value ", tsd9n.get(i) / 100, comprest.getData("d-tables.d9", TsData.class).get(i), 0.000001);
        }

        double[] d10n
                = {106.81835, 102.09284, 100.60454, 98.44266, 97.41303, 96.40659, 96.56703, 98.48312, 101.11051, 101.47606, 100.27348, 100.32179, 106.78805, 102.03913, 100.47621, 98.45037, 97.49339, 96.5813, 96.77407, 98.71908, 100.94708, 101.21615, 100.05049, 100.15503, 106.82107, 102.10803, 100.34902, 98.47763, 97.67819, 96.87878, 97.09469, 99.07852, 100.66918, 100.69055, 99.653, 99.84289, 106.99541, 102.29625, 100.31135, 98.49967, 97.90391, 97.17533, 97.49702, 99.4417, 100.38086, 100.10026, 99.29702, 99.60834, 107.00221, 102.34552, 100.23361, 98.59277, 98.07102, 97.52025, 98.05377, 99.93443, 100.14403, 99.38718, 98.85621, 99.37599, 107.0114, 102.31447, 100.21506, 98.74924, 98.19451, 97.77825, 98.5088, 100.34807, 100.02523, 98.86519, 98.52737, 99.25657, 106.90087, 102.20289, 100.19409, 98.92383, 98.24273, 97.95499, 98.81969, 100.65972, 100.02381, 98.49545, 98.26108, 99.15519, 106.88687, 102.1581, 100.21999, 99.03033, 98.27509, 97.97829, 98.90653, 100.74419};
        TsData tsd10n = new TsData(TsFrequency.Monthly, 2002, 11, d10n, false);
        for (int i = 0; i < 92; i++) {
            Assert.assertEquals("For Calendarsigma None in Table D10 the  " + i + ". value ", tsd10n.get(i) / 100, comprest.getData("d-tables.d10", TsData.class).get(i), 0.000001);
        }

        double[] d10an
                = {100.06397, 98.39337, 98.23106, 99.16849, 106.82116, 102.09453, 100.2128, 99.07129, 98.25467, 97.97093, 98.98226, 100.77688};
        TsData tsd10an = new TsData(TsFrequency.Monthly, 2010, 7, d10an, false);

        for (int i = 0; i < 12; i++) {
            Assert.assertEquals("For Calendarsigma none in Table D10a the  " + i + ". value ", tsd10an.get(i) / 100, comprest.getData("d-tables.d10a", TsData.class).get(i), 0.000001);
        }

    }

    @Test
    public void B4CalendarSigmaAllTest() {
        setInputData();

        setSpec();

        x13spec.getX11Specification().setCalendarSigma(CalendarSigma.All);
        comprest = X13ProcessingFactory.process(tsInput, x13spec);

        double[] b4 = {
            Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.02541, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 97.98676, 99.22613, Double.NaN, Double.NaN, 100.02541, Double.NaN, Double.NaN, 97.45552, 96.75977, 98.58201, Double.NaN, 100.65272, 99.50762, 100.16087, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.03486, 99.83226, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.49341, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.10598, Double.NaN, Double.NaN, Double.NaN, 97.97925, 97.5297, 98.17952, 97.21857, 98.9404, 99.54938, 98.44164, Double.NaN, Double.NaN, 107.51346, 102.85324, 100.02541, 98.54441, 98.17398, Double.NaN, 98.33302, 100.28046, 101.0714, Double.NaN, Double.NaN, Double.NaN, 106.22022, 101.55534, 100.02541, Double.NaN, 97.49679, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN
        };
        TsData tsb4 = new TsData(TsFrequency.Monthly, 2003, 3, b4, false);

        double[] b9 = {
            Double.NaN, Double.NaN, 100.21247, Double.NaN, 97.86026, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.62082, Double.NaN, Double.NaN, 99.80562, Double.NaN, Double.NaN, 97.41973, 98.00637, Double.NaN, 100.6019, Double.NaN, 98.71394, Double.NaN, Double.NaN, Double.NaN, 100.16881, Double.NaN, 98.07836, 97.47577, 97.9992, 99.22266, Double.NaN, 100.52853, 99.32178, Double.NaN, 106.69432, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.54836, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.12392, 97.73951, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.20906, Double.NaN, 102.27148, 100.12385, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.97915, 98.71626, Double.NaN, Double.NaN, 107.27299, 102.23768, Double.NaN, 98.5357, Double.NaN, 97.7085, Double.NaN, Double.NaN, 100.3463, Double.NaN, Double.NaN, 99.54368, 106.70643, 101.80779, Double.NaN, 98.9377, 98.006, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0.73418, 0.70059, 0.63515, 0.34366, 0.39132, 0.82904, 1.18542, 0.8488, 1.35427, 1.0401, 0.62524, 0.9933};

        TsData tsb9 = new TsData(TsFrequency.Monthly, 2002, 11, b9, false);

        double[] b10 = {
            106.74658, 101.96729, 100.09896, 98.71983, 97.93437, 97.33801, 98.03361, 99.41293, 100.4836, 100.41405, 99.3312, 99.4971, 106.78302, 101.93545, 100.05731, 98.72629, 97.94944, 97.39938, 98.07756, 99.56342, 100.40606, 100.27589, 99.20773, 99.4623, 106.87701, 101.93883, 100.00532, 98.7054, 97.97547, 97.52699, 98.20076, 99.83441, 100.29722, 100.03165, 98.9631, 99.39815, 106.9689, 102.00744, 99.96561, 98.6795, 98.02593, 97.64068, 98.34714, 100.13493, 100.21741, 99.70165, 98.73953, 99.38804, 107.01596, 102.02849, 99.9501, 98.63729, 98.06153, 97.76487, 98.52663, 100.40337, 100.1521, 99.38054, 98.56899, 99.35925, 107.00973, 102.05866, 99.97482, 98.66479, 98.10457, 97.81071, 98.6061, 100.5366, 100.14099, 99.15578, 98.50451, 99.37339, 106.97432, 102.05667, 100.01591, 98.68175, 98.1196, 97.83907, 98.65633, 100.54874, 100.15888, 99.07232, 98.48541, 99.35624, 106.93547, 102.08507, 100.058, 98.71137, 98.13008, 97.82152, 98.64727, 100.49936, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        TsData tsb10 = new TsData(TsFrequency.Monthly, 2002, 11, b10, false);

        double[] b17
                = {100, 100, 96.21466, 100, 96.62022, 99.49098, 100, 100, 100, 100, 100, 95.768, 100, 100, 100, 100, 100, 96.94349, 95.75299, 100, 94.23014, 100, 100, 100, 100, 100, 96.9926, 100, 98.96627, 98.69594, 94.75999, 91.10986, 100, 94.46062, 93.36672, 100, 95.50809, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 96.88308, 99.93782, 100, 100, 100, 100, 100, 98.47668, 100, 97.19688, 98.27545, 100, 100, 100, 100, 100, 99.67698, 97.36744, 100, 100, 96.83732, 98.41493, 100, 100, 100, 98.79915, 100, 100, 100, 100, 100, 99.25532, 99.68418, 96.80332, 100, 92.93654, 100, 100, 100, 100, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};

        TsData tsb17 = new TsData(TsFrequency.Monthly, 2002, 11, b17, false);

        double[] b20
                = {100, 100, 100.05214, 100, 99.97849, 99.99721, 100, 100, 100, 100, 100, 100.05303, 100, 100, 100, 100, 100, 99.97583, 99.91468, 100, 100.11217, 100, 100, 100, 100, 100, 100.0379, 100, 100.00474, 99.99187, 99.88353, 99.67772, 100, 100.16705, 100.15966, 100, 99.91513, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100.019, 100.00031, 100, 100, 100, 100, 100, 99.98607, 100, 100.03183, 100.01839, 100, 100, 100, 100, 100, 99.99673, 99.93892, 100, 100, 100.04992, 100.0153, 100, 100, 100, 99.99263, 100, 100, 100, 100, 100, 100.00593, 99.99661, 99.96105, 100, 100.11066, 100, 100, 100, 100, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        TsData tsb20 = new TsData(TsFrequency.Monthly, 2002, 11, b20, false);

        double[] d8
                = {106.20158, 101.78602, 101.44762, 98.22529, 96.9226, 96.3185, 97.29622, 98.65822, 100.40528, 100.50927, 100.68045, 101.19764, 107.66819, 101.82948, 99.37855, 98.76046, 97.34746, 96.08465, 95.6935, 99.23777, 102.55039, 101.53902, 98.78496, 99.63608, 107.20704, 102.78554, 101.20655, 98.46274, 97.83321, 96.21473, 95.48695, 96.112, 101.05553, 103.75095, 102.14335, 100.48692, 105.39058, 101.65975, 99.96354, 98.00752, 97.46218, 97.33492, 98.50864, 100.32576, 99.68517, 99.31178, 98.92589, 99.49146, 107.71429, 102.17569, 99.76093, 99.20429, 98.59188, 98.09702, 97.90352, 100.42516, 99.61217, 99.08951, 97.89196, 98.53888, 107.06175, 103.43268, 101.24654, 98.34818, 98.36466, 98.08568, 98.99599, 100.56805, 98.79781, 96.53038, 98.21741, 99.096, 108.72024, 103.22716, 100.12893, 98.43039, 98.36973, 97.49005, 99.1611, 101.06752, 100.97011, 98.50865, 98.04777, 99.78463, 105.45443, 100.64292, 99.81663, 100.36566, 98.02136, 98.31352, 98.85004, 100.13571, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        TsData tsd8 = new TsData(TsFrequency.Monthly, 2002, 11, d8, false);

        double[] d9
                = {Double.NaN, Double.NaN, 100.22095, Double.NaN, 97.77191, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.74737, Double.NaN, Double.NaN, 99.81183, Double.NaN, Double.NaN, 97.22963, 97.81688, Double.NaN, 100.64778, Double.NaN, 98.90026, Double.NaN, Double.NaN, Double.NaN, 100.173, Double.NaN, Double.NaN, 97.25968, 97.79123, 98.99924, Double.NaN, 100.81031, 99.5398, 99.74737, 107.01637, Double.NaN, Double.NaN, Double.NaN, 97.8769, Double.NaN, 98.37285, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 98.49056, 98.09829, 97.62836, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.74737, Double.NaN, 102.37278, 100.17855, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 100.02785, 99.00992, Double.NaN, Double.NaN, 107.66717, 102.33339, Double.NaN, Double.NaN, Double.NaN, 97.50855, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 99.74737, 107.02596, 101.82855, Double.NaN, 98.70153, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        TsData tsd9 = new TsData(TsFrequency.Monthly, 2002, 11, d9, false);

        double[] d10
                = {107.05158, 102.08869, 100.07842, 98.4367, 97.70974, 97.02058, 97.77048, 99.19319, 100.5761, 100.73429, 99.61606, 99.69982, 107.08929, 102.06028, 100.03779, 98.41674, 97.7558, 97.09483, 97.82389, 99.34297, 100.47506, 100.56071, 99.47235, 99.69182, 107.17853, 102.06309, 99.98386, 98.38807, 97.82207, 97.27001, 97.97936, 99.61025, 100.33687, 100.27422, 99.15118, 99.63137, 107.26645, 102.12726, 99.95933, 98.36094, 97.93979, 97.43002, 98.17351, 99.9251, 100.27152, 99.86927, 98.81642, 99.59504, 107.30043, 102.12268, 99.93962, 98.34746, 98.0422, 97.61984, 98.43738, 100.24691, 100.23469, 99.47903, 98.52402, 99.50019, 107.23472, 102.10077, 99.95679, 98.3975, 98.14547, 97.7436, 98.62241, 100.49688, 100.2624, 99.17033, 98.34812, 99.44154, 107.11439, 102.02981, 99.96931, 98.46833, 98.18172, 97.867, 98.78447, 100.63534, 100.29974, 99.04634, 98.23377, 99.33774, 106.99274, 101.9939, 99.98924, 98.53413, 98.19971, 97.91123, 98.8668, 100.70128, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 102.07331, 99.98929, 98.41873, 97.97456, 97.49464, 98.30729, 100.01899, 100.35091, 99.87631, 98.88028, 99.55679, 107.15352};
        TsData tsd10 = new TsData(TsFrequency.Monthly, 2002, 11, d10, false);

        double[] d10a
                = {100.36258, 99.00381, 98.18515, 99.28964, 106.92492, 101.95439, 100.00008, 98.55567, 98.20291, 97.92461, 98.93009, 100.72369};
        TsData tsd10a = new TsData(TsFrequency.Monthly, 2010, 7, d10a, false);

        double[] d11
                = {136291.3063, 137736.12317, 140790.59237, 139257.0111, 138869.46782, 138965.35524, 138822.06733, 138060.89297, 137991.02942, 137499.35482, 139016.74565, 139555.92446, 132587.4826, 137637.29244, 137511.03212, 139741.47635, 139993.7402, 140763.41634, 140793.83096, 145336.90455, 150082.02664, 150396.70892, 150440.79484, 154666.64465, 158683.8318, 163983.86716, 168823.24913, 170250.3207, 173176.67377, 174831.8883, 177047.4883, 181317.68345, 196200.05219, 208302.78923, 212803.31138, 211943.29497, 208506.02007, 212621.97688, 215275.54614, 216905.19731, 219407.25236, 222716.77938, 225267.48243, 226025.28667, 223756.45894, 223401.05, 224564.89872, 224177.83938, 225899.3755, 226015.40914, 226242.60314, 229008.45737, 228271.08704, 227582.84524, 224151.63617, 224111.6475, 220382.79287, 219169.81063, 217435.291, 216113.16405, 226302.64108, 221075.71716, 221324.64533, 218710.85127, 219690.21685, 220730.56269, 222443.36428, 224844.79049, 226221.39697, 229921.58177, 243737.23499, 251490.47671, 264074.68411, 269945.61876, 272519.6442, 276181.18461, 280388.23784, 281745.6238, 286050.02398, 286990.62826, 286618.89054, 280252.65149, 277138.91562, 274570.3663, 265911.49995, 264152.06308, 266624.68862, 272523.84491, 268177.96981, 271309.02271, 271953.79159, 272507.95118};
        TsData tsd11 = new TsData(TsFrequency.Monthly, 2002, 11, d11, false);

        double[] d11a
                = {277143.16234, 280888.76662, 283221.18109, 283515.84698, 284641.99641, 287460.51082, 290064.96041, 291728.22608, 292457.43875, 293630.02531, 294284.11987, 296632.00911};
        TsData tsd11a = new TsData(TsFrequency.Monthly, 2010, 7, d11a, false);

        double[] c17 = {100, 100, 98.94853, 100, 97.42232, 100, 100, 100, 100, 100, 100, 97.55122, 100, 100, 97.55941, 100, 100, 99.04927, 98.39478, 100, 96.33981, 100, 99.46053, 100, 100, 100, 98.88024, 100, 100, 97.02746, 95.12302, 91.78139, 100, 94.93419, 93.85182, 99.72022, 97.53832, 100, 100, 100, 98.25034, 100, 99.09874, 100, 100, 100, 100, 100, 100, 100, 100, 99.95183, 96.85841, 98.32497, 100, 100, 100, 100, 100, 98.01345, 100, 98.18636, 97.76964, 100, 100, 100, 100, 100, 98.50039, 97.16704, 100, 100, 96.61704, 98.80392, 100, 100, 100, 99.57032, 100, 100, 100, 100, 100, 99.80513, 98.46452, 95.83721, 100, 93.57968, 100, 100, 100, 100, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        TsData tsc17 = new TsData(TsFrequency.Monthly, 2002, 11, c17, false);

        double[] c20 = {100, 100, 100.0091, 100, 99.98711, 100, 100, 100, 100, 100, 100, 100.02178, 100, 100, 99.97375, 100, 100, 99.99498, 99.98146, 100, 100.05809, 100, 99.9931, 100, 100, 100, 100.00979, 100, 100, 99.97919, 99.91643, 99.74696, 100, 100.14763, 100.14928, 100.00179, 99.96213, 100, 100, 100, 99.99218, 100, 100.00915, 100, 100, 100, 100, 100, 100, 100, 100, 100.0003, 100.01667, 100.00976, 100, 100, 100, 100, 100, 99.9831, 100, 100.01978, 100.02286, 100, 100, 100, 100, 100, 99.98146, 99.93189, 100, 100, 100.05646, 100.01191, 100, 100, 100, 99.99792, 100, 100, 100, 100, 100, 100.00123, 99.97924, 99.93801, 100, 100.08712, 100, 100, 100, 100, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        TsData tsc20 = new TsData(TsFrequency.Monthly, 2002, 11, c20, false);

        for (int i = 0;
                i < 92; i++) {
            Assert.assertEquals("For Calendarsigma All in Table B4 the  " + i + ". value ", tsb4.get(i) / 100, comprest.getData("b-tables.b4", TsData.class).get(i), 0.000001);
        }

        for (int i = 0;
                i < 92; i++) {
            Assert.assertEquals("For Calendarsigma All in Table B9 the  ", tsb9.get(i) / 100, comprest.getData("b-tables.b9", TsData.class).get(i), 0.000001);
        }

        for (int i = 0;
                i < 92; i++) {
            Assert.assertEquals("For Calendarsigma All in Table B10 the  ", tsb10.get(i) / 100, comprest.getData("b-tables.b10", TsData.class).get(i), 0.000001);
        }

        for (int i = 0;
                i < 92; i++) {
            Assert.assertEquals("For Calendarsigma All in Table B17 the  ", tsb17.get(i) / 100, comprest.getData("b-tables.b17", TsData.class).get(i), 0.000001);
        }

        for (int i = 0;
                i < 92; i++) {
            Assert.assertEquals("For Calendarsigma All in Table B20 the  ", tsb20.get(i) / 100, comprest.getData("b-tables.b20", TsData.class).get(i), 0.000001);
        }

        for (int i = 0;
                i < 92; i++) {
            Assert.assertEquals("For Calendarsigma All in Table C17 the  " + i + ". value ", tsc17.get(i) / 100, comprest.getData("c-tables.c17", TsData.class).get(i), 0.000001);
        }
        for (int i = 0;
                i < 92; i++) {
            Assert.assertEquals("For Calendarsigma All in table C20 the  " + i + ". value ", tsc20.get(i) / 100, comprest.getData("c-tables.c20", TsData.class).get(i), 0.000001);
        }

        for (int i = 0;
                i < 92; i++) {
            Assert.assertEquals("For Calendarsigma All in Table D8 the  " + i + ". value ", tsd8.get(i) / 100, comprest.getData("d-tables.d8", TsData.class).get(i), 0.000001);
        }

        for (int i = 0;
                i < 92; i++) {
            Assert.assertEquals("For Calendarsigma All in Table D9 the  " + i + ". value ", tsd9.get(i) / 100, comprest.getData("d-tables.d9", TsData.class).get(i), 0.000001);
        }

        for (int i = 0;
                i < 92; i++) {
            Assert.assertEquals("For Calendarsigma All in Table D10 the  " + i + ". value ", tsd10.get(i) / 100, comprest.getData("d-tables.d10", TsData.class).get(i), 0.000001);
        }

        for (int i = 0;
                i < 12; i++) {
            Assert.assertEquals("For Calendarsigma All in Table D10a the  " + i + ". value ", tsd10a.get(i) / 100, comprest.getData("d-tables.d10a", TsData.class).get(i), 0.000001);
        }

        for (int i = 0;
                i < 92; i++) {
            Assert.assertEquals("For Calendarsigma All in Table D11 the  " + i + ". value ", tsd11.get(i), comprest.getData("d-tables.d11", TsData.class).get(i), 0.1);
        }

        for (int i = 0;
                i < 12; i++) {
            Assert.assertEquals("For Calendarsigma All in Table D11a the  " + i + ". value ", tsd11a.get(i), comprest.getData("d-tables.d11a", TsData.class).get(i), 1.0);
        }
    }

    void setSpec() {
        //x11spec
        x11spec = new X11Specification();
        x11spec.setHendersonFilterLength(13);
        x11spec.setExcludefcst(true);
        x11spec.setSigma(1.0, 20.0);
        x11spec.setForecastHorizon(12);
        //x13spec
        x13spec = new X13Specification();

        x13spec.setX11Specification(x11spec);
        RegArimaSpecification regspec = new RegArimaSpecification();
        AutoModelSpec autospec = new AutoModelSpec();
        regspec.setAutoModel(autospec);

        regspec.setUsingAutoModel(true);
        TransformSpec trafoSpec = new TransformSpec();
        trafoSpec.setFunction(DefaultTransformationType.Auto);
        regspec.setTransform(trafoSpec);

        OutlierSpec outlierSp = new OutlierSpec();
        outlierSp.add(OutlierType.AO);
        outlierSp.add(OutlierType.LS);
        regspec.setOutliers(outlierSp);
        x13spec.setRegArimaSpecification(regspec);

    }

    public void setInputData() {

        tsInput = new TsData(TsFrequency.Monthly, 2002, 11, g_inputser, false);
    }

    public void setInputData4() {

        tsInput = new TsData(TsFrequency.Quarterly, 2002, 1, g_inputser, false);
    }

    private static final double[] g_inputser = {145902.0, 140613.0, 140901.0, 137080.0,
        135689.0, 134825.0, 135727.0, 136947.0,
        138786.0, 138509.0, 138483.0, 139137.0,
        141987.0, 140473.0, 137563.0, 137529.0,
        136852.0, 136674.0, 137730.0, 144382.0,
        150795.0, 151240.0, 149647.0, 154190.0,
        170075.0, 167367.0, 168796.0, 167506.0,
        169405.0, 170059.0, 173470.0, 180611.0,
        196861.0, 208874.0, 210997.0, 211162.0,
        223657.0, 217145.0, 215188.0, 213350.0,
        214887.0, 216993.0, 221153.0, 225856.0,
        224364.0, 223109.0, 221907.0, 223270.0,
        242391.0, 230813.0, 226106.0, 225224.0,
        223802.0, 222166.0, 220649.0, 224665.0,
        220900.0, 218028.0, 214226.0, 215033.0,
        242675.0, 225720.0, 221229.0, 215206.0,
        215616.0, 215750.0, 219379.0, 225962.0,
        226815.0, 228014.0, 239711.0, 250086.0,
        282862.0, 275425.0, 272436.0, 271951.0,
        275290.0, 275736.0, 282573.0, 288814.0,
        287478.0, 277580.0, 272244.0, 272752.0,
        284506.0, 269419.0, 266596.0, 268529.0,
        263350.0, 265642.0, 268872.0, 274419.0};

}
