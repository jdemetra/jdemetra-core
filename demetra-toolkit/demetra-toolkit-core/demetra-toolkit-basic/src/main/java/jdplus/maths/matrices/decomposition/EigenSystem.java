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

package jdplus.maths.matrices.decomposition;

/// <summary>
import demetra.maths.Complex;
import jdplus.maths.matrices.decomposition.IEigenSystem;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.MatrixException;

/// The class represents routines for finding EigenValues and EigenVectors of matrices. The
/// routines are available as static members.  The routines are based on code found in
/// Press et Al. (2002) Numerical Recipes in C++
/// </summary>
class EigenRoutines {
    /// <summary>
    /// Default constructur.
    /// </summary>

    private EigenRoutines() {
    }

    /// <summary>
    /// The static method reduces a real symmetric matrix to a TriDiagonal matrix using HouseHolder
    /// reduction. This is a transcription in C# of the C++ routine tred2.
    /// </summary>
    /// <param name="sm">On input the data array of a real symmetric matrix. Contains - if bVec == true - the orthogonal
    /// matrix Q. Otherwise sm contains no useful info on return.
    /// It is the responsibility of the user to ensure that the array represents a symmetric array.
    /// </param>
    /// <param name="n">The number of rows of the matrix represented by the array</param>
    /// <param name="ev">The Tridiagonal matrix in compact form. The array contains 3*n elements representing
    /// the subdiagonal, the diagonal and the superdiagonal. Because the subdiagonal counts only n-1
    /// elements the first and last elements of the array will be zero</param>
    /// <param name="bVec">The boolean indicates whether eigenvectors are to be computed.</param>
    /// <exception cref="TSToolkit.Math.Matrix.NotSymmetricException">Thrown when the matrix is not symmetric</exception>
    static double[] householder(double[] sm, int n, boolean bVec) {
        if (sm.length != n * n) {
            throw new MatrixException(MatrixException.SQUARE);
        }

        double[] ev = new double[n * 3];

        for (int i = n - 1; i > 0; i--) {
            int l = i - 1;
            double h = 0.0, scale = 0.0;
            if (l > 0) {
                int idxl = l * n + i;
                for (int k = 0, idx2 = i; k < l + 1; k++, idx2 += n) {
                    scale += Math.abs(sm[idx2]);
                }
                if (scale == 0.0) {
                    ev[i] = sm[idxl];
                } else {
                    for (int k = 0, idx2 = i; k < l + 1; k++, idx2 += n) {
                        sm[idx2] /= scale;
                        h += sm[idx2] * sm[idx2];
                    }
                    double f = sm[idxl];
                    double hsqrt = Math.sqrt(h);
                    double g = (f >= 0.0 ? -hsqrt : hsqrt);
                    ev[i] = scale * g;
                    h -= f * g;
                    sm[idxl] = f - g;
                    f = 0.0;

                    for (int j = 0, idx3 = i * n, idx5 = i, idx4 = j * n + i; j < l + 1; j++, idx3++, idx4 += n, idx5 += n) {
                        // eigenvectors
                        if (bVec) {
                            sm[idx3] = sm[idx4] / h;
                        }
                        g = 0.0;
                        for (int k = 0, idxj = k * n + j, idxi = k * n + i; k < j + 1; k++, idxj += n, idxi += n) {
                            g += sm[idxj] * sm[idxi];
                        }
                        for (int k = j + 1, idxj = j * n + k, idxi = k * n + i; k < l + 1; k++, idxj++, idxi += n) // test idxi should start at i,k
                        {
                            g += sm[idxj] * sm[idxi];
                        }
                        ev[j] = g / h;
                        f += ev[j] * sm[idx5];
                    }

                    double hh = f / (h + h);
                    for (int j = 0, idx3 = i; j < l + 1; j++, idx3 += n) {
                        f = sm[idx3];
                        g = ev[j] - hh * f;
                        ev[j] = g;
                        for (int k = 0, idxj = j, idxi = i; k < j + 1; k++, idxj += n, idxi += n) {
                            sm[idxj] -= (f * ev[k] + g * sm[idxi]);
                        }
                    }
                }
            } else {
                ev[i] = sm[i];   // code says m_sm[i,l] but if not l>0 then l==0
            }
            ev[n + i] = h;		// we use the middle colom to store diagonal elements
        }  // end of for-loop

        ev[0] = 0.0;
        ev[n] = 0.0;
        for (int i = 0, idxii = 0, idxi = 0; i < n; idxii += (n + 1), i++, idxi += n) {
            int l = i;

            // for eigenvectors only
            if (bVec) {
                if (ev[n + i] != 0.0) {
                    for (int j = 0, idxj = 0; j < l; idxj += n, j++) {
                        double g = 0.0;
                        for (int k = 0, idxji = i, idxk = idxj; k < l; k++, idxji += n, idxk++) {
                            g += sm[idxji] * sm[idxk];
                        }
                        for (int k = 0, idxk = idxj, idxki = i * n; k < l; k++, idxk++, idxki++) {
                            sm[idxk] -= g * sm[idxki];
                        }
                    }
                }
            }

            ev[n + i] = sm[idxii];

            // for eigenvectors only
            if (bVec) {
                sm[idxii] = 1.0;
                for (int j = 0, idxij = i; j < l; idxij += n, j++) {
                    sm[idxi + j] = 0.0;
                    sm[idxij] = 0.0;
                }
            }
        }

        for (int i = 1, idxm = 2 * n; i < n; i++, idxm++) {
            ev[idxm] = ev[i];
        }

        return ev;
    }

