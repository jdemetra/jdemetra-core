/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package rssf;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.information.InformationMapping;
import demetra.likelihood.ILikelihood;
import demetra.maths.MatrixType;
import demetra.maths.matrices.Matrix;
import demetra.msts.MstsMapping;
import demetra.msts.MstsMonitor;
import demetra.processing.IProcResults;
import demetra.ssf.StateInfo;
import demetra.ssf.StateStorage;
import demetra.ssf.akf.AkfToolkit;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import demetra.ssf.multivariate.M2uAdapter;
import demetra.ssf.multivariate.SsfMatrix;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class CompositeModel {

    public static class Estimation implements IProcResults {

        static Estimation estimate(CompositeModel model, Matrix data, double eps, boolean marginal) {
            Estimation rslt = new Estimation();
            rslt.data = data;
            MstsMonitor monitor = MstsMonitor.builder()
                    .marginalLikelihood(marginal)
                    .precision(eps)
                    .build();
            monitor.process(data, model.getMapping(), null);
            rslt.likelihood = monitor.getLikelihood();
            rslt.ssf = monitor.getSsf();
            rslt.cmpPos = rslt.getSsf().componentsPosition();
            rslt.parameters = monitor.getParameters().toArray();
            rslt.fullParameters = monitor.fullParameters().toArray();
            rslt.parametersName = model.getMapping().parametersName();
            return rslt;
        }

        static Estimation compute(CompositeModel model, Matrix data, DoubleSequence fullParameters, boolean marginal) {
            Estimation rslt = new Estimation();
            rslt.data = data;
            rslt.fullParameters = fullParameters.toArray();
            DoubleSequence fp = model.getMapping().functionParameters(fullParameters);
            rslt.parameters = fp.toArray();
            rslt.ssf = model.getMapping().map(fp);
            rslt.cmpPos = rslt.getSsf().componentsPosition();
            rslt.parametersName = model.getMapping().parametersName();
            if (marginal) {
                rslt.likelihood = AkfToolkit.marginalLikelihoodComputer().
                        compute(M2uAdapter.of(rslt.getSsf()), M2uAdapter.of(new SsfMatrix(data)));
            } else {
                rslt.likelihood = DkToolkit.likelihood(rslt.getSsf(), new SsfMatrix(data));
            }
            return rslt;
        }

        private ILikelihood likelihood;
        private MultivariateCompositeSsf ssf;
        private int[] cmpPos;
        private Matrix data;
        private double[] fullParameters, parameters;
        private String[] parametersName;
        private StateStorage smoothedStates, filteredStates;

        StateStorage getSmoothedStates() {
            if (smoothedStates == null) {
                smoothedStates = DkToolkit.smooth(getSsf(), new SsfMatrix(getData()), true);
            }
            return smoothedStates;
        }

        StateStorage getFilteredStates() {
            if (filteredStates == null) {

                ISsf ussf = M2uAdapter.of(ssf);
                ISsfData udata = M2uAdapter.of(new SsfMatrix(data));
                DefaultDiffuseSquareRootFilteringResults fr = DkToolkit.sqrtFilter(ussf, udata, true);
                StateStorage ss = StateStorage.full(StateInfo.Forecast);
                int m = data.getColumnsCount(), n = data.getRowsCount();
                ss.prepare(ussf.getStateDim(), 0, n);
                int nd = fr.getEndDiffusePosition() / m;
                if (fr.getEndDiffusePosition()%m != 0)
                    ++nd;
                for (int i = nd; i < n; ++i) {
                    ss.save(i, fr.a(i * m), fr.P(i * m));
                }
                for (int i = 0; i < nd; ++i) {
                    ss.a(i).set(Double.NaN);
                    ss.P(i).set(Double.NaN);
                }
                ss.rescaleVariances(likelihood.sigma());
                filteredStates = ss;
            }
            return filteredStates;
        }

        private static final InformationMapping<Estimation> MAPPING = new InformationMapping<>(Estimation.class);

        static {
            MAPPING.set("loglikelihood", Double.class, source -> source.getLikelihood().logLikelihood());
            MAPPING.set("scalingfactor", Double.class, source -> source.getLikelihood().sigma());
            MAPPING.set("ssf.ncmps", Integer.class, source -> source.getCmpPos().length);
            MAPPING.set("ssf.cmppos", int[].class, source -> source.getCmpPos());
            MAPPING.set("parameters", double[].class, source -> source.getFullParameters());
            MAPPING.set("parametersname", String[].class, source -> source.getParametersName());
            MAPPING.set("fn.parameters", double[].class, source -> source.getParameters());
            MAPPING.setArray("ssf.T", 0, 10000, MatrixType.class, (source, t) -> {
                int dim = source.getSsf().getStateDim();
                Matrix T = Matrix.square(dim);
                source.getSsf().dynamics().T(t, T);
                return T;
            });
            MAPPING.setArray("ssf.V", 0, 10000, MatrixType.class, (source, t) -> {
                int dim = source.getSsf().getStateDim();
                Matrix V = Matrix.square(dim);
                source.getSsf().dynamics().V(t, V);
                return V;
            });
            MAPPING.setArray("ssf.Z", 0, 10000, MatrixType.class, (source, t) -> {
                int dim = source.getSsf().getStateDim();
                int m = source.getSsf().measurementsCount();
                Matrix M = Matrix.make(m, dim);
                for (int i = 0; i < m; ++i) {
                    source.getSsf().loading(i).Z(t, M.row(i));
                }
                return M;
            });
            MAPPING.set("ssf.T", MatrixType.class, source -> {
                if (!source.getSsf().dynamics().isTimeInvariant()) {
                    return null;
                }
                int dim = source.getSsf().getStateDim();
                Matrix T = Matrix.square(dim);
                source.getSsf().dynamics().T(0, T);
                return T;
            });
            MAPPING.set("ssf.V", MatrixType.class, source -> {
                if (!source.getSsf().dynamics().isTimeInvariant()) {
                    return null;
                }
                int dim = source.getSsf().getStateDim();
                Matrix V = Matrix.square(dim);
                source.getSsf().dynamics().V(0, V);
                return V;
            });
            MAPPING.set("ssf.P0", MatrixType.class, source -> {
                int dim = source.getSsf().getStateDim();
                Matrix V = Matrix.square(dim);
                source.getSsf().initialization().Pf0(V);
                return V;
            });
            MAPPING.set("ssf.B0", MatrixType.class, source -> {
                int dim = source.getSsf().getStateDim();
                int nd = source.getSsf().initialization().getDiffuseDim();
                Matrix V = Matrix.make(dim, nd);
                source.getSsf().initialization().diffuseConstraints(V);
                return V;
            });
            MAPPING.set("ssf.Z", MatrixType.class, source -> {
                if (!source.getSsf().measurements().isTimeInvariant()) {
                    return null;
                }
                int dim = source.getSsf().getStateDim();
                int m = source.getSsf().measurementsCount();
                Matrix M = Matrix.make(m, dim);
                for (int i = 0; i < m; ++i) {
                    source.getSsf().loading(i).Z(0, M.row(i));
                }
                return M;
            });
            MAPPING.setArray("ssf.smoothing.array", 0, 1000, double[].class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.getComponent(p).toArray();
            });
            MAPPING.setArray("ssf.smoothing.varray", 0, 1000, double[].class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.getComponentVariance(p).toArray();
            });
            MAPPING.setArray("ssf.smoothing.cmp", 0, 100, double[].class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.getComponent(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.smoothing.vcmp", 0, 100, double[].class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.getComponentVariance(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.filtering.array", 0, 1000, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponent(p).toArray();
            });
            MAPPING.setArray("ssf.filtering.varray", 0, 1000, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponentVariance(p).toArray();
            });
            MAPPING.setArray("ssf.filtering.cmp", 0, 100, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponent(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.filtering.vcmp", 0, 100, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponentVariance(source.getCmpPos()[p]).toArray();
            });
        }

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        public static final InformationMapping<Estimation> getMapping() {
            return MAPPING;
        }

        /**
         * @return the likelihood
         */
        ILikelihood getLikelihood() {
            return likelihood;
        }

        /**
         * @return the ssf
         */
        MultivariateCompositeSsf getSsf() {
            return ssf;
        }

        /**
         * @return the cmpPos
         */
        int[] getCmpPos() {
            return cmpPos;
        }

        /**
         * @return the data
         */
        Matrix getData() {
            return data;
        }

        /**
         * @return the fullParameters
         */
        double[] getFullParameters() {
            return fullParameters;
        }

        /**
         * @return the parameters
         */
        double[] getParameters() {
            return parameters;
        }

        String[] getParametersName() {
            return parametersName;
        }
    }

    private MstsMapping mapping;
    private final List<ModelItem> items = new ArrayList<>();
    private final List<ModelEquation> equations = new ArrayList<>();

    public int getEquationsCount() {
        return equations.size();
    }

    public int getItemsCount() {
        return items.size();
    }

    public ModelItem getItem(int pos) {
        return items.get(pos);
    }

    public ModelEquation getEquation(int pos) {
        return equations.get(pos);
    }

    public void add(ModelItem item) {
        this.items.add(item);
        mapping = null;
    }

    public void add(ModelEquation eq) {
        this.equations.add(eq);
        mapping = null;
    }

    MstsMapping getMapping() {
        return mapping;
    }

    public void build() {
        mapping = new MstsMapping();
        for (ModelItem item : items) {
            item.addTo(mapping);
        }
        for (ModelEquation eq : equations) {
            eq.addTo(mapping);
        }
    }

    public double[] defaultParameters() {
        if (mapping == null) {
            build();
        }
        return mapping.getDefaultParameters().toArray();
    }

    public double[] fullDefaultParameters() {
        if (mapping == null) {
            build();
        }
        return mapping.trueParameters(mapping.getDefaultParameters()).toArray();
    }

    public Estimation estimate(MatrixType data, double eps, boolean marginal) {
        if (mapping == null) {
            build();
        }
        return Estimation.estimate(this, Matrix.of(data), eps, marginal);
    }

    public Estimation compute(MatrixType data, double[] parameters, boolean marginal) {
        if (mapping == null) {
            build();
        }
        return Estimation.compute(this, Matrix.of(data), DoubleSequence.ofInternal(parameters), marginal);
    }
}
