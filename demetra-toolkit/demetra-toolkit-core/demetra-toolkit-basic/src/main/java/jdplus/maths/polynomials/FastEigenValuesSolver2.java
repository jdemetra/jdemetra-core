/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.polynomials;

import demetra.maths.Complex;
import demetra.maths.Constants;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdplus.data.DataBlock;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.matrices.decomposition.EigenSystem;
import jdplus.maths.matrices.decomposition.IEigenSystem;

/**
 *
 * @author Jean Palate
 */
public class FastEigenValuesSolver2 implements RootsSolver {

    static final class Reflector implements Cloneable {

        double c, s;

        Reflector(double c, double s) {
            this.c = c;
            this.s = s;
        }

        // Do nothing
        Reflector() {
            this.c = 1;
            this.s = 0;
        }

        CanonicalMatrix asMatrix(int n, int pos) {
            CanonicalMatrix M = CanonicalMatrix.identity(n);
            fill(M.extract(pos, 2, pos, 2));
            return M;
        }

        void fill(FastMatrix m) {
            m.set(0, 0, c);
            m.set(0, 1, s);
            m.set(1, 0, s);
            m.set(1, 1, -c);
        }

        @Override
        public Reflector clone() {
            try {
                return (Reflector) super.clone();
            } catch (CloneNotSupportedException ex) {
                return null;
            }
        }
    }

    static final class Rotator implements Cloneable {

        double c, s;

        Rotator(double c, double s) {
            this.c = c;
            this.s = s;
        }

        // Do nothing
        Rotator() {
            this.c = 1;
            this.s = 0;
        }

        CanonicalMatrix asMatrix(int n, int pos) {
            CanonicalMatrix M = CanonicalMatrix.identity(n);
            fill(M.extract(pos, 2, pos, 2));
            return M;
        }

        void fill(FastMatrix m) {
            m.set(0, 0, c);
            m.set(0, 1, s);
            m.set(1, 0, -s);
            m.set(1, 1, c);
        }

        @Override
        public Rotator clone() {
            try {
                return (Rotator) super.clone();
            } catch (CloneNotSupportedException ex) {
                return null;
            }
        }
    }

    private Complex[] roots;

    @Override
    public boolean factorize(Polynomial p) {
        n = p.degree();
        roots = new Complex[n];
        switch (n) {
            case 0:
                return true;
            case 1:
                monic(p);
                return true;
            case 2:
                quadratic(p);
                return true;
            default:
//                double pn = p.get(n);
//                CanonicalMatrix M = CanonicalMatrix.square(n + 1);
//                M.subDiagonal(-1).drop(0, 1).set(1);
//                DataBlock col = M.column(n - 1).drop(0, 1);
//                col.setAY(-1 / pn, p.coefficients().drop(0, 1));
//                M.set(0, n, 1);
//                IEigenSystem es = EigenSystem.create(M, false);
//                Complex[] ev = es.getEigenValues();
//                System.arraycopy(ev, 0, roots, 0, n);
                qr(p);
                return true;
        }
    }

    @Override
    public Polynomial remainder() {
        return Polynomial.ZERO;
    }

    @Override
    public Complex[] roots() {
        return roots;
    }

    private void monic(Polynomial p) {
        double a = p.get(1), b = p.get(0);
        roots[0] = Complex.cart(-b / a);
    }

    private void quadratic(Polynomial p) {
        /* discr = p1^2-4*p2*p0 */
        double a = p.get(2), b = p.get(1), c = p.get(0), aa = 2 * a;
        double rdiscr = b * b - 4 * a * c;
        if (rdiscr < 0) {
            double z = Math.sqrt(-rdiscr);
            Complex r = Complex.cart(-b / aa, +z / aa);
            roots[0] = r;
            roots[1] = r.conj();
        } else {
            double z = Math.sqrt(rdiscr);
            roots[0] = Complex.cart((-b + z) / aa);
            roots[1] = Complex.cart((-b - z) / aa);
        }
    }

    private Rotator[] Q, C, B;
    private CanonicalMatrix A, R;

    private int n, itcnt, strt, zero;
    private int[] its;
    private double tol = Constants.getEpsilon();
    private double rrho1, irho1, rrho2, irho2;
    private double[] qcb, b1, b2;

