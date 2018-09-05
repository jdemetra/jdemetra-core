/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.msts.LoadingParameter;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceParameter;
import demetra.msts.MstsMonitor;
import demetra.msts.GenericParameters;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.MatrixSerializer;
import demetra.maths.MatrixType;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfLoading;
import demetra.ssf.implementations.Loading;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import demetra.ssf.models.SsfAr;
import demetra.ssf.models.LocalLevel;
import demetra.ssf.models.LocalLinearTrend;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;

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

        MstsMonitor monitor = MstsMonitor.builder()
                .marginalLikelihood(true)
                .build();
        MstsMapping mapping = new MstsMapping();
        generateU(mapping);
        generateY(mapping);
        generatePicore(mapping);
        generatePi(mapping);
        generateCycle(mapping);
        monitor.process(D, mapping, null);
        System.out.println(monitor.getLikelihood().logLikelihood());
        System.out.println(mapping.trueParameters(monitor.fullParameters()));
        System.out.println(monitor.smoothedComponent(4));
    }

    @Test
    public void testSimpleX() throws URISyntaxException, IOException {

        URI uri = MultivariateCompositeSsf.class.getResource("/mssf1").toURI();
        MatrixType data = MatrixSerializer.read(new File(uri), "\t|,");

        Matrix D = Matrix.make(data.getRowsCount(), 6);
        D.column(0).copy(data.column(0));
        D.column(1).copy(data.column(9));
        D.column(2).copy(data.column(2));
        D.column(3).copy(data.column(3));
        D.column(4).copy(data.column(5));
        D.column(5).copy(data.column(6));

//        DataBlockIterator cols = D.columnsIterator();
//        while (cols.hasNext()) {
//            DataBlock col = cols.next();
//            col.normalize();
//        }
        MstsMapping mapping = new MstsMapping();

        generateU(mapping);
        generateY(mapping);
        generatePicore(mapping);
        generatePi(mapping);
        generateXCycle(mapping);

        MstsMonitor monitor = MstsMonitor.builder()
                .marginalLikelihood(true)
                .build();
        monitor.process(D, mapping, null);
        System.out.println(monitor.getLikelihood().logLikelihood());
        System.out.println(mapping.trueParameters(monitor.fullParameters()));
        System.out.println(monitor.smoothedComponent(4));
    }

    //@Test
    public void testSimpleX2() throws URISyntaxException, IOException {

        URI uri = MultivariateCompositeSsf.class.getResource("/mssf1").toURI();
        MatrixType data = MatrixSerializer.read(new File(uri), "\t|,");

        Matrix D = Matrix.make(data.getRowsCount(), 6);
        D.column(0).copy(data.column(0));
        D.column(1).copy(data.column(9));
        D.column(2).copy(data.column(2));
        D.column(3).copy(data.column(3));
        D.column(4).copy(data.column(5));
        D.column(5).copy(data.column(6));

//        DataBlockIterator cols = D.columnsIterator();
//        while (cols.hasNext()) {
//            DataBlock col = cols.next();
//            col.normalize();
//        }
        MstsMapping mapping = new MstsMapping();

        generateU(mapping);
        generateY(mapping);
        generatePicore(mapping);
        generatePi(mapping);
        generateCycle(mapping);
        generateB(mapping);
        generateC(mapping);

        MstsMonitor monitor = MstsMonitor.builder()
                .marginalLikelihood(true)
                .build();
        monitor.process(D, mapping, null);
        System.out.println(monitor.getLikelihood().logLikelihood());
        System.out.println(mapping.trueParameters(monitor.fullParameters()));
        System.out.println(monitor.smoothedComponent(4));
    }

    private void generateU(MstsMapping mapping) {
        mapping.add(new VarianceParameter("u_var", 1));
        mapping.add(new LoadingParameter("u_c"));
        mapping.add(new VarianceParameter("tu_var"));
        mapping.add((p, builder) -> {
            MultivariateCompositeSsf.Equation eq = new MultivariateCompositeSsf.Equation(p.get(0));
            eq.add(new MultivariateCompositeSsf.Item("tu"));
            eq.add(new MultivariateCompositeSsf.Item("cycle", p.get(1)));
            builder.add("tu", LocalLinearTrend.of(0, p.get(2)))
                    .add(eq);
            return 3;
        });

    }

    private void generateY(MstsMapping mapping) {
        mapping.add(new VarianceParameter("y_var"));
        mapping.add(new LoadingParameter("y_c"));
        mapping.add(new VarianceParameter("ty_var"));
        mapping.add((p, builder) -> {
            MultivariateCompositeSsf.Equation eq = new MultivariateCompositeSsf.Equation(p.get(0));
            eq.add(new MultivariateCompositeSsf.Item("ty"));
            eq.add(new MultivariateCompositeSsf.Item("cycle", p.get(1)));
            builder.add("ty", LocalLinearTrend.of(0, p.get(2)))
                    .add(eq);
            return 3;
        });

    }

    private void generatePicore(MstsMapping mapping) {
        mapping.add(new VarianceParameter("picore_var"));
        mapping.add(new LoadingParameter("picore_c"));
        mapping.add(new VarianceParameter("tpicore_var"));
        mapping.add((p, builder) -> {
            MultivariateCompositeSsf.Equation eq = new MultivariateCompositeSsf.Equation(p.get(0));
            eq.add(new MultivariateCompositeSsf.Item("tpicore"));
            eq.add(new MultivariateCompositeSsf.Item("cycle", p.get(1), Loading.fromPosition(4)));
            builder.add("tpicore", LocalLevel.of(p.get(2)))
                    .add(eq);
            return 3;
        });
    }

    private void generatePi(MstsMapping mapping) {
        mapping.add(new VarianceParameter("pi_var"));
        mapping.add(new LoadingParameter("pi_c"));
        mapping.add(new VarianceParameter("tpi_var"));
        mapping.add((p, builder) -> {
            MultivariateCompositeSsf.Equation eq = new MultivariateCompositeSsf.Equation(p.get(0));
            eq.add(new MultivariateCompositeSsf.Item("tpi"));
            eq.add(new MultivariateCompositeSsf.Item("cycle", p.get(1)));
            builder.add("tpi", LocalLevel.of(p.get(2)))
                    .add(eq);
            return 3;
        });

    }

    private void generateCycle(MstsMapping mapping) {
        mapping.add(new GenericParameters("ar", new ARDomain(), new double[]{.2, .2}, null));
        mapping.add(new VarianceParameter("ar_var", 1));
        mapping.add((p, builder) -> {
            double c1 = p.get(0), c2 = p.get(1), v = p.get(2);
            builder.add("cycle", SsfAr.of(new double[]{c1, c2}, v, 5));
            return 3;
        });

    }

    private void generateXCycle(MstsMapping mapping) {
        mapping.add(new GenericParameters("ar", new ARDomain(), new double[]{.2, .2}, null));
        mapping.add(new VarianceParameter("ar_var", 1));
        mapping.add(new LoadingParameter("b_c"));
        mapping.add(new VarianceParameter("b_var"));
        mapping.add(new LoadingParameter("c_c"));
        mapping.add(new VarianceParameter("c_var"));
        mapping.add((p, builder) -> {
            double c1 = p.get(0), c2 = p.get(1), v = p.get(2);
            double b1 = p.get(3), v1 = p.get(4);
            double b2 = p.get(5), v2 = p.get(6);
            ISsfLoading pl = Loading.from(new int[]{0, 1}, new double[]{b1 * c1, b1 * c2});
            MultivariateCompositeSsf.Equation eq1 = new MultivariateCompositeSsf.Equation(v1);
            eq1.add(new MultivariateCompositeSsf.Item("tb"));
            eq1.add(new MultivariateCompositeSsf.Item("cycle", 1, pl));
            double c12 = c1 * c1, c13 = c12 * c1, c14 = c13 * c1, c22 = c2 * c2;
            double d1 = c1 + c12 + c13 + c14 + c2 + c22 + 2 * c1 * c2 + 3 * c12 * c2;
            double d2 = c2 + c1 * c2 + c22 + c12 * c2 + 2 * c1 * c22 + c13 * c2;
            pl = Loading.from(new int[]{0, 1}, new double[]{b2 * d1, b2 * d2});
            MultivariateCompositeSsf.Equation eq2 = new MultivariateCompositeSsf.Equation(v2);
            eq2.add(new MultivariateCompositeSsf.Item("tc"));
            eq2.add(new MultivariateCompositeSsf.Item("cycle", 1, pl));
            builder.add("cycle", SsfAr.of(new double[]{c1, c2}, v, 5))
                    .add("tb", LocalLevel.of(0))
                    .add("tc", LocalLevel.of(0))
                    .add(eq1)
                    .add(eq2);

            return 7;
//            builder.add("cycle", AR.componentOf(new double[]{c1, c2}, v, 5));
//            return 3;
        });

    }

    private void generateB(MstsMapping mapping) {
        mapping.add(new VarianceParameter("bs_var"));
        mapping.add(new LoadingParameter("bs1_c"));
        mapping.add(new LoadingParameter("bs2_var"));
        mapping.add((p, builder) -> {
            double v = p.get(0), a1 = p.get(1), a2 = p.get(2);
            ISsfLoading pl = Loading.from(new int[]{0, 1}, new double[]{a1, a2});
            MultivariateCompositeSsf.Equation eq = new MultivariateCompositeSsf.Equation(v);
            eq.add(new MultivariateCompositeSsf.Item("tb"));
            eq.add(new MultivariateCompositeSsf.Item("cycle", 1, pl));
            builder.add("tb", LocalLevel.of(0))
                    .add(eq);
            return 3;
        });
    }

    private void generateC(MstsMapping mapping) {
        mapping.add(new VarianceParameter("cs_var"));
        mapping.add(new LoadingParameter("cs1_c"));
        mapping.add(new LoadingParameter("cs2_var"));
        mapping.add((p, builder) -> {
            double v = p.get(0), a1 = p.get(1), a2 = p.get(2);
            ISsfLoading pl = Loading.from(new int[]{0, 1}, new double[]{a1, a2});
            MultivariateCompositeSsf.Equation eq = new MultivariateCompositeSsf.Equation(v);
            eq.add(new MultivariateCompositeSsf.Item("tc"));
            eq.add(new MultivariateCompositeSsf.Item("cycle", 1, pl));
            builder.add("tc", LocalLevel.of(0))
                    .add(eq);
            return 3;
        });
    }
}
