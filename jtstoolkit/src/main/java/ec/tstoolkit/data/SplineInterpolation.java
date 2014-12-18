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
package ec.tstoolkit.data;

import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Natural splines. See for instance Burden and Faires
 *
 * @author Jean Palate
 */
public class SplineInterpolation {

    private static class Point {

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
        final double x, y;
    }

    private static class Spline {

        Spline(double x0, double h) {
            this.x0 = x0;
            this.h = h;
        }
        
        double span() {
            return h;
        }

        double evaluate(double x) {
            double z = x - x0;
            double p = d * z + c;
            p = p * z + b;
            p = p * z + a;
            return p;
        }
        final double x0, h;
        // coefficients of the polynomial
        double a, b, c, d;
    }
    private final List<Point> points_ = new ArrayList<>();
    private Spline[] splines_;
    
    public boolean add(double x, double y){
        if (!points_.isEmpty() && points_.get(points_.size()-1).x>=x)
            return false;
        points_.add(new Point(x,y));
        return true;
    }

    public double evaluate(double x) {
        if (!calc()) {
            return Double.NaN;
        }
        if (x < splines_[0].x0) {
            return lextrapolate(x);
        }
        for (int i = 0; i < splines_.length; ++i) {
            if (x <= splines_[i].x0+splines_[i].h) {
                return splines_[i].evaluate(x);
            }
        }
        return uextrapolate(x);
    }

    private double lextrapolate(double x) {
        Spline cur=splines_[0];
        return cur.a+cur.b*(x-cur.x0);
    }

    private double uextrapolate(double x) {
        //
        Ols ols=new Ols();
        int n=(int)points_.get(points_.size()-1).x+1;
        double[] y=new double[n];
        double[] t=new double[n];
        for (int i=0; i<y.length; ++i){
            t[i]=i;
            y[i]=evaluate(i);
        }
        RegModel model=new RegModel();
        model.setY(new DataBlock(y));
        model.addX(new DataBlock(t));
        model.setMeanCorrection(true);
        ols.process(model);
        double[] b=ols.getLikelihood().getB();
                
        int last=points_.size()-1;
        Spline cur=splines_[last-1];
        Point p=points_.get(last);
        // linear extrapolation
        double dx=x-p.x;
        return p.y+dx*(cur.b+cur.h*cur.c);
    }

    private boolean calc() {
        if (splines_ != null) {
            return true;
        }
        int n = points_.size();
        if (n < 2) {
            return false;
        }
        int m = n - 1;
        splines_ = new Spline[m];
        double[] q = new double[m];
        Point x0 = points_.get(0), xm1 = x0;
        double h, hprev=0;
        for (int i = 0; i < m; ++i) {
            Point x1 = points_.get(i + 1);
            h=x1.x-x0.x;
            Spline spline = new Spline(x0.x, h);
            spline.a = x0.y;
            splines_[i] = spline;
             if (i > 0) {
                q[i] = 3 * ((x1.y - x0.y) / h - (x0.y - xm1.y) / hprev);
                xm1 = x0;
            }
            x0 = x1;
            hprev=h;
        }
        double[] z = new double[n];
        double[] w = new double[m];
        for (int i = 1; i < m; ++i) {
            h=splines_[i].h;
            hprev=splines_[i-1].h;
            double l=2*(h+hprev)-splines_[i-1].h*w[i-1];
            w[i]=h/l;
            z[i]=(q[i]-h*z[i-1])/l;
        }
        double c=0,a=points_.get(m).y;
        for (int i=m-1; i>=0; --i){
            Spline cur=splines_[i];
            cur.c=z[i]-w[i]*c;
            cur.b=(a-cur.a)/cur.h-cur.h*(c+2*cur.c)/3;
            cur.d=(c-cur.c)/(3*cur.h);
            a=cur.a;
            c=cur.c;
        }
        return true;
    }
}