    /// <summary>
    /// The method reduces a real nonsymmetric square matrix to hessenberg form (i.e.Only the first
    /// subdiagonal is non-zero). The matrix is transformed using Gaussian Elimination with Pivoting.
    /// The routine is a transcript in C# of the C++ routine elmhes from Press et al. (2002)
    /// Numerical Recipes in C++
    /// The hessenberg matrix is returned in std. The cells belows the first subdiagonal will be set
    /// to zero.
    /// </summary>
    /// <param name="std">In/Out An array of double representing a real non-symmetric matrix</param>
    /// <param name="n">The number of rows of the matrix represented by std</param>
    /// <exception cref="TSToolkit.Math.Matrix.NotSquareException">Thrown when the matrix is not square</exception>
    static void hessenberg(double[] std, int n) {
        if (std.length != n * n) {
            throw new MatrixException(MatrixException.SQUARE);
        }

        for (int m = 1, idx = 0; m < n - 1; m++, idx += n) {
            double x = 0.0;
            int i = m;
            for (int j = m, idx2 = idx + j; j < n; j++, idx2++) {
                if (Math.abs(std[idx2]) > Math.abs(x)) {
                    x = std[idx2];
                    i = j;
                }
            }

            int idxi = i * n;
            int idxm = m * n;

            if (i != m) {
                for (int j = m - 1, idx2 = j * n; j < n; j++, idx2 += n) {
                    double tmp = std[idx2 + i];
                    std[idx2 + i] = std[idx2 + m];
                    std[idx2 + m] = tmp;
                }
                for (int j = 0; j < n; j++) {
                    double tmp = std[idxi + j];
                    std[idxi + j] = std[idxm + j];
                    std[idxm + j] = tmp;
                }
            }

            if (x != 0.0) {
                for (int l = m + 1; l < n; l++) {
                    double y = std[idx + l];
                    if (y != 0.0) {
                        y /= x;
                        std[idx + l] = y;
                        for (int j = m, idxj = m * n; j < n; j++, idxj += n) {
                            std[idxj + l] -= y * std[idxj + m];
                        }
                        for (int j = 0, idxl = l * n; j < n; j++, idxl++) {
                            std[idxm + j] += y * std[idxl];
                        }
                    }
                }
            }
        }

        // zero out all subdiagonal elements except for the first subdiagonal
        for (int i = 0, idx = 0; i < n - 2; i++, idx += n) {
            for (int j = i + 2, idx2 = idx + j; j < n; j++, idx2++) {
                std[idx2] = 0.0;
            }
        }
    }

