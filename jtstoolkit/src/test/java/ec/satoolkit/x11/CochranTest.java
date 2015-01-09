/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x11;

import org.junit.Test;
import ec.tstoolkit.timeseries.simplets.TsData;
import data.*;
import ec.satoolkit.DecompositionMode;
import static org.junit.Assert.assertEquals;

/**
 * Das ist eine reine Entwiclungsklasse für den Cochrantest, diese wird Später
 * nur TEsts für den CochranTest usw enthatlen
 *
 * @author s4504ch
 */
public class CochranTest {

    public CochranTest() {
    }

    private static final Integer round = 5;//CH: Not sure weather this is enough or 10 is possible 

    @Test
    public void TestCochranDefault() {
        TsData TsInput, TsOutD10Default;
        X11Kernel kernel;

        TsInput = DataCochran.C; //Die Variable bräuchte man eigentlich nicht zu belegen
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

    @Test
    public void TestCochranTest() {
        System.out.println("Cochran Test");
        assertEquals(true, calcCochranTest());
        System.out.println("Test tw");
        assertEquals(0.116226, tw, 0.000001);
        //assertEquals(0.11622,tw);
    }
    private double[] s;
    private double tw;
    //  Critical values for monthly data  
    private static final double[] t = {0.5410, 0.3934, 0.3264, 0.2880, 0.2624, 0.2439, 0.2299, 0.2187,
        0.2098, 0.2020, 0.1980, 0.194, 0.186, 0.182, 0.178, 0.174, 0.17,
        0.166, 0.162, 0.158, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15,
        0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.1403, 0.14,
        0.14, 0.14, 0.14};

//critical values for quarterly data
    private static final double[] t4 = {
        0.9065, 0.7679, 0.6841, 0.6287, 0.5895, 0.5598, 0.5365, 0.5175,
        .5017, 0.4884, 0.480, 0.471, 0.463, 0.454, 0.445, 0.4366,
        .433, 0.430, 0.427, 0.424, 0.421, 0.417, 0.414, 0.411, 0.408,
        .404, 0.401, 0.398, 0.395, 0.391, 0.388, 0.385, 0.382, 0.379,
        .375, 0.3720, 0.369, 0.366, 0.362, 0.359};
//calculate the Corantest for a given Timseries, and gives the false if the Nullhypothesis
    // of equal variances of the periodes has to be rejected and different standarddeviations should be used 
    //for outlier detection

    private boolean calcCochranTest() {
// X,I1,Ib,Ie Was steht in diesen variablen, daher folgende Testbelegung
        int Ib = 0; //X conatins the values from the beginning to the end of the, Ib index of the first period 
        int Ie;//index of the last period
        int PSP = 5;// warum benötigt man den Wert und nimmt nicht Ny
        double[] X;//array of the values of the time series, cant be usesd 
//Damit ich mit den Daten arbeiten kann
        TsData TsInput;
        TsInput = DataCochran.C;
        System.out.println("Periode " + TsInput.getStart() + " Erster Wert: " + TsInput.getValues().get(0));
        System.out.println("Periode" + TsInput.getLastPeriod() + "Letzter Wert: " + TsInput.getValues().get(TsInput.getLength() - 1));
        X11Kernel kernel;
        kernel = getX11Kernel();
        X11Results rslt = kernel.process(TsInput);
//

        boolean I1 = true; //is the boolen that is true if the test has not be rejected

        int j; //counter for the periodes from 1,...4 or 12  
        int n1; //number of values in a periode eg. in January
        int nmin; // minimal number of observations of a period
        boolean blngoto = true;
        //  double[] s;//array for the standarddeviation for each period dimension 1,...4 or 12

        double smax; //max standarddeviation of periods
        double st;// theoretical mean 0 for multi and 1 for add
        // double tw;// teststatistik 
        double tt;// critical vaule 
        int Ny = TsInput.getFrequency().intValue(); //Beobachtungen pro Jahr
        System.out.println("Anzahl an Perioden pro Jahr: " + TsInput.getFrequency().intValue());
        s = new double[Ny]; //original PSP first remains empty 0,...,Ny-1 
        //  Double X[],t[40],t4[40],s[PSP];
//C-----------------------------------------------------------------------
        // LOGICAL dpeq
        // EXTERNAL dpeq
//C-----------------------------------------------------------------------

//
//     This routine performs Cochran's test to determine if the months
//     are heteroskedastic.
//C
        tw = 0; //wird schon größer werden
        smax = -10.0;
        nmin = 100;

        st = 1; //Additve 
        if (rslt.getSeriesDecomposition().getMode().isMultiplicative())//(Muladd.eq.1)// das muss hinterher im X11 anders getesetet werden
        {
            st = 0;
        }
//
        System.out.println("st= " + st);
        for (int i = 0; i <= Ny - 1; i++) { //each period is taken into accoutn
            n1 = 1;
            j = Ib + i; //hier später ggf. mit start und endperiode arbeiten
            s[i] = 0;
            blngoto = true;
            do {
                s[i] = s[i] + ((TsInput.getValues().get(j) - st)*(TsInput.getValues().get(j) - st));//
                
                j = j + Ny; // for each year
                n1 = n1 + 1;//count values 

                if (j > TsInput.getValues().getLength() - 1) { //IE keine Ahnung ob +1 oder nicht
                    if (nmin > n1 - 3) {// fortran initilizes with zero
                        nmin = n1 - 3;//      
                    }
                    s[i] = s[i] / (n1 - 1);
                    if (smax < s[i]) {
                        smax = s[i];
                    }
                    tw = tw + s[i];
                    System.out.println("s(" + i + "):" + s[i] + " n1= " + n1);
                    blngoto = false;
                };

            } while (blngoto);

        }
        if (!(tw == 0)) {
            tw = smax / tw;
        }

        if (nmin > 40) {
            nmin = 40;
        }

        tt = t[nmin];
        if (Ny == 4) {
            tt = t4[nmin];
        }
        System.out.println("tw: " + tw);
        System.out.println("tt: " + tt);
        if (tw >= tt) {
            I1 = false;
        }
        System.out.println("I1: " + I1);

        return I1;
    }

    //calculates the Cochran Test for a ts
    private boolean Cochran() {
        //Daten einlesen
        TsData TsInput;
        TsInput = DataCochran.C;
        int nmin; // minimal number of observations of a period
     double smax; //max standarddeviation of periods
        double st;// theoretical mean 0 for multi and 1 for add
        
        return true;
    }

}
