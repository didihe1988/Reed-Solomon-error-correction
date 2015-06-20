package com.didihe1988.hadoop.rscode;

/**
 * Created by didihe1988 on 15-4-23.
 */
public class RSEncoder extends RSCode{
	
    public RSEncoder() {
    	super();
	}
    
    public RSEncoder(int n,int k){
        super(n,k);
    }
    
    public Polynomial encode(String message){
        //输入信息
        Polynomial m=new Polynomial(message);
        System.out.println(m.toValue());
        System.out.println("m(x) length: "+m.length());
        //x^(2t)
        //System.out.println(m);
        Polynomial twoT=new Polynomial(this.two_t);
        twoT.coefficients[twoT.length()-1]=ONE;
        //System.out.println(twoT);
        Polynomial mprime=m.mul(twoT);
        //System.out.println(mprime);
        //System.out.println(this.g);
        Polynomial b=mprime.mod(this.g);
        //System.out.println(b);
        Polynomial c=mprime.sub(b);
        System.out.println("c(x) length: "+c.length());
        //System.out.println(c);
        return c;
    }
    
    public Polynomial encode(byte[] bytes){
    	//生成信息多项式m(x)
    	System.out.println("m(x) length: "+bytes.length);
        Polynomial m=new Polynomial(bytes);
        //x^(2t)
        Polynomial twoT=new Polynomial(this.two_t);
        twoT.coefficients[twoT.length()-1]=new GF256(1);
        //用x^2t乘以m(x)
        Polynomial mprime=m.mul(twoT);
        //得到余式b(x)
        Polynomial b=mprime.mod(this.g);
        //生成码字多项式
        Polynomial c=mprime.sub(b);
        System.out.println("c(x) length: "+c.length());
        return c;
    }


}
