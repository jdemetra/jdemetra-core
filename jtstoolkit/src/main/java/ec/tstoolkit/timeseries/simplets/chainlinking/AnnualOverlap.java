/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.timeseries.simplets.chainlinking;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.timeseries.simplets.YearIterator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mats Maggi
 */
public class AnnualOverlap extends AChainLinking {

    public static final String TOTAL_VALUES = "totalvalues";
    public static final String VALUES_PREVIOUS_PRICE = "valuesPreviousYearPrice";
    public static final String CHAIN_LINKED_INDEXES = "percPreviousYearPrice";
    public static final String REF_YEAR = "refYear";

    @Override
    public void process() {
        if (input == null || input.isEmpty()) {
            results = null;
            return;
        }

        results = new InformationSet();
        
        // Calculate the 3rd missing Ts
        calculateMissingData();

        // Find the intersection domain between all series
        TsDomain intersection = intersectAllDomains(findSmallestDomain());
        if (intersection.getLength() == 0) {
            throw new IllegalArgumentException("The common domain is empty");
        }

        if (refYear == - 1 || intersection.getStart().getYear() > refYear
                || intersection.getEnd().getYear() < refYear) {
            refYear = intersection.getStart().getYear();
        }

        results.add(REF_YEAR, refYear);

        // Then, drop everything before the reference year (-1 quarter each iteration)
        while (intersection.getStart().getYear() < refYear) {
            intersection = intersection.drop(1, 0);
        }

        // Create the list of domain fitted products, we keep original list unchanged
        List<Product> prds = cloneInputList();

        for (int i = 0; i < prds.size(); i++) {
            prds.get(i).setQuantities(prds.get(i).getQuantities().fittoDomain(intersection));
            prds.get(i).setPrice(prds.get(i).getPrice().fittoDomain(intersection));
            prds.get(i).setValue(prds.get(i).getValue().fittoDomain(intersection));
        }

        List<TsData> allPricesY = new ArrayList<>();
        for (int i = 0; i < prds.size(); i++) {
            allPricesY.add(prds.get(i).getPrice().changeFrequency(TsFrequency.Yearly, TsAggregationType.Average, true));
        }

        //-------------------------------------------------------------------
        // Create list of first year dropped quantities
        List<TsData> allQuantitiesQ = new ArrayList<>();
        for (int i = 0; i < prds.size(); i++) {
            TsData ts = prds.get(i).getQuantities();
            ts = ts.update(ts.changeFrequency(TsFrequency.Quarterly, TsAggregationType.Sum, true));
            ts = ts.drop(4, 0);
            allQuantitiesQ.add(ts);
        }

        // Calculate total value TsData (column Total at current prices)        
        TsData totalValueQ = prds.get(0).getValue();
        for (int i = 1; i < prds.size(); i++) {
            totalValueQ = TsData.add(totalValueQ, prds.get(i).getValue());
        }

        results.add(TOTAL_VALUES, totalValueQ);

        TsData totalValueY = totalValueQ.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        totalValueQ = totalValueQ.drop(4, 0);

        // Iterates through the years for computing the series at the price of previous year
        TsData Qq = new TsData(totalValueQ.getDomain());
        YearIterator yq = new YearIterator(Qq);
        List<YearIterator> iterators = new ArrayList<>();
        for (int i = 0; i < allQuantitiesQ.size(); i++) {
            iterators.add(new YearIterator(allQuantitiesQ.get(i)));
        }
        while (yq.hasMoreElements()) {
            TsDataBlock qcur = yq.nextElement();
            List<TsDataBlock> dataBlocks = new ArrayList<>();
            for (int i = 0; i < prds.size(); i++) {
                dataBlocks.add(iterators.get(i).nextElement());
            }
            TsPeriod prev = TsPeriod.year(qcur.start.getYear() - 1);

            qcur.data.setAY(allPricesY.get(0).get(prev), dataBlocks.get(0).data);
            for (int i = 1; i < dataBlocks.size(); i++) {
                qcur.data.addAY(allPricesY.get(i).get(prev), dataBlocks.get(i).data);
            }
        }

        // In Qq I have values of each quarter computed with the price of previous year
        results.set(VALUES_PREVIOUS_PRICE, Qq.clone());

        //---------------------------------------------------------------
        // STEP 3. Chain the indexes.
        double idx = 100;
        int ifreq = Qq.getFrequency().intValue();
        yq.reset();

        while (yq.hasMoreElements()) {
            TsDataBlock qcur = yq.nextElement();
            TsPeriod prev = TsPeriod.year(qcur.start.getYear() - 1);
            double val0 = totalValueY.get(prev);
            double val1 = qcur.data.sum();
            qcur.data.mul(idx / (val0 / ifreq));
            idx *= val1 / val0;
        }
        // In Qq I have % of chain linked indexes
        results.set(CHAIN_LINKED_INDEXES, Qq);
    }

    private TsDomain findSmallestDomain() {
        // Looking for the smallest domain of all inputs
        TsDomain dRef = input.get(0).getQuantities().fullYears().getDomain();
        for (int i = 1; i < input.size(); i++) {
            TsDomain current = input.get(i).getQuantities().fullYears().getDomain();
            if (dRef.getLength() <= dRef.getLength()) {
                dRef = current;
            }
        }

        return dRef;
    }

    private TsDomain intersectAllDomains(TsDomain ref) {
        for (int i = 0; i < input.size(); i++) {
            TsDomain current = input.get(i).getQuantities().getDomain();
            ref = ref.intersection(current);
        }

        return ref;
    }

    private ArrayList<Product> cloneInputList() {
        ArrayList<Product> products = new ArrayList<>(input.size());
        try {
            for (Product p : input) {
                products.add((Product) p.clone());
            }
        } catch (CloneNotSupportedException ex) {
            throw new IllegalArgumentException("Impossible to clone the list of inputs !");
        }
        return products;
    }

    private void calculateMissingData() {
        for (Product p : input) {
            p.calculateMissingTs();
        }
    }

}
