/* Dedicated.java
 * --------------------------------- DISCLAMER ---------------------------------
 * Copyright (c) 2015, Bastien Enjalbert All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * The views and conclusions contained in the software and documentation are 
 * those of the authors and should not be interpreted as representing official 
 * policies, either expressed or implied, of the FreeBSD Project.
 */
package gsm.tools;

import gsm.conf.Configuration;
import gsm.gui.Principal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * Dedicated channel management for GSM
 *
 * @author Enjalbert Bastien
 */
public class Dedicated extends Principal {

    /**
     * Convert an arraylist of string into an arraylist of String array
     * we split the string with spaces
     * @param inLine
     * @return splitted line into an ArrayList
     */
    public static ArrayList<String[]> lignesToTab(ArrayList<String> inLine) {

        ArrayList<String[]> framesEnTab = new ArrayList<>();
        for (int i = 0; i < inLine.size(); i++) {
            framesEnTab.add(ligneToTab(inLine.get(i)));
        }
        return framesEnTab;
    }

    /**
     * Convert a String into an array (split with spaces)
     *
     * @param line the line to split
     * @return the splitted line into an array
     */
    public static String[] ligneToTab(String line) {

        /* Array are like this (with dedicated channel [ S config with airprobe])
         * 
         * Index    | Contents
         * 0		| Burst Type (i.e "C1", "P0", "S0", ...)
         * 1        | fn 
         * 2        | fn (a5/1) + ":"
         * 3        | data (one burst)
         */
        /*
         * Index    | Contents
         * 0		| fn
         * 1        | number + ":" 
         * 2-25     | hex frame 
         */
        String[] splitArray = null;
        splitArray = line.split(" ");

        return splitArray;
    }

    /**
     * Search for SI type 5/5ter/6
     * only SI 5 for test purpose
     * @param inArray frames in an array
     * @return
     */
    public static ArrayList<String[]> getSysInfo(ArrayList<String[]> inArray) {
        ArrayList<String[]> si = new ArrayList<>();

        boolean si5getted = false;
        //boolean si6getted = false;
        //boolean si5tergetted = false;

        for (int i = 0; i < inArray.size(); i++) {
            if (inArray.get(i).length == 25) {
                // SI5 
                if (si5getted == false && inArray.get(i)[7].equals("06")
                        && inArray.get(i)[8].equals("1d")) {
                    String[] temp = new String[3];
                    //fn
                    temp[0] = inArray.get(i)[0];
                    //hex value
                    temp[1] = "";
                    for (int a = 2; a < 25; a++) {
                        temp[1] += inArray.get(i)[a];
                    }
                    //si type
                    temp[2] = "SI5";
                    si5getted = true;
                    si.add(temp);
                }
                // SI5TER
                /*if (si5tergetted == false && inArray.get(i)[7].equals("06")
                        && inArray.get(i)[8].equals("06")) {
                    String[] temp = new String[3];
                    //fn
                    temp[0] = inArray.get(i)[0];
                    //hex value
                    temp[1] = "";
                    for (int a = 2; a < 25; a++) {
                        temp[1] += inArray.get(i)[a];
                    }
                    //si type
                    temp[2] = "SI5TER";
                    si5tergetted = true;
                    si.add(temp);
                }
                // SI6
                if (si6getted == false && inArray.get(i)[7].equals("06")
                        && inArray.get(i)[8].equals("1e")) {
                    String[] temp = new String[3];
                    //fn
                    temp[0] = inArray.get(i)[0];
                    //hex value
                    temp[1] = "";
                    for (int a = 2; a < 25; a++) {
                        temp[1] += inArray.get(i)[a];
                    }
                    //si type
                    temp[2] = "SI6";
                    si6getted = true;
                    si.add(temp);
                }*/
                // stop the loop -> all si5 are the same so stop when we caught one
                if (si5getted == true /*&& si6getted == true && si5tergetted == true*/) {
                    break;
                }
            }
        }

        // intialize object
        systemInfo = si;
        /*
         * ind 0 : fn
         * ind 1 : hex value
         * ind 3 : SI type
         */
        return si;
    }

    /**
     * Search for Ciphering Mode Command
     *
     * @param inArray frames in an array
     * @return
     */
    public static ArrayList<String[]> getCipherModCmd(ArrayList<String[]> inArray) {
        ArrayList<String[]> ciphModCmd = new ArrayList<>();

        for (int i = 0; i < inArray.size(); i++) {
            if (inArray.get(i).length == 25) {
  
                if (inArray.get(i)[5].equals("06")
                        && inArray.get(i)[6].equals("35")) {
                    String[] temp = new String[2];
                    //fn
                    temp[0] = inArray.get(i)[0];
                    //hex value
                    temp[1] = "";
                    for (int a = 2; a < 25; a++) {
                        temp[1] += inArray.get(i)[a];
                    }
                    ciphModCmd.add(temp);
                }
            }
        }

        cipherModCommand = ciphModCmd;

        /*
         * ind 0 : fn
         * ind 1 : hex value
         */
        return ciphModCmd;
    }

