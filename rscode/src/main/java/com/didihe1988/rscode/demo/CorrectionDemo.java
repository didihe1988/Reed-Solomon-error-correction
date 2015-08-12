package com.didihe1988.rscode.demo;

import com.didihe1988.rscode.coder.RSDecoder;
import com.didihe1988.rscode.coder.RSEncoder;

/**
 * Created by didihe1988 on 15-4-22.
 */
public class CorrectionDemo {
	
	private static final String string="Dear Tom,I received your letter yesterday.\n I'm very glad to know that you will come to ChengDu.I'm just going to have a one-month holiday after the exam. We can spend our holidays together. We can swim and climb the hills. I am looking forward to your reply.";

    public static void main(String[] args) {
    	//编码
    	System.out.println("准备传输的字符串： "+string);
    	RSEncoder encoder=new RSEncoder();
    	byte[] bytes=encoder.encode(string);
        RSDecoder decoder=new RSDecoder();
        //修改receive中两个字节的内容，来模拟网络传输或是磁盘存储过程中错误的发生
        byte[] receive=bytes;
        receive[0]='a';
        receive[300]='a';
        receive[301]='b';
        //解码
        String result=decoder.decode(receive);
        System.out.println("纠错后的字符串： "+result);
    	System.out.println("两个字符串是否相同： "+string.equals(result));
    }
}
