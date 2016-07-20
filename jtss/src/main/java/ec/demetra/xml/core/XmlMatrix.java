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

package ec.demetra.xml.core;

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.maths.matrices.Matrix;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlMatrix.RNAME)
@XmlType(name = XmlMatrix.NAME)
public class XmlMatrix implements IXmlConverter<Matrix> {

    static final String RNAME = "Matrix", NAME=RNAME+"Type";
    
    @XmlElement(name="Dimensions")
    @XmlList
    public int[] dim;
    
    @XmlElement(name="Values")
    @XmlList
    public double[] data;

    @Override
    public Matrix create() {
        return new Matrix(data, dim[0], dim[1]);
    }

    @Override
    public void copy(Matrix t) {
        dim=new int[]{t.getRowsCount(), t.getColumnsCount()};
        data=t.internalStorage();
    }
}