    /// <summary>
    /// The method computes the eigenvalues and optionnaly the eigenvectors of a tridiagonal matrix.
    /// The method is a transcription in C# of the C++ routine tqli from Press et al. (2002)
    /// Numerical Recipes in C++.
    /// </summary>
    /// <param name="ev">An array of double representing a tridiagonal matrix. Data are stored
    /// columnwise.</param>
    /// <param name="n">The number of rows of the tridiagonal matrix. The length of the array must be 3*n</param>
    /// <param name="zz">Contains an array representing the similarity transformation matrix upon entry. if the original matrix
    /// was tridiagonal this will be an identity matrix. On exit the array will contain the eigenvectors if bVec==true.
    /// Otherwise the array id unaffected. The length of the array must be n*n</param>
    /// <param name="bVec">The parameter indicates whether eigenvectors will be computed</param>
    /// <exception cref="TSToolkit.Math.Matrix.NotSquareException">Thrown when length of z != n*n</exception>"
    /// <exception cref="System.NullReferenceException">Thrown when either ev or z are zero</exception>
    /// <exception cref="TSToolkit.Math.Matrix.DataException">Thrown when length of ev != 3*n</exception>"
    static void triQL(double[] ev, int n, double[] zz, boolean bVec) {
        // shift off-diagonalelements up
        for (int i = 1; i < n; i++) {
            ev[i - 1] = ev[i];
        }

        // clean out the last one
        ev[n - 1] = 0.0;

        int m = 0;

        for (int l = 0, idxld = n, idxle = 0; l < n; l++, idxld++, idxle++) {
            int iter = 0;
            do {
                int mm = l + n;
                int idxd, idxe;
                for (idxd = n + m, m = l, idxe = m; m < n - 1; m++, idxd++, idxe++, mm++) {
                    double dd = Math.abs(ev[idxd]) + Math.abs(ev[idxd + 1]);
                    if (Math.abs(ev[idxe]) + dd == dd) {
                        break;
                    }
                }
                if (m != l) {
                    if (iter++ == m_maxiter) {
                        throw new MatrixException(IEigenSystem.EIGENFAILED);
                    }
                    double g = (ev[idxld + 1] - ev[idxld]) / (2.0 * ev[idxle]);
                    double r = Support.pythagoras(g, 1.0);
                    //g = ev[mm]-ev[idxld]+ev[idxle]/(g+sign(r,g));
                    g = ev[m + n] - ev[idxld] + ev[idxle] / (g + Support.sign(r, g));

                    double s = 1.0;
                    double c = 1.0;
                    double p = 0.0;

                    int i;
                    for (i = m - 1; i >= l; i--) {
                        double f = s * ev[i];
                        double b = c * ev[i];
                        r = Support.pythagoras(f, g);
                        ev[i + 1] = r;

                        if (r == 0.0) {
                            ev[n + i + 1] -= p;
                            ev[m] = 0.0;
                            break;
                        }

                        s = f / r;
                        c = g / r;
                        g = ev[n + i + 1] - p;
                        r = (ev[n + i] - g) * s + 2.0 * c * b;
                        p = s * r;
                        ev[n + i + 1] = g + p;
                        g = c * r - b;


                        // eigenvectors
                        if (bVec) {
                            for (int k = 0, idxz = i * n, idxzz = idxz + n; k < n; k++, idxz++, idxzz++) {
                                f = zz[idxzz];
                                zz[idxzz] = s * zz[idxz] + c * f;
                                zz[idxz] = c * zz[idxz] - s * f;
                            }
                        }
                    }

                    if (r == 0.0 && i >= l) {
                        continue;
                    }
                    ev[idxld] -= p;
                    ev[idxle] = g;
                    ev[m] = 0.0;
                }

            } while (m != l);
        }
    }

