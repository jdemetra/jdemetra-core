/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.x11.X11Spec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class X13ProtosUtilityTest {

    public X13ProtosUtilityTest() {
    }

    @Test
    public void testX11Spec() throws InvalidProtocolBufferException {
        X11Spec spec = X11Spec.DEFAULT;
        byte[] bytes = X13ProtosUtility.toBuffer(spec);
        X11Spec nspec = X13ProtosUtility.x11SpecOf(bytes);
        assertTrue(spec.equals(nspec));
    }

}
