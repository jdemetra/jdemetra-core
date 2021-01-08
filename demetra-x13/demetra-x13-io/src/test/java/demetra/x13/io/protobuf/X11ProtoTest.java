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
public class X11ProtoTest {

    public X11ProtoTest() {
    }

    @Test
    public void testX11Spec() throws InvalidProtocolBufferException {
        X11Spec spec = X11Spec.DEFAULT;
        X11Spec nspec = X11Proto.convert(X11Proto.convert(spec));
        assertTrue(spec.equals(nspec));
    }

}