    /// <summary>
    /// The method computes the eigenvalues of a matrix in Upper hessenberg form.
    /// No eigenvectors will be computed.
    /// </summary>
    /// <param name="std">An array representing the matrix in Upper hessenberg form</param>
    /// <param name="n">The number of rows of the hessenberg matrix</param>
    /// <returns>An array of - possibly - complex eigenvalues</returns>
    /// <exception cref="TSToolkit.Math.Matrix.NotSquareException">Thrown when std.length != n*n</exception>
    /// <exception cref="System.NullReferenceException">Thrown when std is null</exception>
    static Complex[] hessenbergQR(double[] std, int n) {
        if (std.length != n * n) {
            throw new MatrixException(MatrixException.SQUARE);
        }

        double anorm = 0.0;
        double z = 0.0, y = 0.0, x = 0.0, w = 0.0, v = 0.0, u = 0.0, t = 0.0, s = 0.0, r = 0.0, p = 0.0, q = 0.0;
        Complex[] eigenval = new Complex[n];

        for (int i = 0; i < n; i++) {
            for (int j = Math.max(i - 1, 0), idx = j * n + i; j < n; j++, idx += n) {
                anorm += Math.abs(std[j * n + i]);
            }
        }

        int nn = n - 1;
        t = 0.0;

        while (nn >= 0) {
            int its = 0;
            int l = 0;
            do {
                for (l = nn; l > 0; l--) {
                    s = Math.abs(std[(l - 1) * n + l - 1]) + Math.abs(std[l * n + l]);
                    if (s == 0.0) {
                        s = anorm;
                    }
                    if (Math.abs(std[(l - 1) * n + l]) + s == s) {
                        std[(l - 1) * n + l] = 0.0;
                        break;
                    }
                }

                x = std[nn * n + nn];
                if (l == nn) {
                    eigenval[nn--] = Complex.cart(x + t, 0);
                } else {
                    y = std[(nn - 1) * n + (nn - 1)];
                    w = std[(nn - 1) * n + nn] * std[nn * n + (nn - 1)];
                    if (l == (nn - 1)) {
                        p = 0.5 * (y - x);
                        q = p * p + w;
                        z = Math.sqrt(Math.abs(q));
                        x += t;
                        if (q >= 0.0) {
                            z = p + Support.sign(z, p);
                            eigenval[nn - 1] = Complex.cart(x + z);
                            eigenval[nn] = eigenval[nn - 1];
                            if (z != 0.0) {
                                eigenval[nn] = Complex.cart(x - w / z);
                            }
                        } else {
                            eigenval[nn] = Complex.cart(x + p, z);
                            eigenval[nn - 1] = eigenval[nn].conj();
                        }
                        nn -= 2;
                    } else // l != n-1
                    {
                        if (its == m_maxiter) {
                            throw new MatrixException(IEigenSystem.EIGENFAILED);
                        }
                        //otherwise exceptional shift
                        if (its == 10 || its == 20) {
                            t += x;
                            for (int i = 0; i < nn + 1; i++) {
                                std[i * n + i] -= x;
                            }
                            s = Math.abs(std[(nn - 1) * n + nn]) + Math.abs(std[(nn - 2) * n + nn - 1]);
                            y = 0.75 * s;
                            x = y;
                            w = -0.4375 * s * s;
                        }
                        ++its;
                        int m = 0;
                        for (m = nn - 2; m >= l; m--) {
                            z = std[m * n + m];
                            r = x - z;
                            s = y - z;
                            p = (r * s - w) / std[m * n + m + 1] + std[(m + 1) * n + m];
                            q = std[(m + 1) * n + m + 1] - z - r - s;
                            r = std[(m + 1) * n + m + 2];
                            s = Math.abs(p) + Math.abs(q) + Math.abs(r);
                            p /= s;
                            q /= s;
                            r /= s;
                            if (m == l) {
                                break;
                            }
                            u = Math.abs(std[(m - 1) * n + m]) * (Math.abs(q) + Math.abs(r));
                            v = Math.abs(p) * (Math.abs(std[(m - 1) * n + m - 1]) + Math.abs(z) + Math.abs(std[(m + 1) * n + m + 1]));
                            if (u + v == v) {
                                break;
                            }
                        }

                        for (int i = m; i < nn - 1; i++) {
                            std[i * n + i + 2] = 0.0;
                            if (i != m) {
                                std[(i - 1) * n + i + 2] = 0.0;
                            }
                        }

                        for (int k = m; k < nn; k++) {
                            if (k != m) {
                                p = std[(k - 1) * n + k];
                                q = std[(k - 1) * n + k + 1];
                                r = 0.0;

                                if (k + 1 != nn) {
                                    r = std[(k - 1) * n + k + 2];
                                }
                                x = Math.abs(p) + Math.abs(q) + Math.abs(r);
                                if (x != 0.0) {
                                    p /= x;
                                    q /= x;
                                    r /= x;
                                }
                            }

                            s = Support.sign(Math.sqrt(p * p + q * q + r * r), p);
                            if (s != 0.0) {
                                if (k == m) {
                                    if (m != l) {
                                        std[(k - 1) * n + k] *= -1.0;
                                    }
                                } else {
                                    std[(k - 1) * n + k] = -s * x;
                                }

                                p += s;
                                x = p / s;
                                y = q / s;
                                z = r / s;
                                q /= p;
                                r /= p;
                                for (int j = k; j < nn + 1; j++) {
                                    p = std[j * n + k] + q * std[j * n + k + 1];
                                    if (k + 1 != nn) {
                                        p += r * std[j * n + k + 2];
                                        std[j * n + k + 2] -= p * z;
                                    }
                                    std[j * n + k + 1] -= p * y;
                                    std[j * n + k] -= p * x;
                                }

                                int mmin = nn < k + 3 ? nn : k + 3;
                                for (int i = l; i < mmin + 1; i++) {
                                    p = x * std[k * n + i] + y * std[(k + 1) * n + i];
                                    if (k != (nn - 1)) {
                                        p += z * std[(k + 2) * n + i];
                                        std[(k + 2) * n + i] -= p * r;
                                    }
                                    std[(k + 1) * n + i] -= p * q;
                                    std[k * n + i] -= p;
                                }
                            }
                        }
                    }
                }
            } while (l + 1 < nn);
        }

        return eigenval;
    }

