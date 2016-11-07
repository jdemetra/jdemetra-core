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

package ec.tss.tsproviders.common.random;

import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceBean;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.Params;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;

/**
 *
 * @author Jean Palate
 * @author Philippe Charles
 */
@Deprecated
public class RandomBean implements IDataSourceBean {

    static final IParam<DataSource, Integer> X_SEED = Params.onInteger(0, "seed");
    static final IParam<DataSource, Integer> X_LENGTH = Params.onInteger(240, "length");
    static final IParam<DataSource, Integer> X_P = Params.onInteger(0, "p");
    static final IParam<DataSource, Integer> X_D = Params.onInteger(1, "d");
    static final IParam<DataSource, Integer> X_Q = Params.onInteger(1, "q");
    static final IParam<DataSource, Integer> X_S = Params.onInteger(12, "s");
    static final IParam<DataSource, Integer> X_BP = Params.onInteger(0, "bp");
    static final IParam<DataSource, Integer> X_BD = Params.onInteger(1, "bd");
    static final IParam<DataSource, Integer> X_BQ = Params.onInteger(1, "bq");
    static final IParam<DataSource, double[]> X_COEFF = Params.onDoubleArray("coeff", -.8, -.6);
    static final IParam<DataSource, Integer> X_COUNT = Params.onInteger(100, "count");
    //
    int seed;
    int length;
    int p, d, q, s, bp, bd, bq;
    // the number of coeff must be p+bp+q+bq !!!
    double[] coeff;
    int count;

    public RandomBean() {
        this.seed = X_SEED.defaultValue();
        this.length = X_LENGTH.defaultValue();
        this.p = X_P.defaultValue();
        this.d = X_D.defaultValue();
        this.q = X_Q.defaultValue();
        this.s = X_S.defaultValue();
        this.bp = X_BP.defaultValue();
        this.bd = X_BD.defaultValue();
        this.bq = X_BQ.defaultValue();
        this.coeff = X_COEFF.defaultValue();
        this.count = X_COUNT.defaultValue();
    }

    public RandomBean(DataSource dataSource) {
        this.seed = X_SEED.get(dataSource);
        this.length = X_LENGTH.get(dataSource);
        this.p = X_P.get(dataSource);
        this.d = X_D.get(dataSource);
        this.q = X_Q.get(dataSource);
        this.s = X_S.get(dataSource);
        this.bp = X_BP.get(dataSource);
        this.bd = X_BD.get(dataSource);
        this.bq = X_BQ.get(dataSource);
        this.coeff = X_COEFF.get(dataSource);
        this.count = X_COUNT.get(dataSource);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    public int getQ() {
        return q;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public int getS() {
        return s;
    }

    public void setS(int s) {
        this.s = s;
    }

    public int getBp() {
        return bp;
    }

    public void setBp(int bp) {
        this.bp = bp;
    }

    public int getBd() {
        return bd;
    }

    public void setBd(int bd) {
        this.bd = bd;
    }

    public int getBq() {
        return bq;
    }

    public void setBq(int bq) {
        this.bq = bq;
    }

    public double[] getCoeff() {
        return coeff;
    }

    public void setCoeff(double[] coeff) {
        this.coeff = coeff;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    //</editor-fold>

    @Override
    public DataSource toDataSource(String providerName, String version) {
        DataSource.Builder builder = DataSource.builder(providerName, version);
        X_SEED.set(builder, seed);
        X_LENGTH.set(builder, length);
        X_P.set(builder, p);
        X_D.set(builder, d);
        X_Q.set(builder, q);
        X_S.set(builder, s);
        X_BP.set(builder, bp);
        X_BD.set(builder, bd);
        X_BQ.set(builder, bq);
        X_COEFF.set(builder, coeff);
        X_COUNT.set(builder, count);
        return builder.build();
    }

    @Deprecated
    public static RandomBean fromDataSource(DataSource dataSource) {
        return new RandomBean(dataSource);
    }

    @Deprecated
    public DataSource toDataSource() {
        return toDataSource(RandomProvider.SOURCE, RandomProvider.VERSION);
    }

    public SarimaSpecification toSpecification() {
        SarimaSpecification result = new SarimaSpecification(s);
        result.setP(p);
        result.setD(d);
        result.setQ(q);
        result.setBP(bp);
        result.setBD(bd);
        result.setBQ(bq);
        return result;
    }

    public SarimaModel toModel() {
        SarimaModel result = new SarimaModel(toSpecification());
        if (coeff != null) {
            result.setParameters(new ReadDataBlock(coeff));
        }
        return result;
    }
}
