package com.didihe1988.rscode.demo;

import com.didihe1988.rscode.coder.RSDecoder;
import com.didihe1988.rscode.coder.RSEncoder;

/**
 * Created by didihe1988 on 15-4-22.
 */
public class CorrectionDemo {

    public static void main(String[] args) {
    	String message="Dear Tom,I received your letter yesterday.\n I'm very glad to know that you will come to ChengDu.I'm just going to have a one-month holiday after the exam. We can spend our holidays together. We can swim and climb the hills. I am looking forward to your reply.";
    	simulateTransmission(message);
    }
    
    public static void simulateTransmission(String message){
    	//encode
    	System.out.println("String ready to translate：");
    	System.out.println(message);
    	System.out.println("--------------------------");
    	RSEncoder encoder=new RSEncoder();
    	byte[] bytes=encoder.encode(message);
        RSDecoder decoder=new RSDecoder();
        //modify several bytes to simulate errors in transmission
        byte[] receive=bytes;
        receive[0]='a';
        receive[300]='a';
        receive[301]='b';
        //decode
        String result=decoder.decode(receive);
        System.out.println("--------------------------");
        System.out.println("String after error correction： ");
        System.out.println(result);
        System.out.println("--------------------------");
    	System.out.println("Is result the same as message？： "+message.equals(result));
    }
}
