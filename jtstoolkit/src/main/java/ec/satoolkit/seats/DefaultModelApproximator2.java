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
public class DefaultModelApproximator2 implements IModelApproximator {
    
    private static final double DEF_RMODP = .7, DEF_BPHI = .3;
    private double rmodp_ = DEF_RMODP;
    private int difsOrig_;
    
    public DefaultModelApproximator2() {
    }
    
    private boolean estimateModel(boolean ml, SeatsModel model,
            InformationSet info, SeatsContext context) {
        return context.getEstimator().estimate(ml, model, info);
    }
    
    @Override
    public boolean approximate(SeatsModel sm, InformationSet info, SeatsContext context) {
        if (difsOrig_ == -1) {
            SarimaSpecification spec = sm.getSarima().getSpecification();
            difsOrig_ = spec.getD() + spec.getBD();
        }
        if (app_known(sm, info, context)) {
            return true;
        }
        if (app_seas(sm, info, context)) {
            return true;
        }
        if (app_redp(sm, info, context)) {
            return true;
        }
        if (app_redq(sm, info, context)) {
            return true;
        }
        return app_last(sm, info, context);
    }
    
    private boolean app_seas(SeatsModel sm, InformationSet info, SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        boolean mean = sm.isMeanCorrection();
        if (spec.getBD() == 1 && spec.getBP() == 1) { // (...)(1 1 0) -> (...)(0 1 1)
            spec.setBP(0);
            spec.setBQ(1);
        } else if (spec.getBD() == 0 && spec.getBQ() == 1) { // (...)(x 0 1) 
            if (spec.getBP() == 0) {
                spec.setBP(1);
                spec.setBQ(0);
            } else {
                double bphi = cur.bphi(1), bth = cur.btheta(1);
                if (bphi < -rmodp_ && bth > 0) {
                    spec.setBD(1);
                    spec.setBP(0);
                    mean = false;
                } else {
                    spec.setBQ(0);
                }
            }
        } else if (spec.getBP() == 1 && spec.getBD() == 0 && spec.getBQ() == 0) {
            if (spec.getP() == 0 && spec.getD() == 1 && spec.getQ() > 1) {
                spec.setQ(spec.getQ() - 1);
            } else if (sm.hasSignificantSeasonality()&& cur.bphi(1) < -DEF_BPHI) {
                spec.setBP(0);
                spec.setBD(1);
                spec.setBQ(1);
            }
        } else {
            return false;
        }
        
        sm.setModelSpecification(spec);
        sm.setMeanCorrection(mean);
        return estimateModel(true, sm, info, context);
    }
    
