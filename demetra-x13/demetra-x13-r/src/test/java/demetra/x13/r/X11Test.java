/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.r;

import demetra.data.Data;
import demetra.math.matrices.MatrixType;
import demetra.x11.X11Spec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class X11Test {
    
    public X11Test() {
    }

    @Test
    public void testSomeMethod() {
        X11.Results rslt = X11.process(Data.TS_PROD, X11Spec.DEFAULT);
        MatrixType data = rslt.getData("all", MatrixType.class);
//        System.out.println(data);
    }
    
    @Test
    public void testBF() {
        X11Spec spec = X11Spec.builder()
                .backcastHorizon(10)
                .forecastHorizon(19)
                .build();
        X11.Results rslt = X11.process(Data.TS_PROD, spec);
        MatrixType data = rslt.getData("all", MatrixType.class);
//        System.out.println(data);
    }
}
