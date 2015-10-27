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

package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.maths.Complex;

/**
 *
 * @author Jean Palate
 */
public interface IEigenSystem {
    /// <summary>
    /// The method forces the computation of the eigenvalues and optionnaly the eigenvectors
    /// </summary>

    void compute();
    /// <summary>
    /// The method returns an array with all the eigenvalues. The eigenvalues can be complex.
    /// </summary>

    Complex[] getEigenValues();
    /// <summary>
    /// The method returns an array with the first n eigenvalues. The eigenvalues can be complex.
    /// </summary>

    Complex[] getEigenValues(int n);
    /// <summary>
    /// The method returns the eigenvector for the eigenvalue in position idx.
    /// </summary>

    double[] getEigenVector(int idx);
    /// <summary>
    /// The method returns all the eigenvectors as an IMatrix. Each eigenvector takes a column of the
    /// matrix
    /// </summary>

    Matrix getEigenVectors();
    /// <summary>
    /// The method returns the first n eigenvectors as an IMatrix. Each eigenvector takes a column of the
    /// matrix
    /// </summary>

    Matrix getEigenVectors(int n);
    /// <summary>
    /// The property sets/gets the treshold for small values
    /// </summary>

    double getZero();

    void setZero(double zero);
    /// <summary>
    /// The property sets/gets the number of iterations allowed in the computation
    /// </summary>

//    int getMaxIter();
//
//    void setMaxIter(int value);
    /// <summary>
    /// The property sets/gets whether eigenvectors should be computed
    /// </summary>

    boolean isComputingEigenVectors();

    void setComputingEigenVectors(boolean value);
}
