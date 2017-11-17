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
package ec.satoolkit.x11;

import data.*;
import ec.satoolkit.DecompositionMode;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import utilities.CompareTsData;

/**
 *
 * @author Jean Palate
 */
public class X11KernelTest {

    public X11KernelTest() {
    }

    public static void main(String[] cmd) {
        X11Specification spec = new X11Specification();
        spec.setMode(DecompositionMode.PseudoAdditive);
        spec.setForecastHorizon(0);
        X11Toolkit toolkit = X11Toolkit.create(spec);
        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        X11Results rslt = kernel.process(Data.P);
        assertTrue(rslt != null);
//        System.out.println(rslt.getData("d-tables.d12", TsData.class));
//        Map<String, Class> dictionary = rslt.getDictionary();
//        for (Entry<String, Class> entry : dictionary.entrySet()){
//            System.out.println(entry.getKey());
//        }
    }

    //@Test
    public void demotMyKernel() {
        X11Specification spec = new X11Specification();
        X11Toolkit toolkit = X11Toolkit.create(spec);
        toolkit.setTrendCycleFilterprovider(new MyDummyTrendCycleComputer());
        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        X11Results rslt = kernel.process(Data.X);
        System.out.println(rslt.getData("d-tables.d12", TsData.class));
    }

    @Test
    public void testFcasts5() {
        X11Specification spec = new X11Specification();
        spec.setForecastHorizon(5);
        X11Toolkit toolkit = X11Toolkit.create(spec);
        toolkit.setPreprocessor(new AirlinePreprocessor());
        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        X11Results rslt = kernel.process(Data.X);
        assertTrue(null != rslt.getData("d-tables.d10a", TsData.class));
    }

     @Test
    public void compositeFilterMsr() {
        X11Specification x11Specification = new X11Specification();
        SeasonalFilterOption[] filterOption = new SeasonalFilterOption[12];
        for (int i = 0; i < 12; i++) {
            filterOption[i] = SeasonalFilterOption.Msr;
        }
        x11Specification.setSeasonalFilters(filterOption);
        x11Specification.setSigma(5.0, 5.5);
        X11Toolkit toolkit = X11Toolkit.create(x11Specification);

        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        X11Results rslt = kernel.process(Data.UNEMPLOY);
        assertTrue(CompareTsData.compareTS(rslt.getData("d-tables.d10", TsData.class), Datax11.UEMPLOMENSEASONAL, 0.000000001));
    }

    @Test
    public void Msr() {
        X11Specification x11Specification = new X11Specification();
        SeasonalFilterOption[] filterOption = new SeasonalFilterOption[1];
        filterOption[0] = SeasonalFilterOption.Msr;
        x11Specification.setSeasonalFilters(filterOption);
        x11Specification.setSigma(5.0, 5.5);        X11Toolkit toolkit = X11Toolkit.create(x11Specification);

        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        X11Results rslt = kernel.process(Data.UNEMPLOY);
        assertTrue(CompareTsData.compareTS(rslt.getData("d-tables.d10", TsData.class), Datax11.UEMPLOMENSEASONAL, 0.000000001));
    }
    @Test
    public void mixedFiltersWithStableTest1() {

        //author C.Hofer
        X11Specification spec = new X11Specification();
        SeasonalFilterOption[] filters = new SeasonalFilterOption[12];
        filters[0] = SeasonalFilterOption.S3X3;
        filters[1] = SeasonalFilterOption.Stable;
        filters[2] = SeasonalFilterOption.S3X3;
        filters[3] = SeasonalFilterOption.S3X3;
        filters[4] = SeasonalFilterOption.S3X3;
        filters[5] = SeasonalFilterOption.S3X3;
        filters[6] = SeasonalFilterOption.S3X3;
        filters[7] = SeasonalFilterOption.S3X3;
        filters[8] = SeasonalFilterOption.S3X3;
        filters[9] = SeasonalFilterOption.S3X3;
        filters[10] = SeasonalFilterOption.S3X3;
        filters[11] = SeasonalFilterOption.S3X3;

        //barriers for the sigma are set so that no outliers are detected
        spec.setSigma(5.0, 5.5);
        spec.setHendersonFilterLength(13);
        spec.setMode(DecompositionMode.Additive);
        spec.setForecastHorizon(0); // set as the default value only to be sure
        spec.setSeasonal(true);
        spec.setSeasonalFilters(filters);

        X11Toolkit toolkit = X11Toolkit.create(spec);

        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        X11Results rslt = kernel.process(Datax11.O);
//        System.out.println("d-tables.d10");
//
//        System.out.println(rslt.getData("d-tables.d10", TsData.class));

        //equals for tsdata is defined for startdate and values
        assertEquals(rslt.getData("b-tables.b5", TsData.class).round(11), Datax11.B5.round(11));
        assertEquals(rslt.getData("d-tables.d10", TsData.class).round(10), Datax11.D10.round(10));

    }

