/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x11;

import ec.tstoolkit.stats.*;
import org.junit.Test;
import ec.tstoolkit.timeseries.simplets.TsData;
import data.*;
import ec.satoolkit.DecompositionMode;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christiane Hofer
 */
public class CalendarsigmaTest {

    public CalendarsigmaTest() {
    }

    private static final Integer round = 5;//CH: Not sure weather this is enough or 10 is possible 

    @Test
    public void TestCochranDefault() {
        TsData TsInput, TsOutD10Default;
        X11Kernel kernel;

        TsInput = DataCochran.CStartJan;
        kernel = getX11Kernel();
        X11Results rslt = kernel.process(TsInput);

        System.out.println("Test Cochran Default:");

        //Solution Calculated with WinX13 Build 9
        TsOutD10Default = DataCochran.C_D10_Default.round(round);

        assertEquals(rslt.getData("d-tables.d10", TsData.class).round(round), TsOutD10Default);
        System.out.println("Results D10 from Win X13");
        System.out.println(TsOutD10Default);
        System.out.println("Results D10 calculated");
        System.out.println(rslt.getData("d-tables.d10", TsData.class).round(round));

    }

    private X11Kernel getX11Kernel() {
        X11Specification spec = new X11Specification();

        SeasonalFilterOption[] filters = new SeasonalFilterOption[12];

        filters[0] = SeasonalFilterOption.S3X9;
        filters[1] = SeasonalFilterOption.S3X9;
        filters[2] = SeasonalFilterOption.S3X9;
        filters[3] = SeasonalFilterOption.S3X9;
        filters[4] = SeasonalFilterOption.S3X9;
        filters[5] = SeasonalFilterOption.S3X9;
        filters[6] = SeasonalFilterOption.S3X9;
        filters[7] = SeasonalFilterOption.S3X9;
        filters[8] = SeasonalFilterOption.S3X9;
        filters[9] = SeasonalFilterOption.S3X9;
        filters[10] = SeasonalFilterOption.S3X9;
        filters[11] = SeasonalFilterOption.S3X9;

        spec.setSigma(1.5, 2.5);
        spec.setHendersonFilterLength(17);
        spec.setMode(DecompositionMode.Additive);
        spec.setForecastHorizon(0);
        spec.setSeasonal(true);
        spec.setSeasonalFilters(filters);

        X11Toolkit toolkit = X11Toolkit.create(spec);
        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        return kernel;
    }

    
  
}
    
    
 

    


