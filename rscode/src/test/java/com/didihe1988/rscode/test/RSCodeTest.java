package com.didihe1988.rscode.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.didihe1988.rscode.coder.RSDecoder;
import com.didihe1988.rscode.coder.RSEncoder;
import com.didihe1988.rscode.math.GF256;
import com.didihe1988.rscode.math.IntPolynomial;
import com.didihe1988.rscode.math.Polynomial;

/**
 * Created by didihe1988 on 15-4-27.
 */
public class RSCodeTest {
	//共223个字符 encode之后应当是255个字符  decode之后还原成223个字符
	private static final String testString="Dear Tom,I received your letter yesterday.\n I'm very glad to know that you will come to ChengDu.I'm just going to have a one-month holiday after the exam. We can spend our holidays together. We can swim and climb the hills.";
	
	
	/**
	 * @description 测试int系数的多项式 因为GF256系数比较难判断，所以先用int型系数测试多项式
	 */
	@Test
	public void testIntPolynomial(){
	    	//测试: x^3 % x^2-3x+2 商:x+3 余数:0x^2+7x-6
	        IntPolynomial intPoly=new IntPolynomial(new int[]{0,0,0,1});
	        IntPolynomial divPoly=new IntPolynomial(new int[]{2,-3,1});
	        //应当是0x^2+7x-6
	        IntPolynomial resPloy=intPoly.mod(divPoly);
	        int[] res=resPloy.coefficients;
	        assertArrayEquals(new int[]{-6,7,0},res);
	}
	
	/**
	 * @description 测试GF256的基本运算
	 */
	@Test
	public void testGF256() {
        //3*1 5*1 3*5 3+5
        GF256 num_3 = new GF256(3);
        GF256 num_5 = new GF256(5);
        GF256 num_1 = new GF256(1);
        assertEquals(new GF256(3), num_3.mul(num_1));
        assertEquals(new GF256(5), num_5.mul(num_1));
        assertEquals(new GF256(15), num_3.mul(num_5));
        assertEquals(new GF256(6), num_3.add(num_5));
        //15*15 平方
        GF256 num_15 = new GF256(15);
        assertEquals(new GF256(85),num_15.pow(2));
    }
	
	
	/**
	 * @description 测试多项式为空(系数均为0)和相等
	 */
	@Test
	public void testPolyEmptyandEqual(){
        Polynomial polynomial=new Polynomial(5);
        assertEquals(true, polynomial.isEmpty());
        GF256[] coefs={new GF256(0),new GF256(0),new GF256(0)};
        Polynomial emptyPoly=new Polynomial(coefs);
        assertEquals(true, emptyPoly.isEmpty());
        Polynomial polynomial1=new Polynomial(5);
        assertEquals(true, polynomial.equals(polynomial1));
        assertEquals(false,polynomial.equals(emptyPoly));
    }
	
	/**
	 * @description 系数属于GF256  测试多项式(x+3)(x+5)=x^2+6x+15成立
	 */
	@Test
    public void testGFPolynomial(){
        GF256 num_1=new GF256(1);
        GF256 num_3=new GF256(3);
        GF256 num_5=new GF256(5);
        GF256[] coef1={num_3,num_1};
        GF256[] coef2={num_5,num_1};
        Polynomial poly1=new Polynomial(coef1);
        //mul
        Polynomial poly2=new Polynomial(coef2);
        Polynomial polyMul=poly1.mul(poly2);
        //将乘法得到的矩阵的系数由GF256数组转换成int数组
        GF256[] coefs=polyMul.coefficients;
        int[] mulCoefs=new int[coefs.length];
        for(int i=0;i<coefs.length;i++){
        	mulCoefs[i]=coefs[i].getValue();
        }
        //进行比较
        assertArrayEquals(new int[]{15,6,1},mulCoefs);
    }
	
	/**
	 * @description 测试多项式阶数(degree)
	 */
	@Test
	public void testPolyDegree(){
		//0
		Polynomial zeroPoly=new Polynomial(0);
		assertEquals(0, zeroPoly.degree());
		//只有常数
		Polynomial constPoly=new Polynomial(new GF256[]{new GF256(1)});
		assertEquals(0, constPoly.degree());
		//1阶
		Polynomial onePoly=new Polynomial(new byte[]{'a','b'});
		assertEquals(1, onePoly.degree());
		//2阶
		Polynomial twoPoly=new Polynomial(new byte[]{'a','b','c'});
		assertEquals(2, twoPoly.degree());
	}
	
	/**
	 * @description 测试RS(223,255)当alpha=3时的生成多项式 
	 */
	@Test
    public void testGeneratorPolynomial(){
        RSEncoder encoder=new RSEncoder();
        Polynomial g=encoder.getGeneratorPolynomial();
        GF256[] coefs=g.coefficients;
        assertEquals(56,coefs[0].getValue());
        assertEquals(180,coefs[1].getValue());
        assertEquals(150,coefs[31].getValue());
        assertEquals(1,coefs[32].getValue());
    }
}
