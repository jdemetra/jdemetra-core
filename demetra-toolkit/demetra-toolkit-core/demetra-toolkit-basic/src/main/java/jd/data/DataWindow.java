/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jd.data;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DataWindow  {
    
    private final DataBlock cur;

    public static DataWindow windowOf(double[] data, int start, int end, int inc) {
        return new DataWindow(DataBlock.of(data, start, end, inc));
    }
 
    public static DataWindow startOf(double[] data) {
        return new DataWindow(DataBlock.of(data, 0, 0, 1));
    }
 
    public static DataWindow startOf(double[] data, int inc) {
        return new DataWindow(DataBlock.of(data, 0, 0, inc));
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
