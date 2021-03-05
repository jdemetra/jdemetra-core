/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.bayes;

import demetra.data.Data;
import static demetra.data.Data.copyToTempFile;
import demetra.data.DoubleSeq;
import demetra.data.MatrixSerializer;
import demetra.math.matrices.MatrixType;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import jdplus.bayes.BayesRegularizedRegression.Model;
import jdplus.bayes.BayesRegularizedRegression.Prior;
import jdplus.math.matrices.Matrix;
import jdplus.stats.samples.Moments;
import org.junit.Test;

/**
 *
 * @author PALATEJ
 */
public class BayesRegularizedRegressionTest {
    
    public BayesRegularizedRegressionTest() {
    }

    @Test
    public void testSomeMethod() {
    }
    
    public static void main(String[] args){
        try {
            File file = copyToTempFile(Data.class.getResource("/ml.txt"));
            MatrixType ml = MatrixSerializer.read(file);
            DoubleSeq y=ml.column(0);
            Matrix X=Matrix.of(ml.extract(0,ml.getRowsCount(), 1, ml.getColumnsCount()-1));
            new BayesRegularizedRegression(
                    y, X,
                    Model.GAUSSIAN, 0, Prior.HORSESHOE, 1000, 5000);
            long t0=System.currentTimeMillis();
            BayesRegularizedRegression reg=new BayesRegularizedRegression(
                    y, X,
                    Model.GAUSSIAN, 0, Prior.HORSESHOE, 1000, 50000);
            long t1=System.currentTimeMillis();
            System.out.println(t1-t0);
            
            for (int i=0; i<X.getColumnsCount(); ++i){
                int idx=i;
            DoubleStream fn = reg.results().stream().mapToDouble(R->R.getB().get(idx));
                DoubleSeq c=DoubleSeq.of(fn.toArray());
                double m=Moments.mean(c);
                double e=Moments.variance(c, m, true);
                System.out.print(m);
                System.out.print('\t');
                System.out.println(Math.sqrt(e));
            }
            
        } catch (IOException ex) {
            Logger.getLogger(BayesRegularizedRegressionTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}
