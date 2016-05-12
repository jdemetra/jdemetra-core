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
package ec.tstoolkit.timeseries;

import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
public class DayClustering {

    private final int[] groups;

    public static DayClustering create(int[] groups) {
        if (groups.length != 7) {
            return null;
        }
        if (!checkGroups(groups)) {
            return null;
        }
        return new DayClustering(groups);
    }

    private static boolean checkGroups(int[] groups) {
        int n = 0;
        int gr = 0;
        while (n < 7) {
            int ncur = 0;
            for (int i = 0; i < groups.length; ++i) {
                if (groups[i] == gr) {
                    ++ncur;
                }
            }
            if (ncur == 0) {
                return false;
            }
            n += ncur;
            gr++;
        }
        return gr > 1;
    }

    DayClustering(final int[] groups) {
        this.groups = groups;
    }

    public int getGroup(DayOfWeek dw) {
        return groups[dw.intValue()];
    }

    public int getGroupsCount() {
        int n = 0;
        for (int i = 0; i < groups.length; ++i) {
            if (groups[i] > n) {
                n = groups[i];
            }
        }
        return n + 1;
    }

    public DayOfWeek[] group(int idx) {
        int n = 0;
        for (int i = 0; i < groups.length; ++i) {
            if (groups[i] == idx) {
                ++n;
            }
        }
        DayOfWeek[] dw = new DayOfWeek[n];
        for (int i = 0, j = 0; j < n; ++i) {
            if (groups[i] == idx) {
                dw[j++] = DayOfWeek.valueOf(i);
            }
        }
        return dw;
    }

    public int[] positions(int idx) {
        int n = 0;
        for (int i = 0; i < groups.length; ++i) {
            if (groups[i] == idx) {
                ++n;
            }
        }
        int[] dw = new int[n];
        for (int i = 0, j = 0; j < n; ++i) {
            if (groups[i] == idx) {
                dw[j++] = i;
            }
        }
        return dw;
    }

    /**
     * 
     * @return element i of the result contains the days of the week corresponding to the group i
     */
    public int[][] allPositions() {
        int n = getGroupsCount();
        int[][] all = new int[n][];
        for (int i = 0; i < n; ++i) {
            all[i] = positions(i);
        }
        return all;
    }

    /**
     *
     * @param dc
     * @return true if each group of the given clustering is included in one
     * group of this clustering
     */
    public boolean isInside(DayClustering dc) {
        int n = dc.getGroupsCount();
        for (int i = 0; i < n; ++i) {
            int cur = -1;
            for (int j = 0; j < 7; ++j) {
                if (dc.groups[j] == i) {
                    int g = groups[j];
                    if (cur == -1) {
                        cur = g;
                    } else if (g != cur) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        if (Arrays.equals(groups, TD7_IDX)) {
            return "TD7";
        }
        if (Arrays.equals(groups, TD2_IDX)) {
            return "TD2";
        }
        if (Arrays.equals(groups, TD3_IDX)) {
            return "TD3";
        }
        if (Arrays.equals(groups, TD4_IDX)) {
            return "TD4";
        }
        StringBuilder builder = new StringBuilder();
        int ng = getGroupsCount();
        for (int i = 0; i < ng; ++i) {
            builder.append('{').append(toString(i)).append('}');
        }
        return builder.toString();
    }

    public String toString(int i) {
        StringBuilder builder = new StringBuilder();
        DayOfWeek[] gr = group(i);
        builder.append(SHORTNAMES[gr[0].intValue()]);
        for (int j = 1; j < gr.length; ++j) {
            builder.append(',').append(SHORTNAMES[gr[j].intValue()]);
        }
        return builder.toString();
    }

    private static final int[] TD7_IDX = new int[]{0, 1, 2, 3, 4, 5, 6}, 
            TD2_IDX = new int[]{0, 1, 1, 1, 1, 1, 0}, 
            TD3_IDX = new int[]{0, 2, 2, 2, 2, 2, 1},
            TD4_IDX = new int[]{0, 3, 3, 3, 3, 2, 1}
            ;

    private static final String[] SHORTNAMES = new String[]{"sun", "mon", "tue", "wed", "thu", "fri", "sat"};

    public static final DayClustering TD2 = new DayClustering(TD2_IDX), TD3 = new DayClustering(TD3_IDX)
            , TD4 = new DayClustering(TD4_IDX), TD7 = new DayClustering(TD7_IDX);

}
