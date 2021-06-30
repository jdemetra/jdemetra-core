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

import demetra.information.InformationMapping;
import demetra.math.functions.Optimizer;
import jdplus.math.matrices.Matrix;
import jdplus.msts.CompositeModel;
import jdplus.msts.CompositeModelEstimation;
import jdplus.ssf.StateStorage;
import java.util.LinkedHashMap;
import java.util.Map;
import demetra.math.matrices.MatrixType;
import demetra.ssf.SsfInitialization;
import jdplus.ssf.multivariate.MultivariateSsf;
import demetra.information.Explorable;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class CompositeModels {

    public static class Results implements Explorable {

        private final CompositeModelEstimation estimation;

        Results(final CompositeModelEstimation estimation) {
            this.estimation = estimation;
        }

        private static final InformationMapping<CompositeModelEstimation> MAPPING = new InformationMapping<CompositeModelEstimation>() {
            @Override
            public Class getSourceClass() {
                return CompositeModelEstimation.class;
            }
        };

        static {
            MAPPING.set("likelihood.ll", Double.class, source -> source.getLikelihood().logLikelihood());
            MAPPING.set("likelihood.ser", Double.class, source -> source.getLikelihood().ser());
            MAPPING.set("likelihood.residuals", double[].class, source -> source.getLikelihood().e().toArray());
            MAPPING.set("scalingfactor", Double.class, source -> source.getLikelihood().sigma2());
            MAPPING.set("ssf.ncmps", Integer.class, source -> source.getCmpPos().length);
            MAPPING.set("ssf.cmppos", int[].class, source -> source.getCmpPos());
            MAPPING.set("ssf.cmpnames", String[].class, source -> source.getCmpName());
            MAPPING.set("parameters", double[].class, source -> source.getFullParameters());
            MAPPING.set("parametersnames", String[].class, source -> source.getParametersName());
            MAPPING.set("fn.parameters", double[].class, source -> source.getParameters());
            MAPPING.setArray("ssf.T", 0, 10000, MatrixType.class, (source, t) -> {
                int dim = source.getSsf().getStateDim();
                Matrix T = Matrix.square(dim);
                source.getSsf().dynamics().T(t, T);
                return T;
            });
            MAPPING.setArray("ssf.V", 0, Integer.MAX_VALUE, MatrixType.class, (source, t) -> {
                int dim = source.getSsf().getStateDim();
                Matrix V = Matrix.square(dim);
                source.getSsf().dynamics().V(t, V);
                return V;
            });
            MAPPING.setArray("ssf.Z", 0, Integer.MAX_VALUE, MatrixType.class, (source, t) -> {
                int dim = source.getSsf().getStateDim();
                int m = source.getSsf().measurementsCount();
                Matrix M = Matrix.make(m, dim);
                for (int i = 0; i < m; ++i) {
                    source.getSsf().loading(i).Z(t, M.row(i));
                }
                return M;
            });
            MAPPING.setArray("ssf.T", 0, Integer.MAX_VALUE, MatrixType.class, (source, t) -> {
                int dim = source.getSsf().getStateDim();
                Matrix T = Matrix.square(dim);
                source.getSsf().dynamics().T(t, T);
                return T;
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
            MAPPING.setArray("ssf.smoothing.array", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.getComponent(p).toArray();
            });
            MAPPING.setArray("ssf.smoothing.varray", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.getComponentVariance(p).toArray();
            });
            MAPPING.setArray("ssf.smoothing.cmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.getComponent(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.smoothing.vcmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.getComponentVariance(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.smoothing.state", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.a(p).toArray();
            });
            MAPPING.setArray("ssf.smoothing.vstate", 0, Integer.MAX_VALUE, MatrixType.class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.P(p).unmodifiable();
            });
            MAPPING.set("ssf.smoothing.states", MatrixType.class, source -> {
                int n = source.getData().getRowsCount(), m = source.getSsf().getStateDim();
                double[] z = new double[n * m];
                StateStorage smoothedStates = source.getSmoothedStates();
                for (int i = 0, j = 0; i < m; ++i, j += n) {
                    smoothedStates.getComponent(i).copyTo(z, j);
                }
                return MatrixType.of(z, n, m);
            });
            MAPPING.set("ssf.smoothing.vstates", MatrixType.class, source -> {
                int n = source.getData().getRowsCount(), m = source.getSsf().getStateDim();
                double[] z = new double[n * m];
                StateStorage smoothedStates = source.getSmoothedStates();
                for (int i = 0, j = 0; i < m; ++i, j += n) {
                    smoothedStates.getComponentVariance(i).copyTo(z, j);
                }
                return MatrixType.of(z, n, m);
            });
            MAPPING.setArray("ssf.filtering.array", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.getComponent(p).toArray();
            });
            MAPPING.setArray("ssf.filtering.varray", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.getComponentVariance(p).toArray();
            });
            MAPPING.setArray("ssf.filtering.cmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.getComponent(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.filtering.vcmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.getComponentVariance(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.filtering.state", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.a(p).toArray();
            });
            MAPPING.set("ssf.filtering.states", MatrixType.class, source -> {
                int n = source.getData().getRowsCount(), m = source.getSsf().getStateDim();
                double[] z = new double[n * m];
                StateStorage fStates = source.getFilteringStates();
                for (int i = 0, j = 0; i < m; ++i, j += n) {
                    fStates.getComponent(i).copyTo(z, j);
                }
                return MatrixType.of(z, n, m);
            });
            MAPPING.set("ssf.filtering.vstates", MatrixType.class, source -> {
                int n = source.getData().getRowsCount(), m = source.getSsf().getStateDim();
                double[] z = new double[n * m];
                StateStorage fStates = source.getFilteringStates();
                for (int i = 0, j = 0; i < m; ++i, j += n) {
                    fStates.getComponentVariance(i).copyTo(z, j);
                }
                return MatrixType.of(z, n, m);
            });
            MAPPING.setArray("ssf.filtering.vstate", 0, Integer.MAX_VALUE, MatrixType.class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.P(p).unmodifiable();
            });

            MAPPING.setArray("ssf.filtered.array", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponent(p).toArray();
            });
            MAPPING.setArray("ssf.filtered.varray", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponentVariance(p).toArray();
            });
            MAPPING.setArray("ssf.filtered.cmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponent(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.filtered.vcmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponentVariance(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.filtered.state", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.a(p).toArray();
            });
            MAPPING.setArray("ssf.filtered.vstate", 0, Integer.MAX_VALUE, MatrixType.class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.P(p).unmodifiable();
            });
            MAPPING.set("ssf.filtered.states", MatrixType.class, source -> {
                int n = source.getData().getRowsCount(), m = source.getSsf().getStateDim();
                double[] z = new double[n * m];
                StateStorage fStates = source.getFilteredStates();
                for (int i = 0, j = 0; i < m; ++i, j += n) {
                    fStates.getComponent(i).copyTo(z, j);
                }
                return MatrixType.of(z, n, m);
            });
            MAPPING.set("ssf.filtered.vstates", MatrixType.class, source -> {
                int n = source.getData().getRowsCount(), m = source.getSsf().getStateDim();
                double[] z = new double[n * m];
                StateStorage fStates = source.getFilteredStates();
                for (int i = 0, j = 0; i < m; ++i, j += n) {
                    fStates.getComponentVariance(i).copyTo(z, j);
                }
                return MatrixType.of(z, n, m);
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
            return MAPPING.getData(estimation, id, tclass);
        }

        public static final InformationMapping<CompositeModelEstimation> getMapping() {
            return MAPPING;
        }

        public double[] signal(int obs, int[] cmps) {
            return estimation.signal(obs, cmps).toArray();
        }

        public double[] stdevSignal(int obs, int[] cmps) {
            return estimation.stdevSignal(obs, cmps).toArray();
        }

        public double[] signal(Matrix m) {
            return estimation.signal(Matrix.of(m)).toArray();
        }

        public double[] stdevSignal(Matrix m) {
            return estimation.stdevSignal(Matrix.of(m)).toArray();
        }

        public Matrix loading(int obs) {
            return estimation.loading(obs, null);
        }

        public MultivariateSsf ssf() {
            return estimation.getSsf();
        }

        public StateStorage smoothedStates() {
            return estimation.getSmoothedStates();
        }

        public StateStorage filteredStates() {
            return estimation.getFilteredStates();
        }

        public StateStorage filteringStates() {
            return estimation.getFilteringStates();
        }
    }

    public Results estimate(CompositeModel model, MatrixType data, boolean marginal, boolean rescaling, String initialization,
            String opt, double eps, double[] parameters) {
        return new Results(model.estimate(Matrix.of(data), marginal, rescaling, SsfInitialization.valueOf(initialization), Optimizer.valueOf(opt), eps, parameters));
    }

    public Results compute(CompositeModel model, MatrixType data, double[] parameters, boolean marginal, boolean concentrated) {
        return new Results(model.compute(Matrix.of(data), parameters, marginal, concentrated));
    }
}
