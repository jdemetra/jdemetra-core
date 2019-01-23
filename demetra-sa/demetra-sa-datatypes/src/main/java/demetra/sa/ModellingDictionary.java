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
public final class ModellingDictionary  {

    private ModellingDictionary() {
    }
    private static final HashMap<String, SeriesInfo> dictionary_ = new HashMap<>();
    public static final String DECOMPOSITION = "decomposition", PREPROCESSING = "preprocessing"
            ,DETAILS = "details";
    public static final String N = "n", SPAN = "span";
    public static final String Y = "y", T = "t", S = "s",
            SA = "sa", I = "i", YC = "yc", YCAL = "ycal", DET = "det", L = "l";
    public static final String MODE = "mode";
    public static final String[] ALL = {MODE, Y, T, S, SA, I};
    public static final String Y_LIN = "y_lin", T_LIN = "t_lin", S_LIN = "s_lin",
            SA_LIN = "sa_lin", I_LIN = "i_lin", SI_LIN = "si_lin";
    public static final String[] ALL_LIN = {Y_LIN, T_LIN, S_LIN, SA_LIN, I_LIN, SI_LIN};
    public static final String Y_CMP = "y_cmp", T_CMP = "t_cmp", S_CMP = "s_cmp",
            SA_CMP = "sa_cmp", I_CMP = "i_cmp", SI_CMP = "si_cmp";
    public static final String[] ALL_CMP = {Y_CMP, T_CMP, S_CMP, SA_CMP, I_CMP, SI_CMP};
    public static final String CAL = "cal", EE = "ee", TDE = "tde", MHE = "mhe", RMDE = "rmde", OMHE = "omhe";
    public static final String[] ALL_MHE = {EE, RMDE, OMHE};
    public static final String OUT_I = "out_i", OUT_S = "out_s", OUT_T = "out_t", OUT = "out";
    public static final String[] ALL_OUT = {OUT_I, OUT_S, OUT_T};
    public static final String REG_I = "reg_i", REG_S = "reg_s", REG_T = "reg_t", REG_SA = "reg_sa",
            REG_Y = "reg_y", REG_U="reg_u", REG = "reg";
    public static final String DET_I = "det_i", DET_S = "det_s", DET_T = "det_t", DET_SA = "det_sa",
            DET_Y = "det_y";
    public static final String[] ALL_REG = {REG_I, REG_S, REG_T, REG_SA, REG_Y};

    public static final String FULL_RES="full_res";


    public static void fillDictionary(List<SeriesInfo> sel, String prefix, List<String> dictionary) {
        if (sel == null) {
            sel = new ArrayList<>(dictionary_.values());
        }
        if (prefix != null) {
            for (SeriesInfo info : sel) {
                dictionary.add(InformationSet.item(prefix, info.getCode()));
            }
        } else {
            for (SeriesInfo info : sel) {
                dictionary.add(info.getCode());
            }
        }
    }

    public static SeriesInfo getInfo(String code) {
        synchronized (dictionary_) {
            return dictionary_.get(code);
        }
    }

    public static SeriesInfo getInfo(String code, ComponentInformation info) {
        SeriesInfo master = getInfo(code);
        if (master == null) {
            return null;
        }
        if (info == ComponentInformation.Value) {
            return master;
        }
        SeriesInfo sinfo = new SeriesInfo(master.getName(),
                master.getComponent(), info, master.getDescription());
        return sinfo;
    }


