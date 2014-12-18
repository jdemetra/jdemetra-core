/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.tss.documents;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class DocumentManagerTest {
    
    public DocumentManagerTest() {
    }

    @Test
    public void testComposite() {
        String str="y=,test,";
        assertTrue(str.equals(DocumentManager.CompositeTs.decode(str).toString()));
        str="y=b,test,f";
        assertTrue(str.equals(DocumentManager.CompositeTs.decode(str).toString()));
        str="y=b,test,";
        assertTrue(str.equals(DocumentManager.CompositeTs.decode(str).toString()));
        str="y=,test,f";
        assertTrue(str.equals(DocumentManager.CompositeTs.decode(str).toString()));
        str="y=,,f";
        assertTrue(str.equals(DocumentManager.CompositeTs.decode(str).toString()));
    }
}