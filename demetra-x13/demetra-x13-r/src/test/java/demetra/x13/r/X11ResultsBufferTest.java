/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.r;

import demetra.data.Data;
import demetra.x11.X11Spec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class X11ResultsBufferTest {

    public X11ResultsBufferTest() {
    }

    @Test
    public void testBuffer() {
        X11.Results rslt = X11.process(Data.TS_PROD, X11Spec.DEFAULT);
        X11ResultsBuffer buffer = new X11ResultsBuffer(rslt.getCore());
    }

}