    /**
     * Search for potential SI position after a ciphering mode command
     *
     * @return potential position
     */
    public static ArrayList<String[]> getEncryptedSi() throws Exception {

        int localDedicatedChannelFn = 0;

        ArrayList<String[]> cipheredSi = new ArrayList<>();

        if (systemInfo == null || cipherModCommand == null) {
            throw new Exception(START_LINE + "Error : you have to find SI cleartext and Ciphering Mode Command position before.\n");
        } else {
            for (int i = 0; i < systemInfo.size(); i++) {
                for (int j = 0; j < dedicatedChannelTab.size(); j++) {
                    
                    if (dedicatedChannelTab.get(j).length == 4 // check the frame is correct (not an error or other thing)
                            && General.isInteger(dedicatedChannelTab.get(j)[1])
                            // current frame is not an unecrypted SI 5
                            && Integer.parseInt(dedicatedChannelTab.get(j)[1]) != Integer.parseInt(systemInfo.get(i)[0])
                            // if frames(unencrypted and possible encrypted) are in the same place in the multi frame
                            && ((Integer.parseInt(dedicatedChannelTab.get(j)[1]) - Integer.parseInt(systemInfo.get(i)[0])) % 204  == 0)) {
                        
                        for (int a = 0; a < cipherModCommand.size(); a++) {
                            // the frame is after a ciphering mod command
                            if(localDedicatedChannelFn != Integer.parseInt(dedicatedChannelTab.get(j)[1])
                                    // the frame is encrypted 
                                    && isParityErr(dedicatedChannelTab.get(j)[1])) {
                                System.out.println("one found");
                                String[] temp = new String[4];
                                /*
                                 * ind 0 : SI plaintext Frame Number
                                 * ind 1 : SI plaintext Frame Number % 102
                                 * ind 2 : SI Ciphered possible position
                                 * ind 3 : SI Ciphered possible position % 102 -
                                 */

                                localDedicatedChannelFn = Integer.parseInt(dedicatedChannelTab.get(j)[1]);
                                temp[0] = systemInfo.get(i)[0];
                                temp[1] = "fn[" + Integer.toString(Integer.parseInt(systemInfo.get(i)[0]) % 102) + "]";
                                temp[2] = dedicatedChannelTab.get(j)[1];
                                temp[3] = "fn[" + Integer.toString(Integer.parseInt(dedicatedChannelTab.get(j)[1]) % 102) + "]";
                                cipheredSi.add(temp);
                            }
                        }
                    }
                }
            }
        }

        return cipheredSi;
    }

