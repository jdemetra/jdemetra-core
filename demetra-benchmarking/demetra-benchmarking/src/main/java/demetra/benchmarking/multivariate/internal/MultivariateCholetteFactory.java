/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.benchmarking.multivariate.internal;

import demetra.benchmarking.multivariate.MultivariateCholetteSpecification.ContemporaneousConstraintDescriptor;
import demetra.benchmarking.spi.MultivariateCholetteAlgorithm;
import demetra.benchmarking.multivariate.MultivariateCholetteSpecification;
import demetra.benchmarking.multivariate.MultivariateCholetteSpecification.TemporalConstraintDescriptor;
import demetra.benchmarking.univariate.CholetteSpecification;
import demetra.benchmarking.univariate.internal.CholetteFactory;
import demetra.data.AggregationType;
import demetra.data.DataBlock;
import demetra.data.DataBlockStorage;
import demetra.data.DoubleSequence;
import demetra.maths.matrices.Matrix;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.M2uAdapter;
import demetra.ssf.multivariate.SsfMatrix;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsUnit;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDataConverter;
import demetra.timeseries.simplets.TsDataToolkit;
import demetra.timeseries.simplets.TsDataView;
import demetra.utilities.WeightedItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ServiceProvider(service = MultivariateCholetteAlgorithm.class)
public class MultivariateCholetteFactory implements MultivariateCholetteAlgorithm {

    @Override
    public Map<String, TsData> benchmark(Map<String, TsData> inputs, MultivariateCholetteSpecification spec) {
        Impl impl = new Impl();
        return impl.benchmark(inputs, spec);
    }

    static class Impl {

        /**
         * Inputs
         */
        private final LinkedHashMap<String, TsData> inputs = new LinkedHashMap<>();
        /**
         * List of the contemporaneous constraints
         */
        private final List<ContemporaneousConstraintDescriptor> contemporaneousConstraints
                = new ArrayList<>();
        /**
         * Map of the temporal constraints The map constains pairs of (detail,
         * aggregated series)
         */
        private final Map<String, String> temporalConstraints
                = new HashMap<>();
        /**
         * List of exogeneous series (not benchmarked) used in the
         * contemporaneous constraints, which appear in the left-side of
         * contemporaneous definitions
         */
        private final ArrayList<String> lcnt = new ArrayList<>();
        /**
         * List of endogeneous series (benchmarked) used in the contemporaneous
         * constraints, which appear in the right-side of contemporaneous
         * definitions.
         *
         */
        private final ArrayList<String> rcnt = new ArrayList<>();
        /**
         * Data of the series in the lcnt list. The length of each array is
         * equal to the length of idomain
         */
        private double[][] lcntData;
        /**
         * Data of the series in the rcnt list The length of each array is equal
         * to the length of idomain
         */
        private double[][] rcntData;
        /**
         *
         */
        private double[][] weights;
        private final HashMap<String, TsData> tcntData = new HashMap<>();
        private Constraint[] cs;
        private double rho, lambda;
        private RegularDomain idomain;
        private TsUnit aggUnit;

        Map<String, TsData> benchmark(Map<String, TsData> inputs, MultivariateCholetteSpecification spec) {
            loadInfo(inputs, spec);

            Map<String, TsData> rslts = new HashMap<>();
            buildMaps();

            benchmarkIndependentConstraints(rslts);
            if (contemporaneousConstraints.isEmpty()) {
                return rslts;
            }

            buildDomain();
            buildEndogeneousData();
            // compute weights, adjust constraints...
            buildWeights();
            buildConstraints();
            if (temporalConstraints.isEmpty()) {
                computeContemporaneous(rslts);
            } else {
                compute(rslts);
            }

            return rslts;
        }

        private void loadInfo(Map<String, TsData> data, MultivariateCholetteSpecification spec) {
            // inputs
            inputs.putAll(data);
            // temporal constraints
            for (TemporalConstraintDescriptor desc : spec.getTemporalConstraints()) {
                addTemporalConstraint(desc);
            }

            // contemporaneous constraints
            for (ContemporaneousConstraintDescriptor desc : spec.getContemporaneousConstraints()) {
                addContemporaneousConstraint(desc);
            }
            rho = spec.getRho();
            lambda = spec.getLambda();
        }

        private void addContemporaneousConstraint(ContemporaneousConstraintDescriptor cnt) {
            if (cnt.constraint != null && !inputs.containsKey(cnt.constraint)) {
                throw new IllegalArgumentException("Invalid contemporaneous constraint: " + cnt.constraint);
            }
            if (cnt.hasWildCards()) {
                cnt = cnt.expand(inputs.keySet());
            }
            for (WeightedItem<String> ws : cnt.components) {
                if (!inputs.containsKey(ws.item)) {
                    throw new IllegalArgumentException("Invalid contemporaneous constraint: " + ws.item);
                }
            }
            contemporaneousConstraints.add(cnt);
        }