    @Test
    public void mixedFiltersWithStableTest2() {
        //set specification
        //author C.Hofer
        X11Specification spec = new X11Specification();
        SeasonalFilterOption[] filters = new SeasonalFilterOption[12];
        filters[0] = SeasonalFilterOption.Stable;
        filters[1] = SeasonalFilterOption.S3X3;
        filters[2] = SeasonalFilterOption.S3X3;
        filters[3] = SeasonalFilterOption.S3X3;
        filters[4] = SeasonalFilterOption.S3X3;
        filters[5] = SeasonalFilterOption.S3X3;
        filters[6] = SeasonalFilterOption.S3X3;
        filters[7] = SeasonalFilterOption.S3X3;
        filters[8] = SeasonalFilterOption.S3X3;
        filters[9] = SeasonalFilterOption.S3X3;
        filters[10] = SeasonalFilterOption.S3X3;
        filters[11] = SeasonalFilterOption.S3X3;

        //barriers for the sigma are set so that no outliers are detected
        spec.setSigma(5.0, 5.5);
        spec.setHendersonFilterLength(13);
        spec.setMode(DecompositionMode.Additive);
        spec.setForecastHorizon(0); // set as the default value only to be sure
        spec.setSeasonal(true);
        spec.setSeasonalFilters(filters);

        X11Toolkit toolkit = X11Toolkit.create(spec);

        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        X11Results rslt = kernel.process(Datax11.O);
        assertEquals(rslt.getData("b-tables.b5", TsData.class).round(11), Datax11.B5_2.round(11));
        assertEquals(rslt.getData("d-tables.d10", TsData.class).round(10), Datax11.D10_2.round(10));

    }

    @Test
    public void mixedFiltersWithStableTest3() {
        //set specification
        //author C.Hofer
        X11Specification spec = new X11Specification();
        SeasonalFilterOption[] filters = new SeasonalFilterOption[12];
        filters[0] = SeasonalFilterOption.S3X3;
        filters[1] = SeasonalFilterOption.Stable;
        filters[2] = SeasonalFilterOption.S3X3;
        filters[3] = SeasonalFilterOption.S3X3;
        filters[4] = SeasonalFilterOption.S3X3;
        filters[5] = SeasonalFilterOption.S3X3;
        filters[6] = SeasonalFilterOption.S3X3;
        filters[7] = SeasonalFilterOption.S3X3;
        filters[8] = SeasonalFilterOption.S3X3;
        filters[9] = SeasonalFilterOption.S3X3;
        filters[10] = SeasonalFilterOption.S3X3;
        filters[11] = SeasonalFilterOption.S3X3;

        //barriers for the sigma are set so that no outliers are detected
        spec.setSigma(5.0, 5.5);
        spec.setHendersonFilterLength(13);
        spec.setMode(DecompositionMode.Additive);
        spec.setForecastHorizon(0); // set as the default value only to be sure
        spec.setSeasonal(true);
        spec.setSeasonalFilters(filters);

        X11Toolkit toolkit = X11Toolkit.create(spec);

        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        X11Results rslt = kernel.process(Datax11.O_3);
        assertEquals(rslt.getData("b-tables.b5", TsData.class).round(11), Datax11.B5_3.round(11));
    }

    @Test
    public void testFcasts105() {
        X11Specification spec = new X11Specification();
        spec.setForecastHorizon(105);
        X11Toolkit toolkit = X11Toolkit.create(spec);
        toolkit.setPreprocessor(new AirlinePreprocessor());
        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        X11Results rslt = kernel.process(Data.X);
//        System.out.println("f105");
//        System.out.println("d-tables.d10a");
//        System.out.println(rslt.getData("d-tables.d10a", TsData.class));
        assertTrue(rslt.getData("d-tables.d10a", TsData.class) != null);
    }
}

class MyDummyTrendCycleComputer extends DefaultX11Algorithm implements ITrendCycleComputer {

    @Override
    public TsData doFinalFiltering(X11Step step, TsData s, InformationSet info) {
        SymmetricFilter trendFilter = TrendCycleFilterFactory.makeTrendFilter(context.getFrequency());
        return new DefaultTrendFilteringStrategy(trendFilter, new CopyEndPoints(context.getFrequency() / 2)).process(s,
                s.getDomain());
    }

    @Override
    public TsData doInitialFiltering(X11Step step, TsData s, InformationSet info) {
        return doFinalFiltering(step, s, info);
    }
}
