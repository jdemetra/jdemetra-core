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
package ec.tss.sa.output;

import ec.satoolkit.GenericSaProcessingFactory;
import ec.tss.formatters.MatrixFormatter;
import ec.tss.sa.SaManager;
import ec.tstoolkit.algorithm.IDiagnosticsFactory;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.Files2;
import ec.tstoolkit.utilities.Id;
import ec.tstoolkit.utilities.Paths;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * @author Jean Palate
 */
public class BasicConfiguration {

    private static Character csvSeparator = null;
    private static final AtomicInteger ndec = new AtomicInteger(9);

    public static int getFractionDigits() {
        return ndec.get();
    }

    public static void setDecimalNumber(int n) {
        ndec.set(n);
    }

    public static char getCsvSeparator() {
        synchronized (ndec) {
            if (csvSeparator != null) {
                return csvSeparator;
            } else {
                DecimalFormat fmt = (DecimalFormat) NumberFormat.getNumberInstance();
                char sep = fmt.getDecimalFormatSymbols().getDecimalSeparator();
                if (sep == ',') {
                    return ';';
                } else {
                    return ',';
                }
            }
        }
    }

    public static void setCsvSeparator(Character c) {
        synchronized (ndec) {
            csvSeparator = c;
        }
    }

    public static final List<String> allSeries(boolean compact, List<? extends IProcessingFactory> facs) {
        LinkedHashSet<String> dic = new LinkedHashSet<>();
        for (IProcessingFactory fac : facs) {
            Map<String, Class> odic = fac.getOutputDictionary(compact);
            odic.forEach((s, c) -> {
                if (c == TsData.class && !dic.contains(s)) {
                    dic.add(s);
                }
            });
        }
        return dic.stream().collect(Collectors.toList());
    }

    public static final List<String> allSaSeries(boolean compact) {
        return allSeries(compact, SaManager.instance.getProcessors());
    }

    public static final List<String> allDetails(boolean compact, List<? extends IProcessingFactory> facs, List<? extends IDiagnosticsFactory> diags) {
        LinkedHashSet<String> dic = new LinkedHashSet<>();
        for (IProcessingFactory fac : facs) {
            Map<String, Class> odic = fac.getOutputDictionary(compact);
            odic.forEach((s, c) -> {
                if (c != TsData.class && MatrixFormatter.canProcess(c) && !dic.contains(s)) {
                    dic.add(s);
                }
            });
        }
        diags.stream().filter(d -> d.isEnabled()).forEach(
                z -> {
                    String lz = z.getName().toLowerCase();
                    z.getTestDictionary().forEach(t -> dic.add(InformationSet.concatenate(GenericSaProcessingFactory.DIAGNOSTICS, lz, ((String) t).toLowerCase())));
                }
        );
        return dic.stream().collect(Collectors.toList());
    }

    public static final List<String> allSaDetails(boolean compact) {
        return allDetails(compact, SaManager.instance.getProcessors(), SaManager.instance.getDiagnostics());
    }

    public static final List<String> allSingleSaDetails(boolean compact) {
        List<String> d = allDetails(compact, SaManager.instance.getProcessors(), SaManager.instance.getDiagnostics());
        List<String> dc=new ArrayList<>();
        for (String cur : d){
            int last = cur.lastIndexOf(":");
            if (last>0)
                dc.add(cur.substring(0, last));
            else
                dc.add(cur);
        }
        return dc;
   }

