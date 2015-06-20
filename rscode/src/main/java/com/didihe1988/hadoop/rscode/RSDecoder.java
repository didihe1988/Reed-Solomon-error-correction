package com.didihe1988.hadoop.rscode;

import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.standard.RequestingUserName;

/**
 * Created by didihe1988 on 15-4-25.
 */
public class RSDecoder extends RSCode {
	
	public RSDecoder(){
		super();
	}

	public RSDecoder(int n, int k) {
		super(n, k);
	}

	public void decode(String receive) {
		Polynomial poly = new Polynomial(receive);
		// verify

		Polynomial syndromes = calSyndromes(poly);
		calBerlekampMassey(syndromes);
	}

	public boolean verify(String receive) {
		Polynomial c = new Polynomial(receive);
		// 因为码字x=m*g(x) 因此通过码字能不能除尽g(x)来判断
		return (c.mod(this.g)).isEmpty();
	}

	public Polynomial calSyndromes(Polynomial receive) {
		Polynomial syndromes = new Polynomial(this.two_t);
		syndromes.coefficients[0] = new GF256(0);
		for (int i = 1; i <= two_t; i++) {
			syndromes.coefficients[i] = receive.evaluate(this.ALPHA.pow(i));
		}
		return syndromes;
	}

	// s为伴随式
	/*
	 * public void calBerlekampMassey(Polynomial s){ //设置迭代初值 GF256 num_1=new
	 * GF256(1); GF256[] coefs={num_1}; Polynomial sigma=new Polynomial(coefs);
	 * Polynomial omega=new Polynomial(coefs); int[] D=new int[this.two_t+1];
	 * D[0]=0; GF256 delta=s.coefficients[0]; GF256[] deltas=new
	 * GF256[this.two_t+1]; deltas[0]=delta; //填表 for(int j=0;j<this.two_t;j++){
	 * //计算修正项 Delta(j) GF256 tmp=new GF256(0); for(int
	 * i=0;i<=sigma.degree();i++){
	 * tmp.add(sigma.coefficients[i].mul(s.coefficients[j-i+1])); }
	 * delta=s.coefficients[j+1].add(tmp); System.out.println(delta.getValue());
	 * //如果Delta(j)==0 if(delta.equals(ZERO)){ D[j+1]=D[j]; } else{
	 * 
	 * 
	 * } int a=0; } }
	 */

	public Polynomial[] calBerlekampMassey(Polynomial s) {
		GF256[] syndromes = s.coefficients;
		Polynomial[] sigmas = new Polynomial[this.two_t + 2];
		Polynomial[] omegas = new Polynomial[this.two_t + 2];
		int[] D = new int[this.two_t + 2];
		int[] JSubD = new int[this.two_t + 2];
		GF256[] deltas = new GF256[this.two_t + 2];

		// 迭代前初始化
		sigmas[0] = new Polynomial(new GF256[] { ONE });
		sigmas[1] = new Polynomial(new GF256[] { ONE });
		omegas[0] = new Polynomial(new GF256[] { ZERO });
		omegas[1] = new Polynomial(new GF256[] { ONE });
		D[0] = D[1] = 0;
		JSubD[0] = -1;
		JSubD[1] = 0;
		deltas[0] = ONE;
		deltas[1] = syndromes[1];

		// 迭代
		// j应该从0开始，因为sigma[1]/D[1]未知
		// syndromes:伴随式 syndromes[n]:x^n项的系数
		// deltas[0]:delta_-1 deltas[1]:delta_0 deltas[2]:delta_1 依次类推
		// sigmas[0]:sigma_-1 sigmas[1]:sigma_0 sigmas[2]:sigma_1 依次类推
		for (int j = 0; j < this.two_t; j++) {
			// 计算修正项 Delta_j
			int degree = sigmas[j + 1].degree();
			GF256 mid_result = ZERO;
			for (int i = 1; i <= degree; i++) {
				mid_result = mid_result.add(syndromes[j + 1 - i]
						.mul(sigmas[j + 1].coefficients[i]));
			}
			GF256 delta = syndromes[j + 1].add(mid_result);
			deltas[j + 1] = delta;
			//System.out.println(delta);

			if (delta.equals(ZERO)) {
				sigmas[j + 2] = sigmas[j + 1];
				//System.out.println(sigmas[j + 2]);
				omegas[j + 2] = omegas[j + 1];
				D[j + 2] = D[j + 1];
				JSubD[j + 2] = JSubD[j + 1] + 1;
			} else {
				// 这里已经确保delta[j]!=0
				// JSubD[0]:-1-D(-1) JSubD[1]:0-D(0) JSubD[2]:1-D(1)依次类推
				int max = j;
				for (int i = j; i >= 0; i--) {
					if ((JSubD[i] > JSubD[max]) && (!deltas[i].equals(ZERO))) {
						max = i;
					}
				}

				GF256 tmp_test = deltas[j + 1].mul(deltas[max].pow(-1));
				Polynomial testPoly = new Polynomial(1);
				testPoly.coefficients[1] = tmp_test;
				sigmas[j + 2] = sigmas[j + 1].sub(testPoly.mul(sigmas[max]));
				omegas[j + 2] = omegas[j + 1].sub(testPoly.mul(omegas[max]));
				//System.out.println(sigmas[j + 2]);
				D[j + 2] = sigmas[j + 2].degree();
				JSubD[j + 2] = j + 1 - D[j + 2];
			}
		}
		System.out.println("Sigma: ");
		System.out.println(sigmas[this.two_t+1]);
		System.out.println("omega: ");
		System.out.println(omegas[this.two_t+1]);
		Polynomial[] results=new Polynomial[2];
		results[0]=sigmas[this.two_t+1];
		results[1]=omegas[this.two_t+1];
		return results;
	}

	// 把"求根"换成"验根"，也就是把alpha^0-alpha^(n-1)逐一代入sigma检验
	// 差错位置多项式sigma的根是差错位置数的倒数
	public List[] chienSearch(Polynomial sigma) {
		List[] results=new List[2];
		List<GF256> gfList = new ArrayList<GF256>();
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i <= this.n; i++) {
			// 在alpha到alpha^255中找到sigma的根
			if (sigma.evaluate(ALPHA.pow(i)).equals(ZERO)) {
				// sigma根的倒数即差错位置数
				gfList.add(ALPHA.pow(-i));
				list.add(this.n - i);
			}
		}
		for (GF256 element : gfList) {
			System.out.print(element.getValue());
		}
		System.out.println("");
		System.out.println(list);
		results[0]=gfList;
		results[1]=list;
		return results;
	}
	
	public List<GF256> forney(Polynomial omega,List<GF256> XList){
		int t=this.two_t/2;
		List<GF256> YList=new ArrayList<GF256>();
		for(int i=0;i<XList.size();i++){
			GF256 X=XList.get(i);
			//计算Y的分子和分母的(1/X)
			GF256 Y=X.pow(t);
			Y=Y.mul(omega.evaluate(X.inverse()));
			Y=Y.mul(X.inverse());
			//计算分母的求和式 
			GF256 prod=new GF256(1);
			for(int j=0;j<t;j++){
				GF256 Xj=this.ZERO;
				if(i==j){
					continue;
				}
				//0到t-1有可能超过XList的范围
				if(j<XList.size()){
					Xj=XList.get(j);
				}
				prod=prod.mul(X.sub(Xj));
			}
			Y=Y.mul(prod.inverse());
			YList.add(Y);
		}
		return YList;
	}

}
