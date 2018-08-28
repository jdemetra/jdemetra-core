/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msts;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.MatrixSerializer;
import demetra.maths.MatrixType;
import demetra.maths.matrices.Matrix;
import demetra.ssf.implementations.Loading;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import demetra.ssf.models.AR;
import demetra.ssf.models.LocalLevel;
import demetra.ssf.models.LocalLinearTrend;
import demetra.ssf.multivariate.M2uAdapter;
import demetra.ssf.multivariate.SsfMatrix;
import demetra.ssf.univariate.ISsfData;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class MstsMonitorTest {
    
    public MstsMonitorTest() {
    }

    @Test
    public void testSimple() throws URISyntaxException, IOException {

        URI uri = MultivariateCompositeSsf.class.getResource("/mssf1").toURI();
        MatrixType data = MatrixSerializer.read(new File(uri), "\t|,");

        Matrix D = Matrix.make(data.getRowsCount(), 4);
        D.column(0).copy(data.column(0));
        D.column(1).copy(data.column(9));
        D.column(2).copy(data.column(2));
        D.column(3).copy(data.column(3));

        DataBlockIterator cols = D.columnsIterator();
        while (cols.hasNext()) {
            DataBlock col = cols.next();
            col.normalize();
        }

        SsfMatrix mdata = new SsfMatrix(D);
        ISsfData udata = M2uAdapter.of(mdata);

        MstsMapping mapping = new MstsMapping();

        // add the parameters
        // 0=tuvar, 1=tyvar, 2=tpivar, 3=tpicorevar, 4=eq2var, 5=eq3var, 6=eq4var
        for (int i = 0; i < 7; ++i) {
            mapping.add(new VarianceParameter());
        }
        // loading
        // 7=l-eq1, 8=l-eq2, 9=l-eq3, 10=l-eq4
        for (int i = 0; i < 4; ++i) {
            mapping.add(new LoadingParameter());
        }

        // AR 11 - 12
        mapping.add(new GenericParameters(new ARDomain(), new double[]{-.1, -.1}, null));

        // fixed parameters var cycle and var eq1
        VarianceParameter vc = new VarianceParameter(1);
        mapping.add(vc);
        VarianceParameter v1 = new VarianceParameter(1);
        mapping.add(v1);

        // Builder
        mapping.add((p, pos, builder) -> {
            builder.add("tu", LocalLinearTrend.of(0, p.get(pos)));
            builder.add("ty", LocalLinearTrend.of(0, p.get(pos + 1)));
            builder.add("tpi", LocalLevel.of(p.get(pos + 2)));
            builder.add("tpicore", LocalLevel.of(p.get(pos + 3)));
            builder.add("cycle", AR.componentOf(p.extract(pos + 11, 2).toArray(), p.get(pos + 13), 5));
            double v = p.get(pos + 14);
            double l = p.get(pos + 7);
            MultivariateCompositeSsf.Equation eq = new MultivariateCompositeSsf.Equation(v);
            eq.add(new MultivariateCompositeSsf.Item("tu"));
            eq.add(new MultivariateCompositeSsf.Item("cycle", l));
            builder.add(eq);
            v = p.get(pos + 4);
            l = p.get(pos + 8);
            eq = new MultivariateCompositeSsf.Equation(v);
            eq.add(new MultivariateCompositeSsf.Item("ty"));
            eq.add(new MultivariateCompositeSsf.Item("cycle", l));
            builder.add(eq);
            v = p.get(pos + 5);
            l = p.get(pos + 9);
            eq = new MultivariateCompositeSsf.Equation(v);
            eq.add(new MultivariateCompositeSsf.Item("tpicore"));
            eq.add(new MultivariateCompositeSsf.Item("cycle", l, Loading.create(4)));
            builder.add(eq);
            v = p.get(pos + 6);
            l = p.get(pos + 10);
            eq = new MultivariateCompositeSsf.Equation(v);
            eq.add(new MultivariateCompositeSsf.Item("tpi"));
            eq.add(new MultivariateCompositeSsf.Item("cycle", l));
            builder.add(eq);
            return 15;
        });
        
        MstsMonitor monitor=new MstsMonitor();
        monitor.process(D, mapping);
        System.out.println(mapping.trueParameters(monitor.getPrslts()));
        System.out.println(monitor.smoothedComponent(4));
    }    
}
