/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tss.sa.revisions;

import data.Data;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tss.sa.SaManager;
import ec.tss.sa.processors.TramoSeatsProcessor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Map;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class RevisionAnalysisProcessorTest {

    static {
        SaManager.instance.add(new TramoSeatsProcessor());
    }

    public RevisionAnalysisProcessorTest() {
    }

//   @Test
    public void demo() {
        RevisionAnalysisProcessor proc = new RevisionAnalysisProcessor();
        RevisionAnalysisSpec spec = new RevisionAnalysisSpec();
        TramoSeatsSpecification trs = TramoSeatsSpecification.RSA5.clone();
        trs.getTramoSpecification().getTransform().setFunction(DefaultTransformationType.Log);
        spec.setSaSpecification(trs);
        spec.setOutOfSample(true);
        spec.setTargetFinal(false);
        TsCollection coll = TsFactory.instance.createTsCollection();
        coll.quietAdd(TsFactory.instance.createTs("P", null, Data.P));
        coll.quietAdd(TsFactory.instance.createTs("M1", null, Data.M1));
        coll.quietAdd(TsFactory.instance.createTs("M2", null, Data.M2));
        coll.quietAdd(TsFactory.instance.createTs("M3", null, Data.M3));
        IProcessing<TsCollection, CompositeResults> p = proc.generateProcessing(spec, null);
        CompositeResults rslt = p.process(coll);

        System.out.println(rslt.getData("batch.series1.vintage0.sa", TsData.class));
        Map<String, Class> dictionary = rslt.getDictionary();
        for (String s : dictionary.keySet()) {
            System.out.println(s);
        }
        double[] max = rslt.getData("summary.samax", double[].class);
        double[] min = rslt.getData("summary.samin", double[].class);
        double[] stdev = rslt.getData("summary.sastdev", double[].class);
        double[] cmax = rslt.getData("summary.cmax", double[].class);
        double[] cmin = rslt.getData("summary.cmin", double[].class);
        double[] cstdev = rslt.getData("summary.cstdev", double[].class);
        double[] smax = rslt.getData("summary.smax", double[].class);
        double[] smin = rslt.getData("summary.smin", double[].class);
        double[] sstdev = rslt.getData("summary.sstdev", double[].class);

        System.out.print('\t');
        System.out.print("SA (stdev)");
        System.out.print('\t');
        System.out.print("SA (min)");
        System.out.print('\t');
        System.out.print("SA (max)");
        System.out.print('\t');
        System.out.print("S (stdev)");
        System.out.print('\t');
        System.out.print("S (min)");
        System.out.print('\t');
        System.out.print("S (max)");
        System.out.print('\t');
        System.out.print("Cal (stdev)");
        System.out.print('\t');
        System.out.print("Cal (min)");
        System.out.print('\t');
        System.out.println("Cal (max)");
        for (int i = 0; i < max.length; ++i) {
            System.out.print(i + 1);
            System.out.print('\t');
            System.out.print(stdev[i]);
            System.out.print('\t');
            System.out.print(min[i]);
            System.out.print('\t');
            System.out.print(max[i]);
            System.out.print('\t');
            System.out.print(sstdev[i]);
            System.out.print('\t');
            System.out.print(smin[i]);
            System.out.print('\t');
            System.out.print(smax[i]);
            System.out.print('\t');
            System.out.print(cstdev[i]);
            System.out.print('\t');
            System.out.print(cmin[i]);
            System.out.print('\t');
            System.out.println(cmax[i]);
        }

    }

}
