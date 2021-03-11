/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import demetra.x13.X13Spec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class SpecProtoTest {
    
    public SpecProtoTest() {
    }

    @Test
    public void testDefault() {
        X13Spec s = X13Spec.RSA0;
        X13Spec ns = SpecProto.convert(SpecProto.convert(s));
        System.out.println(s);
        System.out.println(ns);
        assertTrue(s.equals(ns));
        s = X13Spec.RSA1;
        ns = SpecProto.convert(SpecProto.convert(s));
        assertTrue(s.equals(ns));
        s = X13Spec.RSA2;
        ns = SpecProto.convert(SpecProto.convert(s));
        assertTrue(s.equals(ns));
        s = X13Spec.RSA3;
        ns = SpecProto.convert(SpecProto.convert(s));
        assertTrue(s.equals(ns));
        s = X13Spec.RSA4;
        ns = SpecProto.convert(SpecProto.convert(s));
        assertTrue(s.equals(ns));
        s = X13Spec.RSA5;
        ns = SpecProto.convert(SpecProto.convert(s));
        assertTrue(s.equals(ns));

    }
    
}
