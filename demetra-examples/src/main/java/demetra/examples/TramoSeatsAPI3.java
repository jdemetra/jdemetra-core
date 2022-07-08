/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.examples;

import com.google.common.primitives.Doubles;
import demetra.data.Data;
import demetra.information.Explorable;
import demetra.sa.EstimationPolicyType;
import demetra.sa.SaEstimation;
import demetra.sa.SaItem;
import demetra.timeseries.Ts;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import demetra.timeseries.TsPeriod;
import demetra.tramoseats.TramoSeatsSpec;
import java.util.Arrays;
import jdplus.sarima.SarimaModel;
import jdplus.tramoseats.TramoSeatsFactory;
import jdplus.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.TramoSeatsResults;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoSeatsAPI3 {

    public Ts createTs() {
        // Be aware that the first month/period is 1 (as in the LocalDate methods)
        TsData data = TsData.ofInternal(TsPeriod.monthly(1967, 1), Data.PROD);
        return Ts.of("test", data);
    }

    public SaItem createItem(Ts s) {
        // No need to clone: immutable objects
        // No need to register TramoSeats: automatically done through dynamic loading
        return SaItem.of(s, TramoSeatsSpec.RSAfull);
    }

    public void main(String[] args) {

        // time series
        Ts s = createTs();

        // First solution
        SaItem item = createItem(s);

        item.process(null, false);
        SaEstimation estimation = item.getEstimation();

        Explorable rslt = estimation.getResults();
        TsData sa = rslt.getData("sa", TsData.class);

        System.out.println(sa);

        if (rslt instanceof TramoSeatsResults fullRslt) {
            SarimaModel arima = fullRslt.getPreprocessing().arima();
            System.out.println(arima);
        }

        // Other solution, more flexible, with less overhead
        TramoSeatsSpec dspec=TramoSeatsSpec.RSAfull;
        TsData data = TsData.ofInternal(TsPeriod.monthly(1967, 1), Data.PROD);
        TramoSeatsResults rslt1 = TramoSeatsKernel.of(dspec, null).process(data, null);

        TsData sa1 = rslt1.getData("sa", TsData.class);
        SarimaModel arima = rslt1.getPreprocessing().arima();
        System.out.println(arima);
        
        // Generate the spec corresponding to the results
        TramoSeatsSpec pspec = TramoSeatsFactory.INSTANCE.generateSpec(dspec, rslt1);
        // Refresh the specification
        TramoSeatsSpec nspec = TramoSeatsFactory.INSTANCE.refreshSpec(pspec, dspec, EstimationPolicyType.FreeParameters, null);
        // Re-execute with new figures (fixed model, except ARIMA parameters)
        double[] xprod = Doubles.concat(Data.PROD, new double[]{125, 130, 73});
        TsData ndata = TsData.ofInternal(TsPeriod.monthly(1967, 1), xprod);
        
        TramoSeatsResults rslt2 = TramoSeatsKernel.of(nspec, null).process(ndata, null);
       
        TsData sa2= rslt2.getData("sa", TsData.class);
        arima = rslt2.getPreprocessing().arima();
        System.out.println(arima);
       
        // same with outliers detection at the end
        TramoSeatsSpec nspec1 = TramoSeatsFactory.INSTANCE.refreshSpec(pspec, dspec, EstimationPolicyType.LastOutliers, null);
        
        TramoSeatsResults rslt3 = TramoSeatsKernel.of(nspec1, null).process(ndata, null);
       
        TsData sa3 = rslt3.getData("sa", TsData.class);
        arima = rslt3.getPreprocessing().arima();
        System.out.println(arima);
        
        // or (old domain is frozen)
        TramoSeatsSpec nspec2 = TramoSeatsFactory.INSTANCE.refreshSpec(pspec, dspec, EstimationPolicyType.Outliers, data.getDomain());
        
        TramoSeatsResults rslt4 = TramoSeatsKernel.of(nspec2, null).process(ndata, null);
       
        TsData sa4 = rslt4.getData("sa", TsData.class);
        arima = rslt4.getPreprocessing().arima();
        System.out.println(arima);
        
        TsData[] all=new TsData[]{sa,sa1, sa2, sa3, sa4};
        TsDataTable table=TsDataTable.of(Arrays.asList(all));
        System.out.println(table);
    }

}