    private final double[][] a = new double[3][2], r = new double[3][2], temp = new double[3][2];

    private void qr(Polynomial p) {
        //    polynomial has a degree larger than 2
        its = new int[n];
        dfcc(p);
        int tr = n - 2;
        int start_index = 0;
        int stop_index = n - 1;
        int zero_index = -1;
        int it_max = 30 * n;
        int it_count = 0;
        int chase_count = 0;
//  
//  ! initialize indices
//  start_index = 1
//  stop_index = N-1
//  zero_index = 0
//  it_max = 30*N
//  it_count = 0
//  chase_count = 0
//
//  ! loop for bulge chasing
//  do kk=1,it_max
//
//     ! check for completion
//     if(stop_index <= 0)then
//        !print*, "Algorithm is complete!"
//        exit
//     end if
//       
//     
//     ! check for deflation
//     call DCFD(N,start_index,stop_index,zero_index,QCB,its,it_count)
//     
//     ! if 1x1 block remove and check again 
//     if(stop_index == zero_index)then
//        ! get 2x2 block
//        call DCDB(N,stop_index,TEMP,QCB) 
//        
//        ! zero at top
//        if(stop_index == 1)then
//           ! store the eigenvalue
//           REIGS(stop_index) = TEMP(1,1)
//           REIGS(stop_index+1) = TEMP(2,2)
//           
//           ! update stop_index
//           stop_index = 0
//           
//        ! anywhere else
//        else
//           ! store the eigenvalue
//           REIGS(stop_index+1) = TEMP(2,2)
//           
//           ! update indices
//           stop_index = stop_index - 1
//           zero_index = 0
//           start_index = 1
//           
//        end if
//        
//		! if 2x2 block remove and check again
//		else if(stop_index-1 == zero_index)then
//			! get 2x2 block
//			call DCDB(N,stop_index,TEMP,QCB) 
//        
//		    ! zero at top
//		    if(stop_index == 2)then
//		       ! store the eigenvalues
//		       call DMQF(TEMP(1:2,:),REIGS(stop_index),IEIGS(stop_index),REIGS(stop_index+1),IEIGS(stop_index+1))
//		       call DCDB(N,1,TEMP,QCB) 
//		       REIGS(1) = TEMP(1,1)
//		       
//		       ! update indices
//		       stop_index = stop_index - 2
//		       zero_index = 0
//		       start_index = 1 
//		       
//		       ! otherwise
//		    else
//		       ! store the eigenvalues
//		       call DMQF(TEMP(1:2,:),REIGS(stop_index),IEIGS(stop_index),REIGS(stop_index+1),IEIGS(stop_index+1))
//		       
//		       ! update indices
//		       stop_index = stop_index - 2
//		       zero_index = 0
//		       start_index = 1 
//		       
//		    end if
//        
//		! if greater than 2x2 chase a bulge and check again
//		else
//        
//		    ! it_count
//		    it_count = it_count + 1	
//		    
//		    ! compute shifts
//		    if(kk == 1) then          
//		       call DCDB(N,stop_index,TEMP,QCB) 
//		       call DMQF(TEMP(1:2,:),re1,ie1,re2,ie2)
//		    elseif (mod(it_count,15) == 0)then
//				call dnormalpoly(1,re1)
//				call dnormalpoly(1,ie1)
//				re2 = re1
//				ie2 = -ie1
//				print*, "Random shift!"
//		    else
//		       call DCDB(N,stop_index,TEMP,QCB) 
//		       call DMQF(TEMP(1:2,:),re1,ie1,re2,ie2)
//		    end if
//
//		    ! build bulge
//		    call DCFT(N,start_index,QCB,re1,ie1,re2,ie2,B1,B2)
//	       
//			! chase bulge
//		    chase_count = chase_count + 1
//		    call DCB(N,start_index,stop_index,QCB,B1,B2,tr)
//                    tr = tr - 2
//		end if
//	end do
//
//  !print*, chase_count
//
//  if (kk>=it_max-1) then
//     if (stop_index < N-1) then
//        ! some eigenvalues have been found, but not all of them
//        ! this is a rare case
//        FLAG = N - 1 - stop_index
//        print*, "QR algorithm did not converged within 30*N&
//             & iterations, although FLAG = ", FLAG ,&
//             & "eigenvalues have been found.&
//             & This is a very rare case."
//        print*, "Try to increase it_max &
//             & or consider a bug-report to email:&
//             & thomas.mach+damvw.bugreport@gmail.com."
//        do ii=1,FLAG
//           reigs(ii) = reigs(stop_index+1+ii)
//           ieigs(ii) = ieigs(stop_index+1+ii)
//           reigs(stop_index+1+ii) = 0d0
//           ieigs(stop_index+1+ii) = 0d0
//        end do
//     end if
//     ! debugging
//
//     !print*, kk
//     !print*, it_max
//     !print*, start_index, stop_index
//     !print*, reigs
//     !print*, ieigs
//
//     !do ii=1,N-1
//     !print*, ""
//     !   print*, poly(ii)
//     !end do
//     !print*, ""
//  end if
//  
//        
    }

//subroutine DCFT(N,str,QCB,rrho1,irho1,rrho2,irho2,B1,B2)
//  
//  implicit none
//  
//  ! input variables
//  integer, intent(in) :: N, str
//  double precision, intent(in) :: QCB(6*N)
//  double precision, intent(inout) :: B1(2), B2(2)
//  double precision, intent(in) :: rrho1, irho1, rrho2, irho2
//  
//  ! compute variables
//  double precision :: scrap, COL(3), scrap2
//  double precision :: TEMP(3,2), T(3,2)
//  
    /**
     * This subroutine computes a factorization of the column companion matrix
     * for P(x), P(x) = x^N + a_N-1 x^N-1 + ... + a_1 x + a_0.
     *
     * @param P Polynomial
     */
    private void dfcc(Polynomial p) {

        Q = new Rotator[n];
        C = new Rotator[n + 1];
        B = new Rotator[n + 1];

        // Q(i): c=0, s=1
        for (int i = 0; i < n; ++i) {
            Q[i] = new Rotator(0, 1);
        }
        // build C, B (from n-1 to 0)
        // C[n]
        Rotator c = new Rotator();
        double r = givensRotation(p.get(0), -1, c);
        C[n] = c;
        B[n] = new Rotator(-c.s, c.c);
       for (int i = n; i > 0; --i) {
            c = new Rotator();
            r = givensRotation(-p.get(i), r, c);
            C[i - 1] = c;
            B[i - 1] = new Rotator(c.c, -c.s);
        }

//        // 
//        double r=givensReflection(p.get(0), 1, c);
//        C[n1]=c;
//        
//        givensReflection(tmp1 * p.get(0), -tmp1, gr);
//        qcb[jlast + 4] = tmp1 * gr[1];
//        qcb[jlast + 5] = tmp1 * gr[0];
//        for (int i = n - 1, j = jlast - 6; i > 0; --i, j -= 6) {
//            tmp1 = tmp2;
//            givensReflection(-p.get(i), tmp1, gr);
//            qcb[j + 2] = gr[0];
//            qcb[j + 3] = gr[1];
//            qcb[j + 4] = gr[0];
//            qcb[j + 5] = -gr[1];
//            tmp2 = gr[2];
//        }
    }