    /// <summary>
    /// The method balances a non-symmetric matrix. Balancing will make the norm of the columns
    /// and the rows of the matrix comparable by a series of similarity transformations.
    /// Balancing is a recommended step before searching the eigenvalues of a nonsymmetric
    /// matrix. Algorithms that find eigenvalues of such matrices tend to be very sensitive to rounding errors.
    /// </summary>
    /// <param name="std">An array of double representing the non-symmetric matrix</param>
    /// <param name="n">The number of rows of the matrix</param>
    /// <exception cref="TSToolkit.Math.Matrix.NotSquareException">Thrown when length of std != n*n</exception>"
    /// <exception cref="System.NullReferenceException">Thrown when std == null</exception>
    static void balance(double[] std, int n) {
        if (std.length != n * n) {
            throw new MatrixException(MatrixException.SQUARE);
        }

        double sqrd = m_radix * m_radix;
        int last = 0;

        while (last == 0) {
            last = 1;
            for (int i = 0, idxci = 0; i < n; i++, idxci += n) {
                double r = 0.0;
                double c = 0.0;
                for (int j = 0, idxji = idxci, idxij = i; j < n; j++, idxji++, idxij += n) {
                    if (j != i) {
                        c += Math.abs(std[idxji]);
                        r += Math.abs(std[idxij]);
                    }
                }

                if (c != 0.0 && r != 0.0) {
                    double g = r / m_radix;
                    double f = 1.0;
                    double s = c + r;
                    while (c < g) {
                        f *= m_radix;
                        c *= sqrd;
                    }

                    g = r * m_radix;
                    while (c > g) {
                        f /= m_radix;
                        c /= sqrd;
                    }

                    if ((c + r) / f < 0.95 * s) {
                        last = 0;
                        g = 1.0 / f;
                        for (int j = 0, idx = i; j < n; j++, idx += n) {
                            std[idx] *= g;
                        }
                        for (int j = 0, idx = idxci; j < n; j++, idx++) {
                            std[idx] *= f;
                        }
                    }
                }
            }
        }
    }

    /// <summary>
    /// The property sets/gets the maximum number of iterations for the numerical routines. If no solution
    /// is reached within this number of iterations, an exception will be thrown.
    /// </summary>
    static int getMaxIter() {
        return m_maxiter;
    }

    static void setMaxIter(int value) {
        m_maxiter = value;
    }

    /// <summary>
    /// The property sets/gets the base value for double numbers. For Intel platforms this value is 2.
    /// </summary>
    static double getRadix() {
        return m_radix;
    }

    static void setRadix(double value) {
        m_radix = value;
    }
    private static int m_maxiter = 100;
    private static double m_radix = 2.0;
}

class Support {
    /// <summary>
    /// Default constructor
    /// </summary>

    private Support() {
    }

