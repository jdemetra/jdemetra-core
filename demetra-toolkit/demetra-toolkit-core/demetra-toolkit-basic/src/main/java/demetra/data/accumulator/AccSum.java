/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.data.accumulator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public strictfp class AccSum {

    /**
     * Error-free transformation of a+b into x+y with x=fl(a+b)
     *
     * @param a
     * @param b
     * @param xy Out. Array containing x,y
     */
    public void fastTwoSum(double a, double b, double[] xy) {
        double x = a + b;
        double y = a - x;
        xy[0] = x;
        xy[1] = y + b;
    }
    
//    function [res,m] = FastAccSum(p)
//%FastAccSum   Ultimately fast and accurate summation with faithful rounding
//%
//%   res = fastaccsum(p)
//%
//%For real or complex input vector, dense or sparse, the result res is
//%sum(p_i) faithfully rounded. Input vector p must not be of type intval.
//%
//%Maximum number of nonzero elements per sum is limited to 67,108,862 in 
//%double precision, which seems sufficient for Matlab.
//%
//%Implements new algorithm in
//%  S.M. Rump: Ultimately Fast Accurate Summation, submitted for publication, 2008.
//%
//%CAUTION: !!! THIS IMPLEMENTATION SUFFERS SEVERELY FROM INTERPRETATION OVERHEAD !!!
//%!!! IT IS INCLUDED TO SHOW THE PRINCIPLES OF THE NEW METHOD !!!
//%!!! DO NOT USE FOR LARGE DIMENSIONS !!!
//%
//
//% written  08/28/08     S.M. Rump
//% modified 09/28/08     S.M. Rump  check for rounding to nearest improved
//%
//
//  res = 0;
//  m = 0;
//  if isempty(p)
//    return
//  end
//
//  % check size
//  if length(size(p))>2
//    error('fastaccsum not defined for multi-dimensional arrays.')
//  end
//  p = p(:)';                            % form row vector
//  if size(p,1)~=1
//    error('fastaccsum only for vector input')
//  end
//
//  % check interval input
//  if isa(p,'intval')
//    error('fastaccsum not defined for interval input')
//  end
//  
//  % check improper input
//  if any(isnan(p)) | any(isinf(p))
//    res = NaN;
//    return
//  end
//
//  % take care of complex input
//  if ~isreal(p)
//    [resreal,exactreal] = fastaccsum(real(p));
//    [resimag,exactimag] = fastaccsum(imag(p));
//    exact = exactreal & exactimag;
//    res = resreal + sqrt(-1)*resimag;
//    return
//  end
//
//  % input real, compute sum(p)
//  e = 1e-30;
//  if 1+e==1-e                           % fast check for rounding to nearest
//    rndold = 0;
//  else
//    rndold = getround;
//    setround(0)
//  end
//
//  if issparse(p)
//    n = nnz(p);                         % initialization
//  else
//    n = length(p);
//  end
//  
//  % initialize constants depending on precision
//  if isa(p,'single')
//    eps = 2^(-24);
//    eta = 2^(-149);
//  else
//    eps = 2^(-53);
//    eta = 2^(-1074);
//  end
//  
//  % check dimension
//  if ((2*n+4)*n+6)*eps>1
//    error('dimension too large for fastaccsum')
//  end
//  
//  % initialize constants
//  c1 = 1-n*eps;
//  c2 = 1-(3*n+1)*eps;
//  c3 = 2*eps;
//  c4 = 1-eps;
//  c5 = 2*n*(n+2)*eps;
//  c6 = 1-5*eps;
//  c7 = (1.5+4*eps)*(n*eps);
//  c8 = 2*n*eps;
//  c9 = eta/eps;
//  m = 0;
//  
//  T = sum(abs(p))/c1;                   % sum(abs(p)) <= T
//  if T<=c8                              % no rounding error
//    res = sum(p)
//    if rndold, setround(rndold), end
//    return
//  end
//  tp = 0;
//  while 1
//    m = m+1;
//    sigma0 = (2*T)/c2;
//    P = cumsum([sigma0 p]);             % [sigma_n,p] = ExtractVectorNew(sigma0,p)     
//    q = P(2:n+1)-P(1:n);
//    p = p-q;                            % extracted vector
//    tau = P(n+1)-sigma0;                % tau = sigma_n-sigma0 exact
//    t = tp;
//    tp = t + tau;                       % s = t + tau + sum(p)
//    if tp==0                            % check for zero t+tau
//      [res,M] = FastAccSum(p(p~=0));    % recursive call, zeros eliminated
//      m = m+M;
//      if rndold, setround(rndold), end
//      return
//    end
//    q = sigma0/c3;
//    u = abs(q/c4 - q);                  % u = ufp(sigma0)
//    Phi = ( c5*u ) / c6;
//    T = min( c7*sigma0 , c8*u );        % sum(abs(p)) <= T
//    if ( abs(tp)>=Phi ) | ( 4*T<= c9 )
//      tau2 = (t-tp) + tau;              % [tp,tau2] = FastTwoSum(t,tau)
//      res = tp + ( tau2 + sum(p) );     % faithful.y rounded result
//      if rndold, setround(rndold), end
//      return
//    end
//  end

//function [x,y] = Split(a)
//%SPLIT        Error-free split a=x+y into two parts.
//%
//%   [x,y] = Split(a)
//%
//%On return, x+y=a and both x and y need at most k bits in the mantissa.
//%In double precision k=26, in single precision k=12.
//%Input may be a vector or matrix as well.
//%
//%Follows T.J. Dekker: A floating-point technique for extending the available
//%  precision, Numerische Mathematik 18:224-242, 1971.
//%Requires 4 flops for scalar input.
//%
//%Reference implementation! Slow due to interpretation!
//%
//
//% written  03/03/07     S.M. Rump
//%
//
//  if isa(a,'double'), prec='double'; else prec='single'; end    
//  factor = 2^ceil(log2(2/eps(prec))/2)+1;
//
//  c = factor*a;            % factor('double')=2^27+1, factor('single')=2^12+1
//  if any(~isfinite(c(:)))
//    error('overflow in Split')
//  end
//  x = c - ( c - a );
//  y = a - x;
    
//function [x,y] = TwoProduct(a,b)
//%TWOPRODUCT   Error free transformation of a+b into x*y with x=fl(a*b)
//%
//%   [x,y] = TwoProduct(a,b)
//%
//%On return, x+y=a*b and x=fl(a*b) provided no over- or underflow occurs .
//%Input a,b may be vectors or matrices as well, in single or double precision.
//%
//%Follows G.W. Veltkamp, see T.J. Dekker: A floating-point technique for 
//%  extending the available precision, Numerische Mathematik 18:224-242, 1971.
//%Requires 17 flops for scalar input.
//%
//%Reference implementation! Slow due to interpretation!
//%
//
//% written  03/03/07     S.M. Rump
//%
//
//  x = a.*b;
//  if any(~isfinite(x(:))) 
//    error('overflow occurred in TwoProduct')
//  end
//  if isa(x,'double'), alpha=realmin('double'); else alpha=realmin('single'); end
//  if any(abs(x(:)))<alpha
//    error('underflow occurred in TwoProduct')
//  end
//  [ah,al] = Split(a);
//  [bh,bl] = Split(b);
//  y = al.*bl - ( ( ( x - ah.*bh ) - al.*bh ) - ah.*bl );
    
//function res = Dot2(x,y)
//%DOT2         Dot product 'as if' computed in 2-fold (quadruple) precision
//%
//%   res = Dot2(x,y)
//%
//%On return, res approximates x'*y with accuracy as if computed 
//%  in 2-fold precision.
//%
//%Implements algorithm Dot2 from
//%  T. Ogita, S.M. Rump, S. Oishi: Accurate Sum and Dot Product, 
//%    SIAM Journal on Scientific Computing (SISC), 26(6):1955-1988, 2005 .
//%Requires 25n flops.
//%
//%Reference implementation! Slow due to interpretation!
//%
//
//% written  03/03/07     S.M. Rump
//%
//
//  [p,s] = TwoProduct(x(1),y(1));
//  for i=2:length(x)
//    [h,r] = TwoProduct(x(i),y(i));
//    [p,q] = TwoSum(p,h);
//    s = s + ( q + r );
//  end
//  res = p + s;

//function s = DotXBLAS(x,y)
//%DOTXBLAS     Dot product 'as if' computed in 2-fold precision
//%
//%   res = DotXBLAS(x,y)
//%
//%On return, res approximates x'*y with accuracy as if computed 
//%  in 2-fold precision.
//%
//%Implements algorithm BLAS_ddot_x from
//%  X. Li, J. Demmel, D. Bailey, G. Henry, Y. Hida, J. Iskandar, 
//%    W. Kahan, S. Kang, {S.}, A. Kapur, M. Martin, B. Thompson, {B.},
//%    T. Tung, {T.}, D. Yoo: Design, Implementation and Testing of 
//%    Extended and Mixed Precision BLAS, ACM Trans. Math. Software, 
//%    2(28), p. 152-205, 2002.
//%Requires 37n flops.
//%
//%Reference implementation! Slow due to interpretation!
//%
//
//% written  03/03/07     S.M. Rump
//%
//
//  s = 0;
//  t = 0;
//  for i=1:length(x)
//    [h,r] = TwoProduct(x(i),y(i));
//    [s1,s2] = TwoSum(s,h);
//    [t1,t2] = TwoSum(t,r);
//    s2 = s2 + t1;
//    [t1,s2] = FastTwoSum(s1,s2);
//    t2 = t2 + s2;
//    [s,t] = FastTwoSum(t1,t2);
//  end
  
}
