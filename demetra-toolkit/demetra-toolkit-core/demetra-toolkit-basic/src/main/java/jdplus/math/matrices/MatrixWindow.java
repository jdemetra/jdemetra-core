/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import demetra.design.Development;
import jdplus.data.DataBlock;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
public class MatrixWindow {

    private final Matrix cur;
    
    public static MatrixWindow of(Matrix m){
        return new MatrixWindow(m.getStorage(), m.getColumnIncrement(), m.getStartPosition(), m.getRowsCount(), m.getColumnsCount());
    }

    MatrixWindow(double[] x, int lda, int start, int nrows, int ncols) {
        cur = new Matrix(x, lda, start, nrows, ncols);
    }

    public Matrix get() {
        return cur;
    }

        /**
     *
     * @param dr
     * @param dc
     * @return
     */
    public Matrix move(final int dr, final int dc) {
        cur.start+=dr + dc * cur.getColumnIncrement();
        return cur;
    }

    /**
     * The following methods can be used to make fast iterations.They avoid
     * the creation of unnecessary objects
     *
     * example:
     *
     * (Sub)Matrix data=... SubMatrix cur=data.topLeft(); while (...){
     * cur.next(r,c); }
     *
     * @return
     */
    public Matrix bshrink() {
        cur.start += cur.getColumnIncrement() + 1;
        cur.nrows--;
        cur.ncols--;
        return cur;
    }

    public Matrix bvshrink() {
        cur.start ++;
        cur.nrows--;
        return cur;
    }

    public Matrix bhshrink() {
        cur.start +=cur.getColumnIncrement();
        cur.ncols--;
        return cur;
    }

    public Matrix eshrink() {
        cur.nrows--;
        cur.ncols--;
        return cur;
    }
    
    public Matrix evshrink() {
        cur.nrows--;
        return cur;
    }

    public Matrix ehshrink() {
        cur.ncols--;
        return cur;
    }

    /**
     * Takes the bottom-right of the current submatrix as the new starting
     * position and updates the number of rows/columns
     *
     * @param nr The number of rows in the submatrix
     * @param nc The number of columns in the submatrix
     * @return
     */
    public Matrix next(int nr, int nc) {
        cur.start += cur.nrows+cur.ncols*cur.getColumnIncrement();
        cur.nrows=nr;
        cur.ncols=nc;
        return cur;
    }

    /**
     * Takes the bottom-right of the current submatrix as the new starting
     * position
     *
     * @return
     */
    public Matrix next() {
        cur.start += cur.nrows+cur.ncols*cur.getColumnIncrement();
        return cur;
    }

    /**
     * Takes the top-right of the current submatrix as the new starting position
     * and updates the number of columns
     *
     * @param nc The number of columns in the submatrix
     * @return
     */
    public Matrix hnext(int nc) {
        cur.start += cur.ncols*cur.getColumnIncrement();
        cur.ncols=nc;
        return cur;
    }

    /**
     * Takes the top-right of the current submatrix as the new starting position
     *
     * @return
     */
    public Matrix hnext() {
        cur.start += cur.ncols*cur.getColumnIncrement();
        return cur;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new starting
     * position and updates the number of rows
     *
     * @param nr The number of rows in the submatrix
     * @return
     */
    public Matrix vnext(int nr) {
        cur.start += cur.nrows;
        cur.nrows=nr;
        return cur;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new starting
     * position
     *
     * @return
     */
    public Matrix vnext() {
        cur.start += cur.nrows;
        return cur;
    }

    /**
     * Takes the top-left of the current submatrix as the new ending position
     * and updates the number of rows/columns
     *
     * @param nr The number of rows in the submatrix
     * @param nc The number of columns in the submatrix
     * @return
     */
    public Matrix previous(int nr, int nc) {
        cur.start -= nr+nc*cur.getColumnIncrement();
        cur.nrows=nr;
        cur.ncols=nc;
        return cur;
    }

    /**
     * Takes the top-left of the current submatrix as the new ending position
     *
     * @return
     */
    public Matrix previous() {
        cur.start -= cur.nrows+cur.ncols*cur.getColumnIncrement();
        return cur;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new ending position
     * and updates the number of columns
     *
     * @param nc The number of columns in the submatrix
     */
    public Matrix hprevious(int nc) {
        cur.start -= nc*cur.getColumnIncrement();
        cur.ncols=nc;
        return cur;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new ending position
     * @return 
     */
    public Matrix hprevious() {
        cur.start -= cur.nrows;
        return cur;
    }

    /**
     * Takes the top-right of the current submatrix as the new ending position
     * and updates the number of rows
     *
     * @param nr The number of rows in the submatrix
     * @return 
     */
    public Matrix vprevious(int nr) {
        cur.start -= nr;
        cur.nrows=nr;
        return cur;
    }

    /**
     * Takes the top-right of the current submatrix as the new ending position
     * @return 
     */
    public Matrix vprevious() {
        cur.start -= cur.nrows;
        return cur;
    }

}
