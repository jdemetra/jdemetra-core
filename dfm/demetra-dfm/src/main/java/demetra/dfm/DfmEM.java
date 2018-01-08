/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.dfm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

///**
// *
// * @author deanton
// */
//public class DfmEM implements IDfmInitializer, IDfmEstimator {
//
//    /**
//     * The DfmEM class implements the EM algorithm as described for instance in
//     * Banbura M and Modugno M, "Maximum likelihood estimation of factor models
//     * on data sets with arbitrary pattern of missing data" (ECB, Working paper
//     * series N° 1189, May 2010).
//     *
//     * The DfmEM class implements both the IDfmInitializer and IDfmEstimator
//     * interfaces. By setting a large number of required iterations and a strict
//     * convergence criterion (e.g. maxiter>100 and 0.000001>eps), the resulting
//     * parameters can be interpreted as maximum likelihood estimates.
//     * Conversely, a small number of iterations can be used when the resulting
//     * parameters are meant to be used as starting values in a subsequent
//     * optimisation procedure.
//     */
//    private int maxiter; //number of iterations
//    private double eps;  //threshold for convergence in terms of 
//    //loglikelihood increase
//    private boolean conv;// TRUE if convergence has been achieved (in terms of
//    // either "maxiter" or "eps"
//
//    private DynamicFactorModel dfm;
//    private DfmProcessor dfmproc;
//    private DataBlockStorage m_a;
//
//    private int m_used, m_dim;
//
//    private HashSet<MeasurementLoads> unique_logic;
//    private Matrix unique_types;
//
//    private TsInformationSet data;
//    private int iter;
//    private Likelihood L, oldL;
//
//    // references to model parameterization
//    private int nf_; // total number of factors (=sum(r))
//    private int nb_; // number of blocks
//    private int[] r; // vector of dimension nb_ specifying the number of factors 
//    // in each block
//    private int c_;  // max(nlags,12) in models with at least a variable 
//    // representing year-on-year growth rates
//    // or max(nlags,5) in models with quarter-on-quarter growth
//    private int nlags; // number of lags in the VAR
//
//    // Matrices needed to implement restrictions in loadings
//    private Matrix R_c, R_con_c, q_con_c;    // restrictions for variables 
//    // representing  YoY growth rates
//    private Matrix R_cd, R_con_cd, q_con_cd; // restrictions variables
//    // representing growth rates over 
//    // the quarter (Mariano-Murasawa)
//
//    private Matrix idx_iY, idx_iM, idx_iQ;  // Matrices of integers selecting
//    // factprs for the three CLASSES of 
//    // variables (YoY, QoQ, MoM)
//    // 
//    // Each matrix selects the factors
//    // that are relevant for each "type"
//    // of loading structure. Note that
//    // the "type" of loading structure 
//    // of each variable is independent 
//    // of its CLASS
//
//    // arrays needed to select the factors
//    // [example for the case with three blocks of one factor each and a join VAR(4) representation]
//    private int[] temp2;  // selects from the state vector the factors "F(t)" needed for the VAR representation
//    // [0 1 2 3      12 13 14 15       24 25 26 27]
//    private int[] temp2_; // selects from the state vector the factors "f(t)" corresponding to each block
//    // [0            12                24         ]
//
//    private boolean[] logic_temp2;
//    private boolean[] logic_temp2_;
//
//    private MSmoothingResults srslts_;
//    private MFilteringResults frslts_;
//
//    private Matrix ezz;   // E[f(t)f(t)']
//    private Matrix eZLZL; // E[F(t-1)F(t-1)']
//    private Matrix ezZL;  // E[f(t)F(t-1)']
//
//    // M-step
//    private Matrix A_, A, A_new;
//    private Matrix Q, Q_;
//    // data
//    int nobs; // number of observations
//    int N;    // number of time series
//    Matrix data_m; // transformed data in matrix form nobs x N (NaN when values are missing)
//    Matrix y;      // transformed data in matrix form nobs x N (with zeros instead of NaN)
//
//    // Arrays needed to selects variables with the unique "types" of loading structure 
//    // The selection is done separately for each CLASS of variables (i.e. YoY, QoQ, MoM)
//    int[] idx_M, idx_Q, idx_Y;
//    List<Integer> Lidx_M, Lidx_Q, Lidx_Y;
//
//    /**
//     * Initialization of variables needed to execute the EM algorithm
//     *
//     * @param dfm0
//     * @param data
//     */
//    void initCalc(DynamicFactorModel dfm0, TsInformationSet data) {
//        this.dfm = dfm0;
//        this.data = data;
//
//        
//       // dfm.getInitialization();
//                 
//                 
//        nobs = data.getCurrentDomain().getLength();
//        L = new Likelihood();
//        oldL = null;
//        conv = false;
//
//        maxiter = 30;
//
//        
//        eps = 1e-6;
//        nf_ = dfm.getFactorsCount(); // nf_=sum(r);
//
//        // this code is redundant, but let's keep it for the moment in case we
//        // want to increase the complexity of the model
//        //  nb_ = 3  ; // assume three blocks
//        nb_ = nf_; // number of blocks (we assume each block has only one factor)
//
//        r = new int[nb_];
//        // because we assume each block has only one factor:
//        for (int i = 0;
//                i < nb_;
//                i++) {
//            r[i] = 1;
//        }
//
//        c_ = dfm.getBlockLength();
//        nlags = dfm.getTransition().nlags;
//
//        LoadingsMarianoMurasawa();
//        LoadingsCumSum();
//
//        UniqueTypes();
//
//        dfmproc = new DfmProcessor();
//        dfmproc.setCalcVariance(true);
//
//        N = data.getSeriesCount();
//
//        y = data.generateMatrix(data.getCurrentDomain());
//        data_m = y.clone();
//
//        // delicate!!! I am changing data_ inside a matrix
//        // now the matrix of data y has zeroes instead of NaN
//        double[] data_ = y.internalStorage();
//        for (int ii = 0; ii < data_.length; ii++) {
//            if (Double.isNaN(data_[ii])) {
//                data_[ii] = 0.0;
//            }
//        }
//
//    }
//
//    /**
//     * Converts a Matrix m of zeroes and ones into a List of booleans[]
//     *
//     * @param m
//     * @return
//     */
//    private List<boolean[]> logic(Matrix m) {
//    // It has to be a matrix of ones and zeros. 
//        // Incorporate an error expection if this is not the case
//        ArrayList<boolean[]> list = new ArrayList<>();
//        DataBlockIterator rows = m.rows();
//        do {
//            DataBlock row = rows.getData();
//            boolean[] b = new boolean[row.getLength()];
//            for (int i = 0; i < b.length; ++i) {
//                b[i] = (row.get(i) == 1);
//            }
//            list.add(b);
//        } while (rows.next());
//        return list;
//    }
//
//    /**
//     * Converts an "selection" array of integers, which can be used to indicate
//     * positions into a boolean array indicating true in the positions to be
//     * selected
//     *
//     * @param index
//     * @return
//     */
//    private boolean[] logic(int[] index) {
//
//        // index contains positions of an array
//        int maxi = index[0];// selects the largest element of the array
//
//        for (int i = 1;
//                i < index.length;
//                i++) {
//            if (maxi <= index[i]) {
//                maxi = index[i];
//            }
//        }
//
//        boolean[] logicSelect = new boolean[maxi + 1]; //?
//
//        for (int i = 0; i < index.length; i++) {
//            logicSelect[index[i]] = true;
//        }
//
//        return logicSelect;
//    }
//
//    /**
//     * Converts an "selection" array of integers, which can be used to indicate
//     * positions into a boolean array indicating true in the positions to be
//     * selected. The size of the returned boolean array is given by "fixedsize"
//     *
//     * @param index
//     * @param fixedsize
//     * @return
//     */
//    private boolean[] logic(int[] index, int fixedsize) {
//
//        // select the largest element of the array
//        boolean[] logicSelect = new boolean[fixedsize]; //?
//
//        for (int i = 0; i < index.length; i++) {
//            logicSelect[index[i]] = true;
//        }
//
//        return logicSelect;
//    }
//
//    /**
//     * Converts an integer, which is used as an index to select a given
//     * position, into a boolean array indicating true in the position to be
//     * selected. The size of the returned boolean array is given by "fixedsize"
//     *
//     * @param index
//     * @param fixedsize
//     * @return
//     */
//    private boolean[] logic(int index, int fixedsize) {
//
//        // select the largest element of the array
//        boolean[] logicSelect = new boolean[fixedsize]; //?
//
//        logicSelect[index] = true;
//
//        return logicSelect;
//    }
//
//    /**
//     * Execute the Expectation-Maximization iterations. The algorithm stops only
//     * after the maximum number of iterations has been reached
//     *
//     * @param dfm0
//     * @param data
//     */
//    private void calc(DynamicFactorModel dfm0, TsInformationSet data) { // private  modifier has been eliminated
//
//        iter = 0;
//        dfm = dfm0.clone();
//        dfmproc.process(dfm, data);
//        dfmproc.getFilteringResults().evaluate(L);
//        double Ln = L.getLogLikelihood();
// 
//        double oldLn = 0;
//
//        while (iter < maxiter) {
//            System.out.println(Ln);
//            iter++;
//            oldLn = Ln;
//            Ln = emstep(dfm, data); // Given the parameters of dfm, get moments
//            convergence(Ln, oldLn, iter, false);
//        }
//    }
//
//    /**
//     * Assessment of convergence at iteration "iter" and print diagnosis if
//     * print is true; It also tries to print largest eigen value in the absence
//     * of convergence
//     *
//     * @param Ln
//     * @param oldLn
//     * @param iter
//     * @param print
//     * @return
//     */
//    private boolean convergence(double Ln, double oldLn, int iter, boolean print) {
//
//        double epsi = (Ln - oldLn);// / Math.abs(oldLn);
//        if (epsi > eps) {  // instead of getEps?
//            //         System.out.print("   The likelihood is increasing very fast ");
//
//            return false;
//        } else if (epsi <= eps && epsi > 0) {
//            if (print) {
//                System.out.print("   The likelihood is increasing very slow! ");
//            }
//            return true;
//        } else {
//            if (print) {
//                System.out.print("   The likelihood is decreasing! ");
//            }
//            try {
//                IEigenSystem es = EigenSystem.create(A, false);
//                Complex[] ev = es.getEigenValues(1);
//                System.out.println(ev[0].abs());
//                //     System.out.println(ev[1].abs());
//                //     System.out.println(ev[2].abs());
//            } catch (MatrixException err) {
//                System.out.println(A);
//            }
//            return false;
//        }
//    }
///**
// * Expectation and maximisation steps for a given iteration
// * @param dfm
// * @param data
// * @return 
// */
//    private double emstep(DynamicFactorModel dfm, TsInformationSet data) {
//
//        double logLike;
//        E_S();
//        M_S();
////        DynamicFactorModel tmp = dfm.clone();
////        tmp.normalize();
////        System.out.println(tmp);
////
//        dfmproc.process(dfm, data);
//        frslts_ = dfmproc.getFilteringResults();
//
//        frslts_.evaluate(L);
//
//        logLike = L.getLogLikelihood();
//        return logLike;
//        
//
//    }
//   
// 
// void M_S(){
//        
//     varmax();
//     loadingsMax();
//   //  loadingsYmax();
//   //  loadingsMmax();
//    // resmax(C_new);
//     
//     
//                 
//
// 
// }
// 
// void varmax(){
//       double scale = 1.0/(nobs); 
//       A_ = ezZL.clone();     //  true stays,  always changes      
//        SymmetricMatrix.lsolve(eZLZL, A_.subMatrix(), true);
//         // VAR form in my state space representation
//        // here i have to define inde3 and indicator2 and convert them to logical
//        A = new Matrix(c_ * nf_, c_ * nf_);
//        
//        int[] ind3 = new int[r.length];
//        int[] indicador2 = new int[r.length];
//        int[] indicador = new int[r.length];
//
//        // constructing vectors to select matrices
//        for (int i = 1; i < temp2_.length; i++) {
//            int ri = r[i - 1];
//            ind3[i] = ind3[i - 1] + r[i - 1];
//            indicador2[i] = indicador2[i - 1] + r[i - 1] * nlags;
//            indicador[i] = indicador[i - 1] + c_ * ri;
//
//        }
//
//        
//       
//        for (int i = 0; i < nb_; i++) {
//            int ri = r[i];
//
//            for (int j = 0; j < nb_; j++) {
//                int rj = r[j];
//
//                A.subMatrix(indicador[i], indicador[i] + ri, indicador[j], indicador[j] + rj * nlags).copy(A_.subMatrix(ind3[i], ind3[i] + ri, indicador2[j], indicador2[j] + rj * nlags));
//
//            }
//
//            int temp = (c_ - 1) * ri;
//
//            for (int ii = 0; ii < temp; ii++) {
//                A.subMatrix(indicador[i] + ri + ii, indicador[i] + ri + ii + 1, indicador[i] + ii, indicador[i] + ii + 1).set(1);
//            }
//
//        }
//
//        
//   //     System.out.println(A);
//        
//          // SHOCKS
//        Q_= new Matrix(nf_, nf_);
//        Q = new Matrix(c_ * nf_, c_ * nf_);
//
//        // CHECK WHETHER IT IS CORRECT: NOTICE I AM DISCARDING THE FIRST OBSERVATION BECAUSE STATE VECTOR DOES NOT CONTAIN INITIALIZATION
//
//              
//        Q_ = (ezz.minus(A_.times(ezZL.transpose()))).times(scale);
//
//        for (int i = 0; i < temp2_.length; i++) {
//            for (int j = 0; j < temp2_.length; j++) {
//                Q.subMatrix(temp2_[i], temp2_[i] + 1, temp2_[j], temp2_[j] + 1).copy(Q_.subMatrix(i, i + 1, j, j + 1));
//            }
//        }
//
//   
// }
// 
// 
// void loadingsMax(){
//     
//        int type = 0;
//        Matrix C_new = new Matrix(N, c_ * nf_); // the Matrix of factor loadings
//        Matrix C_index = new Matrix(N, c_ * nf_); // position of non zero loadings
//
//        List<boolean[]> logic_idx_iM = logic(idx_iM);  // matrix of positions where each row corresponds to a type
//        List<boolean[]> logic_idx_iQ = logic(idx_iQ);  // matrix of positions where each row corresponds to a type
//        List<boolean[]> logic_idx_iY = logic(idx_iY);  // matrix of positions where each row corresponds to a type
//
//        for (MeasurementLoads loads : unique_logic) {
//
//            // ideally, we should use the fact that we have 
//            // ennumerated the MeasurementType
//            Lidx_M = new ArrayList<>();
//            Lidx_Q = new ArrayList<>();
//            Lidx_Y = new ArrayList<>();
//
//            int counting = 0;
//
//            for (MeasurementDescriptor mdesc : dfm.getMeasurements()) {
//                mdesc.getStructure();
//
//                if (DynamicFactorModel.getMeasurementType(mdesc.type) == MeasurementType.YoY && mdesc.getLoads().equals(loads)) {
//                    Lidx_Y.add(counting);
//                }
//                if (DynamicFactorModel.getMeasurementType(mdesc.type) == MeasurementType.Q && mdesc.getLoads().equals(loads)) {
//                    Lidx_Q.add(counting);
//                }
//                if (DynamicFactorModel.getMeasurementType(mdesc.type) == MeasurementType.M && mdesc.getLoads().equals(loads)) {
//                    Lidx_M.add(counting);
//                }
//
//            //if (mdesc.type=="CD" ){Listx_Q.add(counter);}
//                //if (mdesc.type=="L" ){Listx_M.add(counter);}
//                counting = counting + 1;
//
//            }
//
//            idx_M = new int[Lidx_M.size()];
//            idx_Q = new int[Lidx_Q.size()];
//            idx_Y = new int[Lidx_Y.size()];
//
//            for (int iM = 0; iM < Lidx_M.size(); iM++) {
//                idx_M[iM] = Lidx_M.get(iM);
//            }
//            for (int iQ = 0; iQ < Lidx_Q.size(); iQ++) {
//                idx_Q[iQ] = Lidx_Q.get(iQ);
//            }
//            for (int iY = 0; iY < Lidx_Y.size(); iY++) {
//                idx_Y[iY] = Lidx_Y.get(iY);
//            }
//
///////////////////////////////////////////////////////////////////
////////////// MONTHLY////////////////////////////////////////////
///////////////////////////////////////////////////////////////////
//           // a couple of parameters needed to define numerator and denominator 
//            int rsi = 0;
//            for (int i = 0; i < r.length; i++) {
//                if (loads.used[i]) { // because loads is public
//                    rsi += r[i];
//                }
//            }
//
//            int nMi = idx_M.length;
//
//            boolean[] logic_idx_M = logic(idx_M, N);          // integer[] with positions for variables of the given type
//
//        //     int nQi = idx_Q.length;
//            //     int nYi = idx_Y.length;
//            Matrix denom = new Matrix(nMi * rsi, nMi * rsi);
//            Matrix denom_interm = new Matrix(nMi * rsi, nMi * rsi);
//            Matrix nom = new Matrix(nMi, rsi);
//            Matrix nom_interm = new Matrix(nMi, rsi);
//
//            // ATTENTION if i=1; I will be ignoring the first observation (because we do not include the initial state in the state vector)
// //           for (int i = 1; i < nobs; i++) {  // we will be ignoring the first observation (because we do not include the initial state in the state vector)
//             for (int i = 0; i < nobs; i++) {  
//                boolean[] logic_i = new boolean[nobs];
//                logic_i[i] = true;
//
//                // this is messy
//                double[] buffer = new double[N];
//                data_m.row(i).copyTo(buffer, 0); //subMatrix(i, i+1, 0, N)
//
//                double[] temp = new double[idx_M.length];
//                for (int j = 0; j < idx_M.length; j++) {
//                    if (!Double.isNaN(buffer[idx_M[j]])) {
//                        temp[j] = 1;
//                    }
//                }
//
//                Matrix nanYt = Matrix.diagonal(temp);
//
//                DataBlock bloque = DataBlock.select(srslts_.A(i).deepClone(), logic_idx_iM.get(type));
//                Matrix ztemp = new Matrix(bloque.getData(), sum(logic_idx_iM.get(type)), 1); //idx_iM.row(type).sum()   sumAll(logic_idx_iM.get(type))                   
//
//                Matrix ezztemp = ztemp.times(ztemp.transpose()).plus(Matrix.select(srslts_.P(i), logic_idx_iM.get(type), logic_idx_iM.get(type)));
//                denom_interm.subMatrix().kronecker(ezztemp.subMatrix(), nanYt.subMatrix());
//                denom.add(denom_interm);
//
//                nom_interm.subMatrix().product(Matrix.select(y.subMatrix(), logic_i, logic_idx_M).subMatrix().transpose(), ztemp.subMatrix().transpose());
//                nom.add(nom_interm);
//
//            }
//
//            Matrix vec_C = new Matrix(nom.internalStorage(), nom.getColumnsCount() * nom.getRowsCount(), 1);
//            SymmetricMatrix.rsolve(denom, vec_C.subMatrix(), true);
//
//            Matrix Ctype = new Matrix(vec_C.internalStorage(), nMi, rsi);
//            // upload Jean's new function 
//            C_new.subMatrix().copy(Ctype.subMatrix(), logic_idx_M, logic_idx_iM.get(type));
//            //   System.out.println(C_new);
//            
//            // UPDATING LOADINGS!!!!!
//            for (int j = 0; j < idx_M.length; j++) {
//                MeasurementDescriptor desc = dfm.getMeasurements().get(idx_M[j]);
//                for (int k = 0; k < dfm.getFactorsCount(); ++k) {
//                    if (!Double.isNaN(desc.coeff[k])) {
//    // --->                      
//                        desc.coeff[k] = C_new.get(idx_M[j], c_ * k);
//                    }
//                }
//  
//            }
//
///////////////////////////////////////////////////////////////////
////////////// QUARTERLY////////////////////////////////////////////
///////////////////////////////////////////////////////////////////
//           // a couple of parameters  and matrices needed to define numerator and denominator 
//            int rpsi = rsi * Math.max(nlags, 5); // 5 is the numer of lags needed in Mariano Murasawa transformation
//
//            int nQi = idx_Q.length;
//
//            boolean[] logic_idx_Q = logic(idx_Q, N);          // integer[] with positions for variables of the given type
//
//            Matrix R_con_cd_i = Matrix.selectColumns(R_con_cd.subMatrix(), logic_idx_iQ.get(type));
//            Matrix q_con_cd_i = q_con_cd.clone();
//
//            // let's remove the rows that are full of zeros because they do not imply any rstrictions (this couldp happen if r[i]>1
//            boolean[] select = new boolean[R_con_cd_i.getRowsCount()];
//            for (int i = 0; i < R_con_cd_i.getRowsCount(); i++) {
//
//                if (R_con_cd_i.row(i).isZero() == false) {
//                    select[i] = true;
//                }
//
//            }
//
//            R_con_cd_i = Matrix.selectRows(R_con_cd_i.subMatrix(), select);
//            q_con_cd_i = Matrix.selectRows(q_con_cd_i.subMatrix(), select);
//
//            // for quarterly variables let's do it one by one
//            for (int i = 0; i < idx_Q.length; i++) {
//
//                boolean[] logic_idx_Q_i = logic(idx_Q[i], N);          // integer[] with positions for variables of the given type
//
//                denom = new Matrix(rpsi, rpsi);
//                denom_interm = new Matrix(rpsi, rpsi);
//                nom = new Matrix(1, rpsi);
//                nom_interm = new Matrix(1, rpsi);
//
//                // here the for loop
//       //       for (int t = 1; t < nobs; t++) {
//                for (int t = 0; t < nobs; t++) {
//                    boolean[] logic_t = new boolean[nobs];
//                    logic_t[t] = true;
//
//                    // this is messy
//                    double[] buffer = new double[N];
//                    data_m.row(t).copyTo(buffer, 0); //subMatrix(i, i+1, 0, N)
//
//                    double temp = 0;
//
//                    if (!Double.isNaN(buffer[idx_Q[i]])) {
//                        temp = 1;
//                    }
//
//                    Matrix nanYt = new Matrix(1, 1);
//                    nanYt.set(temp);
//
//                    DataBlock bloque = DataBlock.select(srslts_.A(t).deepClone(), logic_idx_iQ.get(type));
//                    Matrix ztemp = new Matrix(bloque.getData(), sum(logic_idx_iQ.get(type)), 1); //idx_iM.row(type).sum()   sumAll(logic_idx_iM.get(type))                   
//
//                    Matrix ezztemp = ztemp.times(ztemp.transpose()).plus(Matrix.select(srslts_.P(t), logic_idx_iQ.get(type), logic_idx_iQ.get(type)));
//                    denom_interm.subMatrix().kronecker(ezztemp.subMatrix(), nanYt.subMatrix());
//                    denom.add(denom_interm);
//
//                    nom_interm.subMatrix().product(Matrix.select(y.subMatrix(), logic_t, logic_idx_Q_i).subMatrix().transpose(), ztemp.subMatrix().transpose());
//                    nom.add(nom_interm);
//
//                }  // closing loop for each time (nobs)
//
//                vec_C = new Matrix(nom.internalStorage(), nom.getColumnsCount() * nom.getRowsCount(), 1);
//                   //vec_C= nom.clone().transpose(); //
//
//                // NOW INCORPORATE RESTRICTIONS
//                SymmetricMatrix.rsolve(denom, vec_C.subMatrix(), true);
//
//                Matrix temp = R_con_cd_i.clone().transpose();
//                SymmetricMatrix.rsolve(denom, temp.subMatrix(), true);
//                Matrix temp2 = R_con_cd_i.times(temp);
//                Matrix temp3 = temp.clone();
//
//                Matrix Ctype_i = new Matrix(nom.getColumnsCount() * nom.getRowsCount(), 1);
//                Ctype_i = vec_C.minus(Matrix.lsolve(temp2.subMatrix(), temp3.subMatrix()).times((R_con_cd_i.times(vec_C)).minus(q_con_cd_i)));
//
//                // upload Jean's new function 
//                C_new.subMatrix().copy(Ctype_i.subMatrix().transpose(), logic_idx_Q_i, logic_idx_iQ.get(type));
//
//            }  // closeing loop for each Q variable
//            
//            
//                        // UPDATING LOADINGS!!!!!
//            for (int j = 0; j < idx_Q.length; j++) {
//                MeasurementDescriptor desc = dfm.getMeasurements().get(idx_Q[j]);
//                for (int k = 0; k < dfm.getFactorsCount(); ++k) {
//                    if (!Double.isNaN(desc.coeff[k])) {
//   // --->                   
//                        desc.coeff[k] = C_new.get(idx_Q[j], c_ * k);
//                    }
//                }
//  
//            }
//
///////////////////////////////////////////////////////////////////
////////////// SURVEYS    ////////////////////////////////////////////
///////////////////////////////////////////////////////////////////
//           // a couple of parameters  and matrices needed to define numerator and denominator 
//            rpsi = rsi * Math.max(nlags, 12); // 12 is the numer of lags needed in cumsum transformation
//
//            int nYi = idx_Y.length;
//
//            boolean[] logic_idx_Y = logic(idx_Y, N);          // integer[] with positions for variables of the given type
//
//            Matrix R_con_c_i = Matrix.selectColumns(R_con_c.subMatrix(), logic_idx_iY.get(type));
//            Matrix q_con_c_i = q_con_c.clone();
//
//            // let's remove the rows that are full of zeros because they do not imply any rstrictions (this couldp happen if r[i]>1
//            select = new boolean[R_con_c_i.getRowsCount()];
//            for (int i = 0; i < R_con_c_i.getRowsCount(); i++) {
//
//                if (R_con_c_i.row(i).isZero() == false) {
//                    select[i] = true;
//                }
//
//            }
//
//            R_con_c_i = Matrix.selectRows(R_con_c_i.subMatrix(), select);
//            q_con_c_i = Matrix.selectRows(q_con_c_i.subMatrix(), select);
// 
//            for (int i = 0; i < idx_Y.length; i++) {
//
//                boolean[] logic_idx_Y_i = logic(idx_Y[i], N);          // integer[] with positions for variables of the given type
//
//                denom = new Matrix(rpsi, rpsi);
//                denom_interm = new Matrix(rpsi, rpsi);
//                nom = new Matrix(1, rpsi);
//                nom_interm = new Matrix(1, rpsi);
//
//                // here the for loop taking into account FULL SAMPLE
//             //   for (int t = 1; t < nobs; t++) {
//                  for (int t = 0; t < nobs; t++) {
//
//                    boolean[] logic_t = new boolean[nobs];
//                    logic_t[t] = true;
//
//                    // this is messy
//                    double[] buffer = new double[N];
//                    data_m.row(t).copyTo(buffer, 0); //subMatrix(i, i+1, 0, N)
//
//                    double temp = 0;
//
//                    if (!Double.isNaN(buffer[idx_Y[i]])) {
//                        temp = 1;
//                    }
//
//                    Matrix nanYt = new Matrix(1, 1);
//                    nanYt.set(temp);
//
//                    DataBlock bloque = DataBlock.select(srslts_.A(t).deepClone(), logic_idx_iY.get(type));
//                    Matrix ztemp = new Matrix(bloque.getData(), sum(logic_idx_iY.get(type)), 1); //idx_iM.row(type).sum()   sumAll(logic_idx_iM.get(type))                   
//
//                    Matrix ezztemp = ztemp.times(ztemp.transpose()).plus(Matrix.select(srslts_.P(t), logic_idx_iY.get(type), logic_idx_iY.get(type)));
//                    denom_interm.subMatrix().kronecker(ezztemp.subMatrix(), nanYt.subMatrix());
//                    denom.add(denom_interm);
//
//                    nom_interm.subMatrix().product(Matrix.select(y.subMatrix(), logic_t, logic_idx_Y_i).subMatrix().transpose(), ztemp.subMatrix().transpose());
//                    nom.add(nom_interm);
//
//                }  // closing loop for each time (nobs)
//
//                vec_C = new Matrix(nom.internalStorage(), nom.getColumnsCount() * nom.getRowsCount(), 1);
//                   //vec_C= nom.clone().transpose(); //
//
//                // NOW INCORPORATE RESTRICTIONS
//                SymmetricMatrix.rsolve(denom, vec_C.subMatrix(), true);
//
//                Matrix temp = R_con_c_i.clone().transpose();
//                SymmetricMatrix.rsolve(denom, temp.subMatrix(), true);
//                Matrix temp2 = R_con_c_i.times(temp);
//                Matrix temp3 = temp.clone();
//
//                Matrix Ctype_i = new Matrix(nom.getColumnsCount() * nom.getRowsCount(), 1);
//                Ctype_i = vec_C.minus(Matrix.lsolve(temp2.subMatrix(), temp3.subMatrix()).times((R_con_c_i.times(vec_C)).minus(q_con_c_i)));
//
//                 C_new.subMatrix().copy(Ctype_i.subMatrix().transpose(), logic_idx_Y_i, logic_idx_iY.get(type));
//
//                
//              
//            }  // closeing loop for each Y variable
//
//                        // UPDATING LOADINGS!!!!!
//            for (int j = 0; j < idx_Y.length; j++) {
//                MeasurementDescriptor desc = dfm.getMeasurements().get(idx_Y[j]);
//                for (int k = 0; k < dfm.getFactorsCount(); ++k) {
//                    if (!Double.isNaN(desc.coeff[k])) {    
//                        desc.coeff[k] = C_new.get(idx_Y[j], c_ * k);
//                    }
//                }
//  
//            }
//            type++;
//        }
////------------------------------------*****************
//
// 
//        
//         // UPDATING TRANSITION EQUATION
//
//        dfm.getTransition().covar.copy(Q_);
//        dfm.getTransition().varParams.copy(A_);
//        
//        
//              // NOW CALCULATE m step for IDIOSYNCRATIC COMPONENT
//
//        Matrix R = new Matrix(N, N);  // replace by the R obtained in previous iteration
//        List<MeasurementDescriptor> measurements = dfm.getMeasurements();
//        int counting = 0;
//        for (MeasurementDescriptor desc : measurements) {
//            R.set(counting, counting, desc.var);
//            counting++;
//        }
//
//        Matrix R_new = new Matrix(N, N);
//        //for (int i = 1; i < nobs; i++) { 
//        for   (int i = 0; i < nobs; i++) {  // take into  account  FULL SAMPLE
//        
//            boolean[] logic_i = new boolean[nobs];
//            logic_i[i] = true;
//            double[] buffer = new double[N];
//            data_m.row(i).copyTo(buffer, 0); //subMatrix(i, i+1, 0, N)
//            double[] temp = new double[N];
//            for (int j = 0; j < N; j++) {
//                if (!Double.isNaN(buffer[j])) {
//                    temp[j] = 1;
//                }
//            }
//
//            Matrix nanYt = Matrix.diagonal(temp);
//            DataBlock bloque = srslts_.A(i).deepClone();
//            Matrix ztemp = new Matrix(bloque.getData(), bloque.getLength(), 1); //idx_iM.row(type).sum()   sumAll(logic_idx_iM.get(type))                   
//            Matrix V = new Matrix(srslts_.P(i));
//
//            Matrix res1, res2, res3, res4;
//            res1 = Matrix.selectRows(y.subMatrix(), logic_i).transpose().minus(nanYt.times(C_new).times(ztemp));
//            res2 = res1.clone();
//            res3 = nanYt.times(C_new).times(V).times(C_new.transpose()).times(nanYt);
//            res4 = (Matrix.identity(N).minus(nanYt)).times(R).times(Matrix.identity(N).minus(nanYt));
//
//            Matrix R_temp = res1.times(res2.transpose()).plus(res3).plus(res4);
//
//            R_new = R_new.plus(R_temp);
//
//        }
//
//        double scale = 1.0 / (nobs);
//        R_new.mul(scale);
//
//        counting = 0;
//        for (MeasurementDescriptor desc : measurements) {
//            desc.var = R_new.get(counting, counting);
//            counting++;
//        }        
//      
// }
// 
// 
// void E_S(){
// 
//       // no neet to repeat!
//       // dfmproc.process(dfm, data);
//        
//       //  MSmoothingResults srslts_;      
//        srslts_ = dfmproc.getSmoothingResults();
//        //srslts_.setStandardError(1);
//        // MFilteringResults frslts_;
//        frslts_ = dfmproc.getFilteringResults();
//
//        m_a = srslts_.getSmoothedStates();
//
//        m_used = m_a.getCurrentSize();
//        m_dim = m_a.getDim();
//        
//        Matrix z0 = allcomponents();
//        ezz   = Ezz(z0);
//        eZLZL = EZLZL(z0);
//        ezZL  = EzZL(z0); 
// }
//
//    Matrix allcomponents() {
//
//        if (m_a == null) {
//            return null;
//        }
//        if (m_dim != c_ * nf_) {
//            System.out.print("Problem in the factors dimension: m_dim is different from c_*nf_");
//            return null;
//        }
//
//        Matrix c = new Matrix(m_dim, m_used);
//
//        for (int i = 0; i < m_dim; i++) {
//            c.row(i).copy(m_a.item(i));
//         }
//
//        return c;
//    }
//
//    MatrixStorage ms;
//    DataBlockStorage ds;
//
//    // notice that the initial state is not being taken into account
//    private Matrix Ezz(Matrix z0) {
//        // how to get the initial states???
//        IMSsf ssf = dfm.ssfRepresentation();   
//        Matrix P0=new Matrix(ssf.getStateDim(), ssf.getStateDim());
//        ssf.Pf0(P0.subMatrix());
//        
//        //getInitialVariance()
//                
//        //dfmproc.process(dfm, data);
//  //      Matrix z0 = allcomponents();
//        
// //     Ssf.getInitialVariance() 
////      SubMatrix z1 = z0.subMatrix(0, z0.getRowsCount(), 1, z0.getColumnsCount());
//        SubMatrix z1 = z0.subMatrix(0, z0.getRowsCount(), 0, z0.getColumnsCount());
//        Matrix z = Matrix.selectRows(z1, logic_temp2_);
//        Matrix zz = z.times(z.transpose());
//
//        Matrix eP = new Matrix(temp2_.length, temp2_.length);
//        double covij_sum;
//        double[] petittest;
//        for (int i = 0; i < temp2_.length; i++) {
//            for (int j = 0; j < temp2_.length; j++) {
//         //     covij_sum = sum0(srslts_.componentCovar(temp2_[i], temp2_[j]));
//                covij_sum =  srslts_.componentCovar(temp2_[i], temp2_[j]).sum();
//
//                eP.set(i, j, covij_sum);
//            }
//        }
//
//        Matrix ezz = zz.plus(eP);
//        return ezz;
//    }
//
//// notice that the initial state is not being taken into account
//    private Matrix EZLZL(Matrix z0) {
//      //  dfmproc.process(dfm, data);
//       // Matrix z0 = allcomponents();
//        
//        int[] temp2shift           = new int[temp2.length];
//        boolean[] logic_temp2shift = new boolean[nf_];  
//        for (int i=0;i<temp2.length;i++){temp2shift[i]=temp2[i]+1;
//        }
//        logic_temp2shift = logic(temp2shift);
//        
//      //SubMatrix z1 = z0.subMatrix(0, z0.getRowsCount(), 0, z0.getColumnsCount() - 1);
//        SubMatrix z1 = z0.subMatrix(0, z0.getRowsCount(), 0, z0.getColumnsCount());
//        Matrix z = Matrix.selectRows(z1, logic_temp2shift);
//        Matrix zz = z.times(z.transpose()); 
//
//       
//       // Matrix eP0 = new Matrix(temp2.length, temp2.length);
//        Matrix eP = new Matrix(temp2.length, temp2.length);
//       // Matrix eP_ = new Matrix(temp2.length, temp2.length);
//         
//        double covij_sum;  // SIMPLIFY IT
//        for (int i = 0; i < temp2.length; i++) {
//            for (int j = 0; j < temp2.length; j++) {
//                //-->NOT NEEDED covij_sum = sumT(srslts_.componentCovar(temp2[i], temp2[j]));
//                                covij_sum =  srslts_.componentCovar(temp2[i]+1, temp2[j]+1).sum();
//
//              // incorporate smoothed initial variance 
//              //-->NOT NEEDED   covij_0 =srslts_.P(0).get(temp2[i]+1, temp2[j]+1);
//              //-->NOT NEEDED   eP.set(i, j, covij_sum+covij_0);
//                                eP.set(i, j, covij_sum);
//                                
//            }
//        }
//        
//        
//        Matrix eZLZL = zz.plus(eP);
//        return eZLZL;
//    }
//
//   // notice that the initial state is not being taken into account
//    private Matrix EzZL(Matrix z0) {
//        
//        
// //               Q_ = (ezz.minus(A_.times(ezZL.transpose()))).times(scale);
//                
//    //    dfmproc.process(dfm, data);
//    //    Matrix z0 = allcomponents();
//        
//        
//    //  SubMatrix z1  = z0.subMatrix(0, z0.getRowsCount(), 1, z0.getColumnsCount());
//
//        SubMatrix z1  = z0.subMatrix(0, z0.getRowsCount(), 0, z0.getColumnsCount());     
////***   SubMatrix z1L = z0.subMatrix(0, z0.getRowsCount(), 0, z0.getColumnsCount() - 1);  
//        SubMatrix z1L = z0.subMatrix(0, z0.getRowsCount(), 0, z0.getColumnsCount() ); // the initial state should be added, but it is zero
// 
//        Matrix z = Matrix.selectRows(z1, logic_temp2_);
//        
//        int[] temp2shift           = new int[temp2.length];
//        boolean[] logic_temp2shift = new boolean[nf_];  
//        for (int i=0;i<temp2.length;i++){temp2shift[i]=temp2[i]+1;
//        }
//        logic_temp2shift = logic(temp2shift);
//        
//        Matrix zL = Matrix.selectRows(z1L, logic_temp2shift);
// //***  Matrix zL = Matrix.selectRows(z1L, logic_temp2);
//      
//        Matrix zz = z.times(zL.transpose());
//
//        Matrix eP = new Matrix(temp2_.length, temp2.length);
//        double covij_sum, covij_0;    // SIMPLIFY IT
//        for (int i = 0; i < temp2_.length; i++) {
//            for (int j = 0; j < temp2.length; j++) {
//                     covij_sum =srslts_.componentCovar(temp2_[i], temp2[j]+1).sum();
////***                covij_sum =sum0(srslts_.componentCovar(temp2_[i], temp2[j]+1));
//                  // just set eP
//                    
//                     eP.set(i, j, covij_sum);
//                     // ->not needed  covij_0 =srslts_.P(0).get(temp2_[i]+1, temp2[j]+1+1);
//                   // -> not needed   eP.set(i, j, covij_sum+covij_0);
// 
//                    
//            }
//        }
//
//        Matrix ezZL = zz.plus(eP);
//        return ezZL;
//    }
//
//     
//    @Override
//    public boolean initialize(DynamicFactorModel dfm0, TsInformationSet data) {
//        initCalc(dfm0, data);
//        calc(dfm0,data);
//        return true;
//    }
//
//    @Override
//    public boolean estimate(DynamicFactorModel dfm0, TsInformationSet data) {
//        initCalc(dfm0, data);
//        calc(dfm0,data);
//        return true;
////        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public Matrix getHessian() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public DataBlock getGradient() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    /**
//     * @return the maxiter
//     */
//    public int getMaxiter() {
//        return maxiter;
//    }
//
//    /**
//     * @param maxiter the maxiter to set
//     */
//    public void setMaxIter(int maxiter) {
//        this.maxiter = maxiter;
//    }
//
//    /**
//     * @return the eps
//     */
//    public double getEps() {
//        return eps;
//    }
//
//    /**
//     * @param eps the eps to set
//     */
//    public void setEps(double eps) {
//        this.eps = eps;
//    }
//
//    /**
//     * @return the conv
//     */
//    public boolean isConv() {
//        return conv;
//    }
//
//    /**
//     * @return the iter
//     */
//    public int getIter() {
//        return iter;
//    }
//
//    // sum the elements of array "r";
//    double sum0(double[] elements) {
//        double suma = 0.0;
//        for (int i = 0; i < elements.length; i++) {
//            suma += elements[i];
//        }
//        suma = suma - elements[0];
//        return suma;
//    }
//
//    double sumT(double[] elements) {
//        double suma = 0.0;
//        for (int i = 0; i < elements.length; i++) {
//            suma += elements[i];
//        }
//        suma = suma - elements[elements.length - 1];
//        return suma;
//    }
//
//    double sum(double[] elements) {
//        double suma = 0.0;
//        for (int i = 0; i < elements.length; i++) {
//            suma += elements[i];
//        }
//        return suma;
//    }
//
//    int sum(boolean[] elements) {
//        int suma = 0;
//        for (int i = 0; i < elements.length; i++) {
//            if (elements[i]) {
//                suma++;
//            }
//
//        }
//        return suma;
//    }
//
//   /** Builds Matrix "R_con_cd", which allows us to impose restrictions on the 
//   * loadings of series that represent QoQ growth rates. 
//   * It also builds the arrays needed to select the factors: for example,
//   * in a model with 3 factors and 4 lags, temp2=[0 1 2 3 12 13 14 15 24 25 26 27] 
//   * selects from the state vector the factors "F(t)" needed for the VAR 
//   * representation and temp2_=[0  12  24] selects from the state vector the 
//   * factors "f(t)" corresponding to each block
//   */
//    private void LoadingsMarianoMurasawa() {
//           // Measurements of type CD
//        R_con_cd = new Matrix(4 * (nf_), c_ * nf_);
//        q_con_cd = new Matrix(4 * (nf_), 1);
//        R_cd = new Matrix(4, c_);
//        R_cd.set(0, 0, 2);
//        R_cd.set(1, 0, 3);
//        R_cd.set(2, 0, 2);
//        R_cd.set(3, 0, 1);
//        R_cd.subMatrix(0, 4, 1, 5).diagonal().set(-1);
//        // Taking block structure into account
//        for (int count = 0, count2 = 0, count3 = 0;
//                count < nb_;
//                count2 += 4 * r[count], count3 += c_ * r[count], count++) {
//
//            R_con_cd.subMatrix(count2, count2 + 4, count3, count3 + c_).kronecker(R_cd.subMatrix(), Matrix.identity(r[count]).subMatrix());
//
//        }
//
//    }
//
//    
//  /** Builds Matrix "R_con_c", which allows us to impose restrictions on the 
//   * loadings of monthly series that represent YoY growth rates. 
//   */
//    private  void LoadingsCumSum() {
//            R_con_c = new Matrix(11 * (nf_), c_ * nf_);
//            q_con_c = new Matrix(11 * (nf_), 1);
//            R_c = new Matrix(11, c_);
//            R_c.subMatrix(0, 11, 0, 11).diagonal().set(1);
//            R_c.subMatrix(0, 11, 1, 12).diagonal().set(-1);
//            // Selection matrices temp2 and temp2_
//            temp2 = new int[nb_ * nlags];
//            temp2_ = new int[nb_];
//
//            int contador = 0;
//            // Taking block structure into account
//            for (int count = 0, count2 = 0, count3 = 0;
//                    count < nb_;
//                    count2 += 11 * r[count], count3 += c_ * r[count], count++) {
//
//                temp2_[count] = count3;
//
//                for (int nl = 0;
//                        nl < nlags;
//                        nl += 1) {
//                    temp2[contador] = count3 + nl;
//                    contador++;
//                }
//
//                R_con_c.subMatrix(count2, count2 + 11, count3, count3 + c_).kronecker(R_c.subMatrix(), Matrix.identity(r[count]).subMatrix());
//            }
//    
//        logic_temp2 = logic(temp2);
//        logic_temp2_ = logic(temp2_);
//        }
//
//      
//  /** Matrices of integers selecting factors for the three CLASSES of 
//      variables (YoY, QoQ, MoM) Each matrix selects the factors
//      that are relevant for each "type" of loading structure. Note that
//      the "type" of loading structure of each variable is independent 
//      of its CLASS
//   */    
//    private void UniqueTypes() {
//      
//        unique_logic = new HashSet<>();
//
//        for (MeasurementDescriptor mdesc : dfm.getMeasurements()) {
//            unique_logic.add(mdesc.getLoads());
//        }
//
//      // all that is probably useless     
//        // sum the elements of array "r";
//        int sum = 0;
//        for (int i : r) {
//            sum += i;
//        }
//        unique_types = new Matrix(unique_logic.size(), sum);
//
//        Iterator<MeasurementLoads> itr = unique_logic.iterator();
//        int i = 0;
//        while (itr.hasNext()) {
//            boolean[] current = itr.next().used;
//            for (int j = 0; j < current.length; j++) {
//                if (current[j] == true) {
//                    unique_types.set(i, j, 1);
//                } else {
//                    unique_types.set(i, j, 0);
//                }
//            }
//            i++;
//        }
//
//        // unique_logic is the set of  unique types of loading structure     
//        int ntypes = unique_logic.size();
//        idx_iQ = new Matrix(ntypes, nf_ * c_);
//        idx_iY = new Matrix(ntypes, nf_ * c_);
//        idx_iM = new Matrix(ntypes, nf_ * c_);
//
//        for (int block_i = 0, counter = 0;
//                block_i < nb_;
//                counter += r[block_i] * c_, block_i++) {
//           
//            for (int iQ = 0; iQ < 5 * r[block_i]; iQ++) {
//                idx_iQ.subMatrix(0, ntypes, counter + iQ, counter + iQ + 1).copy(unique_types.subMatrix(0, ntypes, block_i, block_i + 1));
//            }
//            for (int iY = 0; iY < 12 * r[block_i]; iY++) {
//                idx_iY.subMatrix(0, ntypes, counter + iY, counter + iY + 1).copy(unique_types.subMatrix(0, ntypes, block_i, block_i + 1));
//            }
//
//            for (int iM = 0; iM < 1 * r[block_i]; iM++) {
//                idx_iM.subMatrix(0, ntypes, counter + iM, counter + iM + 1).copy(unique_types.subMatrix(0, ntypes, block_i, block_i + 1));
//
//            }
//        }
//    }
//
//}
//
//
//
//          
//
//
//      
//        
//          
//
//         
//      
//         
//
//                  
//        
