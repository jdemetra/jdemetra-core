/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.DayClustering;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class GenericTradingDays {

    private final DayClustering clustering;
    private final int contrastGroup;
    private final boolean normalized;
    
    public static GenericTradingDays contrasts(DayClustering clustering){
        return new GenericTradingDays(clustering, 0);
    }

    public static GenericTradingDays of(DayClustering clustering){
        return new GenericTradingDays(clustering, false);
    }

    public static GenericTradingDays normalized(DayClustering clustering){
        return new GenericTradingDays(clustering, true);
    }

    private GenericTradingDays(DayClustering clustering, int contrastGroup) {
        this.clustering = clustering;
        this.contrastGroup = contrastGroup;
        normalized = true;
    }

    private GenericTradingDays(DayClustering clustering, boolean normalized ) {
        this.clustering = clustering;
        this.contrastGroup = -1;
        this.normalized = normalized;
    }

    public DayClustering getClustering() {
        return clustering;
    }

    public void data(TsDomain domain, List<DataBlock> buffer) {
        if (contrastGroup >= 0) {
            dataContrasts(domain, buffer);
        } else {
            dataNoContrast(domain, buffer);
        }
    }

    private void dataNoContrast(TsDomain domain, List<DataBlock> buffer) {
        int n = domain.getLength();
        int[][] days = Utilities.tdCount(domain);
        adjust(days);

        int[][] groups = clustering.allPositions();
        int ng = groups.length;
        for (int i = 0; i < n; ++i) {
            for (int ig = 0; ig < ng; ++ig) {
                int[] group = groups[ig];
                int sum = days[group[0]][i];
                int np = group.length;
                for (int ip = 1; ip < np; ++ip) {
                    sum += days[group[ip]][i];
                }
                double dsum = sum;
                if (normalized) {
                    dsum /= np;
                }
                buffer.get(ig).set(i, dsum);
            }
        }
    }

    private void dataContrasts(TsDomain domain, List<DataBlock> buffer) {
        int n = domain.getLength();
        int[][] days = Utilities.tdCount(domain);
        adjust(days);

        int[][] groups = clustering.allPositions();
        rotate(groups);
        int ng = groups.length - 1;
        int[] cgroup = groups[ng];
        for (int i = 0; i < n; ++i) {
            int csum = days[cgroup[0]][i];
            int cnp = cgroup.length;
            for (int ip = 1; ip < cnp; ++ip) {
                csum += days[cgroup[ip]][i];
            }
            double dcsum = csum;
            dcsum /= cnp;
            for (int ig = 0; ig < ng; ++ig) {
                int[] group = groups[ig];
                int sum = days[group[0]][i];
                int np = group.length;
                for (int ip = 1; ip < np; ++ip) {
                    sum += days[group[ip]][i];
                }
                double dsum = sum;
                buffer.get(ig).set(i, dsum - np*dcsum);
            }
        }
//        int[] ndays = Utilities.daysCount(domain);
//        DataBlock nb = buffer.get(ng);
//        nb.set(i -> ndays[i]);
    }

    public int getCount() {
        int n=clustering.getGroupsCount();
        return contrastGroup>= 0 ? n-1 : n;
    }

    public String getDescription(int idx) {
        return clustering.toString(idx);
    }

    /**
     * @return the contrastGroup
     */
    public int getContrastGroup() {
        return contrastGroup;
    }

    /**
     * @return the normalization
     */
    public boolean isNormalized() {
        return normalized;
    }

    private void rotate(int[][] groups) {
        if (contrastGroup >= 0) {
            // we put the contrast group at the end
            int[] cgroup = groups[contrastGroup];
            for (int i = contrastGroup + 1; i < groups.length; ++i) {
                groups[i - 1] = groups[i];
            }
            groups[groups.length - 1] = cgroup;
        }
    }

    private void adjust(int[][] days) {
        // adjust the days to meet the definition of DayOfWeek from the java library
        int[] sun=days[0];
        for (int i=1; i<days.length; ++i){
            days[i-1]=days[i];
        }
        days[6]=sun;

    }
}
