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
package ec.benchmarking.simplets;

import ec.benchmarking.ssf.multivariate.*;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.ssf.*;
import ec.tstoolkit.ssf.multivariate.IMultivariateSsf;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.*;
import ec.tstoolkit.utilities.Comparator;
import ec.tstoolkit.utilities.Jdk6;
import ec.tstoolkit.utilities.WeightedItem;
import ec.tstoolkit.utilities.WildCards;
import java.util.*;
import java.util.Map.Entry;

/**
 * Multi-variate benchmarking by means of an generalization of the Cholette's
 * algorithms to the multi-variate case. The model is defined by a set of
 * temporal and/or contemporaneous constrains on a set of time series. The
 * current version doesn't check the coherence of the constraints.
 *
 * A complete processing will follow the steps listed below: 0. Creation of the
 * algorithm: - TsMultiBenchmarking bench=new TsMultiBenchmarking(); -
 * bench.setRho(...); bench.setLambda(...); 1. Definition of the set of time
 * series: bench.addIput(name, ts); 2. Definition of the temporal constraints:
 * bench.addemporalConstraint(nameY, nameQ); 3. Definition of the
 * contemporaneous constraint: - ConstraintDescriptor desc = ... -
 * bench.addContemporaneousConstraint(desc); 4. Processing: bench.process(); 5.
 * Retrieval of the endogenous (benchmarked) series: bench.endogenous(); 6.
 * Retrieval of the benchmarked series: bench.getResult(name);
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class TsMultiBenchmarking {

    /**
     * Description of a contemporaneous constraint. The constraint may be
     * binding y = w1*x1+...+wn*xn or free constant = w1*x1+...+wn*xn
     */
    public final static class ContemporaneousConstraintDescriptor {

        public ContemporaneousConstraintDescriptor(double cnt) {
            constant = cnt;
            constraint = null;
        }

        public ContemporaneousConstraintDescriptor(String cnt) {
            constant = 0;
            constraint = cnt;
        }

        private ContemporaneousConstraintDescriptor(double d, String s) {
            constant = d;
            constraint = s;
        }
        
        public void add(String cmp, double w){
            this.components.add(new WeightedItem<>(cmp, w));
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof ContemporaneousConstraintDescriptor && equals((ContemporaneousConstraintDescriptor) obj));
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 23 * hash + (int) (Double.doubleToLongBits(this.constant) ^ (Double.doubleToLongBits(this.constant) >>> 32));
            hash = 23 * hash + (this.constraint != null ? this.constraint.hashCode() : 0);
            return hash;
        }

        public boolean equals(ContemporaneousConstraintDescriptor c) {
            if (!this.constraint.equals(c.constraint)) {
                return false;
            }
            if (this.constant != c.constant) {
                return false;
            }
            return Comparator.equals(this.components, c.components);
        }
        public final double constant;
        public final String constraint;
        public final List<WeightedItem<String>> components =
                new ArrayList<>();

        public boolean hasWildCards() {
            for (WeightedItem<String> ws : components) {
                if (ws.item.contains("?") || ws.item.contains("*")) {
                    return true;
                }
            }
            return false;
        }

        public ContemporaneousConstraintDescriptor expand(Collection<String> input) {
            ContemporaneousConstraintDescriptor ndesc = new ContemporaneousConstraintDescriptor(constant, constraint);
            for (WeightedItem<String> ws : components) {
                double w = ws.weight;
                if (ws.item.contains("*") || ws.item.contains("?")) {
                    WildCards wc = new WildCards(ws.item);
                    for (String i : input) {
                        if (!i.equals(constraint) && wc.match(i)) {
                            ndesc.components.add(new WeightedItem<>(i, w));
                        }
                    }
                } else {
                    ndesc.components.add(ws);
                }
            }
            return ndesc;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (constraint != null) {
                builder.append(constraint);
            } else {
                builder.append(constant);
            }
            builder.append('=');

            boolean first = true;
            for (WeightedItem<String> ws : components) {
                double w = ws.weight;
                if (w == 0) {
                    continue;
                }
                double aw = Math.abs(w);
                if (w < 0) {
                    builder.append('-');
                } else if (!first) {
                    builder.append('+');
                }
                if (aw != 1) {
                    builder.append(aw).append('*');
                }
                builder.append(ws.item);
                first = false;
            }
            return builder.toString();
        }

        public static ContemporaneousConstraintDescriptor parse(String s) {
            try {
                Scanner scanner = new Scanner(s).useDelimiter("\\s*=\\s*");
                String n = scanner.next();
                // try first double
                double dcnt = 0;
                String scnt = null;
                try {
                    dcnt = Double.parseDouble(n);
                } catch (NumberFormatException err) {
                    scnt = n;
                }
                n = scanner.next();
                ContemporaneousConstraintDescriptor desc = new ContemporaneousConstraintDescriptor(dcnt, scnt);
                if (!parseComponents(n, desc.components)) {
                    return null;
                }
                return desc;
            } catch (Exception err) {
                return null;
            }
        }

        private static boolean parseComponents(String str, List<WeightedItem<String>> cmps) {
            int pos = 0;
            int ppos = 0, mpos = 0;
            while (pos < str.length()) {
                if (Character.isWhitespace(str.charAt(pos))) {
                    ++pos;
                } else {
                    if (ppos >= 0) {
                        ppos = str.indexOf('+', pos + 1);
                    }
                    if (mpos >= 0) {
                        mpos = str.indexOf('-', pos + 1);
                    }
                    int npos;
                    if (ppos < 0) {
                        npos = mpos;
                    } else if (mpos < 0) {
                        npos = ppos;
                    } else {
                        npos = Math.min(ppos, mpos);
                    }
                    if (npos < 0) {
                        npos = str.length();
                    }
                    char c = str.charAt(pos);
                    boolean plus = c != '-';
                    if (c == '-' || c == '+') {
                        ++pos;
                    }
                    String cur = str.substring(pos, npos);
                    int wpos = cur.indexOf('*');
                    if (wpos < 0) {
                        String cmp = cur.trim();
                        cmps.add(new WeightedItem<>(cmp, plus ? 1 : -1));
                    } else {
                        String cmp = cur.substring(wpos + 1).trim();
                        try {
                            double w = Double.parseDouble(cur.substring(0, wpos));
                            cmps.add(new WeightedItem<>(cmp, plus ? w : -w));
                        } catch (NumberFormatException err) {
                            return false;
                        }
                    }
                    pos = npos;
                }
            }
            return true;
        }
    }

    public final static class TemporalConstraintDescriptor {

        public final String aggregate, detail;

        public TemporalConstraintDescriptor(final String constraint, final String component) {
            this.aggregate = constraint;
            this.detail = component;
        }

        public static TemporalConstraintDescriptor parse(String s) {
            try {
                Scanner scanner = new Scanner(s).useDelimiter("\\s*=\\s*");
                String n = scanner.next();
                String cnt = n;
                n = scanner.next();
                String[] fn = function(n);
                if (fn == null) {
                    return null;
                }
                if (!fn[0].equalsIgnoreCase("sum")) {
                    return null;
                }
                String cmp = fn[1];
                return new TemporalConstraintDescriptor(cnt, cmp);
            } catch (Exception err) {
                return null;
            }
        }

        public static String[] function(String str) {
            try {
                String s = str.trim();
                int a0 = s.indexOf('(', 0);
                if (a0 <= 0) {
                    return null;
                }
                int a1 = s.indexOf('(', a0 + 1);
                if (a1 >= 0) {
                    return null;
                }
                a1 = s.indexOf(')', a0 + 1);
                if (a1 != s.length() - 1) {
                    return null;
                }
                return new String[]{s.substring(0, a0), s.substring(a0 + 1, a1)};
            } catch (Exception err) {
                return null;
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(aggregate).append("=sum(")
                    .append(detail).append(')');
            return builder.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof TemporalConstraintDescriptor && equals((TemporalConstraintDescriptor) obj));
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + this.aggregate.hashCode();
            hash = 67 * hash + this.detail.hashCode();
            return hash;
        }

        public boolean equals(TemporalConstraintDescriptor c) {
            return this.aggregate.equals(c.aggregate) && this.detail.equals(c.detail);
        }
    }
    private double rho_ = 0, lambda_ = 0;
    private LinkedHashMap<String, String> tConstraints =
            new LinkedHashMap<>();
    private ArrayList<ContemporaneousConstraintDescriptor> cConstraints =
            new ArrayList<>();
    private LinkedHashMap<String, TsData> inputs = new LinkedHashMap<>();
    private ArrayList<String> lcnt = new ArrayList<>();
    private ArrayList<String> rcnt = new ArrayList<>();
    private double[][] lcntData, rcntData, weights;
    private HashMap<String, TsData> tcntData;
    private ArrayList<Constraint> cs = new ArrayList<>();
    private TsDomain idomain_;
    private TsFrequency tfreq_ = TsFrequency.Undefined;
    private HashMap<String, TsData> bench_;

    public boolean addInput(String name, TsData s) {
        if (inputs.containsKey(name)) {
            return false;
        }
        inputs.put(name, s);
        return true;
    }

    public boolean addContemporaneousConstraint(ContemporaneousConstraintDescriptor cnt) {
        if (!Jdk6.isNullOrEmpty(cnt.constraint) && !contains(cnt.constraint)) {
            return false;
        }
        if (cnt.hasWildCards()) {
            cnt = cnt.expand(inputs.keySet());
        }
        for (WeightedItem<String> ws : cnt.components) {
            if (!contains(ws.item)) {
                return false;
            }
        }
        cConstraints.add(cnt);
        return true;
    }

    @Deprecated
    public boolean addTemporalConstraint(String agg, String detail) {
        return addTemporalConstraint(new TemporalConstraintDescriptor(agg, detail));
    }

    public boolean addTemporalConstraint(TemporalConstraintDescriptor cnt) {
        TsData sagg = inputs.get(cnt.aggregate), sdetail = inputs.get(cnt.detail);
        if (sagg != null && sdetail != null) {
            tConstraints.put(cnt.detail, cnt.aggregate);
            if (sagg.getFrequency() != sdetail.getFrequency()) {
                if (tfreq_ == TsFrequency.Undefined) {
                    tfreq_ = sagg.getFrequency();
                } else if (tfreq_ != sagg.getFrequency()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public double getRho() {
        return rho_;
    }

    public void setRho(double value) {
        rho_ = value;
    }

    public double getLambda() {
        return lambda_;
    }

    public void setLambda(double lambda) {
        lambda_ = lambda;
    }

    public boolean contains(String s) {
        return inputs.containsKey(s);
    }

    public List<String> endogenous() {
        ArrayList<String> list = new ArrayList<>();
        Set<String> bk = bench_.keySet();
        for (String s : inputs.keySet()) {
            if (bk.contains(s)) {
                list.add(s);
            }
        }
        return list;
    }
    public Set<String> input() {
        return inputs.keySet();
    }

    /**
     * Gets the original series corresponding to a given name
     *
     * @param s
     * @return
     */
    public TsData getInput(String s) {
        return inputs.get(s);
    }

    /**
     * Gets the benchmarked series corresponding to a given name
     *
     * @param s
     * @return
     */
    public TsData getResult(String s) {
        return bench_ == null ? null : bench_.get(s);
    }

    /**
     * @param args the command line arguments
     */
    public boolean process() {

        if (cConstraints.isEmpty() && tConstraints.isEmpty()) {
            return false;
        }
        bench_ = new HashMap<>();
        TsData[] bench;
        if (cConstraints.isEmpty()) {
            bench = computeUnivariate();
        } else if (tConstraints.isEmpty()) {
            bench = computeMultivariate();
        } else {
            bench = compute();
        }
        if (bench == null) {
            return false;
        }
        for (int i = 0; i < bench.length; ++i) {
            bench_.put(rcnt.get(i), bench[i]);
        }
        return true;
    }

    private TsData[] compute() {
        if (!buildMaps()) {
            return null;
        }
        benchmarkIndependentConstraints();
        if (!buildDomain()) {
            return null;
        }
        buildEndogeneousData();
        // compute weights, adjust constraints...
        buildWeights();
        buildConstraints();

        int hfreq = idomain_.getFrequency().intValue();
        int lfreq = tfreq_.intValue();
        int c = hfreq / lfreq;

        IMultivariateSsf ssf;
        if (rho_ < 1) {
            MultivariateSsfCholette cssf = new MultivariateSsfCholette(c, rho_, weights);
            cssf.setConstraints(cs);
            ssf = cssf;
        } else {
            MultivariateSsfDenton cssf = new MultivariateSsfDenton(c, weights);
            cssf.setConstraints(cs);
            ssf = cssf;
        }

        // build the observations
        ec.tstoolkit.ssf.multivariate.FullM2uMap map = new ec.tstoolkit.ssf.multivariate.FullM2uMap(rcnt.size() + cs.size());
        ec.tstoolkit.ssf.multivariate.M2uSsfAdapter adapter =
                new ec.tstoolkit.ssf.multivariate.M2uSsfAdapter(ssf, map);
        int len = idomain_.getLength();
        int nvars = rcnt.size(), ncnts = cs.size();
        Matrix M = new Matrix(nvars + ncnts, len);
        M.set(Double.NaN);

        // fill the matrix: first rows with temporal constraints
        for (int i = 0; i < nvars; ++i) {
            if (tConstraints.containsKey(rcnt.get(i))) {
                TsData a = tcntData.get(rcnt.get(i));
                DataBlock b = M.row(i).extract(c - 1, a.getLength(), c);
                b.copy(a.getValues());
            }
        }
        for (int i = 0; i < ncnts; ++i) {
            DataBlock row = M.row(i + nvars);
            row.copyFrom(lcntData[i], 0);
            // avoid redundancy with temporal constraints. It is no longer a problem !!
            /*if (hasTemporalConstraint(cConstraints.get(i))) {
                DataBlock b = row.extract(c - 1, len / c, c);
                b.set(Double.NaN);
            }*/
        }

        ec.tstoolkit.ssf.DisturbanceSmoother dsmoother = new ec.tstoolkit.ssf.DisturbanceSmoother();
        dsmoother.setSsf(adapter);
        ec.tstoolkit.ssf.multivariate.M2uData data = new ec.tstoolkit.ssf.multivariate.M2uData(M, null);
        SmoothingResults states;
//        ec.tstoolkit.ssf.Smoother smoother = new ec.tstoolkit.ssf.Smoother();
//        smoother.setSsf(adapter);
//        smoother.setCalcVar(false);
//        SmoothingResults states = new SmoothingResults();
        if (adapter.isDiffuse()) {
            Filter<ISsf> filter = new Filter<>();
            filter.setInitializer(new DiffuseSquareRootInitializer());
            filter.setSsf(adapter);
            DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
            frslts.getFilteredData().setSavingA(data.hasData());
            frslts.getVarianceFilter().setSavingP(false);
            filter.process(data, frslts);
            dsmoother.process(data, frslts);
            states = dsmoother.calcSmoothedStates();
        } else {
            dsmoother.process(data);
            states = dsmoother.calcSmoothedStates();
        }



        int neq = nvars + ncnts;
        TsData[] y = new TsData[rcnt.size()];
        for (int i = 0; i < y.length; ++i) {
            TsData s = getInput(rcnt.get(i));
            TsDataBlock sc = TsDataBlock.select(s, idomain_);
            y[i] = new TsData(idomain_);
            double[] t = states.component(2 * i + 1);
            for (int j = 0; j < s.getLength(); ++j) {
                y[i].set(j, sc.data.get(j) + t[j * neq] * weights[i][j]);
            }
        }
        return y;
    }

    private boolean hasTemporalConstraint(ContemporaneousConstraintDescriptor cnt) {
        for (WeightedItem<String> ws : cnt.components) {
            if (!tConstraints.containsKey(ws.item)) {
                return false;
            }
        }
        return true;
    }

    private TsData[] computeUnivariate() {
        ArrayList<TsData> bench = new ArrayList<>();
        TsCholette cholette = new TsCholette();
        cholette.setRho(rho_);
        cholette.setLambda(lambda_);
        for (Entry<String, String> cur : tConstraints.entrySet()) {
            TsData q = getInput(cur.getKey());
            TsData a = getInput(cur.getValue());
            if (a != null && q != null) {
                if (a.getFrequency() == q.getFrequency()) {
                    a = a.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
                }
                TsData b = cholette.process(q, a);
                if (b != null) {
                    bench.add(b);
                    rcnt.add(cur.getKey());
                }
            }
        }
        TsData[] r = new TsData[bench.size()];
        bench.toArray(r);
        return r;
    }

    private boolean buildMaps() {
        // first of all, we go through the constraints to get information on the used series
        for (int i = 0; i < cConstraints.size(); ++i) {
            ContemporaneousConstraintDescriptor desc = cConstraints.get(i);
            if (!Jdk6.isNullOrEmpty(desc.constraint)) {
                if (rcnt.contains(desc.constraint)) {
                    return false;
                }
                if (!lcnt.contains(desc.constraint)) {
                    lcnt.add(desc.constraint);
                }
            }
            for (WeightedItem<String> wc : desc.components) {
                if (lcnt.contains(wc.item)) {
                    return false;
                } else if (!rcnt.contains(wc.item)) {
                    rcnt.add(wc.item);
                }
            }
        }
        // we create the actual constraints
        for (int i = 0; i < cConstraints.size(); ++i) {
            ContemporaneousConstraintDescriptor desc = cConstraints.get(i);
            HashMap<Integer, Double> constraint = new HashMap<>();
            for (WeightedItem<String> cur : desc.components) {
                constraint.put(rcnt.indexOf(cur.item), cur.weight);
            }
            Constraint acnt = new Constraint(constraint);
            cs.add(acnt);
        }

        lcntData = new double[cConstraints.size()][];
        for (int i = 0; i < cConstraints.size(); ++i) {
            ContemporaneousConstraintDescriptor desc = cConstraints.get(i);
            if (!Jdk6.isNullOrEmpty(desc.constraint)) {
                TsData s = getInput(desc.constraint);
                lcntData[i] = s.getValues().internalStorage().clone();
            } else {
                lcntData[i] = new double[]{desc.constant};
            }
        }
        return true;
    }

    private void benchmarkIndependentConstraints() {
        if (tfreq_ == TsFrequency.Undefined) {
            tfreq_ = TsFrequency.Yearly;
        }
        TsCholette cholette = new TsCholette();
        cholette.setRho(rho_);
        cholette.setLambda(lambda_);
        for (String s : tConstraints.keySet()) {
            if (!rcnt.contains(s)) {
                // update inputs with Cholette !!!
                TsData q = getInput(s);
                TsData a = getInput(tConstraints.get(s));
                if (a != null && q != null) {
                    if (a.getFrequency() == q.getFrequency()) {
                        a = a.changeFrequency(tfreq_, TsAggregationType.Sum, true);
                    }
                    TsData b = cholette.process(q, a);
                    if (b != null) {
                        bench_.put(s, b);
                    }
                }
            }
        }
    }

    private boolean buildDomain() {

        for (String cur : rcnt) {
            TsDomain d = getInput(cur).getDomain();
            if (idomain_ == null) {
                idomain_ = d;
            } else if (idomain_.getFrequency() != d.getFrequency()) {
                return false;
            } else {
                idomain_ = idomain_.intersection(d);
            }
        }
        return !idomain_.isEmpty();
    }

    private void buildEndogeneousData() {
        rcntData = new double[rcnt.size()][];
        for (int i = 0; i < rcnt.size(); ++i) {
            TsData s = getInput(rcnt.get(i)).fittoDomain(idomain_);
            rcntData[i] = s.getValues().internalStorage();
        }
    }

    private void buildWeights() {
        weights = new double[rcntData.length][];
        for (int i = 0; i < weights.length; ++i) {
            weights[i] = calcWeights(rcntData[i]);
        }
    }

    private void buildConstraints() {
        buildContemporaneousConstraints();
        buildTemporalConstraints();
    }

    private void buildContemporaneousConstraints() {
        for (int i = 0; i < cs.size(); ++i) {
            Constraint cur = cs.get(i);
            DataBlock z;
            if (lcntData[i].length == 1) {
                z = new DataBlock(idomain_.getLength());
                z.set(lcntData[i][0]);
                lcntData[i] = z.getData();
            } else {
                z = new DataBlock(lcntData[i]);
            }

            for (int j = 0; j < cur.index.length; ++j) {
                z.addAY(-cur.weights[j], new DataBlock(rcntData[cur.index[j]]));
            }
        }
    }

    private void buildTemporalConstraints() {
        tcntData = new HashMap<>();
        TsDomain tdom = idomain_.changeFrequency(tfreq_, true);
        for (int i = 0; i < rcnt.size(); ++i) {
            String n = rcnt.get(i);
            if (tConstraints.containsKey(n)) {
                TsData orig = getInput(rcnt.get(i));
                TsData cur = getInput(tConstraints.get(rcnt.get(i)));
                if (cur.getFrequency() == orig.getFrequency()) {
                    cur = cur.changeFrequency(tfreq_, TsAggregationType.Sum, true);
                }
                TsData a = orig.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
                a = cur.minus(a);
                a = a.fittoDomain(tdom);
                tcntData.put(n, a);
            }
        }
    }

    private TsData[] computeMultivariate() {

        if (!buildMaps()) {
            return null;
        }
        if (!buildDomain()) {
            return null;
        }
        buildEndogeneousData();
        // compute weights, adjust constraints...
        buildWeights();
        buildContemporaneousConstraints();

        IMultivariateSsf ssf;
        if (rho_ < 1) {
            ContemporaneousSsfCholette cssf = new ContemporaneousSsfCholette(rho_, weights);
            cssf.setConstraints(cs);
            ssf = cssf;
        } else {
            ContemporaneousSsfDenton cssf = new ContemporaneousSsfDenton(weights);
            cssf.setConstraints(cs);
            ssf = cssf;
        }

        // build the observations
        ec.tstoolkit.ssf.multivariate.FullM2uMap map = new ec.tstoolkit.ssf.multivariate.FullM2uMap(cs.size());
        ec.tstoolkit.ssf.multivariate.M2uSsfAdapter adapter =
                new ec.tstoolkit.ssf.multivariate.M2uSsfAdapter(ssf, map);
        ec.tstoolkit.ssf.multivariate.M2uData data = new ec.tstoolkit.ssf.multivariate.M2uData(lcntData, null);
        ec.tstoolkit.ssf.DisturbanceSmoother dsmoother = new ec.tstoolkit.ssf.DisturbanceSmoother();
        dsmoother.setSsf(adapter);
        SmoothingResults states;
//        ec.tstoolkit.ssf.Smoother smoother = new ec.tstoolkit.ssf.Smoother();
//        smoother.setSsf(adapter);
//        smoother.setCalcVar(false);
//        SmoothingResults states = new SmoothingResults();
        if (adapter.isDiffuse()) {
            Filter<ISsf> filter = new Filter<>();
            filter.setInitializer(new DiffuseSquareRootInitializer());
            filter.setSsf(adapter);
            DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
            frslts.getFilteredData().setSavingA(data.hasData());
            frslts.getVarianceFilter().setSavingP(false);
            filter.process(data, frslts);
//            smoother.process(data, frslts, states);
            dsmoother.process(data, frslts);
            states = dsmoother.calcSmoothedStates();
        } else {
            dsmoother.process(data);
            states = dsmoother.calcSmoothedStates();
//            smoother.process(data, states);
        }

        int neq = cs.size();
        TsData[] y = new TsData[rcnt.size()];
        for (int i = 0; i < y.length; ++i) {
            TsData s = getInput(rcnt.get(i));
            y[i] = new TsData(s.getDomain());
            double[] t = states.component(i);
            for (int j = 0; j < s.getLength(); ++j) {
                y[i].set(j, t[j * neq] * weights[i][j]);
            }
            y[i] = TsData.add(y[i], s);
        }
        return y;
    }

    private double[] calcWeights(double[] x) {
        if (lambda_ == 1) {
            return x;
        }
        double[] w = new double[x.length];
        if (lambda_ == 0) {
            for (int i = 0; i < w.length; ++i) {
                w[i] = 1;
            }
        } else if (lambda_ == 0.5) {
            for (int i = 0; i < w.length; ++i) {
                w[i] = Math.sqrt(Math.abs(x[i]));
            }
        } else {
            for (int i = 0; i < w.length; ++i) {
                w[i] = Math.pow(Math.abs(x[i]), lambda_);
            }
        }
        return w;
    }
}
