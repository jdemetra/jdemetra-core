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

package ec.satoolkit.seats;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;

/**
 * This class is largely based on the program SEATS+ developed by Gianluca Caporello 
 * and Agustin Maravall -with programming support from Domingo Perez and Roberto Lopez- 
 * at the Bank of Spain, and on the program SEATS, previously developed by 
 * Victor Gomez and Agustin Maravall.<br>It corresponds more especially to an older version of the
 * routine <i>APPROXIMATE</i>
 */
@Deprecated
@Development(status = Development.Status.Temporary)
public class DefaultModelApproximator implements IModelApproximator {

    private char m_status;
    private static final double[] AIRQ = new double[]{-1.50568861051561e+07,
        -4.07864271885349e+06, 1.25495713468045e+08, 3.72893488076512e+07,
        -4.79316449488926e+08, -1.55227766622531e+08, 1.11199260604581e+09,
        3.89926250516196e+08, -1.75105862443012e+09, -6.60203090340804e+08,
        1.98087443551278e+09, 7.96938928504665e+08, -1.66146607023159e+09,
        -7.07321965792925e+08, 1.05100610295520e+09, 4.69148293267340e+08,
        -5.04888372223982e+08, -2.33997977625452e+08, 1.83900005449306e+08,
        8.76043491537447e+07, -5.02818844050958e+07, -2.43866695225120e+07,
        1.01154607380367e+07, 4.95920421687316e+06, -1.44719924386675e+06,
        -7.16551068193267e+05, 1.38950643779603e+05, 7.05870266796903e+04,
        -7.97845888823479e+03, -4.45727946957010e+03, 1.83672933301260e+02,
        1.61704844615717e+02, 6.28102914263037e+00, -1.85013363024192e+00,
        -4.67101412373999e-01, -3.14388900223822e-01};
    private static final double[] AIRM = new double[]{1.95347563065735e+07,
        1.40869407022632e+07, -1.82924843962725e+08, -1.24837916382162e+08,
        7.84012309810082e+08, 5.06656270824311e+08, -2.03824923561787e+09,
        -1.24723236243656e+09, 3.59083883274152e+09, 2.07891569193133e+09,
        -4.53585473370324e+09, -2.48015536619207e+09, 4.23918446756410e+09,
        2.18248985662395e+09, -2.98178573585705e+09, -1.43853103116210e+09,
        1.59018446079529e+09, 7.13771251305322e+08, -6.42824470238530e+08,
        -2.65666810599684e+08, 1.95564418343889e+08, 7.33014344799244e+07,
        -4.41316525032554e+07, -1.46797381353294e+07, 7.21747845147075e+06,
        2.06510268195992e+06, -8.25912908016856e+05, -1.94312700336529e+05,
        6.26921279186573e+04, 1.13487961429655e+04, -2.90128263818433e+03,
        -3.64564339836437e+02, 7.07021116258866e+01, 5.00986705245536e+00,
        -8.06732206713255e-01, -1.29571848439844e-01, -1.50316939442371e-01};
    private static final double[] D2Q = new double[]{-7.36886387334525e+03,
        5.71043080817024e+03, 4.89655041459870e+04, -2.72040336684033e+04,
        -1.40067644904055e+05, 5.38068353807028e+04, 2.26611284963096e+05,
        -5.62886010543245e+04, -2.28695251634143e+05, 3.21568573231266e+04,
        1.49568754383030e+05, -8.38954361805799e+03, -6.37582467027026e+04,
        -4.93256763478040e+02, 1.73902925238205e+04, 9.03967061027686e+02,
        -2.91111942281865e+03, -2.25701468391660e+02, 2.77900060158067e+02,
        2.36818663168160e+01, -1.32508103079524e+01, -9.99194312958687e-01,
        2.39589706532146e-01, 1.13994409953707e-02, -6.95571335187068e-04};
    private static final double[] D2M = new double[]{-6.16086458289453e+05,
        -1.93205965815733e+06, 1.88840463291657e+06, 9.78701910995365e+06,
        -1.11444188423980e+06, -2.21556751224427e+07,
        -3.17747695176425e+06, 2.95610373631368e+07, 7.13230914290129e+06,
        -2.57929135874340e+07, -6.93754106056724e+06, 1.54167965673704e+07,
        3.96437423488546e+06, -6.41657328297885e+06, -1.41686692105580e+06,
        1.84861784825349e+06, 3.14705412928108e+05, -3.58340765118489e+05,
        -4.10545347750240e+04, 4.41702532829458e+04, 2.79277887146530e+03,
        -3.15413411558056e+03, -7.90998715967224e+01, 1.14532961893102e+02,
        -6.90866544602480e-01, -9.20391422924575e-01,
        -5.95788710264705e-01, -6.82655329201659e-01};

