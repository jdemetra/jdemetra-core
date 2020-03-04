/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.legacy;

import demetra.arima.SarimaModel;
import demetra.data.Data;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.modelling.LinearModelEstimation;
import demetra.timeseries.regression.modelling.ModellingContext;
import demetra.timeseries.regression.modelling.RegSarimaResults;
import demetra.tramo.Tramo;
import demetra.tramo.TramoSpec;
import org.junit.Test;
import static org.junit.Assert.*;

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
        RegSarimaResults rslt = Tramo.process(s, TramoSpec.TRfull, ModellingContext.getActiveContext(), null);
        RegSarimaResults lrslt = Tramo.processLegacy(s, TramoSpec.TRfull, ModellingContext.getActiveContext(), null);
//        System.out.println("New");
//        System.out.println(rslt.getStatistics());
//        System.out.println("Legacy");
//        System.out.println(lrslt.getStatistics());
    }

    public static void main(String[] arg) {
        TsData s = Data.TS_PROD;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 200; ++i) {
            RegSarimaResults lrslt = Tramo.processLegacy(s, TramoSpec.TRfull, ModellingContext.getActiveContext(), null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Legacy");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 200; ++i) {
            RegSarimaResults rslt = Tramo.process(s, TramoSpec.TRfull, ModellingContext.getActiveContext(), null);
        }
        t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);

    }

}
