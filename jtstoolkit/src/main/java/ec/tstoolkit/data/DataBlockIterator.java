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
import ec.tstoolkit.design.NewObject;

/**
 * Iterator of data blocks. The underlying data block can be accessed by the 
 * "getData" method and moved by calls to other methods of the class.
 * It should be noted that the sliding of the data block is defined at the construction
 * of the iterator and applied to the current data block. So, in most cases,
 * only 1 call to getData() is required. It also means that that data block 
 * should be modified with much caution.
 * Example of use:
 * 
 * Matrix m=new Matrix(10, 6);
 * DataBlockIterator rows=m.getRows();
 * DataBlock row=rows.getData()
 * do{
 *    row.set(rows.getPosition()+1);
 * while (rows.next());
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public final class DataBlockIterator implements Cloneable{

    // positions of the first column; 
    private final int bstart_, estart_, del_;

    private double[] data_;

    private DataBlock cur_;

    /**
     * Creates a new iterator. The parameters correspond to a rows iterator
     * of a matrix. However, they can be interpreted in a much large way.
     * The initial data block corresponds to the first item, which is different
     * than usual iterators. It also means that data block iterators can't be empty.
     * @param data The underlying data
     * @param start The initial starting position of the data block
     * @param nr The number of rows / the number of iterations
     * @param nc The number of columns / the number of elements in the data block
     * @param rinc The row increment / the sliding that is applied to the data block between two iterations
     * @param cinc The column increment / the space between two adjacent items of the data block
     */
    public DataBlockIterator(double[] data, int start, int nr, int nc,
	    int rinc, int cinc) {
	data_ = data;
	bstart_ = start;
	estart_ = start + (nr-1) * rinc;
	del_ = rinc;
	cur_ = new DataBlock(data, start, start + nc * cinc, cinc);
    }
    
    @Override
    public DataBlockIterator clone(){
        try {
            DataBlockIterator iter= (DataBlockIterator) super.clone();
            iter.cur_=cur_.clone();
            return iter;
        } catch (CloneNotSupportedException ex) {
           throw new AssertionError();
        }
    }

    /**
     * Sets the iterator to the first element. 
     */
    public void begin() {
	int del = cur_.getStartPosition() - bstart_;
	if (del > 0)
	    cur_.slide(-del);
    }

    /**
     * Sets the iterator to the last block. The current block is a valid block
     */
    public void end() {
	int del = estart_ - cur_.beg_;
	if (del != 0)
	    cur_.slide(del);
    }

    /**
     * Gets the number of elements
     * @return A strictly positive number
     */
    public int getCount() {
	return 1+ (del_ == 1 ? estart_ - bstart_ : (estart_ - bstart_) / del_);
    }

    /**
     * Gets the underlying data block, corresponding to the position of the iterator.
     * Calls to methods of the iterator will modify that data block
     * @return The current data block is returned. It should be modified with caution.
     * Successive calls to getData() always return the same object
     */
    public DataBlock getData() {
	return cur_;
    }

    /**
     * Gets the 0-based position of the iterator
     * @return The position of the iterator. Belongs to [0, getCount()[.
     */
    public int getPosition() {
	return (cur_.beg_ - bstart_) / del_;
    }

    /**
     * Moves the data block to the next position.
     * @return True if the data block has been moved, false otherwise (the iterator
     * was already at the end).
     */
    public boolean next() {
        if (cur_.beg_ == estart_)
            return false;
        else{
            cur_.slide(del_);
	    return true;
        }
    }

    /**
     * Moves the data block to the previous position.
     * @return True if the data block has been moved, false otherwise (the iterator
     * was already at the beginning).
     */
    public boolean previous() {
	if (cur_.getStartPosition() != bstart_) {
	    cur_.slide(-del_);
	    return true;
	} else
	    return false;
    }

    /**
     * Sets the iterator to a given position
     * @param value The new position of the iterator. Should be in [0, getCount()[.
     * Using a value outside that range will lead to unpredictable behavior.
     */
    public void setPosition(int value) {
	int del = (bstart_ + value * del_) - cur_.getStartPosition();
	if (del != 0)
	    cur_.slide(del);
    }

    /**
     * Gets an array of data blocks that corresponds to all the position of the
     * iterator.
     * @return An array of data blocks. It cannot be null or empty.
     */
    public DataBlock[] toArray()
    {
	DataBlock[] rc = new DataBlock[getCount()];
	int l = cur_.getEndPosition() - cur_.getStartPosition();
	int inc = cur_.getIncrement();
	for (int i = 0, b = bstart_; i < rc.length; ++i, b += del_)
	    rc[i] = new DataBlock(data_, b, b + l, inc);
	return rc;
    }
    
    /**
     * Computes the sum of this iterator, starting from its current position
     * @return A new Datablock is returned; may be null if the iterator is at its
     * end-position.
     */
    @NewObject
    public DataBlock sum(){
        DataBlockIterator tmp=this.clone();
        if (tmp.cur_.beg_ == tmp.estart_)
            return null;
        DataBlock sum=tmp.getData().deepClone();
        while (tmp.next()){
            sum.add(tmp.getData());
        }
        return sum;
    }
}
