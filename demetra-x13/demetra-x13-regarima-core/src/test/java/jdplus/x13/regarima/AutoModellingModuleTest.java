/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.regarima;

import demetra.arima.SarimaOrders;
import demetra.data.Data;
import demetra.timeseries.TsData;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.sarima.SarimaSpecification;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModelling;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class AutoModellingModuleTest {
    
    public AutoModellingModuleTest() {
    }

    @Test
    public void testSomeMethod() {
    }
    
    @Test
    public void testInsee() {
        long t0 = System.currentTimeMillis();
        TsData[] insee = Data.insee();
        for (int i = 0; i < insee.length; ++i) {
            AutoModellingModule ami = new AutoModellingModule(
                    DifferencingModule.builder().build(), ArmaModule.builder().build());
            ModelDescription model = new ModelDescription(insee[i], null);
            model.setAirline(true);
            RegSarimaModelling m = RegSarimaModelling.of(model);
            ami.process(m);
            ec.tstoolkit.modelling.arima.x13.AutoModel oami = new ec.tstoolkit.modelling.arima.x13.AutoModel();
            ec.tstoolkit.timeseries.simplets.TsData s = convert(insee[i]);
            ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
            ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();
            desc.setAirline(true);
            context.description = desc;
            context.hasseas = true;
            oami.process(context);
            
            SarimaOrders spec=m.getDescription().specification();
            SarimaSpecification ospec = context.description.getSpecification();
            assertTrue(spec.getP()==ospec.getP());
            assertTrue(spec.getD()==ospec.getD());
            assertTrue(spec.getQ()==ospec.getQ());
            assertTrue(spec.getBp()==ospec.getBP());
            assertTrue(spec.getBd()==ospec.getBD());
            assertTrue(spec.getBq()==ospec.getBQ());
            assertTrue(m.getDescription().isMean()==context.description.isMean());
        }
         long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    public void testInseeLog() {
        long t0 = System.currentTimeMillis();
        TsData[] insee = Data.insee();
        for (int i = 0; i < insee.length; ++i) {
            TsData cur=insee[i].log();
            AutoModellingModule ami = new AutoModellingModule(
                    DifferencingModule.builder().build(), ArmaModule.builder().build());
            ModelDescription model = new ModelDescription(insee[i], null);
            model.setAirline(true);
            model.setLogTransformation(true);
            RegSarimaModelling m = RegSarimaModelling.of(model);
            ami.process(m);
            ec.tstoolkit.modelling.arima.x13.AutoModel oami = new ec.tstoolkit.modelling.arima.x13.AutoModel();
            ec.tstoolkit.timeseries.simplets.TsData s = convert(insee[i]);
            ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
            ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();
            desc.setAirline(true);
            desc.setTransformation(DefaultTransformationType.Log);
            context.description = desc;
            context.hasseas = true;
            oami.process(context);
            
            SarimaOrders spec=m.getDescription().specification();
            SarimaSpecification ospec = context.description.getSpecification();
            assertTrue(spec.getP()==ospec.getP());
            assertTrue(spec.getD()==ospec.getD());
            assertTrue(spec.getQ()==ospec.getQ());
            assertTrue(spec.getBp()==ospec.getBP());
            assertTrue(spec.getBd()==ospec.getBD());
            assertTrue(spec.getBq()==ospec.getBQ());
            assertTrue(m.getDescription().isMean()==context.description.isMean());
        }
         long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    private static ec.tstoolkit.timeseries.simplets.TsData convert(TsData s) {
        int period = s.getAnnualFrequency();
        int year = s.getStart().year(), pos = s.getStart().annualPosition();
        return new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(period),
                year, pos, s.getValues().toArray(), false);
    }
}
