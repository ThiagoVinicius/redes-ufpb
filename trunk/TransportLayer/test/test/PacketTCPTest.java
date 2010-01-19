/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import junit.framework.TestCase;

/**
 *
 * @author Jailton
 */
public class PacketTCPTest extends TestCase {

//    public void testClassPacketTCP() {
//        PacketTCP p = new PacketTCP("100100100100100100100100111");
//    }

    public void testParseIntToString() {
        int s = parseStringToInt("1000");
       // char c = s.charAt(23);
        System.out.println(s);
    }

     private String parseIntToString( int value, int numBit ) {

         /*
         if ( value > (Math.pow(2, numBit)-1) ) {
             throw new IllegalArgumentException("O valor estoura o numero de bits!");
         }
         */

         while ( value > (Math.pow(2, numBit)-1) ) {
             value = value - (int) (Math.pow(2, numBit));
         }

         String string = Integer.toBinaryString(value);

         if(string.length() < numBit) {
             int addBit = numBit - string.length();

             for(int c = 0; c < addBit; c++) {
                string = "0" + string;
             }
         }


         return string;
     }

     private int parseStringToInt( String value ) {

         return Integer.parseInt(value, 2);
     }




}