    /**
     * 
     */
    public DefaultModelApproximator() {
    }

    private boolean app_a(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        spec.setQ(2);
        if (spec.getP() > 0 && cur.phi(1) < -.5) {
            spec.setD(1);
        }
        else {
            spec.setP(0);
        }
        m_status = 'C';
        sm.setModelSpecification(spec);
        return estimateModel(true, sm, info, context);
    }

    private boolean app_b(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        m_status = 'Z';
        spec.setQ(1);
        sm.setModelSpecification(spec);
        return estimateModel(true, sm, info, context);
    }

    private boolean app_bdd0(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        spec.setP(1);
        spec.setQ(2);
        m_status = 'A';
        sm.setModelSpecification(spec);
        return estimateModel(true, sm, info, context);
    }

    private boolean app_bdd1(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        spec.setQ(Math.max(1, spec.getQ() - 1));
        sm.setModelSpecification(spec);
        return estimateModel(true, sm, info, context);
    }

    private boolean app_bdd2(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        if (spec.getQ() == 0) {
            spec.setQ(1);
            sm.setModelSpecification(spec);
            return estimateModel(true, sm, info, context);
        }

        double rdroot = 0;
        Complex[] proots = cur.getRegularMA().roots();
        for (int i = 0; i < proots.length; ++i) {
            double re = proots[i].getRe(), im = proots[i].getIm();
            if (im == 0 && re > 0) {
                re = 1 / re;
                if (re > rdroot) {
                    rdroot = re;
                }
            }
        }
        spec.setQ(spec.getQ() - 1);
        if (rdroot > .5) {
            spec.setD(spec.getD() - 1);
            sm.setMeanCorrection(true);
            m_status = 'C';
        }
        else {
            spec.setQ(Math.max(1, spec.getQ()));
        }
        sm.setModelSpecification(spec);
        return estimateModel(true, sm, info, context);
    }

    private boolean app_bdq(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        if (cur.btheta(1) > 0) {
            spec.setBD(1);
            spec.setBQ(1);
        }
        else {
            spec.setBQ(0);
        }
        sm.setModelSpecification(spec);
        return estimateModel(false, sm, info, context);
    }

    private boolean app_c(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        spec.setQ(Math.max(spec.getQ() - 1, 1));
        sm.setModelSpecification(spec);
        return estimateModel(true, sm, info, context);
    }

    private boolean app_d(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        spec.setD(2);
        spec.setQ(1);
        spec.setBD(1);
        spec.setBQ(1);
        sm.setMeanCorrection(false);
        m_status = 'E';
        sm.setModelSpecification(spec);
        return estimateModel(false, sm, info, context);
    }

    private boolean app_d0(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        if (spec.getQ() > 2) {
            spec.setQ(2);
            m_status = 'I';
        }
        else if (spec.getQ() > 1) {
            spec.setQ(1);
            m_status = 'H';
        }
        else {
            spec.setD(1);
            spec.setQ(1);
            spec.setBD(1);
            spec.setBQ(1);
            sm.setMeanCorrection(true);
            m_status = 'F';
        }
        sm.setModelSpecification(spec);
        return estimateModel(false, sm, info, context);
    }

    private boolean app_d1(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        boolean ml = true;
        if (spec.getQ() > 2) {
            spec.setQ(2);
            ml = false;
            m_status = 'G';
        }
        else {
            spec.setQ(1);
        }
        sm.setModelSpecification(spec);
        return estimateModel(ml, sm, info, context);
    }

    private boolean app_e(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        app_ef(sm, info, context);
        SarimaModel cur = sm.getSarima().clone();
        if (cur.getSpecification().getBQ() > 0 && cur.btheta(1) > -.1) {
            cur.setBTheta(1, 0);
            cur.adjustSpecification();
        }
        sm.setModel(cur);
        return true;
    }

