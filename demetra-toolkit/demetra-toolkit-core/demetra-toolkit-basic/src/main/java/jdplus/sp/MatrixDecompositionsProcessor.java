/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.sp;

import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.spi.MatrixDecompositions;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = MatrixDecompositions.Processor.class)
public class MatrixDecompositionsProcessor implements MatrixDecompositions.Processor {

    @Override
    public Matrix cholesky(Matrix matrix) {
        CanonicalMatrix M=CanonicalMatrix.of(matrix);
        SymmetricMatrix.lcholesky(M, 1e-9);
        return M.unmodifiable();
    }
    
}