    /**
     * Check for deflation
     *
     * @param strt Start index of current block (included)
     */
    private void dcfd(int stp) {
        // loop for deflation
        for (int j = 6 * stp, i = 1; j >= 0; j -= 6, ++i) {
            if (Math.abs(qcb[j + 1]) < tol) {
                // set sub-diagonal to 0
                qcb[j + 1] = 0;
                qcb[j] = qcb[j] > 0 ? 1 : -1;
                zero = i;
                its[zero] = itcnt;
                itcnt = 0;
                return;
            }
        }
    }

//! D Compute First Transformation
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! This subroutine computes the first two givens rotation to initialize 
//! the bulge chase. The shifts rho1 and rho2 are expected to be both 
//! real or a complex conjugate pair.
//!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! N		problem size
//!
//! str		index for first relevant column of A
//!
//! Q,C,B		generators for A
//!
//! rrho1, rrho2	real parts of shifts
//!
//! irho1, irho2	imaginary parts of shifts
//!
//! B1, B2	generators for the first two givens transforms
//!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//subroutine DCFT(N,str,QCB,rrho1,irho1,rrho2,irho2,B1,B2)
//  
//  implicit none
//  
//  ! input variables
//  integer, intent(in) :: N, str
//  double precision, intent(in) :: QCB(6*N)
//  double precision, intent(inout) :: B1(2), B2(2)
//  double precision, intent(in) :: rrho1, irho1, rrho2, irho2
//  
//  ! compute variables
//  double precision :: scrap, COL(3), scrap2
//  double precision :: TEMP(3,2), T(3,2)
//  
//  ! compure first two columns of A
//  TEMP = 0d0
//  call DCDB(N,str,T,QCB)
//  TEMP(1:2,1:2) = T(1:2,1:2)
//  call DCDB(N,str+1,T,QCB)
//  TEMP(3,2) = T(2,1)
//
//  ! compute (A-rho1)(A-rho2)e_1
//  COL(1) = TEMP(1,1)*TEMP(1,1) + TEMP(1,2)*TEMP(2,1) + rrho1*rrho2 - irho1*irho2 - TEMP(1,1)*(rrho1+rrho2)
//  COL(2) = TEMP(2,1)*(TEMP(1,1)+TEMP(2,2)-(rrho1+rrho2))
//  COL(3) = TEMP(2,1)*TEMP(3,2)
//  
//  ! compute first two givens rotations
//  call DGR(COL(2),COL(3),B1(1),B1(2),scrap)
//  call DGR(COL(1),scrap,B2(1),B2(2),scrap2)
//
//end subroutine
    /**
     * Specialised implementation of Givens reflections
     *
     * @param a
     * @param b
     * @param gr Givens reflection corresponding to a, b (gr(a,b)=(rslt,0)
     * @return sqrt(a*a+b*b)
     */
    private double givensReflection(double a, double b, Reflector gr) {
        double absa = Math.abs(a), absb = Math.abs(b);
        if (absb < Constants.getEpsilon()) {
            gr.c = a < 0 ? 1 : -1;
            gr.s = 0;
            return absa;
        } else {
            double s, c, r;
            if (absa >= absb) {
                s = b / a;
                r = Math.sqrt(1 + s * s);
                if (a < 0) {
                    c = -1 / r;
                    s *= c;
                    r *= -a;
                } else {
                    c = 1 / r;
                    s *= c;
                    r *= a;
                }
            } else {
                c = a / b;
                r = Math.sqrt(1 + c * c);
                if (b < 0) {
                    s = -1 / r;
                    c *= s;
                    r *= -b;
                } else {
                    s = 1 / r;
                    c *= s;
                    r *= b;
                }
            }
            gr.c = c;
            gr.s = s;
            return r;
        }
    }

