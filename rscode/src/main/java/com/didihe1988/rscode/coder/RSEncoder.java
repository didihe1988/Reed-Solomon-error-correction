package com.didihe1988.rscode.coder;

import java.nio.ByteBuffer;
import com.didihe1988.rscode.math.Polynomial;

/**
 * Created by didihe1988 on 15-4-23.
 */
public class RSEncoder extends RSCode{
	
	/**
	 * 构造函数 默认为RS(223,255)
	 */
    public RSEncoder() {
    	super();
	}
    
    /**
     * RS编码
     * @param message 信息字符串
     * @return 已编码的字节数组
     */
    public byte[] encode(String message){
    	//补足输入字符串
    	String enlargedMessage=enlargeMessage(message);
    	//计算RS编码的单元数:223字节为一个单元
    	int unitCount=enlargedMessage.length()/this.k;
    	ByteBuffer buffer=ByteBuffer.allocate(unitCount*this.n);
    	for(int i=0;i<unitCount;i++){
    		//逐个单元进行编码
    		Polynomial poly=doEncode(enlargedMessage.substring(i*this.k, (i+1)*this.k));
    		//将编码得到的码字多项式转换成字节形式，投入buffer中
    		buffer.put(poly.toBytes());
    	}
    	return buffer.array();
    }
    
    /**
     * 对一个编码单元(大小为成员变量k)执行具体的编码操作
     * @param unit 信息字符串
     * @return 码字多项式
     */
    private Polynomial doEncode(String unit){
        //生成信息多项式m(x)
        Polynomial m=new Polynomial(unit);
        //x^(2t)
        Polynomial twoT=new Polynomial(this.two_t);
        twoT.coefficients[twoT.length()-1]=ONE;
        //用x^2t乘以m(x)
        Polynomial mprime=m.mul(twoT);
        //得到余式b(x)
        Polynomial b=mprime.mod(this.g);
        //生成码字多项式
        Polynomial c=mprime.sub(b);
        return c;
    }  
    
    /**
     * 补足字符串长度，使其正好为成员变量k的整数倍
     * @param message
     * @return
     */
    public  String enlargeMessage(String message){
    	//例如message长度是400，长度应为223＊2=446，后面添加46个零
    	int rawLen=message.length();
    	int unitCount=(int)Math.ceil(rawLen/(double)this.k);
    	//得到补足之后的长度
		int len=this.k*unitCount;
		//如果原始长度不够，将补足的部分添0
        if(len!=rawLen){
        	byte[] rawBytes=message.getBytes();
    		byte[] bytes=new byte[len];
    		for(int i=0;i<rawBytes.length;i++){
    			bytes[i]=rawBytes[i];
    		}
    		for(int i=rawBytes.length;i<len;i++){
    			bytes[i]=0;
    		}
    		return new String(bytes);
        }
        //原始长度正好为成员变量k的整数倍，直接返回
        return message;
    }
}
