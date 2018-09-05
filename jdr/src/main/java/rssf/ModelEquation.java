/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.msts.LoadingParameter;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceParameter;
import demetra.ssf.ISsfLoading;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author palatej
 */
public class ModelEquation implements ModelItem {

    private final String name;
    private final List<Item> items = new ArrayList<>();

    private final Double var;

    @lombok.Value
    private static class Item {

        String cmp;
        Double c;
        ISsfLoading loading;
    }

    public static ModelEquation withFixedError(String name, double var) {
        return new ModelEquation(name, var);
    }

    public static ModelEquation withError(String name) {
        return new ModelEquation(name, null);
    }

    private ModelEquation(String name, Double var) {
        this.name = name;
        this.var = var;
    }

    public String getName() {
        return name;
    }

    public void add(String item) {
        items.add(new Item(item, 1.0, null));
    }

    public void add(String item, Double coeff, ISsfLoading loading) {
        items.add(new Item(item, coeff, loading));
    }

    @Override
    public void addTo(MstsMapping mapping) {
        if (var == null) {
            mapping.add(new VarianceParameter(name + "_var"));
        } else if (var != 0) {
            mapping.add(new VarianceParameter(name + "_var", var));
        }
        for (Item item : items) {
            if (item.c == null) {
                mapping.add(new LoadingParameter(item.cmp + "_c"));
            } else if (item.c != 1) {
                mapping.add(new LoadingParameter(item.cmp + "_c", item.c));
            }
        }
        mapping.add((p, builder) -> {
            int pos = 0;
            double v = 0;
            if (var == null || var != 0) {
                v = p.get(pos++);
            }
            MultivariateCompositeSsf.Equation eq = new MultivariateCompositeSsf.Equation(v);
            for (Item item : items) {
                if (item.c == null || item.c != 1) {
                    double c = p.get(pos++);
                    eq.add(new MultivariateCompositeSsf.Item(item.cmp, c, item.loading));
                } else {
                    eq.add(new MultivariateCompositeSsf.Item(item.cmp, 1, item.loading));
                }
            }
            builder.add(eq);
            return pos;
        });
    }

}
