/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tss.disaggregation.documents;

import ec.satoolkit.ISaSpecification;
import ec.tss.Ts;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.IModellingSpecification;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author palatej
 */
public class QNAModel implements InformationSetSerializable, Cloneable {

    public static class Variable implements Cloneable {

        private Ts s;
        private IModellingSpecification fcast;
        private ISaSpecification sa;

        public Variable(Ts s, ISaSpecification sa, IModellingSpecification fcast) {
            this.s = s;
            this.sa = sa;
            this.fcast = fcast;
        }

        @Override
        public Variable clone() {
            try {
                Variable v = (Variable) super.clone();
                if (fcast != null) {
                    v.fcast = fcast.clone();
                }
                if (sa != null) {
                    v.sa = sa.clone();
                }
                return v;
            } catch (CloneNotSupportedException ex) {
                throw new AssertionError();
            }
        }
    }

    private Variable y;
    private final List<Variable> indicators = new ArrayList<>();

    public void setY(Variable y) {
        this.y = y;
    }

    public void addX(Variable x) {
        this.indicators.add(x);
    }

    public void clearIndicators() {
        this.indicators.clear();
    }

    public List<Variable> getIndicators() {
        return Collections.unmodifiableList(indicators);
    }

    @Override
    public QNAModel clone() {
        try {
            QNAModel model = (QNAModel) super.clone();
            if (y != null) {
                model.y = y.clone();
            }
            model.indicators.clear();
            for (Variable x : indicators) {
                model.indicators.add(x.clone());
            }
            return model;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }

    }

    @Override
    public InformationSet write(boolean verbose) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean read(InformationSet info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