    // init the dictionary
    static {
        SeriesInfo info = new SeriesInfo(CAL, ComponentType.CalendarEffect, 
                ComponentInformation.Value, "Calendar effects");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(I, ComponentType.Irregular, 
                ComponentInformation.Value, "Final irregular component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(S, ComponentType.Seasonal, 
                ComponentInformation.Value, "Final seasonal component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(SA, ComponentType.SeasonallyAdjusted, 
                ComponentInformation.Value, "Final seasonally adjusted series");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(T, ComponentType.Trend, 
                ComponentInformation.Value, "Final trend component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(EE, ComponentType.CalendarEffect, 
                ComponentInformation.Value, "Easter effect");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(TDE, ComponentType.CalendarEffect, 
                ComponentInformation.Value, "Trading days effect");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(MHE, ComponentType.CalendarEffect, 
                ComponentInformation.Value, "Moving holidays effect");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(RMDE, ComponentType.CalendarEffect, 
                ComponentInformation.Value, "Ramadan effect");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(OMHE, ComponentType.CalendarEffect, 
                ComponentInformation.Value, "Other moving holidays effects");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(OUT_I, ComponentType.Irregular, 
                ComponentInformation.Value, "Outliers effect on the irregular component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(OUT_S, ComponentType.Seasonal, 
                ComponentInformation.Value, "Outliers effect on the seasonal component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(OUT_T, ComponentType.Seasonal, 
                ComponentInformation.Value, "Outliers effect on the trend component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(OUT, ComponentType.Undefined, 
                ComponentInformation.Value, "Total outliers effect");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(REG_I, ComponentType.Irregular, 
                ComponentInformation.Value, "Regression effect on the irregular component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(REG_S, ComponentType.Seasonal, 
                ComponentInformation.Value, "Regression effect on the seasonal component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(REG_SA, ComponentType.SeasonallyAdjusted, 
                ComponentInformation.Value, "Regression effect on the seasonally adjusted series");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(REG_T, ComponentType.Trend, 
                ComponentInformation.Value, "Regression effect on the trend component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(REG, ComponentType.Undefined, 
                ComponentInformation.Value, "Total regression effects");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(REG_Y, ComponentType.Series, 
                ComponentInformation.Value, "Separate regression effect");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(I_CMP, ComponentType.Irregular, 
                ComponentInformation.Value, "Irregular component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(S_CMP, ComponentType.Seasonal, 
                ComponentInformation.Value, "Seasonal component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(SA_CMP, ComponentType.SeasonallyAdjusted, 
                ComponentInformation.Value, "Seasonally adjusted series");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(SI_CMP, ComponentType.Undefined, 
                ComponentInformation.Value, "SI-Ratio");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(T_CMP, ComponentType.Trend, 
                ComponentInformation.Value, "Trend");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(I_LIN, ComponentType.Irregular, 
                ComponentInformation.Value, "Linearized irregular component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(S_LIN, ComponentType.Seasonal, 
                ComponentInformation.Value, "Linearized seasonal component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(SA_LIN, ComponentType.SeasonallyAdjusted, 
                ComponentInformation.Value, "Linearized seasonally adjusted series");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(SI_LIN, ComponentType.Undefined, 
                ComponentInformation.Value, "Linearized SI-Ratio");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(T_LIN, ComponentType.Trend, 
                ComponentInformation.Value, "Linearized trend");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(Y, ComponentType.Series, 
                ComponentInformation.Value, "Original series");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(YC, ComponentType.Series, 
                ComponentInformation.Value, "Interpolated series");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(YCAL, ComponentType.Series, 
                ComponentInformation.Value, "Series corrected for the calendar effects");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(Y_CMP, ComponentType.Series, 
                ComponentInformation.Value, "Series component");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(Y_LIN, ComponentType.Series, 
                ComponentInformation.Value, "Linearized series");
        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(DET, ComponentType.Undefined, 
                ComponentInformation.Value, "Deterministic component");

        dictionary_.put(info.getName(), info);

        info = new SeriesInfo(L, ComponentType.Series, 
                ComponentInformation.Value, "[Log-transformed] linearized series");
        dictionary_.put(info.getName(), info);
    }

    public static List<SeriesInfo> getFinalSeries() {
        return Arrays.asList(FINALS);
    }

    public static List<SeriesInfo> getMainSeries() {
        ArrayList<SeriesInfo> dic=new ArrayList<>();
        dic.add(getInfo(Y));
        dic.add(getInfo(SA));
        dic.add(getInfo(T));
        dic.add(getInfo(S));
        dic.add(getInfo(I));
        return dic;
    }

    public static List<SeriesInfo> getStochasticSeries() {
        return Arrays.asList(STOCHASTICS);
    }

    public static List<SeriesInfo> getDeterministicSeries() {
        return Arrays.asList(DETERMINISTICS);
    }

    public static List<SeriesInfo> getOtherSeries() {
        return Arrays.asList(OTHERS);
    }

    private static final SeriesInfo[] FINALS = new SeriesInfo[]{
        getInfo(Y),
        getInfo(SA),
        getInfo(T),
        getInfo(S),
        getInfo(I),
        getInfo(Y, ComponentInformation.Forecast),
        getInfo(Y, ComponentInformation.StdevForecast),
        getInfo(SA, ComponentInformation.Forecast),
        getInfo(T, ComponentInformation.Forecast),
        getInfo(S, ComponentInformation.Forecast),
        getInfo(I, ComponentInformation.Forecast),};
    
    private final static SeriesInfo[] STOCHASTICS = new SeriesInfo[]{
        getInfo(Y_LIN),
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
    
    private final static SeriesInfo[] DETERMINISTICS = new SeriesInfo[]{
        getInfo(YC),
        getInfo(Y_LIN),
        getInfo(YCAL),
        getInfo(DET),
        getInfo(CAL),
        //getInfo(MHE),
        getInfo(OMHE),
        getInfo(RMDE),
        getInfo(TDE),
        getInfo(EE),
        getInfo(OUT_T),
        getInfo(OUT_S),
        getInfo(OUT_I),
        getInfo(OUT),
        getInfo(REG_Y),
        getInfo(REG_SA),
        getInfo(REG_T),
        getInfo(REG_S),
        getInfo(REG_I),
        getInfo(REG)
    };
    
    private static final SeriesInfo[] OTHERS = new SeriesInfo[]{
        new SeriesInfo(FULL_RES, ComponentType.Undefined, 
                ComponentInformation.Value, "White-noise residuals")
    };
}