    /// <summary>
    /// The static method modifies the sign of a depending on the positiviness of b.
    /// (b>=0.0 ? (a>=0.0 ? a : -a) : (a>=0.0 ? -a : a))
    /// </summary>
    /// <param name="a">a double value</param>
    /// <param name="b">a double value</param>
    /// <returns>A double</returns>
    static double sign(double a, double b) {
        return (b >= 0.0 ? (a >= 0.0 ? a : -a) : (a >= 0.0 ? -a : a));
    }

    /// <summary>
    /// Calculates the norm of a and b. I.e. Sqrt(a^2+b^2)
    /// </summary>
    /// <param name="a">A double value</param>
    /// <param name="b">A double value</param>
    /// <returns></returns>
    public static double pythagoras(double a, double b) {
        double aa = Math.abs(a);
        double bb = Math.abs(b);

        if (aa > bb) {
            double aa_bb = bb / aa;
            return aa * Math.sqrt(1.0 + aa_bb * aa_bb);
        } else {
            double bb_aa = aa / bb;
            return (bb == 0.0 ? 0.0 : bb * Math.sqrt(1.0 + bb_aa * bb_aa));
        }

    }
}

public class EigenSystem {

    private EigenSystem() {
    }
    /// <summary>
    /// A static Constructor. It will return an IEigenSystem pointer adapted to the exact nature
    /// of the matrix passed as a parameter
    /// </summary>
    /// <param name="im">An IMatrix interface pointer</param>
    /// <returns>An IEigenSystem interface pointer</returns>

    public static IEigenSystem create(CanonicalMatrix m, boolean symmetric) {
        if (symmetric) {
            return new SymmetricEigenSystem(m);
        } else {
            return new GeneralEigenSystem(m);
        }
    }

    public static IEigenSystem create(CanonicalMatrix m) {
        if (isSymmetric(m)) {
            return new SymmetricEigenSystem(m);
        } else {
            return new GeneralEigenSystem(m);
        }
    }

    public static boolean isSymmetric(CanonicalMatrix m) {
        if (m.getRowsCount() != m.getColumnsCount()) {
            return false;
        }
        for (int i = 0; i < m.getRowsCount(); i++) {
            for (int j = 0; j < i; j++) {
                if (Math.abs(m.get(i, j) - m.get(j, i)) > 1e-12) {
                    return false;
                }
            }
        }

        return true;
    }

    public static double[] convertToArray(CanonicalMatrix m) {
        return m.toArray();
    }
}

class SymmetricEigenSystem implements IEigenSystem {

    public SymmetricEigenSystem() {
    }

    public SymmetricEigenSystem(CanonicalMatrix m) {
        m_sm = m.deepClone();
    }

    @Override
    public void compute() {
        m_bCalc = false;
        calc();
    }

    void calc() {
        if (m_bCalc) {
            return;
        }

        // reduction to TriDiagonal matrix. It is returned in m_ev which is an array of 3*n elements.
        // position 0 of ev is zero; the next n-1 positions contain the off-diagonal elements.
        // The elements n to 2n-1 contains the diagonal elements of the reduced matrix
        // the rest of the array is a copy of the subdiagonal matrix
        // the matrix m_eivec contains the cumulative transformations needed to compute the eigenvectors
        // the boolean parameter indicates whether eigenvectors will be computed

        EigenRoutines.setMaxIter(m_maxiter);
        double[] data = EigenSystem.convertToArray(m_sm);
        m_ev = EigenRoutines.householder(data, m_sm.getRowsCount(), m_bVec);

        // compute the eigenvalues and - if m_bVec == true - the eigenvectors of the reduction
        EigenRoutines.triQL(m_ev, m_sm.getRowsCount(), data, m_bVec);

        m_eivec = new CanonicalMatrix(data, m_sm.getRowsCount(), m_sm.getColumnsCount());
        m_bCalc = true;
    }

    @Override
    public Complex[] getEigenValues() {
        calc();
        int n = m_sm.getRowsCount();
        Complex[] rout = new Complex[n];
        for (int i = 0; i < rout.length; i++) {
            rout[i] = Complex.cart(m_ev[n + i], 0);
        }
        return rout;
    }

    @Override
    public Complex[] getEigenValues(int m) {
        calc();
        int n = m_sm.getRowsCount();
        int mel = Math.min(n, m);
        Complex[] rout = new Complex[mel];
        for (int i = 0; i < mel; i++) {
            rout[i] = Complex.cart(m_ev[n + i], 0);
        }
        return rout;
    }

