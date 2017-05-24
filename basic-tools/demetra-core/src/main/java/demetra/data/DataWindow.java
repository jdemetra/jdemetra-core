/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import demetra.design.Unsafe;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DataWindow  {
    
    private final DataBlock cur;

    public static DataWindow windowOf(double[] data, int start, int end, int inc) {
        return new DataWindow(DataBlock.ofInternal(data, start, end, inc));
    }
 
    public static DataWindow startOf(double[] data) {
        return new DataWindow(DataBlock.ofInternal(data, 0, 0, 1));
    }
 
    public static DataWindow startOf(double[] data, int inc) {
        return new DataWindow(DataBlock.ofInternal(data, 0, 0, inc));
    }

    private DataWindow(DataBlock cur) {
        this.cur=cur;
    }

    DataWindow(double[] data, int start, int end, int inc) {
        cur=new DataBlock(data, start, end, inc);
    }
    
    public DataBlock get() {
        return cur;
    }
    
    public DataBlock next(int n) {
        cur.beg = cur.end;
        cur.end += n * cur.inc;
        return cur;
    }

    public DataBlock previous(int n) {
        cur.end = cur.beg;
        cur.beg -= n * cur.inc;
        return cur;
    }

    public DataBlock bexpand() {
        cur.beg -= cur.inc;
        return cur;
    }

    public DataBlock eexpand() {
        cur.end += cur.inc;
        return cur;
    }

    public DataBlock bshrink() {
        cur.beg += cur.inc;
        return cur;
    }

    public DataBlock eshrink() {
        cur.end -= cur.inc;
        return cur;
    }

    public DataBlock shrink(int nbeg, int nend) {
        cur.beg += cur.inc * nbeg;
        cur.end -= cur.inc * nend;
        return cur;
    }

    public DataBlock move(int n) {
        int del=cur.inc * n;
        cur.beg += del;
        cur.end += del;
        return cur;
    }

    public DataBlock slide(int n) {
        cur.beg += n;
        cur.end += n;
         return cur;
   }

    public DataBlock slideAndShrink(int n) {
        cur.beg += n;
        cur.end += n-cur.inc;
         return cur;
   }

    public DataBlock  expand(int nbeg, int nend) {
        cur.beg -= nbeg * cur.inc;
        cur.end += nend * cur.inc;
          return cur;
  }
}
