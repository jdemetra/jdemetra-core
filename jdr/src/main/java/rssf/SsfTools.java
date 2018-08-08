/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfError;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class SsfTools {

    public Matrix transitionMatrix(ISsf ssf, int pos) {
        Matrix m = Matrix.square(ssf.getStateDim());
        ssf.dynamics().T(pos, m);
        return m;
    }

    public Matrix innovationMatrix(ISsf ssf, int pos) {
        Matrix m = Matrix.square(ssf.getStateDim());
        ssf.dynamics().V(pos, m);
        return m;
    }

    public double[] loading(ISsf ssf, int pos) {
        DataBlock m = DataBlock.make(ssf.getStateDim());
        ssf.loading().Z(pos, m);
        return m.getStorage();
    }

    public double measurementError(ISsf ssf, int pos) {
        ISsfError e = ssf.measurementError();
        return e == null ? 0 : e.at(pos);
    }

    public double[] initialState(ISsf ssf) {
        DataBlock m = DataBlock.make(ssf.getStateDim());
        ssf.initialization().a0(m);
        return m.getStorage();
    }

    public Matrix stationaryInitialVariance(ISsf ssf) {
        Matrix m = Matrix.square(ssf.getStateDim());
        ssf.initialization().Pf0(m);
        return m;
    }

    public Matrix diffuseInitialConstraint(ISsf ssf) {
        if (!ssf.initialization().isDiffuse()) {
            return null;
        }
        Matrix m = Matrix.make(ssf.getStateDim(), ssf.getDiffuseDim());
        ssf.initialization().diffuseConstraints(m);
        return m;
    }

    public Matrix diffuseInitialVariance(ISsf ssf) {
        if (!ssf.initialization().isDiffuse()) {
            return null;
        }
        Matrix m = Matrix.square(ssf.getStateDim());
        ssf.initialization().Pi0(m);
        return m;
    }
}
