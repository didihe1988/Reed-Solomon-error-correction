package com.didihe1988.hadoop.rscode;

/**
 * Created by didihe1988 on 15-4-25.
 */
public class RSCode {
    protected int n;
    protected int k;
    protected int two_t;
    protected Polynomial g;

    //GF(2^8)的一个本原元
    protected final static GF256 ALPHA=new GF256(3);
    protected final static GF256 ZERO=new GF256(0);
    protected final static GF256 ONE=new GF256(1);
    
    RSCode() {
		// TODO Auto-generated constructor stub
    	this(255, 223);
	}

    RSCode(int n,int k){
        this.n=n;
        this.k=k;
        this.two_t=n-k;
        calGeneratorPolynomial();
    }

    public Polynomial getGeneratorPolynomial() {
        return g;
    }

    public int getK() {
        return k;
    }

    public int getN() {
        return n;
    }

    public int getTwoT(){
        return this.two_t;
    }

    //计算生成矩阵
    private void calGeneratorPolynomial(){
        GF256 num_1=new GF256(1);
        this.g=new Polynomial(new GF256[]{num_1});
        for(int i=1;i<=this.two_t;i++){
            Polynomial p=new Polynomial( new GF256[]{ALPHA.pow(i),num_1} );
            //System.out.println(p);
            this.g=this.g.mul(p);
            //System.out.println(g);
        }
    }
}