    private double givensRotation(double a, double b, Rotator gr) {
        double absa = Math.abs(a), absb = Math.abs(b);
        if (absb < Constants.getEpsilon()) {
            gr.c = a < 0 ? 1 : -1;
            gr.s = 0;
            return absa;
        } else {
            double s, c, r;
            if (absa >= absb) {
                s = b / a;
                r = Math.sqrt(1 + s * s);
                if (a < 0) {
                    c = -1 / r;
                    s *= c;
                    r *= -a;
                } else {
                    c = 1 / r;
                    s *= c;
                    r *= a;
                }
            } else {
                c = a / b;
                r = Math.sqrt(1 + c * c);
                if (b < 0) {
                    s = -1 / r;
                    c *= s;
                    r *= -b;
                } else {
                    s = 1 / r;
                    c *= s;
                    r *= b;
                }
            }
            gr.c = c;
            gr.s = s;
            return r;
        }
    }

    /**
     * Specialised implementation of Givens reflection. Same as dgr, without the
     * computation of r
     *
     * @param a
     * @param b
     * @param rslt Contains cos, sin
     */
    private void dgr2(double a, double b, double[] rslt) {
        double absa = Math.abs(a), absb = Math.abs(b);
        if (absb < Constants.getEpsilon()) {
            rslt[0] = a < 0 ? 1 : -1;
            rslt[1] = 0;
        } else {
            double s, c;
            if (absa >= absb) {
                s = b / a;
                double r = Math.sqrt(1 + s * s);
                if (a < 0) {
                    c = -1 / r;
                    s *= c;
                } else {
                    c = 1 / r;
                    s *= c;
                }
            } else {
                c = a / b;
                double r = Math.sqrt(1 + c * c);
                if (b < 0) {
                    s = -1 / r;
                    c *= s;
                } else {
                    s = 1 / r;
                    c *= s;
                }
            }
            rslt[0] = c;
            rslt[1] = s;
        }
    }

