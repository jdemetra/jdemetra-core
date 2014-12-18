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
package ec.tstoolkit.data;

import ec.tstoolkit.design.Development;

/**
 * Read only data block. A data block is just an array of doubles
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IReadDataBlock {

    /**
     * Copies the data into a given buffer
     * 
     * @param buffer The buffer that will receive the data.
     * @param start The start position in the buffer for the copy. The data will
     *            be copied in the buffer at the indexes [start,
     *            start+getLength()[. The length of the buffer is not checked.
     */
    public void copyTo(double[] buffer, int start);

    /**
     * Gets the data at a given position
     * 
     * @param idx
     *            0-based position of the data. idx must belong to [0,
     *            getLength()[
     * @return The idx-th element
     */
    public double get(int idx);

    /**
     * Gets the number of data in the block
     * 
     * @return The number of data (&gt= 0).
     */
    public int getLength();

     /**
     * Makes an extract of this data block.
     * 
     * @param start
     *            The position of the first extracted item.
     * @param length
     *            The number of extracted items. The size of the result could be
     *            smaller than length, if the data block doesn't contain enough
     *            items. Cannot be null.
     * @return A new (read only) data block. Cannot be null (but the length of
     *         the result could be 0.
     */
    public IReadDataBlock rextract(int start, int length);
    
    
}
