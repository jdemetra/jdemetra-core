/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tempdisagg.univariate;

import jdplus.tempdisagg.univariate.DisaggregationModel;
import jdplus.tempdisagg.univariate.DisaggregationModelBuilder;
import demetra.data.AggregationType;
import demetra.modelling.regression.Constant;
import demetra.modelling.regression.UserVariable;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DisaggregationModelBuilderTest {
    
    private static final TsData Y, Q;
    
    static {
        Random rnd = new Random(0);
        double[] y = new double[4];
        for (int i = 0; i < y.length; ++i) {
            y[i] = i + rnd.nextDouble();
        }
        Y = TsData.ofInternal(TsPeriod.yearly(2000), y);
        
        double[] q = new double[32];
        for (int i = 0; i < q.length; ++i) {
            q[i] = i + rnd.nextDouble();
        }
        Q = TsData.ofInternal(TsPeriod.quarterly(1998, 1), q);
        
    }
    
    public DisaggregationModelBuilderTest() {
    }
    
    @Test
    public void testFull() {
        
        DisaggregationModel model = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.Sum)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 16))
                .addX(new Constant())
                .addX(new UserVariable("q", Q))
                .build();
        assertTrue(model.getHEDom().length() == 16);
        model = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.Last)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 16))
                .addX(new Constant())
                .addX(new UserVariable("q", Q))
                .build();
        assertTrue(model.getHEDom().length() == 13);
        model = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.First)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 16))
                .addX(new Constant())
                .addX(new UserVariable("q", Q))
                .build();
        assertTrue(model.getHEDom().length() == 13);
        model = new DisaggregationModelBuilder(Y)
                .observationPosition(2)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 16))
                .addX(new Constant())
                .addX(new UserVariable("q", Q))
                .build();
        assertTrue(model.getHEDom().length() == 13);
    }
    
    @Test
    public void testSum() {
        DisaggregationModelBuilder builder = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.Sum)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 15))
                .addX(new Constant())
                .addX(new UserVariable("q", Q));
        DisaggregationModel model = builder.build();
        assertTrue(model.getHEDom().length() == 12);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 2), 15)).build();
        assertTrue(model.getHEDom().length() == 12);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 14)).build();
        assertTrue(model.getHEDom().length() == 12);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 14)).build();
        assertTrue(model.getHEDom().length() == 12);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 2), 14)).build();
        assertTrue(model.getHEDom().length() == 8);
    }

    @Test
    public void testFirst() {
        DisaggregationModelBuilder builder = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.First)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 15))
                .addX(new Constant())
                .addX(new UserVariable("q", Q));
        DisaggregationModel model = builder.build();
        assertTrue(model.getHEDom().length() == 13);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 2), 15)).build();
        assertTrue(model.getHEDom().length() == 9);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 14)).build();
        assertTrue(model.getHEDom().length() == 9);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 13)).build();
        assertTrue(model.getHEDom().length() == 13);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 12)).build();
        assertTrue(model.getHEDom().length() == 9);
    }

    @Test
    public void testLast() {
        DisaggregationModelBuilder builder = new DisaggregationModelBuilder(Y)
                .aggregationType(AggregationType.Last)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 15))
                .addX(new Constant())
                .addX(new UserVariable("q", Q));
        DisaggregationModel model = builder.build();
        assertTrue(model.getHEDom().length() == 9);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 2), 15)).build();
        assertTrue(model.getHEDom().length() == 13);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 14)).build();
        assertTrue(model.getHEDom().length() == 13);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 12)).build();
        assertTrue(model.getHEDom().length() == 9);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 12)).build();
        assertTrue(model.getHEDom().length() == 9);
    }

    @Test
    public void testUser() {
        DisaggregationModelBuilder builder = new DisaggregationModelBuilder(Y)
                .observationPosition(2)
                .disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 1), 15))
                .addX(new Constant())
                .addX(new UserVariable("q", Q));
        DisaggregationModel model = builder.build();
        assertTrue(model.getHEDom().length() == 13);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 2), 15)).build();
        assertTrue(model.getHEDom().length() == 13);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 14)).build();
        assertTrue(model.getHEDom().length() == 9);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 12)).build();
        assertTrue(model.getHEDom().length() == 9);
        model = builder.disaggregationDomain(TsDomain.of(TsPeriod.quarterly(2000, 3), 11)).build();
        assertTrue(model.getHEDom().length() == 5);
    }
}
