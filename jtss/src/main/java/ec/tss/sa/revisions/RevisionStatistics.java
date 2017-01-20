/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tss.sa.revisions;

import ec.satoolkit.DecompositionMode;
import ec.tss.TsCollection;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.ProxyResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author palatej
 */
public class RevisionStatistics implements IProcResults {

    public static final String DSACOUNT = "dn", DSAMIN = "dsamin", DSAMAX = "dsamax", DSAAVERAGE = "dsaaverage", DSASTDEV = "dsastdev",
            SACOUNT = "n", SAMIN = "samin", SAMAX = "samax", SAAVERAGE = "saaverage", SASTDEV = "sastdev",
            SCOUNT = "sn", SMIN = "smin", SMAX = "smax", SAVERAGE = "saverage", SSTDEV = "sstdev",
            CCOUNT = "cn", CMIN = "cmin", CMAX = "cmax", CAVERAGE = "caverage", CSTDEV = "cstdev",
            DSAREVS = "dsarevs", SAREVS = "sarevs", SREVS = "srevs", CREVS = "crevs";

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public Map<String, Class> getDictionary(boolean compact) {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, map, compact);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return MAPPING.getData(this, id, tclass);
    }

    // MAPPING
    public static InformationMapping<RevisionStatistics> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<RevisionStatistics, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static <T> void setTsData(String name, Function<RevisionStatistics, TsData> extractor) {
        MAPPING.set(name, extractor);
    }

    private static final InformationMapping<RevisionStatistics> MAPPING = new InformationMapping<>(RevisionStatistics.class);

    static {
        MAPPING.set(DSACOUNT, int[].class, source -> source.dsan_);
        MAPPING.set(DSAMIN, double[].class, source -> source.dsamin_);
        MAPPING.set(DSAMAX, double[].class, source -> source.dsamax_);
        MAPPING.set(DSAAVERAGE, double[].class, source -> source.dsaavg_);
        MAPPING.set(DSASTDEV, double[].class, source -> source.dsastdev_);
        MAPPING.set(SACOUNT, int[].class, source -> source.san_);
        MAPPING.set(SAMIN, double[].class, source -> source.samin_);
        MAPPING.set(SAMAX, double[].class, source -> source.samax_);
        MAPPING.set(SAAVERAGE, double[].class, source -> source.saavg_);
        MAPPING.set(SASTDEV, double[].class, source -> source.sastdev_);
        MAPPING.set(SCOUNT, int[].class, source -> source.sn_);
        MAPPING.set(SMIN, double[].class, source -> source.smin_);
        MAPPING.set(SMAX, double[].class, source -> source.smax_);
        MAPPING.set(SAVERAGE, double[].class, source -> source.savg_);
        MAPPING.set(SSTDEV, double[].class, source -> source.sstdev_);
        MAPPING.set(CCOUNT, int[].class, source -> source.cn_);
        MAPPING.set(CMIN, double[].class, source -> source.cmin_);
        MAPPING.set(CMAX, double[].class, source -> source.cmax_);
        MAPPING.set(CAVERAGE, double[].class, source -> source.cavg_);
        MAPPING.set(CSTDEV, double[].class, source -> source.cstdev_);
    }

    private int[] dsan_, san_, sn_, cn_;
    private double[] samin_, samax_, saavg_, sastdev_;
    private double[] dsamin_, dsamax_, dsaavg_, dsastdev_;
    private double[] smin_, smax_, savg_, sstdev_;
    private double[] cmin_, cmax_, cavg_, cstdev_;
    private double[][] dsarevs_, sarevs_, srevs_, crevs_;
    private final boolean outofsample_, ftarget_;

    public RevisionStatistics(TsCollection input, IProcResults results, boolean outofsample, boolean ftarget) {
        outofsample_ = outofsample;
        ftarget_ = ftarget;
        // gets the differents revisions analysis
        InformationSet[] all = new InformationSet[input.getCount()];
        InformationSet[][] details = new InformationSet[input.getCount()][];
        int[] nrevs = new int[all.length];
        int revmax = 0;
        for (int i = 0; i < nrevs.length; ++i) {
            String item = InformationSet.item(RevisionAnalysisProcessor.SERIES + i, ProxyResults.ALL);
            all[i] = results.getData(item, InformationSet.class);
            if (all[i] != null) {
                List<Information<InformationSet>> ver = all[i].select(SingleRevisionAnalysisProcessor.VER + '*', InformationSet.class);
                int n = ver.size();
                nrevs[i] = n;
                if ((outofsample_ && !ftarget_) || (!outofsample_ && ftarget_)) {
                    --n;
                }
                if (nrevs[i] > revmax) {
                    revmax = nrevs[i];
                }
                // we don't check the names...It should be ordered!
                details[i] = new InformationSet[n];
                for (int j = 0; j < n; ++j) {
                    details[i][j] = ver.get(j).value;
                }
            }
        }
        // foreach vintage, we collect the information and we compute the statistics...
        san_ = new int[revmax];
        samin_ = new double[revmax];
        samax_ = new double[revmax];
        saavg_ = new double[revmax];
        sastdev_ = new double[revmax];
        sarevs_ = new double[revmax][];
        sn_ = new int[revmax];
        smin_ = new double[revmax];
        smax_ = new double[revmax];
        savg_ = new double[revmax];
        sstdev_ = new double[revmax];
        srevs_ = new double[revmax][];
        cn_ = new int[revmax];
        cmin_ = new double[revmax];
        cmax_ = new double[revmax];
        cavg_ = new double[revmax];
        cstdev_ = new double[revmax];
        crevs_ = new double[revmax][];
        dsan_ = new int[revmax];
        dsamin_ = new double[revmax];
        dsamax_ = new double[revmax];
        dsaavg_ = new double[revmax];
        dsastdev_ = new double[revmax];
        dsarevs_ = new double[revmax][];
        if (ftarget_) {
            computeTowardsEnd(all, details, revmax);
        } else {
            computeTowardsStart(all, details, revmax);
        }
    }

    private static boolean isConstant(TsData s) {
        if (s == null) {
            return true;
        }
        double[] d = s.internalStorage();
        for (int i = 1; i < s.getLength(); ++i) {
            if (d[i] != d[0]) {
                return false;
            }
        }
        return true;
    }

    private double[] toarray(List list) {
        double[] x = new double[list.size()];
        for (int i = 0; i < x.length; ++i) {
            x[i] = (Double) list.get(i);
        }
        return x;
    }

    private void computeTowardsStart(InformationSet[] all, InformationSet[][] details, int revmax) {
        List[] rsadata = new List[revmax];
        List[] rdsadata = new List[revmax];
        List[] rsdata = new List[revmax];
        List[] rcdata = new List[revmax];
        for (int i = 0; i < revmax; ++i) {
            rsadata[i] = new ArrayList<>();
            rdsadata[i] = new ArrayList<>();
            rsdata[i] = new ArrayList<>();
            rcdata[i] = new ArrayList<>();
        }
        String ref = SingleRevisionAnalysisProcessor.REF0;
        String refs = InformationSet.item(ref, "s_lin");
        String refsa = InformationSet.item(ref, "sa");
        String refc = InformationSet.item(ref, "cal");
        String refmode = InformationSet.item(ref, "mode");
        String refser = InformationSet.item(ref, "residuals.ser");
        for (int i = 0; i < all.length; ++i) {
            DecompositionMode rmode = all[i].search(refmode, DecompositionMode.class);
            double ser = all[i].search(refser, Double.class);
            TsData rs = all[i].search(refs, TsData.class);
            TsData rsa = all[i].search(refsa, TsData.class);
            TsData rc = all[i].search(refc, TsData.class);
            if (rmode.isMultiplicative()) {
                if (rs != null) {
                    rs = rs.log();
                }
//                if (rsa != null) {
//                    rsa = rsa.log();
//                }
                if (rc != null) {
                    rc = rc.log();
                }
            }
            boolean hass = !isConstant(rs);
            if (hass) {
                for (int j = 0; j < revmax; ++j) {
                    if (j < details[i].length && details[i][j] != null) {
                        DecompositionMode jmode = details[i][j].get("mode", DecompositionMode.class);
                        if (rmode == jmode) {
                            TsData saj = details[i][j].get("sa", TsData.class);
//                            if (rmode.isMultiplicative()) {
//                                saj = saj.log();
//                            }
                            int l = rsa.getLength() - 1;
                            int d;
                            if (!outofsample_) {
                                d = j;
                            } else {
                                d = saj.getLength() - rsa.getLength() - 1;
                            }
                            rsadata[d].add((saj.get(l) / rsa.get(l)) - 1);
                            rdsadata[d].add(saj.get(l) / saj.get(l - 1) - rsa.get(l) / rsa.get(l - 1));
//                            rsadata[d].add((saj.get(l) - rsa.get(l)) / ser);
//                            rdsadata[d].add((saj.get(l) - saj.get(l - 1) - rsa.get(l) + rsa.get(l - 1)) / ser);
                            TsData slinj = details[i][j].get("s_lin", TsData.class);
                            TsData cj = details[i][j].get("cal", TsData.class);
                            boolean hasc = !isConstant(rc) && !isConstant(cj);
                            boolean hasslin = !isConstant(rs) && !isConstant(slinj);
                            if (hasslin) {
                                if (rmode.isMultiplicative()) {
                                    slinj = slinj.log();
                                }
                                rsdata[d].add((slinj.get(l) - rs.get(l)) / ser);

                            }
                            if (hasc) {
                                if (rmode.isMultiplicative()) {
                                    cj = cj.log();
                                }
                                rcdata[d].add((cj.get(l) - rc.get(l)) / ser);
                            }
                        }
                    }
                }
            }
        }

        computeStatistics(rsadata, rdsadata, rsdata, rcdata, revmax);
    }

    private void computeTowardsEnd(InformationSet[] all, InformationSet[][] details, int revmax) {
        List[] rsadata = new List[revmax];
        List[] rdsadata = new List[revmax];
        List[] rsdata = new List[revmax];
        List[] rcdata = new List[revmax];
        for (int i = 0; i < revmax; ++i) {
            rsadata[i] = new ArrayList<>();
            rdsadata[i] = new ArrayList<>();
            rsdata[i] = new ArrayList<>();
            rcdata[i] = new ArrayList<>();
        }
        String ref = SingleRevisionAnalysisProcessor.REF1;
        String refs = InformationSet.item(ref, "s_lin");
        String refsa = InformationSet.item(ref, "sa");
        String refc = InformationSet.item(ref, "cal");
        String refmode = InformationSet.item(ref, "mode");
        String refser = InformationSet.item(ref, "residuals.ser");
        for (int i = 0; i < all.length; ++i) {
            DecompositionMode rmode = all[i].search(refmode, DecompositionMode.class);
            double ser = all[i].search(refser, Double.class);
            TsData rs = all[i].search(refs, TsData.class);
            TsData rsa = all[i].search(refsa, TsData.class);
            TsData rc = all[i].search(refc, TsData.class);
            if (rmode.isMultiplicative()) {
                if (rs != null) {
                    rs = rs.log();
                }
//                if (rsa != null) {
//                    rsa = rsa.log();
//                }
                if (rc != null) {
                    rc = rc.log();
                }
            }
            boolean hass = !isConstant(rs);
            if (hass) {
                for (int j = 0; j < revmax; ++j) {
                    if (j < details[i].length && details[i][j] != null) {
                        DecompositionMode jmode = details[i][j].get("mode", DecompositionMode.class);
                        if (rmode == jmode) {
                            TsData saj = details[i][j].get("sa", TsData.class);
                            int l = saj.getLength() - 1;
                            int d;
                            if (outofsample_) {
                                d = j;
                            } else {
                                d = rsa.getLength() - saj.getLength() - 1;
                            }
//                            if (rmode.isMultiplicative()) {
//                                saj = saj.log();
//                            }
//                            rsadata[d].add((rsa.get(l) - saj.get(l)) / ser);
//                            rdsadata[d].add((rsa.get(l) - rsa.get(l - 1) - saj.get(l) + saj.get(l - 1)) / ser);
                            rsadata[d].add(rsa.get(l) / saj.get(l) - 1);
                            rdsadata[d].add(rsa.get(l) / rsa.get(l - 1) - saj.get(l) / saj.get(l - 1));
                            TsData slinj = details[i][j].get("s_lin", TsData.class);
                            TsData cj = details[i][j].get("cal", TsData.class);
                            boolean hasc = !isConstant(rc) && !isConstant(cj);
                            boolean hasslin = !isConstant(rs) && !isConstant(slinj);
                            if (hasslin) {
                                if (rmode.isMultiplicative()) {
                                    slinj = slinj.log();
                                }
                                rsdata[d].add((rs.get(l) - slinj.get(l)) / ser);
                            }
                            if (hasc) {
                                if (rmode.isMultiplicative()) {
                                    cj = cj.log();
                                }
                                rcdata[d].add((rc.get(l) - cj.get(l)) / ser);
                            }
                        }
                    }
                }
            }
        }
        computeStatistics(rsadata, rdsadata, rsdata, rcdata, revmax);
    }

    private void computeStatistics(List[] rsadata, List[] rdsadata, List[] rsdata, List[] rcdata, int revmax) {
        for (int i = 0; i < revmax; ++i) {
            double[] data = toarray(rsadata[i]);
            sarevs_[i] = data;
            DescriptiveStatistics stats = new DescriptiveStatistics(data);
            samax_[i] = stats.getMax();
            samin_[i] = stats.getMin();
            saavg_[i] = stats.getAverage();
            sastdev_[i] = stats.getRmse();
            san_[i] = stats.getObservationsCount();
            data = toarray(rdsadata[i]);
            dsarevs_[i] = data;
            stats = new DescriptiveStatistics(data);
            dsamax_[i] = stats.getMax();
            dsamin_[i] = stats.getMin();
            dsaavg_[i] = stats.getAverage();
            dsastdev_[i] = stats.getRmse();
            dsan_[i] = stats.getObservationsCount();
            data = toarray(rsdata[i]);
            srevs_[i] = data;
            stats = new DescriptiveStatistics(data);
            smax_[i] = stats.getMax();
            smin_[i] = stats.getMin();
            savg_[i] = stats.getAverage();
            sstdev_[i] = stats.getRmse();
            sn_[i] = stats.getObservationsCount();
            data = toarray(rcdata[i]);
            crevs_[i] = data;
            stats = new DescriptiveStatistics(data);
            cmax_[i] = stats.getMax();
            cmin_[i] = stats.getMin();
            cavg_[i] = stats.getAverage();
            cstdev_[i] = stats.getRmse();
            cn_[i] = stats.getObservationsCount();
        }
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
