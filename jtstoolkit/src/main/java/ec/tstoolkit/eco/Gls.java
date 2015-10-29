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
package ec.tstoolkit.eco;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class Gls {

    private RegModel model_, lmodel_;
    private Matrix var_, l_;
    private boolean scaling_;
    private ConcentratedLikelihood ll_;

    /**
     *
     */
    public Gls() {
    }

    public boolean isScaling() {
        return scaling_;
    }

    public void setScaling(boolean scaling) {
        scaling_ = scaling;
    }

    /**
     *
     * @return
     */
    public ConcentratedLikelihood getLikelihood() {
        return ll_;
    }

    /**
     *
     * @return
     */
    public RegModel getModel() {
        return model_;
    }

    public Matrix getVariance() {
        return var_;
    }

    /**
     *
     * @return
     */
    public DataBlock getResiduals() {
        return lmodel_.calcRes(new DataBlock(ll_.getB()));
    }

    public boolean process(RegModel model, Matrix var) {
        model_ = model;
        var_ = var;
        if (var_.getRowsCount() != model.getObsCount()) {
            return false;
        }

        l_ = var_.clone();
        SymmetricMatrix.lcholesky(l_);
        // Xl = L^-1 * X or L * Xl = X
        lmodel_ = new RegModel();
        Matrix x = model.variables();
        DataBlock y = model.getY().deepClone();
        int n = y.getLength(), nx = 0;

        if (x != null) {
            nx = x.getColumnsCount();
        }

        double[] factors = null;
        double yfactor = 1;
        if (scaling_) {
            double yn = y.nrm2();
            if (yn != 0) {
                yfactor = n / yn;
                y.mul(yfactor);
            }
            if (nx > 0) {
                DataBlockIterator cols = x.columns();
                DataBlock col = cols.getData();
                factors = new double[nx];
                do {
                    double xn = col.nrm2();
                    if (xn != 0) {
                        double w = n / xn;
                        factors[cols.getPosition()] = w;
                        col.mul(w);
                    } else {
                        factors[cols.getPosition()] = 1;
                    }
                } while (cols.next());
            }
        }
        // computes lmodel
        LowerTriangularMatrix.rsolve(l_, y);
        lmodel_.setY(y);
        if (nx > 0) {
            DataBlockIterator cols = x.columns();
            DataBlock col = cols.getData();
            do {
                LowerTriangularMatrix.rsolve(l_, col);
                lmodel_.addX(col.deepClone());
            } while (cols.next());
        }

        Ols ols = new Ols();
        if (!ols.process(lmodel_)) {
            return false;
        }
        ConcentratedLikelihood ll = ols.getLikelihood();
        ll_ = new ConcentratedLikelihood();

        ll_.set(ll.getSsqErr(), 2 * l_.diagonal().sumLog().value, ll.getN());
        if (nx > 0) {
            ll_.setB(ll.getB(), ll.getBVar(), nx);
        }
        if (scaling_) {
            ll_.rescale(yfactor, factors);
        }
        return true;
    }
}
