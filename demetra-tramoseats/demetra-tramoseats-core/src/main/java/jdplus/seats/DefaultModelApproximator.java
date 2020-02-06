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
package jdplus.seats;

import demetra.math.Complex;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;

/**
 * This class is largely based on the program SEATS+ developed by Gianluca Caporello
 * and Agustin Maravall -with programming support from Domingo Perez and Roberto Lopez-
 * at the Bank of Spain, and on the program SEATS, previously developed by
 * Victor Gomez and Agustin Maravall.<br>It corresponds more especially to the
 * routine <i>APPROXIMATE</i>
 *
 * @author
 */
public class DefaultModelApproximator implements IModelApproximator {

    private final IModelEstimator estimator;
    private static final double DEF_RMODP = .7;
    private double rmodp_ = DEF_RMODP;
    private int originalDifferencing;

    public DefaultModelApproximator(IModelEstimator estimator) {
        if (estimator == null) {
            this.estimator = new DefaultModelEstimator(null, null);
        } else {
            this.estimator = estimator;
        }
    }

    @Override
    public boolean approximate(SeatsModel sm) {
        SarimaOrders spec = sm.getCurrentModel().specification();
        originalDifferencing = spec.getD() + spec.getBd();
        if (app_known(sm)) {
            return true;
        }
        if (app_seas(sm)) {
            return true;
        }
        if (app_redp(sm)) {
            return true;
        }
        if (app_redq(sm)) {
            return true;
        }
        return app_last(sm);
    }

//    private boolean app_first(SeatsModel sm, InformationSet info, SeatsContext context) {
//        SarimaModel cur = sm.getSarima();
//        SarimaSpecification spec = cur.getSpecification();
//        if (spec.getQ() == 0) {
//            return false;
//        }
//        spec.setQ(spec.getQ() - 1);
//        sm.setModelSpecification(spec);
//        return estimateModel(true, sm, info, context);
//    }
    private boolean app_seas(SeatsModel sm) {
        SarimaModel cur = sm.getCurrentModel();
        SarimaOrders spec = cur.specification();
        boolean mean = sm.isMeanCorrection();
        int p = spec.getP(), d = spec.getD(), q = spec.getQ(),
                bp = spec.getBp(), bd = spec.getBd(), bq = spec.getBq();
        if (bd == 1 && bp == 1) {// (...)(1 1 x) -> (...)(0 1 1)
            spec.setBp(0);
            spec.setBq(1);
        } else if (bd == 0 && bq == 1) { // (...)(x 0 1) 
            double bth = cur.btheta(1);
            if (bp == 0) {
                if (bth > 0) {// (...)(0 0 1) -> (...)(1 0 0)
                    spec.setBp(1);
                }
                spec.setBq(0);// (...)(0 0 1) -> (...)(0 0 0)
            } else {
                double bphi = cur.bphi(1);
                if (bphi < -rmodp_ && bth > 0) {
                    spec.setBd(1);// (...)(1 0 1) -> (...)(0 1 1)
                    spec.setBp(0);
                    mean = false;
                } else {
                    spec.setBq(0);// (...)(1 0 1) -> (...)(1 0 0)
                }
            }
        } else if (bp == 1 && bd == 0 && bq == 0) {// (...)(1 0 0) 
            if (p == 0 && d == 1 && q > 1) {
                spec.setQ(q - 1);// (0 1 x)(1 0 0) -> (0 1 x-1)(1 0 0)
            } else if (cur.bphi(1) < -.3 && sm.isSignificantSeasonality()) {
                spec.setBp(0);// (...)(1 0 0) -> (...)(0 1 1)
                spec.setBd(1);
                spec.setBq(1);
                mean = false;
            } else {
                spec.setBp(0);// (...)(1 0 0) -> (...)(0 0 0)
            }
        } else {
            return false;
        }

        sm.setMeanCorrection(mean);
        return estimateModel(sm, spec);
    }

    private boolean app_redp(SeatsModel sm) {
        SarimaModel cur = sm.getCurrentModel();
        SarimaOrders spec = cur.specification();
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
            if (spec.getBd() + spec.getD() > originalDifferencing) {
                sm.setMeanCorrection(false);
            }
        }
        int q = spec.getQ();
        q = Math.min(q + 1, spec.getD() + spec.getP());
        spec.setQ(Math.min(q, 3));
        return estimateModel(sm, spec);
    }

    private boolean app_redq(SeatsModel sm) {
        SarimaModel cur = sm.getCurrentModel();
        SarimaOrders spec = cur.specification();
        if (spec.getQ() == 1) {
            return false;
        }
        spec.setQ(Math.max(spec.getQ() - 1, 1));
        return estimateModel(sm, spec);
    }

    private boolean app_last(SeatsModel sm) {
        SarimaModel cur = sm.getCurrentModel();
        SarimaOrders spec = cur.specification();

        spec.setBq(0);
        return estimateModel(sm, spec);
    }

    private boolean app_known(SeatsModel sm) {
        SarimaModel cur = sm.getCurrentModel();
        SarimaOrders spec = cur.specification();
        int p = spec.getP(), d = spec.getD(), q = spec.getQ(),
                bp = spec.getBp(), bd = spec.getBd(), bq = spec.getBq();
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

    private boolean app_001011(SeatsModel sm, SarimaModel cur, SarimaOrders spec) {
        sm.setCurrentModel(cur.toBuilder().btheta(1, 0).adjustOrders(true).build());
        return true;
    }

    private boolean app_011011(SeatsModel sm, SarimaModel cur, SarimaOrders spec) {

        double th = spec.getQ() == 1 ? -cur.theta(1) : 0;
        double bth;
        switch (spec.getPeriod()) {
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
        spec.setBq(bth == 0 ? 0 : 1);
        SarimaModel.Builder builder = SarimaModel.builder(spec);
        if (th != 0) {
            builder.theta(1, -th);
        }
        if (bth != 0) {
            builder.btheta(1, -bth);
        }
        sm.setCurrentModel(builder.build());
        return true;
    }

    private boolean app_021011(SeatsModel sm, SarimaModel cur, SarimaOrders spec) {
        double th = spec.getQ() == 1 ? -cur.theta(1) : 0;
        double bth = spec.getBq() == 1 ? -cur.btheta(1) : 0;
        switch (spec.getPeriod()) {
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
                } else if (bth < -0.3) {
                    th = -0.4;
                    bth = -0.3;
                } else {
                    th = -0.45 * bth - 0.535;
                }
                break;
            case 6:
                if (th >= 0.1) {
                    bth = -0.2;
                } else if (bth < -0.3) {
                    bth = -0.3;
                    th = 0.1;
                } else if (th > -0.3) {
                    bth = -1.5 * th - 0.05;
                } else if (th >= -0.9) {
                    bth = -th - 0.05;
                } else {
                    bth = 0.95;
                    th = -0.9;
                }
                break;
            case 3:
                if (th > 0.55) {
                    bth = -0.1;
                } else if (bth < -0.3) {
                    th = 0.55;
                    bth = -0.3;
                } else if (th > -0.4) {
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
        spec.setBq(hasbq ? 1 : 0);
        SarimaModel.Builder builder = SarimaModel.builder(spec);
        if (hasq) {
            builder.theta(1, -th);
        }
        if (hasbq) {
            builder.btheta(1, -bth);
        }
        sm.setCurrentModel(builder.build());
        return true;
    }

    private boolean estimateModel(SeatsModel sm, SarimaOrders spec) {
        sm.setCurrentModel(SarimaModel.builder(spec).setDefault().build());
        return estimator.estimate(sm);
    }

}
