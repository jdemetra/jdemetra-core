/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import javax.annotation.Nonnull;

/**
 * Root of all regression variable definition. All definitions must contain
 * enough information for generating the actual regression variables in a given
 * context (corresponding to a ModellingContext).
 *
 * @author palatej
 */
public interface ITsVariable {

    int dim();

    public static int dim(@Nonnull ITsVariable... vars) {
        int nvars = 0;
        for (int i = 0; i < vars.length; ++i) {
            nvars += vars[i].dim();
        }
        return nvars;
    }

    public static String nextName(String name) {
        int pos0 = name.lastIndexOf('('), pos1 = name.lastIndexOf(')');
        if (pos0 > 0 && pos1 > 0) {
            String prefix = name.substring(0, pos0);
            int cur = 1;
            try {
                String num = name.substring(pos0 + 1, pos1);
                cur = Integer.parseInt(num) + 1;
            } catch (NumberFormatException err) {

            }
            StringBuilder builder = new StringBuilder();
            builder.append(prefix).append('(').append(cur).append(')');
            return builder.toString();
        } else {
            return name + "(1)";
        }
    }

}
