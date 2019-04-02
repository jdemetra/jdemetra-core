/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

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

    private final VarianceInterpreter var;

    @lombok.Value
    public static class Item {

        String cmp;
        LoadingInterpreter c;
        ISsfLoading loading;

        Item(String cmp) {
            this.cmp = cmp;
            c = null;
            loading = null;
        }

        Item(String eq, String cmp, double p, boolean fixed, ISsfLoading loading) {
            this.cmp = cmp;
            if (p == 1 && fixed) {
                this.c = null;
            } else {
                StringBuilder fullname = new StringBuilder();
                fullname.append(eq).append('.').append(cmp);
                this.c = new LoadingInterpreter(fullname.toString(), p, fixed);
            }
            this.loading = loading;
        }
    }

    public ModelEquation(String name, double var, boolean fixed) {
        this.name = name;
        if (var == 0 && fixed) {
            this.var = null;
        } else {
            this.var = new VarianceInterpreter(name + ".var", var, fixed, var == 0);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void add(String item) {
        items.add(new Item(item));
    }

    public void add(String item, double coeff, boolean fixed, ISsfLoading loading) {
        items.add(new Item(name, item, coeff, fixed, loading));
    }

    public void free() {
        var.free();
    }

    public double getVariance() {
        return var.variance();
    }

    public boolean isFixed() {
        return var.isFixed();
    }

    public int getItemsCount() {
        return items.size();
    }

    public Item getItem(int pos) {
        return items.get(pos);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        if (var != null) {
            mapping.add(var);
        }
        for (Item item : items) {
            if (item.c != null) {
                mapping.add(item.c);
            }
        }
        mapping.add((p, builder) -> {
            int pos = 0;
            double v = var == null ? 0 : p.get(pos++);
            MultivariateCompositeSsf.Equation eq = new MultivariateCompositeSsf.Equation(v);
            for (Item item : items) {
                double c;
                if (item.c != null) {
                    c = p.get(pos++);
                } else {
                    c = 1;
                }
                eq.add(new MultivariateCompositeSsf.Item(item.cmp, c, item.loading));
            }
            builder.add(eq);
            return pos;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        ArrayList<ParameterInterpreter> list = new ArrayList<>();
        list.add(var);
        for (Item item : items) {
            if (item.c != null) {
                list.add(item.c);
            }
        }
        return list;
    }

}
