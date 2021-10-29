/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import nbbrd.design.Development;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
public class MatrixWindow {

    private final FastMatrix cur;
    
    public static MatrixWindow of(FastMatrix m){
        return new MatrixWindow(m.getStorage(), m.getColumnIncrement(), m.getStartPosition(), m.getRowsCount(), m.getColumnsCount());
    }

    MatrixWindow(double[] x, int lda, int start, int nrows, int ncols) {
        cur = new FastMatrix(x, lda, start, nrows, ncols);
    }

    public FastMatrix get() {
        return cur;
    }

        /**
     *
     * @param dr
     * @param dc
     * @return
     */
    public FastMatrix move(final int dr, final int dc) {
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
    public FastMatrix bshrink() {
        cur.start += cur.getColumnIncrement() + 1;
        cur.nrows--;
        cur.ncols--;
        return cur;
    }

    public FastMatrix bvshrink() {
        cur.start ++;
        cur.nrows--;
        return cur;
    }

    public FastMatrix bhshrink() {
        cur.start +=cur.getColumnIncrement();
        cur.ncols--;
        return cur;
    }

    public FastMatrix eshrink() {
        cur.nrows--;
        cur.ncols--;
        return cur;
    }
    
    public FastMatrix evshrink() {
        cur.nrows--;
        return cur;
    }

    public FastMatrix ehshrink() {
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
    public FastMatrix next(int nr, int nc) {
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
    public FastMatrix next() {
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
    public FastMatrix hnext(int nc) {
        cur.start += cur.ncols*cur.getColumnIncrement();
        cur.ncols=nc;
        return cur;
    }

    /**
     * Takes the top-right of the current submatrix as the new starting position
     *
     * @return
     */
    public FastMatrix hnext() {
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
    public FastMatrix vnext(int nr) {
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
    public FastMatrix vnext() {
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
    public FastMatrix previous(int nr, int nc) {
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
    public FastMatrix previous() {
        cur.start -= cur.nrows+cur.ncols*cur.getColumnIncrement();
        return cur;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new ending position
     * and updates the number of columns
     *
     * @param nc The number of columns in the submatrix
     */
    public FastMatrix hprevious(int nc) {
        cur.start -= nc*cur.getColumnIncrement();
        cur.ncols=nc;
        return cur;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new ending position
     * @return 
     */
    public FastMatrix hprevious() {
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
    public FastMatrix vprevious(int nr) {
        cur.start -= nr;
        cur.nrows=nr;
        return cur;
    }

    /**
     * Takes the top-right of the current submatrix as the new ending position
     * @return 
     */
    public FastMatrix vprevious() {
        cur.start -= cur.nrows;
        return cur;
    }

}
