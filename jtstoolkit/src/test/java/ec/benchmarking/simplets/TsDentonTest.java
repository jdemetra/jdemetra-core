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
package ec.benchmarking.simplets;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.HouseholderR;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TsDentonTest {

    public final TsData y, m, ym;

    public TsDentonTest() {
        m = data.Data.X;
        ym = m.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        y = ym.clone();
        for (int i = 0; i < y.getLength(); ++i) {
            y.set(i, y.get(i) * (1 + 0.01 * i));
        }
    }

    @Test
    public void testSomeMethod() {
        boolean mul = true;
        TsDenton denton = new TsDenton();
        denton.setMultiplicative(mul);
        TsData mc = denton.benchmark(m, y);
        TsData mc2 = denton(mul);
        TsDataTable table = new TsDataTable();
        table.insert(-1, m);
        table.insert(-1, mc);
        table.insert(-1, mc2);
        System.out.println(table);
    }

    public TsData denton(boolean mul) {
        int n = m.getLength();
        int ny = y.getLength();

        DataBlock x=new DataBlock(m);
        double xm=x.sum()/x.getLength();
        TsData M=m.div(xm);
        
        Matrix D = new Matrix(n - 1, n);
        if (mul) {
            TsData im = M.inv();
            D.diagonal().copy(im.rextract(0, n - 1));
            D.diagonal().chs();
            D.subDiagonal(1).copy(im.rextract(1, n - 1));
        } else {
            D.diagonal().set(-1);
            D.subDiagonal(1).set(1);
        }
 
        Matrix A = new Matrix(n + ny, n + ny);

        SymmetricMatrix.XtX(D.subMatrix(), A.subMatrix(0, n, 0, n));
        J(A.subMatrix(n, n + ny, 0, n));
        Matrix B = A.clone();
        J(A.subMatrix(0, n, n, n + ny).transpose());
        B.diagonal().drop(n, 0).set(1);
        x = new DataBlock(n + ny);
        x.range(0, n).copy(M);
        x.range(n, n + ny).copy(y.minus(ym).div(xm));
        DataBlock z = new DataBlock(n + ny);
        z.product(B.rows(), x);
        HouseholderR qr = new HouseholderR(true);
        qr.decompose(A);
        qr.solve(z, x);
        return new TsData(m.getStart(), x.range(0, n)).times(xm);
    }

    public void J(SubMatrix M) {
        int j = 0;
        DataBlockIterator rows = M.rows();
        DataBlock data = rows.getData();
        do {
            data.range(j, j + 12).set(1);
            j += 12;
        } while (rows.next());
    }
}