    /**
     * Check if a frame number is link to an encrypted SI frame (after founding
     * them)
     *
     * @param fn the frame number
     * @param fn2 the second frame number
     * @return true if the frame number is link, false if not
     */
    public static boolean isLinkToThisSI(String fn, String fn2) {
        for (int i = 0; i < encryptedSiPosition.size(); i++) {
            if (encryptedSiPosition.get(i)[0].equals(fn)
                    && encryptedSiPosition.get(i)[2].equals(fn2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a frame number is linked to a parity error (cannot decode)
     * @param fn the frame number
     * @return true if the frame seems unable to be decoded by airprobe
     */
    public static boolean isParityErr(String fn) {

        // read dedicated channel (xS) line by line
        ArrayList<String> temp = General.readFile(file.getAbsolutePath() + "_" + timeslot + "S");

        // and check if e found a parity error linked to the fn
        for (int i = 0; i < temp.size(); i++) {
            if (General.RGX_PARITY.matcher(temp.get(i)).matches()) {
                Matcher recup_err = General.RGX_PARITY.matcher(temp.get(i));
                if (recup_err.find()) {
                    if (recup_err.group(1).equals(fn));
                        return true;
                } 
            }
        }

        return false;
    }

    /**
     * Get Bursts from a frame number
     *
     * @param fn the frame number
     * @return an array with at least one burst from the frame number (if other
     * bursts are missing in the dump), else 4 bursts are returned
     * ind 0 first burst from the frame
     * ind 1 a5/1 fn from the bursts
     * ind 2 second burst from the frame
     * ind 3 a5/1 fn from the bursts 
     * ... etc
     */
    public static String[] getBurstsFromFn(String fn){

        String[] bursts = new String[8];
        for (int i = 0; i < 8; i++) 
            bursts[i] = "no exist";
        
        int integerFn = Integer.parseInt(fn);
        boolean finish = false;

        for (int i = 0; finish == false && i < dedicatedChannelTab.size(); i++) {
            String[] line = dedicatedChannelTab.get(i);
            // check element from array is a frame burst
            if (line.length == 4 && line[0].length() == 2 && General.isInteger(line[1])
                && (Integer.parseInt(line[1]) <= 2715647 || Integer.parseInt(line[1]) > 0)
                     && line[0].charAt(0) == 'C') {
                if (Integer.parseInt(line[1]) == integerFn - 3) {
                    // add the burst
                    bursts[0] = line[3];
                    // add a5/1 fn
                    bursts[4] = line[2].substring(0, line[2].length()-1);
                } else if (Integer.parseInt(line[1]) == integerFn - 2) {
                    bursts[1] = line[3];
                    bursts[5] = line[2].substring(0, line[2].length()-1);
                } else if (Integer.parseInt(line[1]) == integerFn - 1) {
                    bursts[2] = line[3];
                    bursts[6] = line[2].substring(0, line[2].length()-1);
                } else if (Integer.parseInt(line[1]) == integerFn) {
                    bursts[3] = line[3];
                    bursts[7] = line[2].substring(0, line[2].length()-1);
                    finish = true;
                } else {
                    // DO NOTHING
                }
            }
        }
        return bursts;
    }

    /**
     * Return bursts from a hexa frame without time advance
     *
     * @param hexaFrame frame in hexadecimal
     * @param fn the frame number
     * @param siType the System Information type (5/5ter/6)
     * @return String[] with all 4 bursts from the frame
     * @throws IOException if error while executing command
     */
    public static String[] getBursts(String hexaFrame, String fn, String siType) throws IOException {
        String[] bursts = new String[6];
        bursts[4] = fn;
        bursts[5] = siType;
        int i = 0;

        // delete Time Advance 
        StringBuilder hexaFrameNoTA = new StringBuilder(hexaFrame);
        hexaFrameNoTA.setCharAt(2, '0');
        hexaFrameNoTA.setCharAt(3, '0');
        ProcessBuilder pb = new ProcessBuilder("./gsmframecoder", hexaFrameNoTA.toString());
        pb.redirectErrorStream(true);
        pb.directory(Configuration.gsmFrameCoder);
        Process p = pb.start();

        p.getOutputStream().flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String ligne = new String();
        while ((ligne = reader.readLine()) != null) {
            if (ligne.length() == 114 && i < 4) {
                bursts[i] = ligne;
                i++;
            }
        }
        p.destroy();
        p.destroyForcibly();
        return bursts;
    }

    /**
     * Get : (fn % 102 == {32,47})
     *
     * @param enTableau frames in an arraylist of string[]
     * @return Une arraylist de int[] composé des fn possibles et de leurs
     * résultats % 102
     * @obselete
     * @unused
     */
    // TODO : delete
    public static ArrayList<String[]> findSysInfo(ArrayList<String[]> enTableau) {
        ArrayList<String[]> lesSi = new ArrayList<String[]>();
        String tempFn = "";

        /*
         * INDEX 0 : FRAME NUMBER
         * INDEX 1 : FRAME NUMBER MODULO 102
         */
        for (int i = 0; i < enTableau.size(); i++) {
            String[] temp = new String[2];
            // Si fn % 102 == 32,47 -> on ajoute 
            if (enTableau.get(i).length > 2 && General.isInteger(enTableau.get(i)[1])
                    && (Integer.parseInt(enTableau.get(i)[1]) % 102 >= 32
                    && Integer.parseInt(enTableau.get(i)[1]) % 102 <= 47)
                    && !(tempFn.equals(enTableau.get(i)[1]))) {
                tempFn = enTableau.get(i)[1];
                temp[0] = enTableau.get(i)[1];
                if (Integer.parseInt(enTableau.get(i)[1]) % 102 == 35
                        || Integer.parseInt(enTableau.get(i)[1]) % 102 == 39
                        || Integer.parseInt(enTableau.get(i)[1]) % 102 == 43
                        || Integer.parseInt(enTableau.get(i)[1]) % 102 == 47) {
                    temp[1] = String.valueOf(Integer.parseInt(enTableau.get(i)[1]) % 102) + "\nFound end of frame";
                } else {
                    temp[1] = String.valueOf(Integer.parseInt(enTableau.get(i)[1]) % 102);
                }

                lesSi.add(temp);
            }
        }
        return lesSi;
    }

   
}
