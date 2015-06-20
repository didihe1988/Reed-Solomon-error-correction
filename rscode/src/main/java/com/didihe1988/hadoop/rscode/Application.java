package com.didihe1988.hadoop.rscode;

import java.util.List;

/**
 * Created by didihe1988 on 15-4-22.
 */
public class Application {
	
	private static final String testString="Dear Tom,I received your letter yesterday.\n I'm very glad to know that you will come to ChengDu.I'm just going to have a one-month holiday after the exam. We can spend our holidays together. We can swim and climb the hills.";

    public static void main(String[] args) {
    	//System.out.println(testString.length());
        //testGF256();
        //testPolynomial();
        //testGFPolynomial();
        //testGeneratorPolynomial();
        //testEncode();
        //testIntPolynomial();
       // testPolyEmptyandEqual();
        //testPolySyndromes();
        //testGFPolynomial();
        //testPolyChienSearch();
        //testEncode();
        //testBM();
        //testZeroDegreePoly();
        //testGFOneMulOne();
    	testRSCode();
    }

    //之后用JUnit替换
    public static void testGF256() {
        GF256 num_66 = new GF256(66);
        GF256 num_67 = new GF256(67);

        //a+b
        System.out.println(num_66.add(num_67).getValue());
        //a*b
        System.out.println(num_66.mul(num_67).getValue());

        GF256 num_3 = new GF256(3);
        GF256 num_5 = new GF256(5);
        GF256 num_6 = new GF256(6);
        GF256 num_1 = new GF256(1);
        System.out.println(num_3.mul(num_1).getValue());
        System.out.println(num_5.mul(num_1).getValue());
        System.out.println(num_3.add(num_5).getValue());

        GF256 num_15 = new GF256(15);
        System.out.println(num_15.mul(num_15).getValue());

    }

    //系数属于GF(2^8)  (x+3)(x+5)=x^2+6x+15
    public static void testGFPolynomial(){
        GF256 num_1=new GF256(1);
        GF256 num_3=new GF256(3);
        GF256 num_5=new GF256(5);
        GF256[] coef1={num_3,num_1};
        GF256[] coef2={num_5,num_1};
        Polynomial p_1=new Polynomial(coef1);
        //mul
        Polynomial p_2=new Polynomial(coef2);
        Polynomial p_mul=p_1.mul(p_2);
        System.out.println(p_1.toString());
        System.out.println(p_2.toString());
        System.out.println(p_mul.toString());
        //pow
        System.out.println(num_3.pow(3).getValue());

        Polynomial zeroPoly=new Polynomial(0);
        System.out.println(zeroPoly.degree());
    }

    public static void testGeneratorPolynomial(){
        RSEncoder encoder=new RSEncoder(255,223);
        Polynomial g=encoder.getGeneratorPolynomial();
        System.out.println(g.toString());
    }


    public static void testIntPolynomial(){
    	//测试: x^3 % x^2-3x+2
        IntPolynomial intPoly=new IntPolynomial(new int[]{0,0,0,1});
        System.out.println(intPoly);
        IntPolynomial divPoly=new IntPolynomial(new int[]{2,-3,1});
        System.out.println(divPoly);
        //应当是7x-6
        IntPolynomial resPloy=intPoly.mod(divPoly);
        System.out.println(resPloy);
        int value=2;
        System.out.println(divPoly.degree());
        System.out.print(divPoly.evaluate(value));
    }

    public static void testPolyEmptyandEqual(){
        Polynomial polynomial=new Polynomial(5);
        System.out.println(polynomial.isEmpty());
        Polynomial polynomial1=new Polynomial(5);
        System.out.println(polynomial.equals(polynomial1));
        polynomial.coefficients[0]=new GF256(1);
        System.out.println(polynomial.equals(polynomial1));
    }

    public static void testEncode(){
        RSEncoder encoder=new RSEncoder(255,223);
        Polynomial poly=encoder.encode(testString);
        System.out.println(poly);
    }

    public static void testPolySyndromes(){
        Polynomial receive=new Polynomial("Hello World");
        RSDecoder decoder=new RSDecoder(255,223);
        System.out.println(decoder.calSyndromes(receive));
    }

    public static void testPolyChienSearch(){
        RSDecoder decoder=new RSDecoder(255,223);
        Polynomial sigma=new Polynomial("Hello World");
        decoder.chienSearch(sigma);
    }

    public static void testBM(){
        Polynomial receive=new Polynomial("Hello World");
        RSDecoder decoder=new RSDecoder(255,223);
        Polynomial syndromes=decoder.calSyndromes(receive);
        decoder.calBerlekampMassey(syndromes);
    }

    public static void testZeroDegreePoly(){
        GF256 num_1=new GF256(1);
        GF256 tmp1=num_1.mul(num_1.pow(-1));
        Polynomial tmpPoly=new Polynomial(0);
        tmpPoly.coefficients[0]=num_1;
        tmpPoly.coefficients[0]=tmpPoly.coefficients[0].mul(tmp1);
        System.out.println(tmpPoly.coefficients[0].getValue());
        Polynomial sigma=new Polynomial(new GF256[] {num_1});
        Polynomial rPoly=sigma.sub(tmpPoly.mul(sigma));
        System.out.println(rPoly);

    }

    public static void testGFOneMulOne(){
        GF256 num_1=new GF256(1);
        GF256 a=num_1;
        GF256 b=num_1;
        GF256 result=a.mul(b.pow(-1));
        System.out.println(result.getValue());
    }
    
    public static void testRSCode(){
    	RSEncoder encoder=new RSEncoder(255,223);
        Polynomial poly=encoder.encode(testString);
        System.out.println("encode:");
        System.out.println(poly);
        RSDecoder decoder=new RSDecoder(255, 223);
        //change poly
        //GF256[] coeffs=poly.coefficients;
        //System.out.println(poly.toValue());
        Polynomial change=new Polynomial(new GF256[]{new GF256(1)});
        System.out.println(change);
        poly=poly.add(change);
        Polynomial syndromes=decoder.calSyndromes(poly);
        System.out.println(syndromes);
        Polynomial[] results=decoder.calBerlekampMassey(syndromes);
        Polynomial sigma=results[0];
        Polynomial omega=results[1];
        List[] chienLists=decoder.chienSearch(sigma);
		List<GF256> YList=decoder.forney(omega,(List<GF256>)chienLists[0]);
		System.out.println("YList");
        for(GF256 Y:YList){
        	System.out.println(Y.getValue());
        }
        List<Integer> jList=chienLists[1];
        GF256[] errors=new GF256[255];
        //error polynomial
        for(int i=0;i<255;i++){
        	if(jList.contains(i)){
        		errors[i]=YList.get(jList.lastIndexOf(i));
        	}else{
        		errors[i]=new GF256(0);
        	}
        }
        Polynomial errorPoly=new Polynomial(errors);
        System.out.println(errorPoly);
        Polynomial codeword=poly.sub(errorPoly);
        System.out.println(codeword.toValue(223));
        //System.out.println(poly.toValue());
    }



}
