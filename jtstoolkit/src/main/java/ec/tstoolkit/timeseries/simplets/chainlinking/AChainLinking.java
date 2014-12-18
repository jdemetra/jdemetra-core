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
package ec.tstoolkit.timeseries.simplets.chainlinking;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mats Maggi
 */
public abstract class AChainLinking {

    protected int refYear;
    protected List<Product> input;
    protected InformationSet results;

    public static enum ChainLinkingMethod {

        ANNUAL_OVERLAP,
        OVER_THE_YEAR
    }

    public void addProduct(Product p) {
        if (input == null) {
            input = new ArrayList<>();
        }
        input.add(p);
    }

    public void setProducts(List<Product> products) {
        if (products == null) {
            input = new ArrayList<>();
        } else {
            this.input = products;
        }
    }

    public void addProduct(String name, TsData q, TsData p) {
        addProduct(new Product(name, q, p, null));
    }

    public List<Product> getInput() {
        return input;
    }

    public InformationSet getResults() {
        return results;
    }

    public abstract void process();

    public static class Product {

        private String name;
        private TsData quantities;
        private TsData price;
        private TsData value;

        public Product(String n, TsData q, TsData p, TsData v) {
            name = n;
            quantities = q;
            price = p;
            value = v;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public TsData getQuantities() {
            return quantities;
        }

        public void setQuantities(TsData quantities) {
            this.quantities = quantities;
        }

        public TsData getPrice() {
            return price;
        }

        public void setPrice(TsData price) {
            this.price = price;
        }

        public TsData getValue() {
            return value;
        }

        public void setValue(TsData value) {
            this.value = value;
        }
        
        public void calculateMissingTs() {
            if (value == null) {
                value = TsData.multiply(quantities, price);
            } else if (quantities == null) {
                quantities = TsData.divide(value, price);
            } else if (price == null) {
                price = TsData.divide(value, quantities);
            }
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return new Product(name, quantities.clone(), price.clone(), value.clone());
        }
    }

    public int getRefYear() {
        return refYear;
    }

    public void setRefYear(int refYear) {
        this.refYear = refYear;
    }
}
