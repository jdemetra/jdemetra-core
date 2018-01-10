/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package demetra.sutse;

import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.sts.BasicStructuralModel;
import demetra.sts.Component;
import java.util.EnumMap;

/**
 *
 * @author Jean Palate
 */
public class SutseModel {

    private final BasicStructuralModel[] models;
    private final EnumMap<Component, Matrix> correlations = new EnumMap<>(Component.class);
    private final EnumMap<Component, Matrix> lcorrelations = new EnumMap<>(Component.class);

    public SutseModel(final BasicStructuralModel[] models) {
        this.models = models.clone();
    }

    public BasicStructuralModel getModel(int pos) {
        return models[pos];
    }

    public int getModelCount() {
        return models.length;
    }

    public boolean setCorrelations(Component cmp, Matrix mcorr) {
        Matrix lm = mcorr.deepClone();
        try {
            SymmetricMatrix.lcholesky(lm, 1e-9);
            correlations.put(cmp, mcorr);
            lcorrelations.put(cmp, mcorr);
            return true;
        } catch (MatrixException err) {
            return false;
        }
    }

    public Matrix getCorrelations(Component cmp) {
        return correlations.get(cmp);
    }

    
}
