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
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.tramo.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.*;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christiane Hofer
 */
public class CalendarsigmaTest {

    public CalendarsigmaTest() {
    }

    private static final Integer round = 4;

    @Test
    public void TestCalendarsigmaDefaultMultStartJan() {
        TsData TsInput;
        X11Kernel kernel;
        TsInput = DataCalendarSigmaX11.CStartJan;
        kernel = getX11Kernel(CalendarSigma.None, DecompositionMode.Multiplicative);
        X11Results rslt = kernel.process(TsInput);
        
        for (int i = 0; i < 238; ++i) {
            // System.out.println(DataCochran.C_D10_Default.getDomain().get(i) + " WinX13: " + DataCochran.C_D10_Default_Mult.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d10", TsData.class).getDomain().get(i) +" " +rslt.getData("d-tables.d10", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_D10_Default_Mult.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }
    }

    @Test
    public void TestCalendarsigmaAllMultStartJan() {
        TsData TsInput;
        X11Kernel kernel;
        TsInput = DataCalendarSigmaX11.CStartJan;
        kernel = getX11Kernel(CalendarSigma.All, DecompositionMode.Multiplicative);
        X11Results rslt = kernel.process(TsInput);

        for (int i = 0; i < 238; ++i) {
            //System.out.println(DataCochran.C_D10_All_Mult.getDomain().get(i) + " WinX13: " + DataCochran.C_D10_All_Mult.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d10", TsData.class).getDomain().get(i) +" " +rslt.getData("d-tables.d10", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_D10_All_Mult.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }
    }

    @Test
    public void TestCalendarsigmaAllMultStartApril() {
        TsData TsInput;
        X11Kernel kernel;
        TsInput = DataCalendarSigmaX11.CStartAprl;
        kernel = getX11Kernel(CalendarSigma.All, DecompositionMode.Multiplicative);
        X11Results rslt = kernel.process(TsInput);

        for (int i = 0; i < 238; ++i) {
            //System.out.println(DataCochran.C_D10_All_Mult.getDomain().get(i) + " WinX13: " + DataCochran.C_D10_All_Mult.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d10", TsData.class).getDomain().get(i) +" " +rslt.getData("d-tables.d10", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_D10_All_Mult.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }
    }

    @Test
    public void TestCalendarsigmaAllMultFourthQuarterQuarterly() {
        TsData TsInput;
        X11Kernel kernel;
        SigmavecOption[] sigmavec;
        sigmavec = new SigmavecOption[1];
        TsInput = DataCalendarSigmaX11.CStartFourQuarter;
        SeasonalFilterOption[] filters = new SeasonalFilterOption[4];

        filters[0] = SeasonalFilterOption.S3X9;
        filters[1] = SeasonalFilterOption.S3X9;
        filters[2] = SeasonalFilterOption.S3X9;
        filters[3] = SeasonalFilterOption.S3X9;
        kernel = getX11Kernel(CalendarSigma.All, sigmavec, DecompositionMode.Multiplicative, filters);
        X11Results rslt = kernel.process(TsInput);

        for (int i = 0; i < 237; ++i) {
            assertEquals(DataCalendarSigmaX11.C_D10_All_Mult_StartForthQuarter.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }
    }

    @Test
    public void TestCalendarsigmaSignifMultFourthQuarterQuarterly() {
        //      System.out.println("TestCalendarsigmaSignifMultFourthQuarterQuarterly");

        TsData TsInput;
        X11Kernel kernel;
        SigmavecOption[] sigmavec;
        sigmavec = new SigmavecOption[1];
        TsInput = DataCalendarSigmaX11.CStartFourQuarter;
        SeasonalFilterOption[] filters = new SeasonalFilterOption[4];

        filters[0] = SeasonalFilterOption.S3X9;
        filters[1] = SeasonalFilterOption.S3X9;
        filters[2] = SeasonalFilterOption.S3X9;
        filters[3] = SeasonalFilterOption.S3X9;
        kernel = getX11Kernel(CalendarSigma.Signif, sigmavec, DecompositionMode.Multiplicative, filters);
        X11Results rslt = kernel.process(TsInput);

        for (int i = 0; i < 237; ++i) {
            assertEquals(DataCalendarSigmaX11.C_D10_Signif_Mult_StartForthQuarter.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }
    }

    @Test
    public void TestCalendarsigmaSignifMultFourthQuarterQuarterlyMixedFilter() {
//        System.out.println("TestCalendarsigmaSignifMultFourthQuarterQuarterlyMixedFilter");
        TsData TsInput;
        X11Kernel kernel;
        SigmavecOption[] sigmavec;
        sigmavec = new SigmavecOption[1];
        TsInput = DataCalendarSigmaX11.CStartFourQuarter;
        SeasonalFilterOption[] filters = new SeasonalFilterOption[4];

        filters[0] = SeasonalFilterOption.S3X9;
        filters[1] = SeasonalFilterOption.Stable;
        filters[2] = SeasonalFilterOption.S3X9;
        filters[3] = SeasonalFilterOption.S3X9;
        kernel = getX11Kernel(CalendarSigma.Signif, sigmavec, DecompositionMode.Multiplicative, filters);
        X11Results rslt = kernel.process(TsInput);

        for (int i = 0; i < 237; ++i) {
            assertEquals(DataCalendarSigmaX11.C_D10_Signif_Mult_StartForthQuarter_MixedFilters.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }
    }

    @Test
    public void TestCalendarsigmaSignifMultFourthQuarterQuarterlyMixedFilterPrepro() {
        //      System.out.println("TestCalendarsigmaSignifMultFourthQuarterQuarterlyMixedFilterPrepro");
        TsData TsInput;
        X11Specification x11spec;
        X13Specification x13spec;

        //X11 Specification
        SigmavecOption[] sigmavec;
        sigmavec = new SigmavecOption[1]; //Default setting not used
        TsInput = DataCalendarSigmaX11.CStartFourQuarter;
        SeasonalFilterOption[] filters = new SeasonalFilterOption[4];

        filters[0] = SeasonalFilterOption.S3X9;
        filters[1] = SeasonalFilterOption.Stable;
        filters[2] = SeasonalFilterOption.S3X9;
        filters[3] = SeasonalFilterOption.S3X9;
        x11spec = getX11Spec(CalendarSigma.Signif, sigmavec, DecompositionMode.Multiplicative, filters);
        x11spec.setForecastHorizon(-1);
        // regSpec
        RegArimaSpecification regSpec = new RegArimaSpecification();
        regSpec.setUsingAutoModel(false);
        ec.tstoolkit.modelling.arima.x13.ArimaSpec arimaSpec = new ec.tstoolkit.modelling.arima.x13.ArimaSpec();
        arimaSpec.setP(0);
        arimaSpec.setD(1);
        arimaSpec.setQ(2);
        arimaSpec.setBP(0);
        arimaSpec.setBD(1);
        arimaSpec.setBQ(1);
        arimaSpec.setMean(false);

        Parameter[] paraTheta = Parameter.create(2);
        paraTheta[0].setType(ParameterType.Fixed);
        paraTheta[1].setType(ParameterType.Fixed);
        paraTheta[0].setValue(0.37832);
        paraTheta[1].setValue(-0.03136);
        arimaSpec.setTheta(paraTheta);

        Parameter[] paraBTheta = Parameter.create(1);
        paraBTheta[0].setType(ParameterType.Fixed);
        paraBTheta[0].setValue(-1.0000);
        arimaSpec.setBTheta(paraBTheta);
//        
        regSpec.setArima(arimaSpec);

        //Transformations spec
        TransformSpec trafoSpec = new TransformSpec();
        trafoSpec.setFunction(DefaultTransformationType.Log);
        regSpec.setTransform(trafoSpec);

        x13spec = new X13Specification();
        x13spec.setX11Specification(x11spec);
        x13spec.setRegArimaSpecification(regSpec);

        CompositeResults rslt = X13ProcessingFactory.process(TsInput, x13spec);

        //Vergelich der Ergebnisse B1
        // System.out.println("B1");
        //  System.out.println(rslt.getData("b-tables.b1", TsData.class));
        // die Forcasts passen gerade nicht
        for (int i = 0; i < 241; ++i) {
            //   System.out.println(DataCochran.C_B1_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.getDomain().get(i) + " WinX13: " + DataCochran.C_B1_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i) + "; Calculated JD+: " + rslt.getData("b-tables.b1", TsData.class).getDomain().get(i) + " " + rslt.getData("b-tables.b1", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX13.C_B1_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i), rslt.getData("b-tables.b1", TsData.class).get(i), 0.0005);
        }

        String strTable;
        strTable = "b3";
        //    System.out.println(strTable);
        //  System.out.println(rslt.getData("b-tables." + strTable, TsData.class));
        for (int i = 0; i < 236; ++i) {
            //   System.out.println(DataCochran.C_B3_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.getDomain().get(i) + " WinX13: " + DataCochran.C_B3_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i) + "; Calculated JD+: " + rslt.getData("b-tables." + strTable, TsData.class).getDomain().get(i) + " " + rslt.getData("b-tables." + strTable, TsData.class).get(i));
            assertEquals(DataCalendarSigmaX13.C_B3_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i), rslt.getData("b-tables.b3", TsData.class).get(i), 0.00001);

        }
        strTable = "b4";
        //   System.out.println(strTable);
        for (int i = 0; i < 236; ++i) {
            //  System.out.println(DataCalendarSigmaX13.C_B4_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.getDomain().get(i) + " WinX13: " + DataCalendarSigmaX13.C_B4_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i) + "; Calculated JD+: " + rslt.getData("b-tables.b4", TsData.class).getDomain().get(i) + " " + rslt.getData("b-tables.b4", TsData.class).get(i));

            assertEquals(DataCalendarSigmaX13.C_B4_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i), rslt.getData("b-tables.b4", TsData.class).get(i), 0.00001);
        }
        //  System.out.println("C9");
        for (int i = 0; i < 238; ++i) {
            //   System.out.println(DataCalendarSigmaX13.C_C9_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.getDomain().get(i) + " WinX13: " + DataCalendarSigmaX13.C_C9_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i) + "; Calculated JD+: " + rslt.getData("c-tables.c9", TsData.class).getDomain().get(i) + " " + rslt.getData("c-tables.c9", TsData.class).get(i));

            assertEquals(DataCalendarSigmaX13.C_C9_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i), rslt.getData("c-tables.c9", TsData.class).get(i), 0.00001);
        }
     // System.out.println("C10");

        for (int i = 0; i < 238; ++i) {
            //  System.out.println(DataCalendarSigmaX13.C_C10_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.getDomain().get(i) + " WinX13: " + DataCalendarSigmaX13.C_C10_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i) + "; Calculated JD+: " + rslt.getData("c-tables.c10", TsData.class).getDomain().get(i) + " " + rslt.getData("c-tables.c10", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX13.C_C10_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i), rslt.getData("c-tables.c10", TsData.class).get(i), 0.00001);
        }

        //    System.out.println("D9");
        for (int i = 0; i < 238; ++i) {
            //  System.out.println(DataCalendarSigmaX13.C_D9_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.getDomain().get(i) + " WinX13: " + DataCalendarSigmaX13.C_D9_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d9", TsData.class).getDomain().get(i) + " " + rslt.getData("d-tables.d9", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX13.C_D9_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i), rslt.getData("d-tables.d9", TsData.class).get(i), 0.00001);
        }

        //      System.out.println("D10");
        for (int i = 0; i < 238; ++i) {
            //System.out.println(DataCalendarSigmaX13.C_D10_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.getDomain().get(i) + " WinX13: " + DataCalendarSigmaX13.C_D10_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d10", TsData.class).getDomain().get(i) + " " + rslt.getData("d-tables.d10", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX13.C_D10_Signif_Mult_StartForthQuarter_MixedFiltersPrepro.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }
    }

    @Test
    public void TestCalendarsigmaSignifMultFourthQuarterQuarterlyMixedFilterPreproNoForcast() {
        //      System.out.println("TestCalendarsigmaSignifMultFourthQuarterQuarterlyMixedFilterPrepro");
        TsData TsInput;
        X11Specification x11spec;
        X13Specification x13spec;

        //X11 Specification
        SigmavecOption[] sigmavec;
        sigmavec = new SigmavecOption[1]; //Default setting not used
        TsInput = DataCalendarSigmaX11.CStartFourQuarter;
        SeasonalFilterOption[] filters = new SeasonalFilterOption[4];

        filters[0] = SeasonalFilterOption.S3X9;
        filters[1] = SeasonalFilterOption.Stable;
        filters[2] = SeasonalFilterOption.S3X9;
        filters[3] = SeasonalFilterOption.S3X9;
        x11spec = getX11Spec(CalendarSigma.Signif, sigmavec, DecompositionMode.Multiplicative, filters);
        x11spec.setForecastHorizon(0);
        // regSpec
        RegArimaSpecification regSpec = new RegArimaSpecification();
        regSpec.setUsingAutoModel(false);
        ec.tstoolkit.modelling.arima.x13.ArimaSpec arimaSpec = new ec.tstoolkit.modelling.arima.x13.ArimaSpec();
        arimaSpec.setP(0);
        arimaSpec.setD(1);
        arimaSpec.setQ(2);
        arimaSpec.setBP(0);
        arimaSpec.setBD(1);
        arimaSpec.setBQ(1);
        arimaSpec.setMean(false);

        Parameter[] paraTheta = Parameter.create(2);
        paraTheta[0].setType(ParameterType.Fixed);
        paraTheta[1].setType(ParameterType.Fixed);
        paraTheta[0].setValue(0.37832);
        paraTheta[1].setValue(-0.03136);
        arimaSpec.setTheta(paraTheta);

        Parameter[] paraBTheta = Parameter.create(1);
        paraBTheta[0].setType(ParameterType.Fixed);
        paraBTheta[0].setValue(-1.0000);
        arimaSpec.setBTheta(paraBTheta);
//        
        regSpec.setArima(arimaSpec);

        //Transformations spec
        TransformSpec trafoSpec = new TransformSpec();
        trafoSpec.setFunction(DefaultTransformationType.Log);
        regSpec.setTransform(trafoSpec);

        x13spec = new X13Specification();
        x13spec.setX11Specification(x11spec);
        x13spec.setRegArimaSpecification(regSpec);

        CompositeResults rslt = X13ProcessingFactory.process(TsInput, x13spec);
      
        for (int i = 0; i < 238; ++i) {
            assertEquals(DataCalendarSigmaX13.C_D10_Signif_Mult_StartForthQuarter_MixedFiltersPreproNoFcast.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.0005);
        }
    }

    @Test
    public void TestCalendarsigmaSignifAddFourthQuarterQuarterlyMixedFilterPrepro() {
        //     System.out.println("TestCalendarsigmaSignifAddFourthQuarterQuarterlyMixedFilterPrepro");
        TsData TsInput;
        X11Specification x11spec;
        X13Specification x13spec;

        //X11 Specification
        SigmavecOption[] sigmavec;
        sigmavec = new SigmavecOption[1]; //Default setting not used
        TsInput = DataCalendarSigmaX11.CStartFourQuarter;
        SeasonalFilterOption[] filters = new SeasonalFilterOption[4];

        filters[0] = SeasonalFilterOption.S3X9;
        filters[1] = SeasonalFilterOption.Stable;
        filters[2] = SeasonalFilterOption.S3X9;
        filters[3] = SeasonalFilterOption.S3X9;
        x11spec = getX11Spec(CalendarSigma.Signif, sigmavec, DecompositionMode.Additive, filters);
        x11spec.setForecastHorizon(-1);
        // regSpec
        RegArimaSpecification regSpec = new RegArimaSpecification();
        regSpec.setUsingAutoModel(false);
        ec.tstoolkit.modelling.arima.x13.ArimaSpec arimaSpec = new ec.tstoolkit.modelling.arima.x13.ArimaSpec();
        arimaSpec.setP(0);
        arimaSpec.setD(1);
        arimaSpec.setQ(2);
        arimaSpec.setBP(0);
        arimaSpec.setBD(1);
        arimaSpec.setBQ(1);
        arimaSpec.setMean(false);

        Parameter[] paraTheta = Parameter.create(2);
        paraTheta[0].setType(ParameterType.Fixed);
        paraTheta[1].setType(ParameterType.Fixed);
        paraTheta[0].setValue(0.42838);
        paraTheta[1].setValue(0.01769);
        arimaSpec.setTheta(paraTheta);

        Parameter[] paraBTheta = Parameter.create(1);
        paraBTheta[0].setType(ParameterType.Fixed);
        paraBTheta[0].setValue(-1.0000);
        arimaSpec.setBTheta(paraBTheta);       
//        
        regSpec.setArima(arimaSpec);

        //Transformations spec
        TransformSpec trafoSpec = new TransformSpec();
        trafoSpec.setFunction(DefaultTransformationType.None);
        regSpec.setTransform(trafoSpec);

        x13spec = new X13Specification();
        x13spec.setX11Specification(x11spec);
        x13spec.setRegArimaSpecification(regSpec);

        CompositeResults rslt = X13ProcessingFactory.process(TsInput, x13spec);

        // System.out.println("B4");
        for (int i = 0; i < 236; ++i) {
            //   System.out.println(DataCalendarSigmaX13.C_B4_Signif_Add_StartForthQuarter_MixedFiltersPrepro.getDomain().get(i) + " WinX13: " + DataCalendarSigmaX13.C_B4_Signif_Add_StartForthQuarter_MixedFiltersPrepro.get(i) + "; Calculated JD+: " + rslt.getData("b-tables.b4", TsData.class).getDomain().get(i) + " " + rslt.getData("b-tables.b4", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX13.C_B4_Signif_Add_StartForthQuarter_MixedFiltersPrepro.get(i), rslt.getData("decomposition.b-tables.b4", TsData.class).get(i), 0.00001);
        }

        //   System.out.println("B9");
        for (int i = 0; i < 236; ++i) {
            //     System.out.println(DataCalendarSigmaX13.C_B9_Signif_Add_StartForthQuarter_MixedFiltersPrepro.getDomain().get(i) + " WinX13: " + DataCalendarSigmaX13.C_B9_Signif_Add_StartForthQuarter_MixedFiltersPrepro.get(i) + "; Calculated JD+: " + rslt.getData("b-tables.b9", TsData.class).getDomain().get(i) + " " + rslt.getData("b-tables.b9", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX13.C_B9_Signif_Add_StartForthQuarter_MixedFiltersPrepro.get(i), rslt.getData("decomposition.b-tables.b9", TsData.class).get(i), 0.00001);
        }

        //  System.out.println("D10");
        for (int i = 0; i < 238; ++i) {
            //  System.out.println(DataCalendarSigmaX13.C_D10_Signif_Add_StartForthQuarter_MixedFiltersPrepro.getDomain().get(i) + " WinX13: " + DataCalendarSigmaX13.C_D10_Signif_Add_StartForthQuarter_MixedFiltersPrepro.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d10", TsData.class).getDomain().get(i) + " " + rslt.getData("d-tables.d10", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX13.C_D10_Signif_Add_StartForthQuarter_MixedFiltersPrepro.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }
    }

    @Test
    public void TestCalendarsigmaSignifMultStartJan() {
     //  System.out.println("TestCalendarsigmaSignifMultStartJan");
        TsData TsInput;
        X11Kernel kernel;
        TsInput = DataCalendarSigmaX11.CStartJan;
        kernel = getX11Kernel(CalendarSigma.Signif, DecompositionMode.Multiplicative);
        X11Results rslt = kernel.process(TsInput);

        for (int i = 0; i < 238; ++i) {
            //System.out.println(DataCochran.C_D10_ignif.getDomain().get(i) + " WinX13: " + DataCochran.C_D10_All_Signif.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d10", TsData.class).getDomain().get(i) +" " +rslt.getData("d-tables.d10", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_D10_Signif_Mult.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }
    }

    @Test
    public void TestCalendarsigmaSelectMultStartJan() {
        TsData TsInput;
        X11Kernel kernel;
        TsInput = DataCalendarSigmaX11.CStartJan;
        SigmavecOption[] sigmavec;

        sigmavec = new SigmavecOption[12];
        sigmavec[0] = SigmavecOption.Group1;
        sigmavec[1] = SigmavecOption.Group1;
        sigmavec[2] = SigmavecOption.Group2;
        sigmavec[3] = SigmavecOption.Group2;
        sigmavec[4] = SigmavecOption.Group2;
        sigmavec[5] = SigmavecOption.Group2;
        sigmavec[6] = SigmavecOption.Group2;
        sigmavec[7] = SigmavecOption.Group2;
        sigmavec[8] = SigmavecOption.Group2;
        sigmavec[9] = SigmavecOption.Group2;
        sigmavec[10] = SigmavecOption.Group2;
        sigmavec[11] = SigmavecOption.Group1;
        kernel = getX11Kernel(CalendarSigma.Select, sigmavec, DecompositionMode.Multiplicative);
        X11Results rslt = kernel.process(TsInput);

        for (int i = 0; i < 238; ++i) {

            assertEquals(DataCalendarSigmaX11.C_D10_Select_Mult.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }
    }

    @Test
    public void TestCalendarsigmaSelectMultStartApril() {
        TsData TsInput;
        X11Kernel kernel;
        TsInput = DataCalendarSigmaX11.CStartAprl;
        SigmavecOption[] sigmavec;

        sigmavec = new SigmavecOption[12];
        sigmavec[0] = SigmavecOption.Group1;
        sigmavec[1] = SigmavecOption.Group1;
        sigmavec[2] = SigmavecOption.Group2;
        sigmavec[3] = SigmavecOption.Group2;
        sigmavec[4] = SigmavecOption.Group2;
        sigmavec[5] = SigmavecOption.Group2;
        sigmavec[6] = SigmavecOption.Group2;
        sigmavec[7] = SigmavecOption.Group2;
        sigmavec[8] = SigmavecOption.Group2;
        sigmavec[9] = SigmavecOption.Group2;
        sigmavec[10] = SigmavecOption.Group2;
        sigmavec[11] = SigmavecOption.Group1;
        kernel = getX11Kernel(CalendarSigma.Select, sigmavec, DecompositionMode.Multiplicative);
        X11Results rslt = kernel.process(TsInput);

        for (int i = 0; i < 238; ++i) {
            //       System.out.println(DataCalendarSigmaX11.C_D10_Select_Mult_StartApril.getDomain().get(i) + " WinX13: " + DataCalendarSigmaX11.C_D10_Select_Mult_StartApril.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d10", TsData.class).getDomain().get(i) + " " + rslt.getData("d-tables.d10", TsData.class).get(i));

            assertEquals(DataCalendarSigmaX11.C_D10_Select_Mult_StartApril.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }
    }

    @Test
    public void TestCalendarsigmaDefaultAddStartJan() {
        TsData TsInput, TsOutD10Default, TsOutB4Default, TsOutB9Default, TsOutD9Default;
        X11Kernel kernel;

        TsInput = DataCalendarSigmaX11.CStartJan;
        kernel = getX11Kernel(CalendarSigma.None, DecompositionMode.Additive);
        X11Results rslt = kernel.process(TsInput);

 //       System.out.println("Test Cochran Default:");
        //Solution Calculated with WinX13 Build 9
        //Test Replacement values in Step B
        TsOutB4Default = DataCalendarSigmaX11.C_B4_Default;
        //       System.out.println("Test Cochran Default: B9");
        for (int i = 0; i < 225; ++i) {
            assertEquals(TsOutB4Default.get(i), rslt.getData("b-tables.b4", TsData.class).get(i), 0.00001);
            // System.out.println("WinX13: " + TsOutB4Default.get(i) + " Calculated JD+: " + rslt.getData("b-tables.b4", TsData.class).round(round).get(i));
        }
        TsOutB9Default = DataCalendarSigmaX11.C_B9_Default;
        //       System.out.println("Test Cochran Default: B9");
        for (int i = 0; i < 225; ++i) {
            assertEquals(TsOutB9Default.get(i), rslt.getData("b-tables.b9", TsData.class).get(i), 0.00001);
            //  System.out.println("WinX13: " + TsOutB9Default.get(i) + " Calculated JD+: " + rslt.getData("b-tables.b9", TsData.class).get(i));
        }

        TsOutD9Default = DataCalendarSigmaX11.C_D9_Default;
        //     System.out.println("Test Cochran Default: D9");
        for (int i = 0; i < 238; ++i) {
            assertEquals(TsOutD9Default.get(i), rslt.getData("d-tables.d9", TsData.class).get(i), 0.00001);
            // System.out.println("WinX13: " + TsOutD9Default.get(i) + " Calculated JD+: " + rslt.getData("d-tables.d9", TsData.class).get(i));
        }
        //     System.out.println("Test Cochran Default: D10");
        TsOutD10Default = DataCalendarSigmaX11.C_D10_Default.round(round);
        assertEquals(rslt.getData("d-tables.d10", TsData.class).round(round), TsOutD10Default);

        for (int i = 0; i < 238; ++i) {
            assertEquals(DataCalendarSigmaX11.C_D10_Default.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
            //   System.out.println("WinX13: " + DataCochran.C_D10_Default.get(i) + " Calculated JD+: " + rslt.getData("d-tables.d10", TsData.class).get(i));
        }

    }

    @Test
    public void TestCalendarsigmaAllAddStartJan() {
        TsData TsInput, TsOutB4All;
        X11Kernel kernel;

        TsInput = DataCalendarSigmaX11.CStartJan;
        kernel = getX11Kernel(CalendarSigma.All, DecompositionMode.Additive);
        X11Results rslt = kernel.process(TsInput);

        // System.out.println("B3_ JD +");
        // System.out.println(rslt.getData("b-tables.b3", TsData.class));
        //  System.out.println("JD+: b4");
        //  System.out.println(rslt.getData("b-tables.b4", TsData.class));
//        System.out.println("Test Cochran All:");
        TsOutB4All = DataCalendarSigmaX11.C_B4_All;
        //  System.out.println("B4_gem Win X13");
        //  System.out.println(TsOutB4All);
        //B4
        for (int i = 0; i < 225; ++i) {
            //  System.out.println(TsOutB4All.getDomain().get(i) + " WinX13: " + TsOutB4All.get(i) + "; Calculated JD+: " + rslt.getData("b-tables.b4", TsData.class).get(i));
            assertEquals(TsOutB4All.get(i), rslt.getData("b-tables.b4", TsData.class).get(i), 0.00001);
        }

        // System.out.println("B5 JD+");
        //  System.out.println(rslt.getData("b-tables.b5", TsData.class));
        assertEquals(DataCalendarSigmaX11.C_B5_All.round(round), rslt.getData("b-tables.b5", TsData.class).round(round));
//B9  
        //System.out.println("B9 JD+");
//        for (int i = 0; i < 238; ++i) {
//            System.out.println(i + ": " + DataCochran.C_B9_All.getDomain().get(i) + " WinX13: " + DataCochran.C_B9_All.get(i) + "; Calculated JD+: " + rslt.getData("b-tables.b9", TsData.class).getDomain().get(i) + " " + rslt.getData("b-tables.b9", TsData.class).get(i));
//            assertEquals(DataCochran.C_B9_All.get(i), rslt.getData("b-tables.b9", TsData.class).get(i), 000001);
//        }
        assertEquals(DataCalendarSigmaX11.C_B9_All.round(round), rslt.getData("b-tables.b9", TsData.class).round(round));
//B10
        // System.out.println("B10 JD+");
        for (int i = 0; i < 238; ++i) {
            //  System.out.println(i + ": " + DataCochran.C_B10_All.getDomain().get(i) + " WinX13: " + DataCochran.C_B10_All.get(i) + "; Calculated JD+: " + rslt.getData("b-tables.b10", TsData.class).getDomain().get(i) + " " + rslt.getData("b-tables.b10", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_B10_All.get(i), rslt.getData("b-tables.b10", TsData.class).get(i), 0.00001);
        }
        //  assertEquals(DataCochran.C_B10_All.round(round),rslt.getData("b-tables.b10", TsData.class).round(round));  
//B17 
//        System.out.println("B17 JD+");
//        for (int i = 0; i < 238; ++i) {
//            System.out.println(i + ": " + DataCochran.C_B17_All.getDomain().get(i) + " WinX13: " + DataCochran.C_B17_All.get(i) + "; Calculated JD+: " + rslt.getData("b-tables.b17", TsData.class).getDomain().get(i) + " " + rslt.getData("b-tables.b17", TsData.class).get(i));
//            assertEquals(DataCochran.C_B17_All.get(i), rslt.getData("b-tables.b17", TsData.class).get(i), 0.00001);
//        }
        assertEquals(DataCalendarSigmaX11.C_B17_All.round(round), rslt.getData("b-tables.b17", TsData.class).round(round));

//B20
        //    System.out.println("Test Cochran All: B20");
        for (int i = 0; i < 238; ++i) {
            //   System.out.println(i + ": " + DataCochran.C_B20_All.getDomain().get(i) + " WinX13: " + DataCochran.C_B20_All.get(i) + "; Calculated JD+: " + rslt.getData("b-tables.b20", TsData.class).getDomain().get(i) + " " + rslt.getData("b-tables.b20", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_B20_All.get(i), rslt.getData("b-tables.b20", TsData.class).get(i), 0.00001);
        }
        //Christiane Hofer: Don't know why the following test resutls a failed
        //assertEquals(DataCochran.C_B20_All.round(round), rslt.getData("b-tables.b20", TsData.class).round(round));
//C17
        //     System.out.println("Test Cochran All: C17");
        for (int i = 0; i < 238; ++i) {
            //   System.out.println(i + ": " + DataCochran.C_C17_All.getDomain().get(i) + " WinX13: " + DataCochran.C_C17_All.get(i) + "; Calculated JD+: " + rslt.getData("c-tables.c17", TsData.class).getDomain().get(i) + " " + rslt.getData("c-tables.c17", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_C17_All.get(i), rslt.getData("c-tables.c17", TsData.class).get(i), 0.00001);
        }
//C20
        //     System.out.println("Test Cochran All: C20");
        for (int i = 0; i < 238; ++i) {
            //    System.out.println(i + ": " + DataCochran.C_C20_All.getDomain().get(i) + " WinX13: " + DataCochran.C_C20_All.get(i) + "; Calculated JD+: " + rslt.getData("c-tables.c20", TsData.class).getDomain().get(i) + " " + rslt.getData("c-tables.c20", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_C20_All.get(i), rslt.getData("c-tables.c20", TsData.class).get(i), 0.00001);
        }
        //Christiane Hofer: Don't know why the following test resutls a failed
        //assertEquals(DataCochran.C_C20_All.round(round), rslt.getData("b-tables.C20", TsData.class).round(round));

//D9
        //  System.out.println("Test Cochran All: D9");
        for (int i = 0; i < 237; ++i) {
            //    System.out.println(DataCochran.C_D9_All.getDomain().get(i) + " WinX13: " + DataCochran.C_D9_All.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d9", TsData.class).getDomain().get(i) + rslt.getData("d-tables.d9", TsData.class).get(i));

            assertEquals(DataCalendarSigmaX11.C_D9_All.get(i), rslt.getData("d-tables.d9", TsData.class).get(i), 0.00001);
        }
        //  assertEquals(DataCochran.C_D9_All.round(round),rslt.getData("d-tables.d9", TsData.class).round(round));
        //D10
        //   System.out.println("Test Cochran All: D10");
        for (int i = 0; i < 237; ++i) {
            //  System.out.println(DataCochran.C_D10_All.getDomain().get(i) + " WinX13: " + DataCochran.C_D10_All.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d10", TsData.class).getDomain().get(i) + rslt.getData("d-tables.d10", TsData.class).get(i));

            assertEquals(DataCalendarSigmaX11.C_D10_All.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }

    }

    /**
     * Test for Calendarsigma Signif with Cochran Result false, which means;
     * that the PeriodSpecificExtremeValuesCorrector ist used with the default
     * replacement in D9
     */
    @Test
    public void TestCalendarsigmaSignifAddStartJan() {
        //   System.out.println("TestCalendarsigmaSignifAddStartJan");
        TsData TsInput;
        X11Kernel kernel;

        TsInput = DataCalendarSigmaX11.CStartJan;
        kernel = getX11Kernel(CalendarSigma.Signif, DecompositionMode.Additive);
        X11Results rslt = kernel.process(TsInput);

//B4
        //    System.out.println("Test Cochran Signif: B4");
        for (int i = 0; i < 225; ++i) {
            assertEquals(DataCalendarSigmaX11.C_B4_SignifFalse.get(i), rslt.getData("b-tables.b4", TsData.class).get(i), 0.00001);
        }

//B9
        //      System.out.println("Test Cochran Signif: B9");
        for (int i = 0; i < 237; ++i) {
            assertEquals(DataCalendarSigmaX11.C_B9_SignifFalse.get(i), rslt.getData("b-tables.b9", TsData.class).get(i), 0.00001);
        }

//B10
        //   System.out.println("Test Cochran Signif: B10");
        for (int i = 0; i < 237; ++i) {
            assertEquals(DataCalendarSigmaX11.C_B10_SignifFalse.get(i), rslt.getData("b-tables.b10", TsData.class).get(i), 0.00001);
        }
//C17
        //    System.out.println("Test Cochran Signif: C17");
        for (int i = 0; i < 237; ++i) {
            assertEquals(DataCalendarSigmaX11.C_C17_SignifFalse.get(i), rslt.getData("c-tables.c17", TsData.class).get(i), 0.00001);
        }
//C20
        //  System.out.println("Test Cochran Signif: C20");
        for (int i = 0; i < 237; ++i) {
            assertEquals(DataCalendarSigmaX11.C_C20_SignifFalse.get(i), rslt.getData("c-tables.c20", TsData.class).get(i), 0.00001);
        }

//D9
        //   System.out.println("Test Cochran Signif: D9");
        for (int i = 0; i < 237; ++i) {
            //  System.out.println(DataCochran.C_D9_SignifFalse.getDomain().get(i) + " WinX13: " + DataCochran.C_D9_SignifFalse.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d9", TsData.class).getDomain().get(i) + rslt.getData("d-tables.d9", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_D9_SignifFalse.get(i), rslt.getData("d-tables.d9", TsData.class).get(i), 0.00001);
        }
//D10
        // System.out.println("Test Cochran Signif: D10");
        for (int i = 0; i < 237; ++i) {
            // System.out.println(DataCochran.C_D10_SignifFalse.getDomain().get(i) + " WinX13: " + DataCochran.C_D10_SignifFalse.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d10", TsData.class).getDomain().get(i) + rslt.getData("d-tables.d10", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_D10_SignifFalse.get(i), rslt.getData("d-tables.d10", TsData.class).get(i), 0.00001);
        }

    }

    /**
     * Test for Calendarsigma Signif with Cochran Result true, which means; the
     * DefaultExtremeValuesCorrector is used with the same procedure as in the
     * Calendarsigme.none
     */
    @Test
    public void TestCalendarsigmaSignifTrueStartJan() {
        //ToDo CH: FindTestData which passes the Cochrantest for B4d or B9d and not for both
    }

    @Test
    public void TestCalendarsigmaSelectAddStartJan() {

        TsData TsInput;
        X11Kernel kernel;
        SigmavecOption[] sigmavec;

        sigmavec = new SigmavecOption[12];
        sigmavec[0] = SigmavecOption.Group1;
        sigmavec[1] = SigmavecOption.Group1;
        sigmavec[2] = SigmavecOption.Group2;
        sigmavec[3] = SigmavecOption.Group2;
        sigmavec[4] = SigmavecOption.Group2;
        sigmavec[5] = SigmavecOption.Group2;
        sigmavec[6] = SigmavecOption.Group2;
        sigmavec[7] = SigmavecOption.Group2;
        sigmavec[8] = SigmavecOption.Group2;
        sigmavec[9] = SigmavecOption.Group2;
        sigmavec[10] = SigmavecOption.Group2;
        sigmavec[11] = SigmavecOption.Group1;

        TsInput = DataCalendarSigmaX11.CStartJan;
        kernel = getX11Kernel(CalendarSigma.Select, sigmavec, DecompositionMode.Additive);
        X11Results rslt = kernel.process(TsInput);

        //   System.out.println("Test Cochran Select: B3");
        for (int i = 0; i < 225; ++i) {
            //   System.out.println(DataCochran.C_B3_Select.getDomain().get(i) + " WinX13: " + DataCochran.C_B3_Select.get(i) + "; Calculated JD+: " + rslt.getData("b-tables.b3", TsData.class).getDomain().get(i) +" " +rslt.getData("b-tables.b3", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_B3_Select.get(i), rslt.getData("b-tables.b3", TsData.class).get(i), 0.00001);
        }

        //  System.out.println("Test Cochran Select: B4");
        for (int i = 0; i < 225; ++i) {
            //  System.out.println(DataCalendarSigmaX11.C_B4_Select.getDomain().get(i) + " WinX13: " + DataCalendarSigmaX11.C_B4_Select.get(i) + "; Calculated JD+: " + rslt.getData("b-tables.b4", TsData.class).getDomain().get(i) + " " + rslt.getData("b-tables.b4", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_B4_Select.get(i), rslt.getData("b-tables.b4", TsData.class).get(i), 0.00001);
        }
        //  System.out.println("Test Cochran Select: D9");
        for (int i = 0; i < 237; ++i) {
            //  System.out.println(DataCalendarSigmaX11.C_D9_Select.getDomain().get(i) + " WinX13: " + DataCalendarSigmaX11.C_D9_Select.get(i) + "; Calculated JD+: " + rslt.getData("d-tables.d9", TsData.class).getDomain().get(i) + " " + rslt.getData("d-tables.d9", TsData.class).get(i));
            assertEquals(DataCalendarSigmaX11.C_D9_Select.get(i), rslt.getData("d-tables.d9", TsData.class).get(i), 0.00001);
        }
    }

    private X11Kernel getX11Kernel(CalendarSigma calendarsigma, DecompositionMode mode) {
        SigmavecOption[] sigmavec;
        sigmavec = new SigmavecOption[1];
        return getX11Kernel(calendarsigma, sigmavec, mode);
    }

    private X11Kernel getX11Kernel(CalendarSigma calendarsigma, SigmavecOption[] sigmavec, DecompositionMode mode) {

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
        return getX11Kernel(calendarsigma, sigmavec, mode, filters);
    }

    private X11Kernel getX11Kernel(CalendarSigma calendarsigma, SigmavecOption[] sigmavec, DecompositionMode mode, SeasonalFilterOption[] filters) {

        X11Toolkit toolkit = X11Toolkit.create(getX11Spec(calendarsigma, sigmavec, mode, filters));
        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        return kernel;
    }

    private X11Specification getX11Spec(CalendarSigma calendarsigma, SigmavecOption[] sigmavec, DecompositionMode mode, SeasonalFilterOption[] filters) {

        X11Specification spec = new X11Specification();

        spec.setCalendarSigma(calendarsigma);
        if (calendarsigma.equals(CalendarSigma.Select)) {
            spec.setSigmavec(sigmavec);
        }

        spec.setSigma(1.5, 2.5);
        spec.setHendersonFilterLength(17);
        spec.setMode(mode);
        spec.setForecastHorizon(0);
        spec.setSeasonal(true);
        spec.setSeasonalFilters(filters);
        return spec;
    }

}
