/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public final class MatrixWindow implements FastMatrix, Cloneable{
    
     
    private final double[] storage;
    private int start, nrows, ncols;
    private final int lda;
    
    /**
     * 
     * @param storage
     * @param start
     * @param nrows
     * @param ncols
     * @param colinc 
     */
    MatrixWindow(final double[] storage, final int start, final int nrows,
            final int ncols, final int colinc) {
        this.storage=storage;
        this.start=start;
        this.nrows=nrows;
        this.ncols=ncols;
        this.lda=colinc;
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

    public void set(final double d) {
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            cols.next().set(d);
        }
    }
    
    public void copy(Matrix M) {
        DataBlockIterator cols = columnsIterator();
        DataBlockIterator mcols = M.columnsIterator();
        while (cols.hasNext()) {
            cols.next().copy(mcols.next());
        }
    }
    /**
     *
     * @param dr
     * @param dc
     */
    public void move(final int dr, final int dc) {
        start += dr + dc * lda;
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
        start+=1+lda;
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
        start += this.nrows + this.ncols * lda;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    /**
     * Takes the bottom-right of the current submatrix as the new starting
 position
     */
    public void next() {
        start += nrows + ncols * lda;
    }

    /**
     * Takes the top-right of the current submatrix as the new starting position
 and updates the number of columns
     *
     * @param ncols The number of columns in the submatrix
     */
    public void hnext(int ncols) {
        start += this.ncols * lda;
        this.ncols = ncols;
    }

    /**
     * Takes the top-right of the current submatrix as the new starting position
     */
    public void hnext() {
        start += ncols * lda;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new starting
 position and updates the number of rows
     *
     * @param nrows The number of rows in the submatrix
     */
    public void vnext(int nrows) {
        start += this.nrows;
        this.nrows = nrows;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new starting
 position
     */
    public void vnext() {
        start += nrows;
    }

    /**
     * Takes the top-left of the current submatrix as the new ending position
 and updates the number of rows/columns
     *
     * @param nrows The number of rows in the submatrix
     * @param ncols The number of columns in the submatrix
     */
    public void previous(int nrows, int ncols) {
        start -= nrows + ncols * lda;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    /**
     * Takes the top-left of the current submatrix as the new ending position
     */
    public void previous() {
        start -= nrows + ncols * lda;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new ending position
 and updates the number of columns
     *
     * @param ncols The number of columns in the submatrix
     */
    public void hprevious(int ncols) {
        start -= ncols * lda;
        this.ncols = ncols;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new ending position
     */
    public void hprevious() {
        start -= ncols * lda;
    }

    /**
     * Takes the top-right of the current submatrix as the new ending position
 and updates the number of rows
     *
     * @param nrows The number of rows in the submatrix
     */
    public void vprevious(int nrows) {
        start -= nrows;
        this.nrows = nrows;
    }

    /**
     * Takes the top-right of the current submatrix as the new ending position
     */
    public void vprevious() {
        start -= nrows;
    }


    /**
     *
     * @param row
     * @param col
     * @return
     */
    @Override
    public final double get(final int row, final int col) {
        return storage[start + row + col * lda];
    }

    /**
     *
     * @return
     */
    @Override
    public final int getColumnsCount() {
        return ncols;
    }

    /**
     *
     * @return
     */
    @Override
    public final int getRowsCount() {

        return nrows;
    }
    
    @Override
    public double[] getStorage(){
        return storage;
    }
    
    @Override
    public int getStartPosition(){
        return start;
    }

    @Override
    public int getColumnIncrement(){
        return lda;
    }
    /**
     *
     * @param row
     * @param col
     * @param value
     */
    @Override
    public void set(final int row, final int col, final double value) {
        storage[start + row + col * lda] = value;
    }

    /**
     *
     * @param row
     * @param col
     * @param fn
     */
    @Override
    public void apply(final int row, final int col, final DoubleUnaryOperator fn) {
        int idx = start + row + col * lda;
        storage[idx] = fn.applyAsDouble(storage[idx]);
    }

}
