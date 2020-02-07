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

package demetra.sa;

import demetra.design.Development;
import demetra.information.InformationSet;
import demetra.modelling.ComponentInformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public final class SaDictionary  {

    private SaDictionary() {
    }
    private static final HashMap<String, SaSeriesInfo> dictionary_ = new HashMap<>();
    public static final String DECOMPOSITION = "decomposition", PREPROCESSING = "preprocessing"
            ,DETAILS = "details";
    public static final String T = "t", S = "s", SA = "sa", I = "i";
    public static final String MODE = "mode";
    public static final String T_LIN = "t_lin", S_LIN = "s_lin", SA_LIN = "sa_lin", I_LIN = "i_lin", SI_LIN = "si_lin";
    public static final String T_CMP = "t_cmp", S_CMP = "s_cmp", SA_CMP = "sa_cmp", I_CMP = "i_cmp", SI_CMP = "si_cmp";
    public static final String OUT_I = "out_i", OUT_S = "out_s", OUT_T = "out_t";
    public static final String REG_I = "reg_i", REG_S = "reg_s", REG_T = "reg_t", REG_SA = "reg_sa",
            REG_Y = "reg_y", REG_U="reg_u";
    public static final String DET_I = "det_i", DET_S = "det_s", DET_T = "det_t", DET_SA = "det_sa",
            DET_Y = "det_y";


    public static void fillDictionary(List<SaSeriesInfo> sel, String prefix, List<String> dictionary) {
        if (sel == null) {
            sel = new ArrayList<>(dictionary_.values());
        }
        if (prefix != null) {
            for (SaSeriesInfo info : sel) {
                dictionary.add(InformationSet.item(prefix, info.getCode()));
            }
        } else {
            for (SaSeriesInfo info : sel) {
                dictionary.add(info.getCode());
            }
        }
    }

    public static SaSeriesInfo getInfo(String code) {
        synchronized (dictionary_) {
            return dictionary_.get(code);
        }
    }

    public static SaSeriesInfo getInfo(String code, ComponentInformation info) {
        SaSeriesInfo master = getInfo(code);
        if (master == null) {
            return null;
        }
        if (info == ComponentInformation.Value) {
            return master;
        }
        SaSeriesInfo sinfo = new SaSeriesInfo(master.getName(),
                master.getComponent(), info, master.getDescription());
        return sinfo;
    }


    // init the dictionary
    static {
        SaSeriesInfo info = new SaSeriesInfo(I, ComponentType.Irregular, 
                ComponentInformation.Value, "Final irregular component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(S, ComponentType.Seasonal, 
                ComponentInformation.Value, "Final seasonal component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(SA, ComponentType.SeasonallyAdjusted, 
                ComponentInformation.Value, "Final seasonally adjusted series");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(T, ComponentType.Trend, 
                ComponentInformation.Value, "Final trend component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(OUT_I, ComponentType.Irregular, 
                ComponentInformation.Value, "Outliers effect on the irregular component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(OUT_S, ComponentType.Seasonal, 
                ComponentInformation.Value, "Outliers effect on the seasonal component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(OUT_T, ComponentType.Seasonal, 
                ComponentInformation.Value, "Outliers effect on the trend component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(REG_I, ComponentType.Irregular, 
                ComponentInformation.Value, "Regression effect on the irregular component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(REG_S, ComponentType.Seasonal, 
                ComponentInformation.Value, "Regression effect on the seasonal component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(REG_SA, ComponentType.SeasonallyAdjusted, 
                ComponentInformation.Value, "Regression effect on the seasonally adjusted series");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(REG_T, ComponentType.Trend, 
                ComponentInformation.Value, "Regression effect on the trend component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(REG_Y, ComponentType.Series, 
                ComponentInformation.Value, "Separate regression effect");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(I_CMP, ComponentType.Irregular, 
                ComponentInformation.Value, "Irregular component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(S_CMP, ComponentType.Seasonal, 
                ComponentInformation.Value, "Seasonal component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(SA_CMP, ComponentType.SeasonallyAdjusted, 
                ComponentInformation.Value, "Seasonally adjusted series");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(SI_CMP, ComponentType.Undefined, 
                ComponentInformation.Value, "SI-Ratio");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(T_CMP, ComponentType.Trend, 
                ComponentInformation.Value, "Trend");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(I_LIN, ComponentType.Irregular, 
                ComponentInformation.Value, "Linearized irregular component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(S_LIN, ComponentType.Seasonal, 
                ComponentInformation.Value, "Linearized seasonal component");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(SA_LIN, ComponentType.SeasonallyAdjusted, 
                ComponentInformation.Value, "Linearized seasonally adjusted series");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(SI_LIN, ComponentType.Undefined, 
                ComponentInformation.Value, "Linearized SI-Ratio");
        dictionary_.put(info.getName(), info);

        info = new SaSeriesInfo(T_LIN, ComponentType.Trend, 
                ComponentInformation.Value, "Linearized trend");
        dictionary_.put(info.getName(), info);

      }

    public static List<SaSeriesInfo> getFinalSeries() {
        return Arrays.asList(FINALS);
    }

    public static List<SaSeriesInfo> getMainSeries() {
        ArrayList<SaSeriesInfo> dic=new ArrayList<>();
        dic.add(getInfo(SA));
        dic.add(getInfo(T));
        dic.add(getInfo(S));
        dic.add(getInfo(I));
        return dic;
    }

    public static List<SaSeriesInfo> getStochasticSeries() {
        return Arrays.asList(STOCHASTICS);
    }

    private static final SaSeriesInfo[] FINALS = new SaSeriesInfo[]{
        getInfo(SA),
        getInfo(T),
        getInfo(S),
        getInfo(I),
        getInfo(SA, ComponentInformation.Forecast),
        getInfo(T, ComponentInformation.Forecast),
        getInfo(S, ComponentInformation.Forecast),
        getInfo(I, ComponentInformation.Forecast),};
    
    private final static SaSeriesInfo[] STOCHASTICS = new SaSeriesInfo[]{
        getInfo(SA_LIN),
        getInfo(T_LIN),
        getInfo(S_LIN),
        getInfo(I_LIN),
        getInfo(SA_LIN, ComponentInformation.Forecast),
        getInfo(T_LIN, ComponentInformation.Forecast),
        getInfo(S_LIN, ComponentInformation.Forecast),
        getInfo(I_LIN, ComponentInformation.Forecast),
        getInfo(SA_LIN, ComponentInformation.StdevForecast),
        getInfo(T_LIN, ComponentInformation.StdevForecast),
        getInfo(S_LIN, ComponentInformation.StdevForecast),
        getInfo(I_LIN, ComponentInformation.StdevForecast)
    };
    
    
}
