/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.stats;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.PeriodicDummies;
import ec.tstoolkit.data.TrigonometricSeries;
import ec.tstoolkit.data.WindowType;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.eco.RobustCovarianceMatrixComputer;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CanovaHansenTest {

    /**
     * @return the lag1
     */
    public boolean isLag1() {
        return lag1;
    }

    /**
     * @param lag1 the lag1 to set
     */
    public void setLag1(boolean lag1) {
        this.lag1 = lag1;
    }

    /**
     * @return the type
     */
    public Variables getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Variables type) {
        this.type = type;
    }

    /**
     * @return the winType
     */
    public WindowType getWinType() {
        return winType;
    }

    /**
     * @param winType the winType to set
     */
    public void setWinType(WindowType winType) {
        this.winType = winType;
    }

    /**
     * @return the truncationLag
     */
    public int getTruncationLag() {
        return truncationLag;
    }

    /**
     * @param truncationLag the truncationLag to set
     */
    public void setTruncationLag(int truncationLag) {
        this.truncationLag = truncationLag;
    }

    /**
     * @return the x
     */
    public Matrix getX() {
        return x;
    }

    /**
     * @return the xe
     */
    public Matrix getXe() {
        return xe;
    }

    /**
     * @return the cxe
     */
    public Matrix getCxe() {
        return cxe;
    }

    /**
     * @return the rcov
     */
    public Matrix getRcov() {
        return rcov;
    }

    /**
     * @return the e
     */
    public DataBlock getE() {
        return e;
    }

    /**
     * @param x the x to set
     */
    public void setX(Matrix x) {
        this.x = x;
    }

    public static enum Variables {
        Dummy, Trigonometric, UserDefined
    }

    private boolean lag1 = true;
    private Variables type = Variables.Dummy;
    private WindowType winType = WindowType.Bartlett;
    private int truncationLag = 12;

    private Matrix x, xe, cxe, rcov;
    private DataBlock e;

    public boolean process(IReadDataBlock s, int period, int startpos) {
        DataBlock y = new DataBlock(s);
        RegModel model = new RegModel();
        if (lag1) {
            model.addX(y.drop(0, 1));
            y = y.drop(1, 0);
            ++startpos;
        }
        switch (type) {
            case Dummy: {
                PeriodicDummies vars = new PeriodicDummies(period, startpos);
                x = vars.matrix(y.getLength());
                break;
            }
            case Trigonometric: {
                TrigonometricSeries vars = TrigonometricSeries.regular(period);
                x = vars.matrix(y.getLength());
                model.setMeanCorrection(true);
                break;
            }
            default:
                model.setMeanCorrection(true);
        }
        model.setY(y);
        if (x == null)
            return false;
        for (int i = 0; i < x.getColumnsCount(); ++i) {
            model.addX(x.column(i));
        }
        Ols ols = new Ols();
        ols.process(model);
        e = ols.getResiduals();
        RobustCovarianceMatrixComputer har = new RobustCovarianceMatrixComputer();
        har.setWindowType(winType);
        har.setTruncationLag(truncationLag);
        rcov = har.compute(x.all(), e);
        xe = har.getXe();
        cxe=xe.clone();
        for (int i = 0; i < x.getColumnsCount(); ++i) {
            cxe.column(i).cumul();
        }
        return true;
    }
    
    public double test(int var){
        return computeStat(rcov.subMatrix(var, var+1, var, var+1), cxe.subMatrix(0, -1, var, var+1));
    }
    
    public double test(int var, int nvars){
        return computeStat(rcov.subMatrix(var, var+nvars, var, var+nvars), cxe.subMatrix(0, -1, var, var+nvars));
    }

    public double testAll(){
        return computeStat(rcov.all(), cxe.all());
    }

    private double computeStat(SubMatrix O, SubMatrix cx) {
        int n = cx.getRowsCount(), nx = cx.getColumnsCount();
        // compute tr( O^-1*xe'*xe)
        // cusum
        Matrix FF = Matrix.square(nx);
        for (int i = 0; i < n; ++i) {
            FF.addXaXt(1, cx.row(i));
        }
        // LL'^-1 * xe2 = L'^-1* L^-1 xe2 = L'^-1*a <-> a=L^-1 xe2 <->La=xe2
        Matrix sig = new Matrix(O);
        SymmetricMatrix.lcholesky(sig);
        LowerTriangularMatrix.rsolve(sig, FF.all());
        // b=L'^-1*a <-> L'b=a <->b'L = a'
        LowerTriangularMatrix.lsolve(sig, FF.all().transpose());
        double tr = FF.diagonal().sum();
        return tr / (n * n);

    }
}