    @Override
    public double[] getEigenVector(int idx) {
        if (!m_bVec) {
            throw new MatrixException(IEigenSystem.EIGENINIT);
        }
        calc();

        int n = m_eivec.getRowsCount();
        double[] dout = new double[n];
        double[] data = EigenSystem.convertToArray(m_eivec);
        System.arraycopy(data, idx * n, dout, 0, n);
        return dout;
    }

    @Override
    public CanonicalMatrix getEigenVectors() {
        if (!m_bVec) {
            throw new MatrixException(IEigenSystem.EIGENINIT);
        }
        calc();

        CanonicalMatrix iout = m_eivec.deepClone();
        return iout;
    }

    @Override
    public CanonicalMatrix getEigenVectors(int m) {
        if (!m_bVec) {
            throw new MatrixException(IEigenSystem.EIGENINIT);
        }
        calc();

        int n = m_eivec.getRowsCount();
        int mel = Math.min(m, m_eivec.getColumnsCount());
        CanonicalMatrix sm = CanonicalMatrix.make(n, mel);
        for (int i = 0; i < sm.getRowsCount(); i++) {
            for (int j = 0; j < mel; j++) {
                sm.set(i, j, m_eivec.get(i, j));
            }
        }
        return sm;
    }

    @Override
    public double getZero() {
        return m_zero;
    }

    @Override
    public void setZero(double value) {
        m_zero = value;
    }

    public int getMaxIter() {
        return m_maxiter;
    }

    public void setMaxIter(int value) {
        m_maxiter = value;
    }

    @Override
    public boolean isComputingEigenVectors() {
        return m_bVec;
    }

    @Override
    public void setComputingEigenVectors(boolean value) {
        m_bVec = value;
        m_bCalc = false;
    }
    private CanonicalMatrix m_sm;
    private double[] m_ev;
    private CanonicalMatrix m_eivec;
    private boolean m_bCalc;
    private double m_zero = 1.0e-6;
    private int m_maxiter = 30;
    private boolean m_bVec;
}

class GeneralEigenSystem implements IEigenSystem {

    public GeneralEigenSystem(CanonicalMatrix im) {
        m_std = im.deepClone();
    }

    @Override
    public void compute() {
        m_bCalc = false;
        calc();
    }

    @Override
    public Complex[] getEigenValues() {
        calc();
        int n = m_std.getRowsCount();
        Complex[] rout = new Complex[n];
        System.arraycopy(m_ev, 0, rout, 0, rout.length);
        return rout;
    }

    @Override
    public Complex[] getEigenValues(int m) {
        calc();
        int n = m_std.getRowsCount();
        int mel = Math.min(n, m);
        Complex[] rout = new Complex[mel];
        System.arraycopy(m_ev, 0, rout, 0, mel);
        return rout;
    }

    @Override
    public double getZero() {
        return m_zero;
    }

    @Override
    public void setZero(double value) {
        m_zero = value;
    }

    public int getMaxIter() {
        return m_maxiter;
    }

    public void setMaxIter(int value) {
        m_maxiter = value;
    }

    @Override
    public boolean isComputingEigenVectors() {
        return m_bVec;
    }

    @Override
    public void setComputingEigenVectors(boolean value) {
        m_bVec = value;
    }

    @Override
    public double[] getEigenVector(int idx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CanonicalMatrix getEigenVectors() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CanonicalMatrix getEigenVectors(int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    void calc() {
        if (m_bCalc) {
            return;
        }


        EigenRoutines.setMaxIter(m_maxiter);

        // balance the matrix
        double[] data = EigenSystem.convertToArray(m_std);
        EigenRoutines.balance(data, m_std.getRowsCount());


        // reduce to upper hessenberg form
        EigenRoutines.hessenberg(data, m_std.getRowsCount());

        // get the eigenvalues
        m_ev = EigenRoutines.hessenbergQR(data, m_std.getRowsCount());

        m_bCalc = true;
    }
    private CanonicalMatrix m_std;
    private Complex[] m_ev;
    private double m_zero = 1.0e-6;
    private int m_maxiter = 30;
    private boolean m_bCalc = false;
    private boolean m_bVec = false;

}
