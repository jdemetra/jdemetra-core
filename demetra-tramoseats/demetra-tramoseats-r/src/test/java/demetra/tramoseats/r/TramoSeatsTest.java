/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.r;

import demetra.data.Data;
import demetra.data.Parameter;
import demetra.arima.SarimaSpec;
import demetra.tramo.TramoSpec;
import demetra.tramoseats.TramoSeatsSpec;
import demetra.tramoseats.io.protobuf.TramoSeatsOutput;
import jdplus.tramoseats.TramoSeatsResults;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class TramoSeatsTest {

    public TramoSeatsTest() {
    }

    @Test
    public void testProd() {
        TramoSeatsResults rslt = TramoSeats.process(Data.TS_PROD, "rsafull");
        assertTrue(rslt != null);
        assertTrue(TramoSeats.toBuffer(rslt) != null);
    }

    @Test
    public void testRefresh() {
        TramoSeatsOutput rslt = TramoSeats.fullProcess(Data.TS_PROD, "RSA0");

        TramoSeatsSpec fspec = TramoSeats.refreshSpec(rslt.getResultSpec(), TramoSeatsSpec.RSA0, null, "Fixed");

        SarimaSpec arima = fspec.getTramo().getArima();
        SarimaSpec.Builder sbuilder = arima.toBuilder();

        SarimaSpec narima = sbuilder.btheta(new Parameter[]{Parameter.fixed(arima.getBtheta()[0].getValue() + 0.01)})
                .build();
        TramoSpec tr = fspec.getTramo().toBuilder()
                .arima(narima)
                .build();

        TramoSeatsSpec nspec = fspec.toBuilder()
                .tramo(tr)
                .build();

        TramoSeatsOutput nrslt = TramoSeats.fullProcess(Data.TS_PROD, nspec, null);
//        System.out.println(rslt.getResult().getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
//        System.out.println(nrslt.getResult().getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
    }

}
