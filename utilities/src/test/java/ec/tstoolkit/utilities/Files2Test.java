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
package ec.tstoolkit.utilities;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class Files2Test {

    static File PATH;
    static File ABSOLUTE_INSIDE;
    static File ABSOLUTE_DIR_INSIDE;
    static File RELATIVE_INSIDE;
    static File RELATIVE_DIR_INSIDE;
    static File ABSOLUTE_OUTSIDE;
    static File RELATIVE_OUTSIDE;
    static File[] PATHS;

    @BeforeClass
    public static void beforeClass() throws IOException {
        PATH = Files.createTempDir();
        ABSOLUTE_INSIDE = new File(PATH, "test.xml");
        Files.touch(ABSOLUTE_INSIDE);
        ABSOLUTE_DIR_INSIDE = new File(PATH, "folder");
        Preconditions.checkState(ABSOLUTE_DIR_INSIDE.mkdir());
        RELATIVE_INSIDE = new File(ABSOLUTE_INSIDE.getName());
        RELATIVE_DIR_INSIDE = new File(ABSOLUTE_DIR_INSIDE.getName());
        ABSOLUTE_OUTSIDE = File.createTempFile("boom", "xml");
        RELATIVE_OUTSIDE = new File(ABSOLUTE_OUTSIDE.getName());
        PATHS = new File[]{PATH};
    }

    @AfterClass
    public static void afterClass() throws IOException {
        Preconditions.checkState(ABSOLUTE_OUTSIDE.delete());
        Preconditions.checkState(ABSOLUTE_DIR_INSIDE.delete());
        Preconditions.checkState(ABSOLUTE_INSIDE.delete());
        Preconditions.checkState(PATH.delete());
    }

    @Test
    public void testSample() {
        assertTrue(PATH.isAbsolute() && PATH.exists() && PATH.isDirectory());
        assertTrue(ABSOLUTE_INSIDE.isAbsolute() && ABSOLUTE_INSIDE.exists() && ABSOLUTE_INSIDE.isFile());
        assertTrue(!RELATIVE_INSIDE.isAbsolute());
        assertTrue(ABSOLUTE_OUTSIDE.isAbsolute() && ABSOLUTE_OUTSIDE.exists() && ABSOLUTE_OUTSIDE.isFile());
        assertTrue(!RELATIVE_OUTSIDE.isAbsolute());
    }

    static void assertNotEquals(File l, File r) {
        assertFalse(l.equals(r));
    }

    @Test
    public void testGetAbsoluteFile() {
        // relative inside
        assertEquals(ABSOLUTE_INSIDE, Files2.getAbsoluteFile(PATHS, RELATIVE_INSIDE));
        assertNotSame(ABSOLUTE_INSIDE, Files2.getAbsoluteFile(PATHS, RELATIVE_INSIDE));
        assertTrue(Files2.getAbsoluteFile(PATHS, RELATIVE_INSIDE).isAbsolute());
        assertEquals(ABSOLUTE_DIR_INSIDE, Files2.getAbsoluteFile(PATHS, RELATIVE_DIR_INSIDE));
        assertNotSame(ABSOLUTE_DIR_INSIDE, Files2.getAbsoluteFile(PATHS, RELATIVE_DIR_INSIDE));
        assertTrue(Files2.getAbsoluteFile(PATHS, RELATIVE_DIR_INSIDE).isAbsolute());

        // absolute inside
        assertEquals(ABSOLUTE_INSIDE, Files2.getAbsoluteFile(PATHS, ABSOLUTE_INSIDE));
        assertSame(ABSOLUTE_INSIDE, Files2.getAbsoluteFile(PATHS, ABSOLUTE_INSIDE));
        assertTrue(Files2.getAbsoluteFile(PATHS, ABSOLUTE_INSIDE).isAbsolute());
        assertEquals(ABSOLUTE_DIR_INSIDE, Files2.getAbsoluteFile(PATHS, ABSOLUTE_DIR_INSIDE));
        assertSame(ABSOLUTE_DIR_INSIDE, Files2.getAbsoluteFile(PATHS, ABSOLUTE_DIR_INSIDE));
        assertTrue(Files2.getAbsoluteFile(PATHS, ABSOLUTE_DIR_INSIDE).isAbsolute());

        // relative outside
        assertNotEquals(ABSOLUTE_OUTSIDE, Files2.getAbsoluteFile(PATHS, RELATIVE_OUTSIDE));
        assertNotSame(ABSOLUTE_OUTSIDE, Files2.getAbsoluteFile(PATHS, RELATIVE_OUTSIDE));
        assertNull(Files2.getAbsoluteFile(PATHS, RELATIVE_OUTSIDE));

        // absolute outside
        assertEquals(ABSOLUTE_OUTSIDE, Files2.getAbsoluteFile(PATHS, ABSOLUTE_OUTSIDE));
        assertSame(ABSOLUTE_OUTSIDE, Files2.getAbsoluteFile(PATHS, ABSOLUTE_OUTSIDE));
        assertTrue(Files2.getAbsoluteFile(PATHS, ABSOLUTE_OUTSIDE).isAbsolute());
    }

    @Test
    public void testGetRelativeFile() {
        // relative inside
        assertEquals(RELATIVE_INSIDE, Files2.getRelativeFile(PATHS, RELATIVE_INSIDE));
        assertSame(RELATIVE_INSIDE, Files2.getRelativeFile(PATHS, RELATIVE_INSIDE));
        assertFalse(Files2.getRelativeFile(PATHS, RELATIVE_INSIDE).isAbsolute());

        // absolute inside
        assertEquals(RELATIVE_INSIDE, Files2.getRelativeFile(PATHS, ABSOLUTE_INSIDE));
        assertNotSame(RELATIVE_INSIDE, Files2.getRelativeFile(PATHS, ABSOLUTE_INSIDE));
        assertFalse(Files2.getRelativeFile(PATHS, ABSOLUTE_INSIDE).isAbsolute());

        // relative outside
        assertEquals(RELATIVE_OUTSIDE, Files2.getRelativeFile(PATHS, RELATIVE_OUTSIDE));
        assertSame(RELATIVE_OUTSIDE, Files2.getRelativeFile(PATHS, RELATIVE_OUTSIDE));
        assertFalse(Files2.getRelativeFile(PATHS, RELATIVE_OUTSIDE).isAbsolute());

        // absolute outside
        assertNotEquals(RELATIVE_OUTSIDE, Files2.getRelativeFile(PATHS, ABSOLUTE_OUTSIDE));
        assertNotSame(RELATIVE_OUTSIDE, Files2.getRelativeFile(PATHS, ABSOLUTE_OUTSIDE));
        assertNull(Files2.getRelativeFile(PATHS, ABSOLUTE_OUTSIDE));
    }

    @Test
    public void testFromPath() {
        assertEquals(PATH, Files2.fromPath(PATH.getPath()));
        assertEquals(PATH + File.separator + "hello", Files2.fromPath(PATH.getPath(), "hello").getPath());
        assertEquals(PATH + File.separator + "hello" + File.separator + "world", Files2.fromPath(PATH.getPath(), "hello", "world").getPath());
    }

    @Test
    public void testAcceptByExtension() {
        assertTrue(Files2.acceptByLowerCaseExtension(new File("hello.xml"), "xml"));
        assertTrue(Files2.acceptByLowerCaseExtension(new File("hello.xml"), "jpg", "xml"));
        assertTrue(Files2.acceptByLowerCaseExtension(new File("hello.xMl"), "xml"));
        assertFalse(Files2.acceptByLowerCaseExtension(new File("hello.xml"), "jpg"));
        assertFalse(Files2.acceptByLowerCaseExtension(new File("hello.xml")));
    }
}