    private boolean app_redp(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        if (spec.getP() == 0) {
            return false;
        }
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
            if (spec.getBD() + spec.getD() > difsOrig_) {
                sm.setMeanCorrection(false);
            }
        }
        int q = spec.getQ();
        q = Math.min(q + 1, spec.getD() + spec.getP());
        spec.setQ(Math.min(q, 3));
        sm.setModelSpecification(spec);
        return estimateModel(true, sm, info, context);
    }
    
    private boolean app_redq(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        if (spec.getQ() == 1) {
            return false;
        }
        spec.setQ(Math.max(spec.getQ() - 1, 1));
        sm.setModelSpecification(spec);
        return estimateModel(true, sm, info, context);
    }
    
    private boolean app_last(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        
        if (spec.getD() == 0 && spec.getBP() == 0 && spec.getBD() == 1 && spec.getBQ() == 1) {
            spec.setBQ(0);
        } else if (spec.getBP() == 1 && spec.getBD() == 0 && spec.getBQ() == 0) {
            double bphi = -cur.bphi(1);
            if (bphi > rmodp_) {
                spec.setBP(0);
                spec.setBD(1);
                spec.setBQ(1);
                sm.setMeanCorrection(false);
            } else {
                spec.setBP(0);
            }
        } else {
            return false;
        }
        sm.setModelSpecification(spec);
        return estimateModel(true, sm, info, context);
    }
    
    @Override
    public boolean pretest(SeatsModel sm, InformationSet info,
            SeatsContext context) {
        // BP == 1 and BPHi > 0 !!!
//        SarimaModel cur = sm.getSarima();
//        SarimaSpecification spec = cur.getSpecification();
//        boolean recalc = false;
//        if (spec.getBD() == 0 && spec.getBQ() == 1) {
//            if (spec.getBP() == 0) {
//                if (cur.btheta(1) > 0) {
//                    spec.setBD(1);
//                    recalc = true;
//                } else {
//                    spec.setBQ(0);
//                    recalc = true;
//                }
//            } else if (cur.bphi(1) < 0) {
//                spec.setBD(1);
//                spec.setBP(0);
//                recalc = true;
//            }
//            if (recalc) {
//                sm.setModelSpecification(spec);
//                if (estimateModel(true, sm, info, context)) {
//                    cur = sm.getSarima();
//                }
//                recalc = false;
//            }
//        }
//        if (spec.getBP() > 0 && cur.bphi(1) > 0) {
//            recalc = true;
//            if (spec.getBD() == 1 && spec.getBQ() == 1) {
//                spec.setBP(0);
//                sm.setMeanCorrection(true);
//            } else if (spec.getBD() == 1 && spec.getBQ() == 0) {
//                spec.setBP(0);
//                spec.setBQ(1);
//                sm.setMeanCorrection(true);
//            } else if (spec.getBD() == 0 && spec.getBQ() == 1) {
//                if (cur.bphi(1) < 0.1 + cur.btheta(1)) {
//                    spec.setBD(1);
//                    spec.setBQ(1);
//                    sm.setMeanCorrection(false);
//                } else {
//                    spec.setBP(0);
//                    spec.setBQ(0);
//                }
//            } else // 1 0 0
//            {
//                spec.setBP(0);
//                sm.setMeanCorrection(true);
//            }
//        }
//        if (recalc) {
//            sm.setModelSpecification(spec);
//            return estimateModel(true, sm, info, context);
//        } else {
//            return false;
//        }
        return true;
    }
    
    @Override
    public void startApproximation() {
        difsOrig_ = -1;
    }
    
    private boolean app_known(SeatsModel sm, InformationSet info, SeatsContext context) {
        SarimaModel cur = sm.getSarima();
        SarimaSpecification spec = cur.getSpecification();
        int p = spec.getP(), d = spec.getD(), q = spec.getQ(),
                bp = spec.getBP(), bd = spec.getBD(), bq = spec.getBQ();
        if (bp != 0 || bd != 1 || q > 1 || p != 0) {
            return false;
        }
        switch (d) {
            case 0:
                return app_001011(sm, cur, spec);
            case 2:
                return app_021011(sm, cur, spec);
            default:
                return app_011011(sm, cur, spec);
        }
    }
    
    private boolean app_001011(SeatsModel sm, SarimaModel cur, SarimaSpecification spec) {
        cur.setBTheta(1, 0);
        cur.adjustSpecification();
        sm.setModel(cur);
        return true;
    }
    
    private boolean app_011011(SeatsModel sm, SarimaModel cur, SarimaSpecification spec) {
        
        double th = spec.getQ() == 1 ? -cur.theta(1) : 0;
        double bth;
        switch (spec.getFrequency()) {
            case 12:
                if (th > 0.36 && th < 0.7) {
                    bth = .2;
                } else {
                    bth = .1;
                }
                break;
            case 4:
                if (th < 0.2 && th >= 0) {
                    bth = -0.3;
                } else if (th < 0 && th > -0.2) {
                    bth = -0.24;
                } else if (Math.abs(th) > 0.4) {
                    bth = -0.1;
                } else {
                    bth = -0.2;
                }
                break;
            
            default:
                bth = 0;
        }
        spec.setQ(th == 0 ? 0 : 1);
        spec.setBQ(bth == 0 ? 0 : 1);
        SarimaModel ncur = new SarimaModel(spec);
        if (th != 0) {
            ncur.setTheta(1, -th);
        }
        if (bth != 0) {
            ncur.setBTheta(1, -bth);
        }
        sm.setModel(ncur);
        return true;
    }
    
    private boolean app_021011(SeatsModel sm, SarimaModel cur, SarimaSpecification spec) {
        double th = spec.getQ() == 1 ? -cur.theta(1) : 0;
        double bth = spec.getBQ() == 1 ? -cur.btheta(1) : 0;
        switch (spec.getFrequency()) {
            case 12:
                if (th < -0.4) {
                    th = -0.5;
                    bth = 0.9;
                } else if (th < 0.25) {
                    bth = (4.95 - 4 * th) / 7;
                } else if (th < 0.55) {
                    if (bth >= -0.3) {
                        bth = -3.2 * th + 1.55;
                    } else {
                        th = 0.55;
                        bth = -0.3;
                    }
                } else {
                    bth = (th - 1.15) / 2;
                }
                break;
            case 4:
                if (th >= -0.4) {
                    bth = -0.3;
                } else {
                    if (bth < -0.3) {
                        th = -0.4;
                        bth = -0.3;
                    } else {
                        th = -0.45 * bth - 0.535;
                    }
                }
                break;
            case 6:
                if (th >= 0.1) {
                    bth = -0.2;
                } else {
                    if (bth < -0.3) {
                        bth = -0.3;
                        th = 0.1;
                    } else {
                        if (th > -0.3) {
                            bth = -1.5 * th - 0.05;
                        } else if (th >= -0.9) {
                            bth = -th - 0.05;
                        } else {
                            bth = 0.95;
                            th = -0.9;
                        }
                    }
                }
                break;
            case 3:
                if (th > 0.55) {
                    bth = -0.1;
                } else {
                    if (bth < -0.3) {
                        th = 0.55;
                        bth = -0.3;
                    } else {
                        if (th > -0.4) {
                            if (bth < 0) {
                                th = 0.55;
                            } else if (bth < 0.7) {
                                th = -0.5 * bth + 0.55;
                            } else {
                                th = -3.21 * bth + 2.45;
                            }
                        } else if (th > -0.7) {
                            bth = 0.95;
                        } else {
                            bth = 0.95;
                            th = -0.7;
                        }
                    }
                }
                break;
            case 2:
                if (th >= 0.4) {
                    bth = -0.3;
                } else {
                    bth = 0.48 * th - 0.49;
                }
                break;
        }
        boolean hasq = Math.abs(th) > 1e-10, hasbq = Math.abs(bth) > 1e-10;
        spec.setQ(hasq ? 1 : 0);
        spec.setBQ(hasbq ? 1 : 0);
        SarimaModel ncur = new SarimaModel(spec);
        if (hasq) {
            ncur.setTheta(1, -th);
        }
        if (hasbq) {
            ncur.setBTheta(1, -bth);
        }
        sm.setModel(ncur);
        return true;
    }
}
