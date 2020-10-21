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
package demetra.toolkit.io.xml.legacy.core;

import demetra.math.matrices.MatrixType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * Matrix of doubles
 *
 *
 * <p>
 * Java class for MatrixType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="MatrixType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Dimensions" type="{ec/eurostat/jdemetra/core}UnsignedInts"/&gt;
 *         &lt;element name="Values" type="{ec/eurostat/jdemetra/core}Doubles"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlRootElement(name="Matrix")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MatrixType", propOrder = {
    "dimensions",
    "values"
})
public class XmlMatrix {

    @XmlElement(name = "Dimensions")
    @XmlList
    protected int[] dimensions;

    @XmlElement(name = "Values")
    @XmlList
    protected double[] values;

    /**
     * @return the dimensions
     */
    public int[] getDimensions() {
        return dimensions;
    }

    /**
     * @param dimensions the dimensions to set
     */
    public void setDimensions(int[] dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * @return the values
     */
    public double[] getValues() {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(double[] values) {
        this.values = values;
    }

    public static class Adapter extends XmlAdapter<XmlMatrix, MatrixType> {

        @Override
        public MatrixType unmarshal(XmlMatrix v) {
            return MatrixType.of(v.values, v.dimensions[0], v.dimensions[1]);
        }

        @Override
        public XmlMatrix marshal(MatrixType v) {
            XmlMatrix x = new XmlMatrix();
            x.dimensions = new int[]{v.getRowsCount(), v.getColumnsCount()};
            x.values = v.toArray();
            return x;
        }
    }
    
    private static final Adapter ADAPTER=new Adapter();
    
    public static Adapter getAdapter(){
        return ADAPTER;
    }
}
