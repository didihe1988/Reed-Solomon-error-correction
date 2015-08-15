# Reed-Solomon-error-correction
Reed–Solomon error correction in java, providing RS(255,223) encode and decode method.

#Usage
encode the string before net transmission or local storage:
  

    RSEncoder encoder=new RSEncoder();
    byte[] bytes=encoder.encode(string);

decode while receiving from network or reading from the disk

    String result=decoder.decode(receive);
#Example

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
    
    ![](https://github.com/didihe1988/Reed-Solomon-error-correction/raw/master/rscode/screenshot/demo.png)

#Document

 - **Class RSEncoder**

**byte[] encode(String message)**
Encode the given string with RS(255,223).Return encoded byte array.
First，invoke *enlargeMessage()* to make sure message string can be divided into several encoding unit.Then,for each encoding unit,invoke *doEncode()*.At last,merge the codeword polynomial ,and turn it to byte array.

**Polynomial doEncode(String unit)**
Conduct encoding for each encoding unit.Return codeword polynomial.The length of an encoding unit is 223.After encoding,32 byte redundancy is added into the unit.

**String enlargeMessage(String message)**
Enlarge message string to integer multiples of 223, by padding zeros at the end of string. 

- **Class RSDecoder**

**String decode(byte[] receive)**
Decode the given byte array ,correct the error and return raw stirng.
For each 255-length decoding unit,invoke the *doDecode()* and use *StringBuffer* to store the decoded string.After that,turn StirngBuffer to string,and trim the string(Because raw string has been padded by zeros after encoding).
**String doDecode(byte[] receive)**
Conduct decoding for each decoding unit.Return decoded string.
Procedure:
(1)*calSyndromes(Polynomial receive)*：Calculate syndromes polynomial.
(2)*calBerlekampMassey(Polynomial s)*:Run BerlekampMassey algorithm,input syndromes polynomial and return array of sigma and omega.
(3)*chienSearch(Polynomial sigma)*:Input error-locator polynomial--sigma,return the roots of sigma.
(4)*forney()*:Input error-value polynomial--omega and the corresponding error location,return the coefficient of error polynomial.
(5)generate error polynomial,codeword = receive - error polynomial.