    /**
     * This subroutine computes A(K:K+2,K:K+1)
     *
     * @param k Desired block index
     */
    private void dcdb(int k) {
        clear(a);
        clear(r);
        if (k == 0) {
            r[0][0] = -qcb[5] / qcb[3];
            r[1][1] = -qcb[11] / qcb[9];
            r[0][1] = -(qcb[4] * qcb[10] - r[1][1] * qcb[2] * qcb[8]) / qcb[3];

            r[1][1] *= qcb[6];
            a[0][0] = qcb[0];
            a[1][0] = qcb[1];
            a[0][1] = -qcb[1];
            a[1][1] = qcb[0];
            matmul_01();

            a[0][0] = r[0][0];
            a[1][0] = r[1][0];
            a[0][1] = r[0][1];
            a[1][1] = r[1][1];

        } else {

            int ind = 6 * k;
            r[1][0] = -qcb[ind + 5] / qcb[ind + 3];
            r[0][0] = -(qcb[ind - 2] * qcb[ind + 4] - r[1][0] * qcb[ind - 4] * qcb[ind + 2]) / qcb[ind - 1];
            ind = 6 * (k + 1);
            r[2][1] = -qcb[ind + 5] / qcb[ind + 3];
            r[1][1] = -(qcb[ind - 2] * qcb[ind + 4] - r[2][1] * qcb[ind + 2]) / qcb[ind - 3];
            r[0][1] = (qcb[ind - 8] * qcb[ind - 1] * qcb[ind + 4] - qcb[ind - 10] * (qcb[ind - 4] * qcb[ind - 2] * qcb[ind + 4]
                    - qcb[ind + 2] * r[2][1]) / qcb[ind - 3]) / qcb[ind - 9];

            r[2][1] *= qcb[ind];
            ind = 6 * k;
            a[0][0] = qcb[ind];
            a[1][0] = qcb[ind + 1];
            a[0][1] = -qcb[ind + 1];
            a[1][1] = qcb[ind];
            matmul_12();

            ind = 6 * (k - 1);
            a[0][0] = qcb[ind];
            a[1][0] = qcb[ind + 1];
            a[0][1] = -qcb[ind + 1];
            a[1][1] = qcb[ind];
            matmul_01();
        }
    }