        private void addTemporalConstraint(TemporalConstraintDescriptor cnt) {
            TsData sagg = inputs.get(cnt.aggregate), sdetail = inputs.get(cnt.detail);
            if (sagg == null || sdetail == null) {
                throw new IllegalArgumentException("Invalid temporal constraint: " + cnt.aggregate);
            }
            temporalConstraints.put(cnt.detail, cnt.aggregate);
        }

        private void benchmarkIndependentConstraints(Map<String, TsData> rslts) {

            CholetteSpecification uspec = new CholetteSpecification();
            uspec.setAggregationType(AggregationType.Sum);
            uspec.setBias(CholetteSpecification.BiasCorrection.None);
            uspec.setLambda(lambda);
            uspec.setRho(rho);
            CholetteFactory cf = new CholetteFactory();
            temporalConstraints.forEach(
                    (detail, agg) -> {
                        if (!rcnt.contains(detail)) {
                            TsData q = inputs.get(detail);
                            TsData a = inputs.get(agg);
                            TsData b = cf.benchmark(q, a, uspec);
                            if (b != null) {
                                rslts.put(detail, b);
                            }
                        }
                    });
        }

        private void computeContemporaneous(Map<String, TsData> rslts) {

            // compute weights, adjust constraints...
            int nvars = rcnt.size(), ncnts = cs.length;;
            IMultivariateSsf ssf = ContemporaneousSsfCholette.builder(nvars)
                    .rho(rho)
                    .weights(weights)
                    .constraints(cs)
                    .build();

            // build the observations
            Matrix M = Matrix.make(idomain.getLength(), ncnts);
            for (int i = 0; i < ncnts; ++i) {
                M.column(i).copyFrom(lcntData[i], 0);
            }

            ISsf ussf = M2uAdapter.of(ssf);
            ISsfData udata = M2uAdapter.of(new SsfMatrix(M));
            DataBlockStorage states = DkToolkit.fastSmooth(ussf, udata);

            int neq = cs.length;
            for (int i = 0; i < nvars; ++i) {
                TsData s = inputs.get(rcnt.get(i));
                TsData sc=TsDataToolkit.fitToDomain(s, idomain);
                double[] y = sc.values().toArray();
                DataBlock t = states.item(i);
                for (int j = 0; j < y.length; ++j) {
                    y[j] += t.get(j * neq) * weights[i][j];
                }
                rslts.put(rcnt.get(i), TsData.ofInternal(idomain.getStartPeriod(), y));
            }
        }

        private void buildMaps() {
            // first of all, we go through the constraints to get information on the used series
            for (ContemporaneousConstraintDescriptor desc : contemporaneousConstraints) {
                if (rcnt.contains(desc.constraint)) {
                    throw new IllegalArgumentException("Binding constraint cannot be used in definitions: " + desc.constraint);
                }
                // TODO Deal with such cases. Use "extended names" and modify the current constraint
                if (!lcnt.contains(desc.constraint)) {
                    lcnt.add(desc.constraint);
                }
                for (WeightedItem<String> wc : desc.components) {
                    if (lcnt.contains(wc.item)) {
                        throw new IllegalArgumentException("Component definition cannot be a constraint: " + wc.item);
                    } else if (!rcnt.contains(wc.item)) {
                        rcnt.add(wc.item);
                    }
                }
            }
            // we create the actual constraints
            cs = new Constraint[contemporaneousConstraints.size()];
            int pos = 0;
            for (ContemporaneousConstraintDescriptor desc : contemporaneousConstraints) {
                HashMap<Integer, Double> constraint = new HashMap<>();
                for (WeightedItem<String> cur : desc.components) {
                    constraint.put(rcnt.indexOf(cur.item), cur.weight);
                }
                Constraint acnt = new Constraint(constraint);
                cs[pos++] = acnt;
            }

            lcntData = new double[contemporaneousConstraints.size()][];
            for (int i = 0; i < contemporaneousConstraints.size(); ++i) {
                ContemporaneousConstraintDescriptor desc = contemporaneousConstraints.get(i);
                if (desc.constraint != null) {
                    TsData s = inputs.get(desc.constraint);
                    lcntData[i] = s.values().toArray();
                } else {
                    lcntData[i] = new double[]{desc.constant};
                }
            }
        }