    private boolean app_ef(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima().clone();
        SarimaSpecification spec = cur.getSpecification();
        sm.setMeanCorrection(true);
        if (spec.getD() == 2) {
            if (spec.getBQ() > 0 && cur.btheta(1) > -.1) {
                cur.setBTheta(1, -.1);
            }
            if (spec.getQ() > 0) {
                if (spec.getFrequency() == 12) {
                    double mtheta = (-5.0 / 9.0) * (cur.btheta(1) + 1);
                    if (cur.theta(1) > mtheta) {
                        cur.setTheta(1, mtheta);
                    }
                }
                else // if (spec.Frequency == 4)
                {
                    double mtheta = (-3.0 / 11.0) * (cur.btheta(1) + .1) + .6;
                    if (cur.theta(1) > mtheta) {
                        cur.setTheta(1, mtheta);
                    }
                }
            }
        }
        else {
            cur.setBTheta(1, polyval(cur.theta(1), spec.getFrequency(), spec.getD()));
        }
        sm.setModel(cur);
        return true;
    }

    private boolean app_f(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        if (!app_ef(sm, info, context)) {
            return false;
        }
        SarimaModel cur = sm.getSarima().clone();
        if (cur.getSpecification().getBQ() > 0 && cur.btheta(1) > 0) {
            cur.setBTheta(1, 0);
            cur.adjustSpecification();
        }
        sm.setModel(cur);
        return true;
    }

    private boolean app_g(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        spec.setD(1);
        spec.setQ(1);
        spec.setBD(1);
        spec.setBQ(1);
        sm.setMeanCorrection(true);
        m_status = 'F';
        sm.setModelSpecification(spec);
        return estimateModel(false, sm, info, context);
    }

    private boolean app_h(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        spec.setD(1);
        spec.setQ(1);
        spec.setBD(1);
        spec.setBQ(1);
        sm.setMeanCorrection(false);
        m_status = 'F';
        sm.setModelSpecification(spec);
        return estimateModel(false, sm, info, context);
    }

    private boolean app_i(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        spec.setQ(1);
        sm.setMeanCorrection(false);
        m_status = 'H';
        return estimateModel(false, sm, info, context);
    }

    private boolean app_last(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        spec.setD(2);
        spec.setQ(2);
        spec.setBD(1);
        spec.setBQ(1);
        sm.setMeanCorrection(false);
        m_status = 'D';
        sm.setModelSpecification(spec);
        return estimateModel(false, sm, info, context);
    }

    private boolean app_redbp(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        spec.setBP(0);
        spec.setBD(1);
        spec.setBQ(1);
        sm.setModelSpecification(spec);
        return estimateModel(false, sm, info, context);
    }

    private boolean app_redp(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        double rdroot = 0;
        Complex[] proots = cur.getRegularAR().roots();
        for (int i = 0; i < proots.length; ++i) {
            double re = proots[i].getRe(), im = proots[i].getIm();
            if (im == 0 && re > 0) {
                re = 1 / re;
                if (re > rdroot) {
                    rdroot = re;
                }
            }
        }

        spec.setP(spec.getP() - 1);
        if (rdroot > .5) {
            spec.setD(Math.min(spec.getD() + 1, 2));
            sm.setMeanCorrection(false);
        }
        else {
            sm.setMeanCorrection(true);
        }
        int q=spec.getQ();
        q=Math.min(q+1, spec.getD() + spec.getP());
        if (q <= 3)
            spec.setQ(q);
        else{
            spec.airline();
        }
        sm.setModelSpecification(spec);
        return estimateModel(true, sm, info, context);
    }

    private boolean app_z(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaSpecification spec = sm.getSarima().getSpecification();
        if (spec.getP() == 0 && spec.getBP() == 0 && spec.getQ() == 1
                && spec.getBQ() == 1 && (spec.getD() == 1 || spec.getD() == 2)
                && spec.getBD() == 1) {
            return app_f(sm, info, context);
        }
        else if (spec.getP() > 0) {
            return app_redp(sm, info, context);
        }
        else if (spec.getBP() > 0) {
            return app_redbp(sm, info, context);
        }
        else if (spec.getBD() == 0) {
            if (spec.getBQ() > 0) {
                return app_bdq(sm, info, context);
            }
            else if (spec.getD() == 2) {
                return app_bdd2(sm, info, context);
            }
            else if (spec.getD() == 1) {
                return app_bdd1(sm, info, context);
            }
            else {
                return app_bdd0(sm, info, context);
            }
        }
        else if (spec.getD() == 0) {
            return app_d0(sm, info, context);
        }
        else if (spec.getD() == 1) {
            return app_d1(sm, info, context);
        }
        else {
            return app_last(sm, info, context);
        }
    }

