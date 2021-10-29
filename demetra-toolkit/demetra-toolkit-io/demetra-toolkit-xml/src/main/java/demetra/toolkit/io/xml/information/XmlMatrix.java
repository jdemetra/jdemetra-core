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

package demetra.toolkit.io.xml.information;

import demetra.toolkit.io.xml.legacy.IXmlConverter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlMatrix.NAME)
public class XmlMatrix implements IXmlConverter<Matrix> {

    static final String NAME = "matrixType";
    
    @XmlAttribute 
    public int nrows;
    
    @XmlAttribute 
    public int ncolumns;
    
    @XmlElement
    @XmlList
    public double[] data;

    @Override
    public Matrix create() {
        return Matrix.of(data, nrows, ncolumns);
    }

    @Override
    public void copy(Matrix t) {
        nrows=t.getRowsCount();
        ncolumns=t.getColumnsCount();
        data=t.toArray();
    }
}
