/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public final class MatrixWindow extends SubMatrix implements Cloneable{
    
   /**
     *
     * @param data
     * @param start
     * @param nrows
     * @param ncols
     * @param rowinc
     * @param colinc
     */
    MatrixWindow(final double[] data, final int start, final int nrows,
            final int ncols, final int rowinc, final int colinc) {
        super(data, start, nrows, ncols, rowinc, colinc);
    }

    @Override
    public MatrixWindow clone() {
        try {
            return (MatrixWindow) super.clone();
        } catch (CloneNotSupportedException ex) {
            // always supported. Should never happen
            throw new RuntimeException();
        }
    }

    public Matrix fix(){
        return new SubMatrix(storage, start, nrows, ncols, rowInc, colInc);
    }

    @Override
    public MatrixWindow transpose() {
        return new MatrixWindow(storage, start, ncols, nrows, colInc, rowInc);
    }
    
    /**
     *
     * @param dr
     * @param dc
     */
    public void move(final int dr, final int dc) {
        start += dr * rowInc + dc * colInc;
    }

    /**
     * The following methods can be used to make fast iterations. They avoid
     * the creation of unnecessary objects
     *
     * example:
     *
     * (Sub)Matrix data=... SubMatrix cur=data.topLeft(); while (...){
     * cur.next(r,c); }
     */
    
    public void bshrink(){
        start+=rowInc+colInc;
        --nrows;
        --ncols;
    }
    
    public void eshrink(){
        --nrows;
        --ncols;
    }

    /**
     * Takes the bottom-right of the current submatrix as the new starting
 position and updates the number of rows/columns
     *
     * @param nrows The number of rows in the submatrix
     * @param ncols The number of columns in the submatrix
     */
    public void next(int nrows, int ncols) {
        start += this.nrows * rowInc + this.ncols * colInc;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    /**
     * Takes the bottom-right of the current submatrix as the new starting
 position
     */
    public void next() {
        start += nrows * rowInc + ncols * colInc;
    }

    /**
     * Takes the top-right of the current submatrix as the new starting position
 and updates the number of columns
     *
     * @param ncols The number of columns in the submatrix
     */
    public void hnext(int ncols) {
        start += this.ncols * colInc;
        this.ncols = ncols;
    }

    /**
     * Takes the top-right of the current submatrix as the new starting position
     */
    public void hnext() {
        start += ncols * colInc;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new starting
 position and updates the number of rows
     *
     * @param nrows The number of rows in the submatrix
     */
    public void vnext(int nrows) {
        start += this.nrows * rowInc;
        this.nrows = nrows;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new starting
 position
     */
    public void vnext() {
        start += nrows * rowInc;
    }

    /**
     * Takes the top-left of the current submatrix as the new ending position
 and updates the number of rows/columns
     *
     * @param nrows The number of rows in the submatrix
     * @param ncols The number of columns in the submatrix
     */
    public void previous(int nrows, int ncols) {
        start -= nrows * rowInc + ncols * colInc;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    /**
     * Takes the top-left of the current submatrix as the new ending position
     */
    public void previous() {
        start -= nrows * rowInc + ncols * colInc;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new ending position
 and updates the number of columns
     *
     * @param ncols The number of columns in the submatrix
     */
    public void hprevious(int ncols) {
        start -= ncols * colInc;
        this.ncols = ncols;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new ending position
     */
    public void hprevious() {
        start -= ncols * colInc;
    }

    /**
     * Takes the top-right of the current submatrix as the new ending position
 and updates the number of rows
     *
     * @param nrows The number of rows in the submatrix
     */
    public void vprevious(int nrows) {
        start -= nrows * rowInc;
        this.nrows = nrows;
    }

    /**
     * Takes the top-right of the current submatrix as the new ending position
     */
    public void vprevious() {
        start -= nrows * rowInc;
    }

}
