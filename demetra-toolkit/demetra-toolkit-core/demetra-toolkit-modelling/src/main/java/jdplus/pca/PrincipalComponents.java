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
package jdplus.pca;

import jdplus.data.DataBlock;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.decomposition.ISingularValueDecomposition;
import jdplus.maths.matrices.decomposition.SingularValueDecomposition;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class PrincipalComponents {

    private CanonicalMatrix data;
    private ISingularValueDecomposition svd;
    private double scaling;

    public boolean process(CanonicalMatrix data) {
        clear();
        this.data = data;
        svd=new SingularValueDecomposition();
       
        CanonicalMatrix ndata;
        if (data.getColumnsCount() == 1){
            ndata=data;
            scaling=1;
        }
        else{
            scaling=1/Math.sqrt(data.getColumnsCount()-1);
            ndata=data.times(scaling);
        }
        svd.decompose(ndata);
        return svd.isFullRank();
    }

    private void clear() {
        svd=null;
        data=null;
        scaling=1;
    }
    
    public double getScaling(){
        return scaling;
    }

    public CanonicalMatrix getData() {
        return data;
    }
    
    public ISingularValueDecomposition getSvd(){
        return svd;
    }
    
    public DoubleSeq getSingularValues(){
        return svd.S();
    }
    
    public CanonicalMatrix getEigenVectors(){
        return svd.V();
    }
    
    public DataBlock getEigenVector(int pos){
        return svd.V().column(pos);
    }
    
    public DataBlock getFactor(int pos){
        DataBlock u=svd.U().column(pos).deepClone();
        u.mul(svd.S().get(pos));
        return u;
    }
}
