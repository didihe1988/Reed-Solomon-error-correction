package com.didihe1988.rscode.coder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.didihe1988.rscode.math.GF256;
import com.didihe1988.rscode.math.Polynomial;

/**
 * Created by didihe1988 on 15-4-25.
 */
public class RSDecoder extends RSCode {

	/**
	 * 构造函数 默认为RS(223,255)
	 */
	public RSDecoder() {
		super();
	}
    
	/**
	 * RS译码
	 * @param receive 收到的数据
	 * @return 译码之后的字符串
	 */
	public String decode(byte[] receive){
		//计算译码单元数
		int unitCount=receive.length/this.n;
		//buffer用于存放已译码的字符串
		StringBuffer buffer=new StringBuffer();
		//为了更好操纵byte，将byte[]封装成ByteBuffer bytes
		ByteBuffer bytes=ByteBuffer.wrap(receive);
		byte[] unitBytes=new byte[this.n];
		for(int i=0;i<unitCount;i++){
			//将要译码的n个字节取到unitBytes中
			bytes.get(unitBytes);
			//将译码得到的字符串投入buffer
			buffer.append(doDecode(unitBytes));
		}
		return buffer.toString().trim();
	}
	
	/**
	 * 对一个译码单元(大小为成员变量m)执行具体的译码操作
	 * @param receive 收到的数据
	 * @return 译码之后的字符串
	 */
	public String doDecode(byte[] receive) {
		Polynomial receivePoly = new Polynomial(receive);
		// 计算伴随式
		Polynomial syndromes = calSyndromes(receivePoly);
		System.out.println("伴随式: "+syndromes);
		// BM算法
		Polynomial[] bmPolys = calBerlekampMassey(syndromes);
		Polynomial sigma = bmPolys[0];
		Polynomial omega = bmPolys[1];
		// 钱搜索
		List[] chienLists = chienSearch(sigma);
		// forney算法
		List<GF256> YList = forney(omega, (List<GF256>) chienLists[0]);
		List<Integer> jList = chienLists[1];
		GF256[] errors = new GF256[255];
		// 错误多项式
		for (int i = 0; i < 255; i++) {
			if (jList.contains(i)) {
				errors[i] = YList.get(jList.lastIndexOf(i));
			} else {
				errors[i] = ZERO;
			}
		}
		Polynomial errorPoly = new Polynomial(errors);
		System.out.println("纠错多项式: "+errorPoly);
		Polynomial codeword = receivePoly.sub(errorPoly);
		return codeword.toValue(223);
	}

	/**
	 * 因为码字c=信息多项是m*生成多项式g(x) 因此通过码字能不能除尽g(x)来判断是否需要纠错 
	 */
	public boolean verify(byte[] receive) {
		Polynomial receivePoly = new Polynomial(receive);
		return (receivePoly.mod(this.g)).isEmpty();
	}

	/**
	 * 计算伴随式
	 * @param receive 收到的多项式
	 * @return 伴随式多项式 
	 */
	public Polynomial calSyndromes(Polynomial receive) {
		Polynomial syndromes = new Polynomial(this.two_t);
		syndromes.coefficients[0] = new GF256(0);
		for (int i = 1; i <= two_t; i++) {
			syndromes.coefficients[i] = receive.evaluate(this.ALPHA.pow(i));
		}
		return syndromes;
	}
	
	/**
	 * BM算法
	 * @param s 伴随式多项式
	 * @return sigma错误位置多项式和omega错误值多项式组成的多项式数组
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

			if (delta.equals(ZERO)) {
				sigmas[j + 2] = sigmas[j + 1];
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
				D[j + 2] = sigmas[j + 2].degree();
				JSubD[j + 2] = j + 1 - D[j + 2];
			}
		}
		System.out.println("错误位置多项式sigma: "+sigmas[this.two_t + 1]);
		System.out.println("错误值多项式omega: "+omegas[this.two_t + 1]);
		Polynomial[] results = new Polynomial[2];
		results[0] = sigmas[this.two_t + 1];
		results[1] = omegas[this.two_t + 1];
		return results;
	}

	/**
	 * 钱搜索 把"求根"换成"验根"，也就是把alpha^0-alpha^(n-1)逐一代入sigma检验
	 * @param sigma 错误位置多项式,sigma的根是差错位置数的倒数
	 * @return 错误位置
	 */
	public List[] chienSearch(Polynomial sigma) {
		List[] results = new List[2];
		List<GF256> gfList = new ArrayList<GF256>();
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i <= this.n; i++) {
			// 在alpha到alpha^255中找到sigma的根
			if (sigma.evaluate(ALPHA.pow(i)).equals(ZERO)) {
				// sigma根的倒数即错误位置数
				gfList.add(ALPHA.pow(-i));
				list.add(this.n - i);
			}
		}
		results[0] = gfList;
		results[1] = list;
		return results;
	}

	/**
	 * forney算法
	 * @param omega 错误值多项式
	 * @param XList 对应的错误位置
	 * @return 错误多项式的系数
	 */
	public List<GF256> forney(Polynomial omega, List<GF256> XList) {
		int t = this.two_t / 2;
		List<GF256> YList = new ArrayList<GF256>();
		for (int i = 0; i < XList.size(); i++) {
			GF256 X = XList.get(i);
			// 计算Y的分子和分母的(1/X)
			GF256 Y = X.pow(t);
			Y = Y.mul(omega.evaluate(X.inverse()));
			Y = Y.mul(X.inverse());
			// 计算分母的求和式
			GF256 prod = new GF256(1);
			for (int j = 0; j < t; j++) {
				GF256 Xj = this.ZERO;
				if (i == j) {
					continue;
				}
				// 0到t-1有可能超过XList的范围
				if (j < XList.size()) {
					Xj = XList.get(j);
				}
				prod = prod.mul(X.sub(Xj));
			}
			Y = Y.mul(prod.inverse());
			YList.add(Y);
		}
		return YList;
	}

}
