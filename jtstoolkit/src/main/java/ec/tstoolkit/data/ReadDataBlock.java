/*
* Copyright 2016 National Bank of Belgium
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

import ec.tstoolkit.design.Development;
import java.text.DecimalFormat;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ReadDataBlock implements IReadDataBlock {

    private final double[] m_data;
    private final int m_start, m_end;

    /**
     * Creates a temporary data set. Attention. The data are not copied...
     *
     * @param data The original data
     */
    public ReadDataBlock(final double[] data) {
        m_data = data;
        m_start = 0;
        m_end = data == null ? 0 : data.length;
    }

    /**
     *
     * @param data
     * @param start
     * @param length
     */
    public ReadDataBlock(final double[] data, int start, int length) {
        m_data = data;
        m_start = start;
        m_end = start + length;
    }

    @Override
    public void copyTo(double[] buffer, int start) {
        if (m_data != null)
        System.arraycopy(m_data, m_start, buffer, start, m_end - m_start);
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public double get(final int idx) {
        return m_data[m_start + idx];
    }

    /**
     *
     * @return
     */
    @Override
    public int getLength() {
        return m_end - m_start;
    }

    /**
     *
     * @param start
     * @param length
     * @return
     */
    @Override
    public IReadDataBlock rextract(int start, int length) {
        return new ReadDataBlock(m_data, m_start + start, length);
    }

    @Override
    public String toString() {
        return toString(this);
    }
    
    public static String toString(IReadDataBlock rd){
        StringBuilder builder = new StringBuilder();
        int n=rd.getLength();
        if (n>0) {
            builder.append(rd.get(0));
            for (int i = 1; i <n; ++i) {
                builder.append('\t').append(rd.get(i));
           }
        }
        return builder.toString();
    }
    
    public static String toString(IReadDataBlock rd, String fmt){
        StringBuilder builder = new StringBuilder();
        int n=rd.getLength();
        if (n>0) {
            builder.append(new DecimalFormat(fmt).format(rd.get(0)));
            for (int i = 1; i <n; ++i) {
                builder.append('\t').append(new DecimalFormat(fmt).format(rd.get(i)));
           }
        }
        return builder.toString();
    }
    
    public static boolean equals(IReadDataBlock l, IReadDataBlock r, double eps){
        int nl=l.getLength(), nr=r.getLength();
        if (nl != nr){
            return false;
        }
        for (int i=0; i<nr; ++i){
            if (Math.abs(l.get(i)-r.get(i))> eps)
                return false;
        }
        return true;
    }

}