    /**
     * This subroutine chases the bulge
     */
    private void dcb(int str, int stp) {

    }

//! D Chase Bulge
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! .
//!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! N		problem size
//!
//! str		index for first relevant column of A
//!
//! stp		index for last relevant block of A
//!
//! QCB		generators for A
//!
//! WORK		workspace to compute blocks
//!
//! B2,B3		generators for the first givens rotations
//!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//subroutine DCB(N,str,stp,QCB,B1,B2,tr)
//
//	implicit none
//  
//	! input variables
//	integer, intent(in) :: N, str, stp
//	double precision, intent(inout) :: QCB(6*N), B1(2), B2(2)
//	integer, intent(in) :: tr
//	  
//	! compute variables
//	integer :: ii, jj, ind
//	double precision :: TEMP(2), B3(2), B1b(2), B2b(2)
//  
// 	! starting at the top 
//	if(str == 1)then
//		TEMP(1) = B2(1)
//		TEMP(2) = -B2(2)
//		B3(1) = B1(1)
//		B3(2) = -B1(2)
//		call DGTO2(TEMP,B3,QCB(1:2))
//		call DFGR(1,B3,QCB(7:8))
//		B3 = QCB(1:2)
//		QCB(1:2) = TEMP
//				
//	! otherwise
//	else
//		ind = 6*(str-1)
//		TEMP(1) = B2(1)
//		TEMP(2) = -QCB(ind-5)*B2(2)
//		B3(1) = B1(1)
//		B3(2) = -B1(2)
//		call DGTO2(TEMP,B3,QCB((ind+1):(ind+2)))
//		call DFGR(1,B3,QCB((ind+7):(ind+8)))
//		B3 = QCB((ind+1):(ind+2))
//		QCB((ind+1):(ind+2)) = TEMP
//	end if
//  
//	! main chasing loop
//	do ii=str,(stp-2)
//		
//		! set ind
//		ind = 6*(ii-1)
//  
//  
//                if (ii<tr-1) then
//                   ! B and C* are equal 
//                   B1b = B1
//                   B2b = B2
//                   ! through B
//                   call DGTO2(QCB((ind+11):(ind+12)),QCB((ind+17):(ind+18)),B1b)
//                   call DGTO2(QCB((ind+5):(ind+6)),QCB((ind+11):(ind+12)),B2b)
//                   QCB(ind+3)  =  QCB(ind+5)
//                   QCB(ind+4)  = -QCB(ind+6) 
//                   QCB(ind+9)  =  QCB(ind+11)
//                   QCB(ind+10) = -QCB(ind+12)
//                   QCB(ind+15) =  QCB(ind+17)
//                   QCB(ind+16) = -QCB(ind+18)
//
//                else                 
//                   ! through B
//                   call DGTO2(QCB((ind+11):(ind+12)),QCB((ind+17):(ind+18)),B1)
//                   call DGTO2(QCB((ind+5):(ind+6)),QCB((ind+11):(ind+12)),B2)
//                   
//                   ! through C
//                   call DGTO2(QCB((ind+15):(ind+16)),QCB((ind+9):(ind+10)),B1)
//                   call DGTO2(QCB((ind+9):(ind+10)),QCB((ind+3):(ind+4)),B2)
//                end if
//
//		! through Q
//		call DGTO2(QCB((ind+7):(ind+8)),QCB((ind+13):(ind+14)),B1)
//		call DGTO2(QCB((ind+1):(ind+2)),QCB((ind+7):(ind+8)),B2)
//		
//		! push B3 down
//		call DGTO2(B3,B1,B2)
//		TEMP = B2
//		B2 = B3
//		B3 = B1
//		B1 = TEMP
//	end do
//	
//	! set ind
//	ind = 6*(stp-2)
//  
//	! through B
//	call DGTO2(QCB((ind+11):(ind+12)),QCB((ind+17):(ind+18)),B1)
//	call DGTO2(QCB((ind+5):(ind+6)),QCB((ind+11):(ind+12)),B2)
//
//	! through C
//	call DGTO2(QCB((ind+15):(ind+16)),QCB((ind+9):(ind+10)),B1)
//	call DGTO2(QCB((ind+9):(ind+10)),QCB((ind+3):(ind+4)),B2)
//
//	! through Q
//	B1(2) = QCB(6*stp+1)*B1(2)
//	call DFGR(0,QCB((ind+7):(ind+8)),B1)
//	call DGTO2(QCB((ind+1):(ind+2)),QCB((ind+7):(ind+8)),B2)
//	call DFGR(0,B3,B2)
//	
//	! last bulge
//	ind = 6*(stp-1)
//	call DGTO2(QCB((ind+5):(ind+6)),QCB((ind+11):(ind+12)),B3)
//	call DGTO2(QCB((ind+9):(ind+10)),QCB((ind+3):(ind+4)),B3)
//	B3(2) = QCB(6*stp+1)*B3(2)
//	call DFGR(0,QCB((ind+1):(ind+2)),B3)
//
//end subroutine
//!!!!!!!
//! D Modified Quadratic Formula
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! This subroutine computes the eigenvalues of a 2x2 matrix using the 
//! modified quadratic formula.
//!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! BLOCK		2x2 block matrix
//!
//! re1, re2	real parts of eig1 and eig2
//!
//! ie1, rie2	imaginary parts of eig1 and eig2
//!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//subroutine DMQF(BLOCK,re1,ie1,re2,ie2)
//
//	implicit none
//
//	! input variables
//	double precision, intent(in) :: BLOCK(2,2)
//	double precision, intent(inout) :: re1, ie1, re2, ie2
//
//	! compute variables
//	double precision :: trace, detm, disc
//
//	! compute intermediate values
//	trace = BLOCK(1,1) + BLOCK(2,2)
//	detm = BLOCK(1,1)*BLOCK(2,2) - BLOCK(2,1)*BLOCK(1,2)
//	disc = trace*trace - 4d0*detm
//
//	! compute e1 and e2
//	! complex eigenvalues
//	if(disc < 0)then
//		re1 = trace/2d0
//		ie1 = sqrt(-disc)/2d0
//		re2 = re1
//		ie2 = -ie1
//	! real eigenvalues
//	else if(abs(trace+sqrt(disc)) > abs(trace-sqrt(disc)))then
//		if(abs(trace+sqrt(disc)) == 0)then
//			re1 = 0d0
//			ie1 = 0d0
//			re2 = 0d0
//			ie2 = 0d0
//		else
//			re1 = (trace+sqrt(disc))/2d0
//			ie1 = 0d0
//			re2 = detm/re1
//			ie2 = 0d0
//		end if
//	else
//		if(abs(trace-sqrt(disc)) == 0)then
//			re1 = 0d0
//			ie1 = 0d0
//			re2 = 0d0
//			ie2 = 0d0
//		else
//			re1 = (trace-sqrt(disc))/2d0
//			ie1 = 0d0
//			re2 = detm/re1
//			ie2 = 0d0
//		end if
//	end if
//
//end subroutine
//! D Givens Turn Over
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! This subroutine passes the givens rotation Q3 from the left down 
//! through Q1 and Q2.
//!
//! It overwrites Q1, Q2 and Q3.
//!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! Q1, Q2, Q3	generators for two givens rotations
//!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//subroutine DGTO2(Q1,Q2,Q3)
//
//	implicit none
//  
//	! input variables
//	double precision, intent(inout) :: Q1(2), Q2(2), Q3(2)
//
//	! compute variables
//	double precision :: tol, nrm, T(3), dnrm2
//	double precision :: a, b 
//	double precision :: c1, s1
//	double precision :: c2, s2
//	double precision :: c3, s3
//	double precision :: c4, s4
//	double precision :: c5, s5
//	double precision :: c6, s6
//	
//	! set tol
//	tol = epsilon(1d0)
//
//	! set local variables
//	c1 = Q1(1)
//	s1 = Q1(2)
//	c2 = Q2(1)
//	s2 = Q2(2)
//	c3 = Q3(1)
//	s3 = Q3(2)
//	
//	! initialize c4 and s4
//	a = s1*c3 + c1*c2*s3
//	b = s2*s3
//        !nrm = a*a + b*b
//        !if (abs(nrm-1d0)<tol) then
//        !   c4 = a
//        !   s4 = b
//        !   nrm = 1d0
//        !else
//        call rot1(a,b,c4,s4,nrm)
//        !end if
//	
//	! initialize c5 and s5
//	a = c1*c3 - s1*c2*s3
//	b = nrm
//        !nrm = a*a + b*b
//        !if (abs(nrm-1d0)<tol) then
//        !   c5 = a
//        !   s5 = b
//        !else
//        call rot2(a,b,c5,s5)
//        !end if
//	
//	! second column
//        ! T(1) = -c1*s3 - s1*c2*c3
//        ! T(2) = -s1*s3 + c1*c2*c3
//        ! T(3) = s2*c3
//
//	! update second column
//        ! b = -T(2)*s4 + T(3)*c4
//        ! T(2) = T(2)*c4 + T(3)*s4
//        ! a = -T(1)*s5 + T(2)*c5
//
//	! third column
//	T(1) = s1*s2
//	T(2) = -c1*s2
//	T(3) = c2
//
//	! update third column
//	a = -T(2)*s4 + T(3)*c4
//	T(2) = T(2)*c4 + T(3)*s4
//	b = -(-T(1)*s5 + T(2)*c5)
//	
//	! initialize c6 and s6
//        !nrm = a*a + b*b
//        !if (abs(nrm-1d0)<tol) then
//        !   c6 = a
//        !   s6 = b
//        !else
//        call rot2(a,b,c6,s6)	
//        !end if
//
//	! set output
//	Q1(1) = c5
//	Q1(2) = s5
//	Q2(1) = c6
//	Q2(2) = s6
//	Q3(1) = c4
//	Q3(2) = s4
//	
//     
//end subroutine DGTO2
//
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//! D Givens Rotation
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! This subroutine computes a givens rotation G1 zeroing b
//!
//!   [ c -s ] [ a ] = [ r ]
//!   [ s  c ] [ b ] = [ 0 ]
//! 
//!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! in
//! a    scalar  
//! b    scalar
//!
//! out
//! c    cosine of rotation
//! s    sine of rotation
//! r    norm of [ a; b ]
//! 
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//subroutine rot1(a,b,c,s,r)
//
//  implicit none
//  
//  ! input variables
//  double precision, intent(in) :: a,b
//  double precision, intent(inout) :: c,s,r
//  
//	if (b == 0 .AND. a < 0) then
//		c = -1d0
//        s = 0d0
//        r = -a
//        
//	else if (b == 0) then
//        c = 1d0
//        s = 0d0
//        r = a
//        
//	else if (abs(a) >= abs(b)) then
//
//		s = b/a
//		r = sqrt(1.d0 + s*s)
//
//		if (a<0) then
//			c= -1.d0/r
//			s= s*c
//			r=-a*r
//		else
//			c = 1.d0/r
//			s = s*c
//			r = a*r
//		end if
//
//	else
//
//		c = a/b;
//		r = sqrt(1.d0 + c*c)
//
//		if (b<0) then
//			s =-1.d0/r
//			c = c*s
//			r =-b*r
//		else
//			s = 1.d0/r
//			c = c*s
//			r = b*r
//		end if
//
//  end if
//
//end subroutine rot1
//
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//! D Givens Rotation
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! This subroutine computes a givens rotation G1 zeroing b
//!
//!   [ c -s ] [ a ] = [ r ]
//!   [ s  c ] [ b ] = [ 0 ]
//! 
//!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!
//! in
//! a    scalar  
//! b    scalar
//!
//! out
//! c    cosine of rotation
//! s    sine of rotation
//! 
//! Remark: Faster than rot1 since r is not computed.
//! 
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//subroutine rot2(a,b,c,s)
//
//	implicit none
//  
//	! input variables
//	double precision, intent(in) :: a,b
//	double precision, intent(inout) :: c,s
//  
//	! compute variables
//	double precision :: r
//  
//	if (b == 0 .AND. a < 0) then
//		c = -1d0
//        s = 0d0
//        
//	else if (b == 0) then
//        c = 1d0
//        s = 0d0
//        
//	else if (abs(a) >= abs(b)) then
//
//		s = b/a
//		r = sqrt(1.d0 + s*s)
//
//		if (a<0) then
//			c= -1.d0/r
//			s= s*c
//		else
//			c = 1.d0/r
//			s = s*c
//		end if
//
//	else
//
//		c = a/b;
//		r = sqrt(1.d0 + c*c)
//
//		if (b<0) then
//			s =-1.d0/r
//			c = c*s
//		else
//			s = 1.d0/r
//			c = c*s
//		end if
//
//  end if
//
//end subroutine rot2
    /**
     * Computes R[1:2,:]=A[1:2,[1:2]*R[1:2,:]
     */
    private void matmul_01() {
        double r00 = r[0][0], r10 = r[1][0], r01 = r[0][1], r11 = r[1][1];
        double a00 = a[0][0], a10 = a[1][0], a01 = a[0][1], a11 = a[1][1];
        r[0][0] = a00 * r00 + a01 * r10;
        r[1][0] = a10 * r00 + a11 * r10;
        r[0][1] = a00 * r01 + a01 * r11;
        r[1][1] = a10 * r01 + a11 * r11;
    }

    /**
     * Computes R[2:3,:]=A[1:2,[1:2]*R[2:3,:]
     */
    private void matmul_12() {
        double r10 = r[1][0], r20 = r[2][0], r11 = r[1][1], r21 = r[2][1];
        double a00 = a[0][0], a10 = a[1][0], a01 = a[0][1], a11 = a[1][1];
        r[1][0] = a00 * r10 + a01 * r20;
        r[2][0] = a10 * r10 + a11 * r20;
        r[1][1] = a00 * r11 + a01 * r21;
        r[2][1] = a10 * r11 + a11 * r21;
    }

    private void clear(double[][] x) {
        for (int i = 0; i < 3; ++i) {
            x[i][0] = 0;
            x[i][1] = 0;
        }
    }

}
