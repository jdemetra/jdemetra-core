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

/**
 * This class consists of <tt>static</tt> utility methods for operating on
 * objects. These utilities include <tt>null</tt>-safe or <tt>null</tt>-tolerant
 * methods for computing the hash code of an object, returning a string for an
 * object, and comparing two objects.
 */
@NextJdk("http://docs.oracle.com/javase/7/docs/api/java/util/Objects.html")
@Deprecated
public final class Objects {

    private Objects() {
        // static class
    }

    /**
     * Returns the hash code of a non-null argument and 0 for a null argument.
     *
     * @param o an object
     * @return the hash code of a non-null argument and 0 for a null argument
     * @see Object.hashCode()
     */
    @Deprecated
    public static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    /**
     * Returns <tt>true</tt> if the arguments are equal to each other and
     * <tt>false</tt> otherwise. Consequently, if both arguments are
     * <tt>null</tt>, <tt>true</tt> is returned and if exactly one argument is
     * <tt>null</tt>, <tt>false</tt> is returned. Otherwise, equality is
     * determined by using the <tt>equals</tt> method of the first argument.
     *
     * @param a an object
     * @param b an object to be compared with a for equality
     * @return <tt>true</tt> if the arguments are equal to each other and
     * <tt>false</tt> otherwise
     * @see Object.equals(Object)
     */
    @Deprecated
    public static boolean equals(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }
}
