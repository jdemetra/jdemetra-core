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
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.eco.ConcentratedLikelihood;
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
        return computer.getWindowType();
    }

    /**
     * @param winType the winType to set
     */
    public void setWinType(WindowType winType) {
        computer.setWindowType(winType);
    }

    /**
     * @return the truncationLag
     */
    public int getTruncationLag() {
        return computer.getTruncationLag();
    }

    /**
     * @param truncationLag the truncationLag to set
     */
    public void setTruncationLag(int truncationLag) {
        computer.setTruncationLag(truncationLag);
    }

    /**
     * @return the xe
     */
    public Matrix getXe() {
        return computer.getXe();
    }

    /**
     * @return the xe
     */
    public Matrix getX() {
        return x;
    }

 
    /**
     * @return the rcov
     */
    public Matrix getOmega() {
        return computer.getRobustCovariance();
    }

    /**
     * @return the rcov
     */
    public Matrix getRobustCovariance() {
        return computer.getRobustCovariance();
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

    /**
     * @return the ll
     */
    public ConcentratedLikelihood getLikelihood() {
        return ll;
    }

    public static enum Variables {

        Dummy, Trigonometric, UserDefined
    }

    private boolean lag1 = true;
    private Variables type = Variables.Dummy;
    protected ConcentratedLikelihood ll;
    private Matrix x, cxe;

    private final RobustCovarianceMatrixComputer computer=new RobustCovarianceMatrixComputer();
    private DataBlock e;

    public boolean process(IReadDataBlock s) {
        if (type != Variables.UserDefined) {
            return false;
        }
        return process(s, 0, 0);
    }

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
        if (x == null) {
            return false;
        }
        for (int i = 0; i < x.getColumnsCount(); ++i) {
            model.addX(x.column(i));
        }
        Ols ols = new Ols();
        ols.process(model);
        ll = ols.getLikelihood();
        e = ols.getResiduals();
        computer.compute(x.all(), e);
        cxe = computer.getXe().clone();
        for (int i = 0; i < x.getColumnsCount(); ++i) {
            cxe.column(i).cumul();
        }
        return true;
    }

    public double test(int var) {
        return computeStat(computer.getOmega().subMatrix(var, var + 1, var, var + 1), cxe.subMatrix(0, -1, var, var + 1));
    }

    public double test(int var, int nvars) {
        return computeStat(computer.getOmega().subMatrix(var, var + nvars, var, var + nvars), cxe.subMatrix(0, -1, var, var + nvars));
    }

    public double testAll() {
        return computeStat(computer.getOmega().all(), cxe.all());
    }

    public double robustTestCoefficients() {
        Matrix rcov = computer.getRobustCovariance().clone();
        SymmetricMatrix.lcholesky(rcov);
        double[] tmp = ll.getB().clone();
        DataBlock b = new DataBlock(tmp, tmp.length-rcov.getRowsCount(), tmp.length, 1);
        LowerTriangularMatrix.rsolve(rcov, b);
        double f = b.ssq() / x.getColumnsCount();
        F ftest = new F();
        ftest.setDFNum(b.getLength());
        ftest.setDFDenom(x.getRowsCount());
        return ftest.getProbability(f, ProbabilityType.Upper);
    }

    public double olsTestCoefficients() {
        Matrix rcov = ll.getBVar().clone();
        SymmetricMatrix.lcholesky(rcov);
        double[] tmp = ll.getB().clone();
        DataBlock b = new DataBlock(tmp, 1, tmp.length, 1);
        LowerTriangularMatrix.rsolve(rcov, b);
        double f = b.ssq() / x.getColumnsCount();
        F ftest = new F();
        ftest.setDFNum(b.getLength());
        ftest.setDFDenom(x.getRowsCount());
        return ftest.getProbability(f, ProbabilityType.Upper);
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
