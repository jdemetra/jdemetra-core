/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.legacy;

import demetra.data.Data;
import demetra.modelling.implementations.SarimaSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.tramo.Tramo;
import demetra.tramo.TramoSpec;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class LegacyUtilityTest {

    static {
        Tramo.setLegacyEngine(new LegacyTramo());
    }

    public LegacyUtilityTest() {
    }

    @Test
    public void testFull() {
        TsData s = Data.TS_ABS_RETAIL;
        GeneralLinearModel<SarimaSpec> rslt = Tramo.process(s, TramoSpec.TRfull, ModellingContext.getActiveContext(), null);
        GeneralLinearModel<SarimaSpec> lrslt = Tramo.processLegacy(s, TramoSpec.TRfull, ModellingContext.getActiveContext(), null);
//        System.out.println("New");
//        System.out.println(rslt.getStatistics());
//        System.out.println("Legacy");
//        System.out.println(lrslt.getStatistics());
    }

    public static void main(String[] arg) {
        TsData s = Data.TS_PROD;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 500; ++i) {
            GeneralLinearModel<SarimaSpec> lrslt = Tramo.processLegacy(s, TramoSpec.TRfull, ModellingContext.getActiveContext(), null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Legacy");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 500; ++i) {
            GeneralLinearModel<SarimaSpec> rslt = Tramo.process(s, TramoSpec.TRfull, ModellingContext.getActiveContext(), null);
        }
        t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);

    }

}