    public static final String DEMETRA = "Demetra+", DEF_FILE = "demetra";
    public static final String defaultFolder = Files2.fromPath(Paths.getDefaultHome(), DEMETRA).getPath();
    @Deprecated
    public static final String[] allDetails = {
        "span.start", "span.end", "span.n",
        "espan.start", "espan.end", "espan.n",
        "likelihood.neffectiveobs", "likelihood.np", "likelihood.logvalue", "likelihood.adjustedlogvalue",
        "likelihood.ssqerr", "likelihood.aic", "likelihood.aicc", "likelihood.bic", "likelihood.bicc",
        "residuals.ser", "residuals.ser-ml", "residuals.mean", "residuals.skewness", "residuals.kurtosis",
        "residuals.dh", "residuals.lb", "residuals.lb2", "residuals.seaslb", "residuals.bp", "residuals.bp2",
        "residuals.seasbp", "residuals.nruns", "residuals.lruns",
        "m-statistics.m1", "m-statistics.m2", "m-statistics.m3", "m-statistics.m4", "m-statistics.m5", "m-statistics.m6",
        "m-statistics.m7", "m-statistics.m8", "m-statistics.m9", "m-statistics.m10", "m-statistics.m11", "m-statistics.q", "m-statistics.q-m2",
        "diagnostics.quality", "diagnostics.basic checks.definition:2", "diagnostics.basic checks.annual totals:2", "diagnostics.visual spectral analysis.spectral seas peaks",
        "diagnostics.visual spectral analysis.spectral td peaks", "diagnostics.regarima residuals.normality:2", "diagnostics.regarima residuals.independence:2",
        "diagnostics.regarima residuals.spectral td peaks:2", "diagnostics.regarima residuals.spectral seas peaks:2", "diagnostics.residual seasonality.on sa:2",
        "diagnostics.residual seasonality.on sa (last 3 years):2", "diagnostics.residual seasonality.on irregular:2",
        "diagnostics.seats.seas variance:2", "diagnostics.seats.irregular variance:2", "diagnostics.seats.seas/irr cross-correlation:2",
        "log", "adjust",
        "arima.mean", "arima.p", "arima.d", "arima.q", "arima.bp", "arima.bd", "arima.bq",
        "arima.phi(1)", "arima.phi(2)", "arima.phi(3)", "arima.phi(4)",
        "arima.th(1)", "arima.th(2)", "arima.th(3)", "arima.th(4)",
        "arima.bphi(1)", "arima.bth(1)",
        "regression.lp:3",
        "regression.ntd", "regression.td(1):3", "regression.td(2):3", "regression.td(3):3", "regression.td(4):3", "regression.td(5):3", "regression.td(6):3", "regression.td(7):3",
        "regression.nmh", "regression.easter:3",
        "regression.nout", "regression.out(1):3", "regression.out(2):3", "regression.out(3):3", "regression.out(4):3", "regression.out(5):3", "regression.out(6):3", "regression.out(7):3", "regression.out(8):3",
        "regression.out(9):3", "regression.out(10):3", "regression.out(11):3", "regression.out(12):3", "regression.out(13):3", "regression.out(14):3", "regression.out(15):3", "regression.out(16):3",
        "decomposition.seasonality", "decomposition.trendfilter", "decomposition.seasfilter"};
    @Deprecated
    public static final String[] allSeries = {
        "y", "y_f", "y_ef", "yc", "yc_f", "yc_ef", "y_lin", "y_lin_f", "l", "ycal", "ycal_f", "l_f", "l_b",
        "t", "t_f", "sa", "sa_f", "s", "s_f", "i", "i_f",
        "det", "det_f", "cal", "cal_f", "tde", "tde_f", "mhe", "mhe_f", "ee", "ee_f",
        "omhe", "omhe_f", "out", "out_f", "out_i", "out_i_f", "out_t", "out_t_f", "out_s",
        "out_s_f", "reg", "reg_f", "reg_t", "reg_t_f", "reg_s", "reg_s_f", "reg_i", "reg_i_f",
        "reg_sa", "reg_sa_f", "reg_y", "reg_y_f", "fullresiduals",
        "decomposition.y_lin",
        "decomposition.y_lin_f",
        "decomposition.t_lin",
        "decomposition.t_lin_f",
        "decomposition.sa_lin",
        "decomposition.sa_lin_f",
        "decomposition.s_lin",
        "decomposition.s_lin_f",
        "decomposition.i_lin",
        "decomposition.i_lin_f",
        "decomposition.y_lin_ef",
        "decomposition.t_lin_e",
        "decomposition.t_lin_ef",
        "decomposition.sa_lin_e",
        "decomposition.sa_lin_ef",
        "decomposition.s_lin_e",
        "decomposition.s_lin_ef",
        "decomposition.i_lin_e",
        "decomposition.i_lin_ef",
        "decomposition.y_cmp",
        "decomposition.y_cmp_f",
        "decomposition.t_cmp",
        "decomposition.t_cmp_f",
        "decomposition.sa_cmp",
        "decomposition.sa_cmp_f",
        "decomposition.s_cmp",
        "decomposition.s_cmp_f",
        "decomposition.i_cmp",
        "decomposition.i_cmp_f",
        "decomposition.t_cmp_e",
        "decomposition.t_cmp_ef",
        "decomposition.sa_cmp_e",
        "decomposition.sa_cmp_ef",
        "decomposition.s_cmp_e",
        "decomposition.s_cmp_ef",
        "decomposition.i_cmp_e",
        "decomposition.i_cmp_ef",
        "decomposition.si_cmp",
        "benchmarking.target",
        "benchmarking.result"
    };

    private static String fileFromId(Id id) {
        int n = id.getCount();
        if (n == 0) {
            return DEF_FILE;
        } else if (n == 1) {
            return id.get(0);
        } else {
            String result = Paths.concatenate(id.get(0), id.get(1));
            for (int i = 2; i < n; ++i) {
                result = Paths.concatenate(result, id.get(i));
            }
            return result;
        }
    }

    private static String fileFromId(String folder, Id id) {
        return Paths.concatenate(folder, fileFromId(id));
    }

    public static String fileFromContext(String folder, Object context) {
        if (context == null) {
            return folder(folder);
        }
        if (context instanceof Id) {
            Id id = (Id) context;
            return BasicConfiguration.fileFromId(folder(folder), (Id) context);
        } else {
            return Paths.concatenate(folder(folder), context.toString());
        }
    }

    public static String folderFromContext(String folder, Object context) {
        String nfolder = folder(folder);
        if (context != null && context instanceof Id) {
            Id parent = (Id) context;
            for (int i = 0; i < parent.getCount(); ++i) {
                nfolder = Paths.concatenate(nfolder, parent.get(i));
            }
        }
        File Folder = new File(nfolder);
        if (!Folder.exists()) {
            Folder.mkdirs();
        }
        return nfolder;
    }

    public static File folderFromContext(File folder, Object context) {
        File nfolder = folder(folder);
        if (context != null && context instanceof Id) {
            Id parent = (Id) context;
            for (int i = 0; i < parent.getCount(); ++i) {
                nfolder = new File(nfolder, parent.get(i));
            }
        }
        if (!nfolder.exists()) {
            nfolder.mkdirs();
        }
        return nfolder;
    }

    public static String folder(String folder) {
        if (folder == null || folder.length() == 0) {
            return defaultFolder;
        } else {
            return folder;
        }
    }

    public static File folder(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return new File(defaultFolder);
        } else {
            return folder;
        }
    }
}
