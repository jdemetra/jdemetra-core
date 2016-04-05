/*
 * Copyright 2016-2017 National Bank of Belgium
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
package ec.tstoolkit.data;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author Jean Palate
 */
public class MatrixNormalizer {
    private final InPlaceNormalizer normalizer;
    private double[] factors;
    
    public MatrixNormalizer(){
        this(new AbsMeanNormalizer());
    }
    
    public MatrixNormalizer(InPlaceNormalizer normalizer){
        this.normalizer=normalizer;
    }
    
    public void normalize(SubMatrix m){
        factors=null;
        if (m == null || m.isEmpty())
            return;
        factors=new double[m.getColumnsCount()];
        DataBlockIterator columns = m.columns();
        DataBlock col = columns.getData();
        int i=0;
        do{
            factors[i++]=normalizer.normalize(col);
        }while (columns.next());
    }
    
    public IReadDataBlock getFactors(){
        return new ReadDataBlock(factors);
    }
}
