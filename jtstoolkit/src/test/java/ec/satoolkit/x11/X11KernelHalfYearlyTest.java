/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x11;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Assert;
import org.junit.Test;
import utilities.CompareTsData;

/**
 *
 * @author s4504ch
 */
public class X11KernelHalfYearlyTest {

    @Test
    public void HKAS_LogAddTest() {
        TsData ts = DataHalfYearly.HKAS;
        X13Specification x13spec = SpecHalfYearly.getSpecHKAS();
        x13spec.getX11Specification().setMode(DecompositionMode.LogAdditive);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ts);
        double d11[]
                = {37536183.7954008, 42779881.9117029, 40686045.3618075, 26723066.3739840, 20086407.9803598, 26814707.4320941, 36724099.5454980, 43118413.4284516, 39749651.5887306, 36046115.8438464, 40301161.1922987, 37519880.9226034, 33810882.2677716, 39437242.4179417, 36355886.1999183, 44201380.7691870, 37321004.2952891, 25895079.9648566, 32912595.9509640, 33280252.7492370, 46621305.5429592, 52181499.1188171};
        TsData tsD11 = new TsData(TsFrequency.HalfYearly, 2007, 0, d11, true);
        Assert.assertTrue("D11 is wrong", CompareTsData.compareTS(tsD11, comprest.getData("d-tables.d11", TsData.class), 0.0000001));

    }

    @Test
    public void HKASTest() {
        TsData ts = DataHalfYearly.HKAS;
        X13Specification x13spec = SpecHalfYearly.getSpecHKAS();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ts);
        double d11[]
                = {37536183.7954008, 42779881.9117029, 40686045.3618075, 26723066.3739840, 20086407.9803598, 26814707.4320941, 36724099.5454980, 43118413.4284516, 39749651.5887306, 36046115.8438464, 40301161.1922987, 37519880.9226034, 33810882.2677716, 39437242.4179417, 36355886.1999183, 44201380.7691870, 37321004.2952891, 25895079.9648566, 32912595.9509640, 33280252.7492370, 46621305.5429592, 52181499.1188171};
        TsData tsD11 = new TsData(TsFrequency.HalfYearly, 2007, 0, d11, true);
        Assert.assertTrue("D11 is wrong", CompareTsData.compareTS(tsD11, comprest.getData("d-tables.d11", TsData.class), 0.0000001));

    }

    @Test
    public void HKASTest_AutoHenderson() {
        TsData ts = DataHalfYearly.HKAS;
        X13Specification x13spec_auto = SpecHalfYearly.getSpecHKAS();
        x13spec_auto.getX11Specification().setHendersonFilterLength(0);//auto
        SequentialProcessing<TsData> processing_auto = X13ProcessingFactory.instance.generateProcessing(x13spec_auto);
        CompositeResults comprest_auto = processing_auto.process(ts);

        X13Specification x13spec_5 = SpecHalfYearly.getSpecHKAS();
        x13spec_auto.getX11Specification().setHendersonFilterLength(5);
        SequentialProcessing<TsData> processing_5 = X13ProcessingFactory.instance.generateProcessing(x13spec_5);
        CompositeResults comprest_5 = processing_5.process(ts);

        //  Assert.assertTrue("D11 is wrong", CompareTsData.compareTS(comprest_5.getData("d-tables.d11", TsData.class), comprest_auto.getData("d-tables.d11", TsData.class), 0.0000001));
    Assert.assertArrayEquals("B6 is wrong", comprest_5.getData("b-tables.b6", TsData.class).internalStorage(), comprest_auto.getData("b-tables.b6", TsData.class).internalStorage(), 0.00000001);
        Assert.assertArrayEquals("B7 is wrong", comprest_5.getData("b-tables.b7", TsData.class).internalStorage(), comprest_auto.getData("b-tables.b7", TsData.class).internalStorage(), 0.00000001);

        Assert.assertArrayEquals("D11 is wrong", comprest_5.getData("d-tables.d11", TsData.class).internalStorage(), comprest_auto.getData("d-tables.d11", TsData.class).internalStorage(), 0.00000001);
    }

    @Test
    public void AggTest() {

        TsData ts = DataHalfYearly.AGG;

        X13Specification x13spec = SpecHalfYearly.getSpecAgg();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ts);
        TsData tableOutTs = comprest.getData("d-tables.d11", TsData.class);

        double d11[]
                = {46.5935351883392, 45.0919664924581, 47.2290219986764, 48.1532888133294, 46.0427548352608, 48.2453095543133, 44.8339481031963, 51.8781756861151, 55.7915502943748, 49.3259379339381, 52.7221650154085, 53.1575130746254, 57.9074013836036, 55.5153747282522, 56.0847635187373, 60.0595960735541, 61.8252332223114, 66.9061802213434, 59.6581398325253, 59.7277466959123, 64.4333001008445, 61.5768624968095, 57.2381105576217, 56.1047906646767, 55.4265103112786, 55.1646620517093, 54.9311618085935, 53.7914395429403, 59.1795680645073, 61.5338669776972, 58.1090557212855, 65.9530007719837, 71.7903572000391, 65.3890617478839, 67.0721383940438, 76.6133154621727, 82.6661141582958, 88.8013353447216, 86.5650254891379, 84.7728902391236, 89.7274417709278, 90.0111379636146, 78.8889495576047, 100.5166984647670, 91.7843394473499, 91.2317226973823, 100.8040691009420, 93.9639019256649, 96.2650807512888, 103.1464051002890, 104.4479979927330, 104.0997958217960, 106.2997648026070, 112.2049970085440};
        System.out.println("d11");
        System.out.println(tableOutTs);
        TsData tsD11 = new TsData(TsFrequency.HalfYearly, 1991, 0, d11, true);

        Assert.assertTrue("D11 is wrong", CompareTsData.compareTS(tsD11, comprest.getData("d-tables.d11", TsData.class), 0.0000001));

    }

    @Test
    public void SimTest() {
        TsData ts = DataHalfYearly.SIM;
        X13Specification x13spec = SpecHalfYearly.getSpecSim();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ts);
        double d11[]
                = {102.418235262785, 104.434233410031, 104.489350045337, 104.728606383718, 105.767787781619, 106.906110281209, 107.862346186463, 107.204588883701, 109.042314057154, 110.365358336957, 111.034946147705, 110.747268565381, 110.563532511607, 110.457498363818, 111.655170328755, 111.836162385412, 111.903494552408, 113.581750734036, 112.929381722929, 114.416744738605, 113.769339641149, 114.220515308544, 116.047861668630, 114.284333839000, 115.073144165086, 116.001412835664, 115.380114888930, 114.864138699352, 117.030476628879, 116.961675257069, 115.954454870212, 117.966313347010, 117.118741800144, 117.948625409704, 119.145902502514, 116.964206611934, 118.905351118360, 120.041374721043, 119.652510760207, 121.707133870907, 119.654882998748, 121.098136326366, 122.417108217349, 121.485661093116, 121.995744732864, 121.274427157329, 122.780516787801, 122.900340265002, 122.748506968282, 122.747290080596, 123.102967717581, 123.187814222927, 122.489346316576, 123.149106224103, 123.796190323133, 123.038493464875, 123.888030634392, 125.687799778083, 123.276421994153, 122.654422769832, 125.289577091190, 125.459512917902, 125.155690232088, 125.995558661931, 124.854353600803, 124.852288122425, 125.061510243109, 125.224542305241, 125.708915991565, 125.987356628103, 126.810944185772, 127.279803536342, 126.591558390038, 126.207997259931, 127.267534943740, 126.905982825716, 128.009357465009, 128.433622087297, 127.688470088778, 129.711175454443, 129.552029375970, 130.008887175818, 130.182604581761, 129.721419677823, 132.124819894279, 130.911692338162, 130.192838305001, 129.918882655354, 130.020301771036, 132.333364494758, 132.368752004876, 134.230578532447, 135.359878563768, 134.678472432013, 135.303095299937, 136.211141260839, 136.281675666438, 136.420428326220, 137.502404210387, 137.762197813626};
        TsData tsD11 = new TsData(TsFrequency.HalfYearly, 2000, 0, d11, true);
        Assert.assertTrue("D11 is wrong", CompareTsData.compareTS(tsD11, comprest.getData("d-tables.d11", TsData.class), 0.0000001));

    }
}
