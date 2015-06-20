package com.didihe1988.hadoop.rscode;

/**
 * Created by didihe1988 on 15-4-24.
 */
//为了测试取模运算
public class IntPolynomial {
    public int[] coefficients;


    //8x^2+3x^3+9x^5  --> [0,0,8,3,0,9]
    IntPolynomial(int[] coefficients){
        this.coefficients=coefficients;
    }

    //degree=5 [0,0,0,0,0,0]
    IntPolynomial(int degree){
        this.coefficients=new int[degree+1];
        for(int i=0;i<degree+1;i++){
            this.coefficients[i]=0;
        }
    }

    public int length(){
        return this.coefficients.length;
    }

    //有一位是常数
    public int degree(){
        int degree=length()-1;
        for(int i=length()-1;i>=0;i--){
            if(this.coefficients[i]==0){
                degree--;
            }
            else{
                break;
            }
        }
        return degree;
    }

    public IntPolynomial add(IntPolynomial b){
        IntPolynomial a=this;
        IntPolynomial c=new IntPolynomial(Math.max(a.degree(),b.degree()));
        for(int i=0;i<=a.degree();i++){
            c.coefficients[i]=c.coefficients[i]+a.coefficients[i];
        }

        for(int i=0;i<=b.degree();i++){
            c.coefficients[i]=c.coefficients[i]+b.coefficients[i];
        }
        return c;
    }

    public IntPolynomial sub(IntPolynomial b){
        IntPolynomial a=this;
        IntPolynomial c=new IntPolynomial(Math.max(a.degree(),b.degree()));
        for(int i=0;i<=a.degree();i++){
            c.coefficients[i]=c.coefficients[i]+a.coefficients[i];
        }

        for(int i=0;i<=b.degree();i++){
            c.coefficients[i]=c.coefficients[i]-b.coefficients[i];
        }
        return c;
    }

    public IntPolynomial mul(IntPolynomial b){
        IntPolynomial a=this;
        IntPolynomial c=new IntPolynomial(a.degree()+b.degree());
        for(int i=0;i<=a.degree();i++){
            for(int j=0;j<=b.degree();j++){
                c.coefficients[i+j]=c.coefficients[i+j]+( a.coefficients[i]*b.coefficients[j] );
            }
        }
        return c;
    }

    /*
      while(remain的阶大于b的阶){
            算出两个多项式阶的差differ
            sub= b * x^differ * remain最高项的系数
            remain
      }
     */
    //(3x^3+5x^2+1)%(x^2+1) --> 3*x*(x^2+1)  --> 3*[0,1,0,1] ([1,0,1]-->[0,1,0,1])
    public IntPolynomial mod(IntPolynomial b){
        IntPolynomial remain=this;
        while(remain.degree()>=b.degree()){
            int differ=remain.degree()-b.degree();
            int[] curCoefs=new int[b.coefficients.length+differ];
            //shift
            for(int i=0;i<differ;i++){
                curCoefs[i]=0;
            }
            for(int i=differ,j=0;i<curCoefs.length;i++,j++){
                curCoefs[i]=b.coefficients[j];
            }
            //mul
            for(int i=0;i<curCoefs.length;i++){
                curCoefs[i]=curCoefs[i]*(remain.coefficients[remain.coefficients.length-1]);
            }
            IntPolynomial subPoly=new IntPolynomial(curCoefs);
            remain =remain.sub(subPoly);
        }
        return remain;
    }

    public int evaluate(int value){
        int result=0;
        //例如3x^2+x+1 -->先将3加入，可以乘以x两次，达到了效果
        for(int i=degree();i>=0;i--){
            result=coefficients[i]+value*result;
        }
        return result;
    }
    
    @Override
    public String toString() {
        if (degree() ==  0) return "" + coefficients[0];
        if (degree() ==  1) return coefficients[1] + "x + " + coefficients[0];
        String s = coefficients[degree()] + "x^" + degree();
        for (int i = degree()-1; i >= 0; i--) {
            if      (coefficients[i] == 0) continue;
            else if (coefficients[i]  > 0) s = s + " + " + ( coefficients[i]);
            else if (coefficients[i]  < 0) s = s + " - " + (-coefficients[i]);
            if      (i == 1) s = s + "x";
            else if (i >  1) s = s + "x^" + i;
        }
        return s;
    }
}
