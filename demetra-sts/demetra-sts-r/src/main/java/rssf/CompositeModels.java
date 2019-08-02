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
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.msts.CompositeModel;
import jdplus.msts.CompositeModelEstimation;
import jdplus.ssf.StateStorage;
import java.util.LinkedHashMap;
import java.util.Map;
import demetra.processing.ProcResults;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class CompositeModels {

    public static class Results implements ProcResults {
        
        private final CompositeModelEstimation estimation;
        
        Results(final CompositeModelEstimation estimation){
            this.estimation=estimation;
        }

        private static final InformationMapping<CompositeModelEstimation> MAPPING = new InformationMapping<>(CompositeModelEstimation.class);

        static {
            MAPPING.set("loglikelihood", Double.class, source -> source.getLikelihood().logLikelihood());
            MAPPING.set("scalingfactor", Double.class, source -> source.getLikelihood().sigma());
            MAPPING.set("ssf.ncmps", Integer.class, source -> source.getCmpPos().length);
            MAPPING.set("ssf.cmppos", int[].class, source -> source.getCmpPos());
            MAPPING.set("ssf.cmpnames", String[].class, source -> source.getCmpName());
            MAPPING.set("parameters", double[].class, source -> source.getFullParameters());
            MAPPING.set("parameternames", String[].class, source -> source.getParametersName());
            MAPPING.set("fn.parameters", double[].class, source -> source.getParameters());
            MAPPING.setArray("ssf.T", 0, 10000, Matrix.class, (source, t) -> {
                int dim = source.getSsf().getStateDim();
                CanonicalMatrix T = CanonicalMatrix.square(dim);
                source.getSsf().dynamics().T(t, T);
                return T;
            });
            MAPPING.setArray("ssf.V", 0, 10000, Matrix.class, (source, t) -> {
                int dim = source.getSsf().getStateDim();
                CanonicalMatrix V = CanonicalMatrix.square(dim);
                source.getSsf().dynamics().V(t, V);
                return V;
            });
            MAPPING.setArray("ssf.Z", 0, 10000, Matrix.class, (source, t) -> {
                int dim = source.getSsf().getStateDim();
                int m = source.getSsf().measurementsCount();
                CanonicalMatrix M = CanonicalMatrix.make(m, dim);
                for (int i = 0; i < m; ++i) {
                    source.getSsf().loading(i).Z(t, M.row(i));
                }
                return M;
            });
            MAPPING.set("ssf.T", Matrix.class, source -> {
                if (!source.getSsf().dynamics().isTimeInvariant()) {
                    return null;
                }
                int dim = source.getSsf().getStateDim();
                CanonicalMatrix T = CanonicalMatrix.square(dim);
                source.getSsf().dynamics().T(0, T);
                return T;
            });
            MAPPING.set("ssf.V", Matrix.class, source -> {
                if (!source.getSsf().dynamics().isTimeInvariant()) {
                    return null;
                }
                int dim = source.getSsf().getStateDim();
                CanonicalMatrix V = CanonicalMatrix.square(dim);
                source.getSsf().dynamics().V(0, V);
                return V;
            });
            MAPPING.set("ssf.P0", Matrix.class, source -> {
                int dim = source.getSsf().getStateDim();
                CanonicalMatrix V = CanonicalMatrix.square(dim);
                source.getSsf().initialization().Pf0(V);
                return V;
            });
            MAPPING.set("ssf.B0", Matrix.class, source -> {
                int dim = source.getSsf().getStateDim();
                int nd = source.getSsf().initialization().getDiffuseDim();
                CanonicalMatrix V = CanonicalMatrix.make(dim, nd);
                source.getSsf().initialization().diffuseConstraints(V);
                return V;
            });
            MAPPING.set("ssf.Z", Matrix.class, source -> {
                if (!source.getSsf().measurements().isTimeInvariant()) {
                    return null;
                }
                int dim = source.getSsf().getStateDim();
                int m = source.getSsf().measurementsCount();
                CanonicalMatrix M = CanonicalMatrix.make(m, dim);
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
            MAPPING.setArray("ssf.smoothing.state", 0, 10000, double[].class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.a(p).toArray();
            });
            MAPPING.setArray("ssf.smoothing.vstate", 0, 10000, Matrix.class, (source, p) -> {
                StateStorage smoothedStates = source.getSmoothedStates();
                return smoothedStates.P(p).unmodifiable();
            });
            MAPPING.set("ssf.smoothing.states", Matrix.class, source -> {
                int n=source.getData().getRowsCount(), m=source.getSsf().getStateDim();
                double[] z=new double[n*m];
                StateStorage smoothedStates = source.getSmoothedStates();
                for (int i=0, j=0; i<m; ++i, j+=n){
                    smoothedStates.getComponent(i).copyTo(z, j);
                }
                return Matrix.ofInternal(z, n, m);
            });
            MAPPING.set("ssf.smoothing.vstates", Matrix.class, source -> {
                int n=source.getData().getRowsCount(), m=source.getSsf().getStateDim();
                double[] z=new double[n*m];
                StateStorage smoothedStates = source.getSmoothedStates();
                for (int i=0, j=0; i<m; ++i, j+=n){
                    smoothedStates.getComponentVariance(i).copyTo(z, j);
                }
                return Matrix.ofInternal(z, n, m);
            });
            MAPPING.setArray("ssf.filtering.array", 0, 1000, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.getComponent(p).toArray();
            });
            MAPPING.setArray("ssf.filtering.varray", 0, 1000, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.getComponentVariance(p).toArray();
            });
            MAPPING.setArray("ssf.filtering.cmp", 0, 100, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.getComponent(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.filtering.vcmp", 0, 100, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.getComponentVariance(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.filtering.state", 0, 10000, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.a(p).toArray();
            });
            MAPPING.set("ssf.filtering.states", Matrix.class, source -> {
                int n=source.getData().getRowsCount(), m=source.getSsf().getStateDim();
                double[] z=new double[n*m];
                StateStorage fStates = source.getFilteringStates();
                for (int i=0, j=0; i<m; ++i, j+=n){
                    fStates.getComponent(i).copyTo(z, j);
                }
                return Matrix.ofInternal(z, n, m);
            });
            MAPPING.set("ssf.filtering.vstates", Matrix.class, source -> {
                int n=source.getData().getRowsCount(), m=source.getSsf().getStateDim();
                double[] z=new double[n*m];
                StateStorage fStates = source.getFilteringStates();
                for (int i=0, j=0; i<m; ++i, j+=n){
                    fStates.getComponentVariance(i).copyTo(z, j);
                }
                return Matrix.ofInternal(z, n, m);
            });
            MAPPING.setArray("ssf.filtering.vstate", 0, 10000, Matrix.class, (source, p) -> {
                StateStorage fStates = source.getFilteringStates();
                return fStates.P(p).unmodifiable();
            });

            MAPPING.setArray("ssf.filtered.array", 0, 1000, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponent(p).toArray();
            });
            MAPPING.setArray("ssf.filtered.varray", 0, 1000, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponentVariance(p).toArray();
            });
            MAPPING.setArray("ssf.filtered.cmp", 0, 100, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponent(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.filtered.vcmp", 0, 100, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.getComponentVariance(source.getCmpPos()[p]).toArray();
            });
            MAPPING.setArray("ssf.filtered.state", 0, 10000, double[].class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.a(p).toArray();
            });
            MAPPING.setArray("ssf.filtered.vstate", 0, 10000, Matrix.class, (source, p) -> {
                StateStorage fStates = source.getFilteredStates();
                return fStates.P(p).unmodifiable();
            });
            MAPPING.set("ssf.filtered.states", Matrix.class, source -> {
                int n=source.getData().getRowsCount(), m=source.getSsf().getStateDim();
                double[] z=new double[n*m];
                StateStorage fStates = source.getFilteredStates();
                for (int i=0, j=0; i<m; ++i, j+=n){
                    fStates.getComponent(i).copyTo(z, j);
                }
                return Matrix.ofInternal(z, n, m);
            });
            MAPPING.set("ssf.filtered.vstates", Matrix.class, source -> {
                int n=source.getData().getRowsCount(), m=source.getSsf().getStateDim();
                double[] z=new double[n*m];
                StateStorage fStates = source.getFilteredStates();
                for (int i=0, j=0; i<m; ++i, j+=n){
                    fStates.getComponentVariance(i).copyTo(z, j);
                }
                return Matrix.ofInternal(z, n, m);
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

    }

    public Results estimate(CompositeModel model, Matrix data, double eps, boolean marginal, boolean rescaling, double[] parameters) {
        return new Results(model.estimate(CanonicalMatrix.of(data), eps, marginal, rescaling, parameters));
    }

    public Results compute(CompositeModel model, Matrix data, double[] parameters, boolean marginal, boolean concentrated) {
        return new Results(model.compute(CanonicalMatrix.of(data), parameters, marginal, concentrated));
    }
}