        private void buildDomain() {

            for (String cur : rcnt) {
                RegularDomain d = inputs.get(cur).domain();
                if (idomain == null) {
                    idomain = d;
                } else if (!idomain.getTsUnit().equals(d.getTsUnit())) {
                    throw new TsException(TsException.INCOMPATIBLE_FREQ);
                } else {
                    idomain = idomain.intersection(d);
                    if (idomain.isEmpty()) {
                        throw new TsException(TsException.DOMAIN_EMPTY);
                    }
                }
            }
        }

        private void buildEndogeneousData() {
            rcntData = new double[rcnt.size()][];
            for (int i = 0; i < rcnt.size(); ++i) {
                TsData s = TsDataToolkit.fitToDomain(inputs.get(rcnt.get(i)), idomain);
                rcntData[i] = s.values().toArray();
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
            for (int i = 0; i < cs.length; ++i) {
                Constraint cur = cs[i];
                if (lcntData[i].length == 1) { // expand the constants
                    double c = lcntData[i][0];
                    lcntData[i] = new double[idomain.getLength()];
                    for (int j = 0; j < lcntData.length; ++j) {
                        lcntData[i][j] = c;
                    }
                }
                // correct the constraints to fit the state space representation
                DataBlock z = DataBlock.ofInternal(lcntData[i]);
                for (int j = 0; j < cur.index.length; ++j) {
                    z.addAY(-cur.weights[j], DataBlock.ofInternal(rcntData[cur.index[j]]));
                }
            }
        }

        private void buildTemporalConstraints() {
            aggUnit = null;
            for (int i = 0; i < rcnt.size(); ++i) {
                String n = rcnt.get(i);
                TsData orig = inputs.get(rcnt.get(i));
                TsData cur = inputs.get(temporalConstraints.get(rcnt.get(i)));
                if (cur != null) {
                    if (aggUnit == null) {
                        aggUnit = cur.getTsUnit();
                    } else if (!aggUnit.equals(cur.getTsUnit())) {
                        throw new TsException(TsException.INCOMPATIBLE_FREQ);
                    }
                    TsData origc = TsDataToolkit.fitToDomain(orig, idomain);
                    TsData a = TsDataConverter.changeTsUnit(origc, cur.getTsUnit(), AggregationType.Sum, true);
                    a = TsDataToolkit.subtract(cur, a);
                    tcntData.put(n, a);
                }
            }
        }

        private double[] calcWeights(double[] x) {
            if (lambda == 1) {
                return x;
            }
            double[] w = new double[x.length];
            if (lambda == 0) {
                for (int i = 0; i < w.length; ++i) {
                    w[i] = 1;
                }
            } else if (lambda == 0.5) {
                for (int i = 0; i < w.length; ++i) {
                    w[i] = Math.sqrt(Math.abs(x[i]));
                }
            } else {
                for (int i = 0; i < w.length; ++i) {
                    w[i] = Math.pow(Math.abs(x[i]), lambda);
                }
            }
            return w;
        }

        private void compute(Map<String, TsData> rslts) {
            int c = idomain.getTsUnit().ratioOf(aggUnit);
            int nvars = rcnt.size(), ncnts = cs.length;
            int len = idomain.getLength();

            IMultivariateSsf ssf = MultivariateSsfCholette.builder(nvars)
                    .conversion(c)
                    .rho(rho)
                    .constraints(cs)
                    .weights(weights)
                    .build();
            // build the observations
            Matrix M = Matrix.make(len, nvars + ncnts);
            M.set(Double.NaN);

            // fill the matrix: first rows with temporal constraints
            for (int i = 0; i < nvars; ++i) {
                if (temporalConstraints.containsKey(rcnt.get(i))) {
                    TsData a = tcntData.get(rcnt.get(i));
                    DataBlock b = M.column(i).extract(c - 1, a.length(), c);
                    b.copy(a.values());
                }
            }
            for (int i = 0; i < ncnts; ++i) {
                DataBlock row = M.column(i + nvars);
                row.copyFrom(lcntData[i], 0);
            }
            ISsf adapter = M2uAdapter.of(ssf);
            ISsfData data = M2uAdapter.of(new SsfMatrix(M));
            DataBlockStorage states = DkToolkit.fastSmooth(adapter, data);

            int neq = nvars + ncnts;
            for (int i = 0; i < rcnt.size(); ++i) {
                TsData s = inputs.get(rcnt.get(i));
                TsDataView sc = TsDataView.select(s, idomain);
                double[] y = sc.getData().toArray();
                DoubleSequence t = states.item(2 * i + 1);
                for (int j = 0; j < y.length; ++j) {
                    y[j] += t.get(j * neq) * weights[i][j];
                }
                rslts.put(rcnt.get(i), TsData.ofInternal(sc.getStart(), y));
            }
        }
    }

}
