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
package jdplus.leastsquares;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.math.matrices.CPointer;
import jdplus.math.matrices.DataPointer;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.UpperTriangularMatrix;
import jdplus.math.matrices.decomposition.Householder2;
import jdplus.math.matrices.decomposition.HouseholderWithPivoting;
import jdplus.math.matrices.decomposition.QRDecomposition;

/**
 * Solves a least squares problem by means of the QR algorithm.
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class QRSolver {
    
    @FunctionalInterface
    public interface Processor{
        QRSolution solve(DoubleSeq y, Matrix X);
    }
    
    public QRSolution fastLeastSquares(DoubleSeq y, Matrix X){
        Householder2 h = new Householder2();
        QRDecomposition qr = h.decompose(X);
        return leastSquares(qr, y, 0);
    }
    
    public QRSolution robustLeastSquares(DoubleSeq y, Matrix X){
        HouseholderWithPivoting h = new HouseholderWithPivoting();
        QRDecomposition qr = h.decompose(X, 0);
        return leastSquares(qr, y, Constants.getEpsilon());
    }
    
    public QRSolution leastSquares(QRDecomposition qr, DoubleSeq x, double rcond){
        int rank = rankOfUpperTriangularMatrix(qr.rawR(), rcond);
        double[] y = x.toArray();
        qr.applyQt(y);
        int m=qr.m(), n=qr.n();
        DoubleSeq e=DoubleSeq.of(y, rank, m-rank);
        // Solve R*X = Y;
        UpperTriangularMatrix.solveUx(qr.rawR(), DataBlock.of(y));
        int[] pivot=qr.pivot();
        DoubleSeq b;
        if (pivot == null) {
            b=DoubleSeq.of(y, 0, rank);
        } else {
            double[] tmp=new double[n];
            for (int i = 0; i < rank; ++i) {
                tmp[pivot[i]]=y[i];
            }
            b=DoubleSeq.of(tmp);
        }

        return new QRSolution(qr, rank, b, e, e.ssq());
    }

    public static int rankOfUpperTriangularMatrix(Matrix U, double rcond) {
        DoubleSeqCursor.OnMutable cursor = U.diagonal().cursor();
        double smax = Math.abs(cursor.getAndNext()), smin = smax;
        if (smax == 0) {
            return 0;
        }
        int rank = 1;
        int n = U.getRowsCount();
        double[] xmin = new double[n], xmax = new double[n];
        xmin[0] = 1;
        xmax[0] = 1;
        LAIC1 cmax = new LAIC1(), cmin = new LAIC1();
        CPointer pxmax = new CPointer(xmax, 0), pxmin = new CPointer(xmin, 0);
        CPointer pw = new CPointer(U.getStorage(), U.getStartPosition());
        while (rank < n) {
            pw.move(U.getColumnIncrement());
            double urr = cursor.getAndNext();
            cmin.minSingularValue(rank, pxmin, smin, pw, urr);
            cmax.maxSingularValue(rank, pxmax, smax, pw, urr);
            double sminpr = cmin.sestpr;
            double smaxpr = cmax.sestpr;
            if (smaxpr * rcond > sminpr) {
                break;
            }
            for (int i = 0; i < rank - 1; ++i) {
                xmin[i] *= cmin.s;
                xmax[i] *= cmax.s;
            }
            xmin[rank] = cmin.c;
            xmax[rank] = cmax.c;
            smin = sminpr;
            smax = smaxpr;
            ++rank;
        }
        return rank;
    }
}

class LAIC1 {

    double sestpr=1, s, c;

    /**
     *
     * @param j Length of the vectors
     * @param x
     * @param sest Estimated singular value of j by j matrix L
     * @param w
     * @param gamma The diagonal element gamma
     */
    void minSingularValue(int j, DataPointer x, double sest, DataPointer w, double gamma) {
        double eps = Constants.getEpsilon();
        double alpha = x.dot(j, w);
        double aalpha = Math.abs(alpha), agamma = Math.abs(gamma), asest = Math.abs(sest);
        // special cases:
        if (sest == 0) {
            double sine, cosine;
            sestpr = 0;
            if (agamma == 0 && aalpha == 0) {
                sine = 1;
                cosine = 0;
            } else {
                sine = -gamma;
                cosine = alpha;
            }
            double s1 = Math.max(Math.abs(sine), Math.abs(cosine));
            s = sine / s1;
            c = cosine / s1;
            double tmp = Math.sqrt(sine * sine + cosine * cosine);
            s /= tmp;
            c /= tmp;
        } else if (agamma <= eps * asest) {
            s = 0;
            c = 1;
            sestpr = agamma;
        } else if (aalpha <= eps * asest) {
            double s1 = agamma, s2 = asest;
            if (s1 <= s2) {
                s = 0;
                c = 1;
                sestpr = s1;
            } else {
                s = 1;
                c = 0;
                sestpr = s2;
            }
        } else if (asest <= eps * aalpha || asest <= eps * agamma) {
            double s1 = agamma, s2 = agamma;
            if (s1 <= s2) {
                double tmp = s1 / s2;
                c = Math.sqrt(1 + tmp * tmp);
                sestpr = asest * (tmp / c);
                s = -(gamma / s2) / c;
                c = Math.copySign(1, alpha) / c;
            } else {
                double tmp = s2 / s1;
                s = Math.sqrt(1 + tmp * tmp);
                sestpr = asest / s;
                c = (alpha / s1) / s;
                s = Math.copySign(1, gamma) / s;

            }
        } else {
            double zeta1 = alpha / asest, zeta2 = gamma / asest;
            double norma = Math.max(1 + zeta1 * zeta1 + Math.abs(zeta1 * zeta2),
                    Math.abs(zeta1 * zeta2) + zeta2 * zeta2);
            double test = 1 + 2 * (zeta1 - zeta2) * (zeta1 + zeta2);
            double sine, cosine;
            if (test >= 0) {
                double b = (zeta1 * zeta1 + zeta2 * zeta2 + 1) / 2;
                c = zeta2 * zeta2;
                double t = c / (b + Math.sqrt(Math.abs(b * b - c)));
                sine = zeta1 / (1 - t);
                cosine = -zeta2 / t;
                sestpr = Math.sqrt(1 + 4 * eps * eps * norma) * asest;
            } else {
                double b = (zeta2 * zeta2 + zeta1 * zeta1 - 1) / 2;
                c = zeta1 * zeta1;
                double t;
                if (b >= 0) {
                    t = -c / (b + Math.sqrt(b * b - c));
                } else {
                    t = b - Math.sqrt(b * b + c);
                }
                sine = -zeta1 / t;
                cosine = -zeta2 / (1 + t);
                sestpr = Math.sqrt(1 + t + 4 * eps * eps * norma) * asest;
            }
            double tmp = Math.sqrt(sine * sine + cosine * cosine);
            s = sine / tmp;
            c = cosine / tmp;
        }
    }

    /**
     *
     * @param j Length of the vectors
     * @param x
     * @param sest Estimated singular value of j by j matrix L
     * @param w
     * @param gamma The diagonal element gamma
     */
    void maxSingularValue(int j, DataPointer x, double sest, DataPointer w, double gamma) {
        double eps = Constants.getEpsilon();
        double alpha = x.dot(j, w);
        double aalpha = Math.abs(alpha), agamma = Math.abs(gamma), asest = Math.abs(sest);
        // special cases:
        if (sest == 0) {
            double s1 = Math.max(agamma, aalpha);
            if (s1 == 0) {
                s = 0;
                c = 1;
                sestpr = 0;
            } else {
                s = alpha / s1;
                c = gamma / s1;
                double tmp = Math.sqrt(s * s + c * c);
                s /= tmp;
                c /= tmp;
                sestpr = s1 * tmp;
            }
        } else if (agamma <= eps * asest) {
            s = 1;
            c = 0;
            double tmp = Math.max(asest, aalpha);
            double s1 = asest / tmp, s2 = aalpha / tmp;
            sestpr = tmp * Math.sqrt(s1 * s1 + s2 * s2);
        } else if (aalpha <= eps * asest) {
            double s1 = agamma, s2 = asest;
            if (s1 <= s2) {
                s = 1;
                c = 0;
                sestpr = s2;
            } else {
                s = 0;
                c = 1;
                sestpr = s1;
            }
        } else if (asest <= eps * aalpha || asest <= eps * agamma) {
            double s1 = agamma, s2 = aalpha;
            if (s1 <= s2) {
                double tmp = s1 / s2;
                s = Math.sqrt(1 + tmp * tmp);
                sestpr = s2 * s;
                c = (gamma / s2) / s;
                s = Math.copySign(1, alpha) / s;
            } else {
                double tmp = s2 / s1;
                c = Math.sqrt(1 + tmp * tmp);
                sestpr = s1 * c;
                s = (alpha / s1) / c;
                c = Math.copySign(1, gamma) / c;
            }
        } else {
            //normal case
            double zeta1 = alpha / asest, zeta2 = gamma / asest;
            double b = (1 - zeta1 * zeta1 - zeta2 * zeta2) / 2;
            c = zeta1 * zeta1;
            double t;
            if (b > 0) {
                t = c / (b + Math.sqrt(b * b + c));
            } else {
                t = Math.sqrt(b * b + c) - b;
            }
            double sine = -zeta1 / t, cosine = -zeta2 / (1 + t);
            double tmp = Math.sqrt(sine * sine + cosine * cosine);
            s = sine / tmp;
            c = cosine / tmp;
            sestpr = Math.sqrt(t + 1) * asest;
        }
    }
}
