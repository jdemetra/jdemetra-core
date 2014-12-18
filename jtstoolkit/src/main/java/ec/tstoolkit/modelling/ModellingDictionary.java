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

package ec.tstoolkit.modelling;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.utilities.Jdk6;
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
            sel = Jdk6.newArrayList(dictionary_.values());
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
        SeriesInfo sinfo = new SeriesInfo(master.name,
                master.description, master.component, info);
        return sinfo;
    }


    // init the dictionary
    static {
        SeriesInfo info = new SeriesInfo(CAL, "Calendar effects", ComponentType.CalendarEffect, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(I, "Final irregular component", ComponentType.Irregular, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(S, "Final seasonal component", ComponentType.Seasonal, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(SA, "Final seasonally adjusted series", ComponentType.SeasonallyAdjusted, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(T, "Final trend component", ComponentType.Trend, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(EE, "Easter effect", ComponentType.CalendarEffect, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(TDE, "Trading days effect", ComponentType.CalendarEffect, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(MHE, "Moving holidays effect", ComponentType.CalendarEffect, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(RMDE, "Ramadan effect", ComponentType.CalendarEffect, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(OMHE, "Other moving holidays effects", ComponentType.CalendarEffect, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(OUT_I, "Outliers effect on the irregular component",
                ComponentType.Irregular, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(OUT_S, "Outliers effect on the seasonal component",
                ComponentType.Seasonal, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(OUT_T, "Outliers effect on the trend component",
                ComponentType.Seasonal, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(OUT, "Total outliers effect",
                ComponentType.Undefined, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(REG_I, "Regression effect on the irregular component",
                ComponentType.Irregular, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(REG_S, "Regression effect on the seasonal component",
                ComponentType.Seasonal, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(REG_SA, "Regression effect on the seasonally adjusted series",
                ComponentType.SeasonallyAdjusted, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(REG_T, "Regression effect on the trend component",
                ComponentType.Trend, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(REG, "Total regression effects",
                ComponentType.Undefined, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(REG_Y, "Separate regression effect",
                ComponentType.Series, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(I_CMP, "Irregular component",
                ComponentType.Irregular, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(S_CMP, "Seasonal component",
                ComponentType.Seasonal, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(SA_CMP, "Seasonally adjusted series",
                ComponentType.SeasonallyAdjusted, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(SI_CMP, "SI-Ratio",
                ComponentType.Undefined, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(T_CMP, "Trend",
                ComponentType.Trend, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(I_LIN, "Linearized irregular component",
                ComponentType.Irregular, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(S_LIN, "Linearized seasonal component",
                ComponentType.Seasonal, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(SA_LIN, "Linearized seasonally adjusted series",
                ComponentType.SeasonallyAdjusted, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(SI_LIN, "Linearized SI-Ratio",
                ComponentType.Undefined, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(T_LIN, "Linearized trend",
                ComponentType.Trend, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(Y, "Original series",
                ComponentType.Series, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(YC, "Interpolated series",
                ComponentType.Series, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(YCAL, "Series corrected for the calendar effects",
                ComponentType.Series, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(Y_CMP, "Series component",
                ComponentType.Series, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(Y_LIN, "Linearized series",
                ComponentType.Series, true);
        dictionary_.put(info.name, info);

        info = new SeriesInfo(DET, "Deterministic component",
                ComponentType.Undefined, true);

        dictionary_.put(info.name, info);

        info = new SeriesInfo(L, "[Log-transformed] linearized series",
                ComponentType.Series, true);
        dictionary_.put(info.name, info);
    }

    public static List<SeriesInfo> getFinalSeries() {
        return Arrays.asList(finals_);
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
        return Arrays.asList(stoch_);
    }

    public static List<SeriesInfo> getDeterministicSeries() {
        return Arrays.asList(det_);
    }

    public static List<SeriesInfo> getOtherSeries() {
        return Arrays.asList(others_);
    }

    private static final SeriesInfo[] finals_ = new SeriesInfo[]{
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
    private static SeriesInfo[] stoch_ = new SeriesInfo[]{
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
    private static SeriesInfo[] det_ = new SeriesInfo[]{
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
    
    private static SeriesInfo[] others_ = new SeriesInfo[]{
        new SeriesInfo(FULL_RES, "White-noise residuals")
    };
}