    /**
     *
     * @param sm
     * @param info
     * @param context
     * @return
     */
    @Override
    public boolean approximate(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        switch (m_status) {
            case 'A':
                return app_a(sm, info, context);
            case 'B':
                return app_b(sm, info, context);
            case 'C':
                return app_c(sm, info, context);
            case 'D':
                return app_d(sm, info, context);
            case 'E':
                return app_e(sm, info, context);
            case 'F':
                return app_f(sm, info, context);
            case 'G':
                return app_g(sm, info, context);
            case 'H':
                return app_h(sm, info, context);
            case 'I':
                return app_i(sm, info, context);
            default:
                return app_z(sm, info, context);
        }
    }

    private boolean estimateModel(boolean ml, SeatsModel model,
            InformationSet info, SeatsContext context) {
        return context.getEstimator().estimate(ml, model, info);
    }

    //
    //
    // THIS FUNCTION COMPUTES THE MINIMUM BTHETA SUCH THAT GIVEN THETA
    // THE MODEL HAS A VALID DECOMPOSITION FOR MONTHLY AIRLINE MODEL,
    // QUARTERLY AIRLINE MODEL, AND MONTHLY AND QUARTERLY AIRLINE MODEL
    // WITH D^2
    // INPUT PARAMETER
    // MQ : FREQUENCY
    // D : DELTA OF THE MODEL
    // real*8 function POLYVAL(theta,mq,d)
    public static double polyval(double theta, int mq, int d) {
        double rslt = 0;
        double ptheta = 1;
        if (d == 2) {
            if (mq == 4) {
                for (int i = D2Q.length - 1; i >= 0; --i, ptheta *= -theta) {
                    rslt += D2Q[i] * ptheta;
                }
            }
            else {
                for (int i = D2M.length - 1; i >= 0; --i, ptheta *= -theta) {
                    rslt += D2M[i] * ptheta;
                }
            }
            rslt = -rslt + .075;
            if (rslt > .98) {
                rslt = .98;
            }
            else if (rslt < -.98) {
                rslt = -.98;
            }
        }
        else {
            if (mq == 4) {
                for (int i = AIRQ.length - 1; i >= 0; --i, ptheta *= theta) {
                    rslt += AIRQ[i] * ptheta;
                }
            }
            else {
                for (int i = AIRM.length - 1; i >= 0; --i, ptheta *= theta) {
                    rslt += AIRM[i] * ptheta;
                }
            }
            rslt += .01;
            rslt = rslt * 100 + 1;
            if (rslt < 0) {
                rslt = (int) (rslt - .5);
            }
            else {
                rslt = (int) (rslt + .5);
            }
            rslt /= 100;
        }
        return rslt;
    }

    /**
     *
     * @param sm
     * @param info
     * @param context
     * @return
     */
    @Override
    public boolean pretest(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        // BP == 1 and BPHi > 0 !!!
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        boolean recalc = false;
        if (spec.getBD() == 0 && spec.getBQ() == 1) {
            if (spec.getBP() == 0) {
                if (cur.btheta(1) > 0) {
                    spec.setBD(1);
                    recalc = true;
                }
                else {
                    spec.setBQ(0);
                    recalc = true;
                }
            }
            else if (cur.bphi(1) < 0) {
                spec.setBD(1);
                spec.setBP(0);
                recalc = true;
            }
            if (recalc) {
                sm.setModelSpecification(spec);
                if (estimateModel(true, sm, info, context)) {
                    cur = sm.getSarima();
                }
                recalc = false;
            }
        }
        if (spec.getBP() > 0 && cur.bphi(1) > 0) {
            recalc = true;
            if (spec.getBD() == 1 && spec.getBQ() == 1) {
                spec.setBP(0);
                sm.setMeanCorrection(true);
            }
            else if (spec.getBD() == 1 && spec.getBQ() == 0) {
                spec.setBP(0);
                spec.setBQ(1);
                sm.setMeanCorrection(true);
            }
            else if (spec.getBD() == 0 && spec.getBQ() == 1) {
                if (cur.bphi(1) < 0.1 + cur.btheta(1)) {
                    spec.setBD(1);
                    spec.setBQ(1);
                    sm.setMeanCorrection(false);
                }
                else {
                    spec.setBP(0);
                    spec.setBQ(0);
                }
            }
            else // 1 0 0
            {
                spec.setBP(0);
                sm.setMeanCorrection(true);
            }
        }
        if (recalc) {
            sm.setModelSpecification(spec);
            return estimateModel(true, sm, info, context);
        }
        else {
            return false;
        }
    }

    /**
     * 
     */
    @Override
    public void startApproximation() {
        m_status = 'Z';
    }
}
