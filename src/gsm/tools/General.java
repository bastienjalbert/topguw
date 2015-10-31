/* General.java
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

import gsm.gui.Principal;
import java.io.BufferedInputStream;
import gsm.conf.Configuration;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General management tools for application(i/o file, pattern, ...)
 *
 * @author Enjalbert Bastien
 */
@SuppressWarnings("serial")
public class General extends Principal {

    public static void main(String[] args) {
        try {
            binToCfile(new File("/root/hacking/gsm_dump/test"));
        } catch (IOException ex) {
            Logger.getLogger(General.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Base regex for a SDCCH ou SACCH frame example : C1 1218124 1881368:
     * 011010011101111.......10010000110101011
     */
    public static Pattern RGX_FRAME_CCCH
            = Pattern.compile("([CPS][10]) ([0-9]*) ([0-9]*): ([10]{114})");

    /**
     * Base regex for a SDCCH ou SACCH frame example : C1 1218124 1881368:
     * 011010011101111.......10010000110101011 BUT b
     */
    public static Pattern RGX_MALFORMED_FRAME_CCCH
            = Pattern.compile("([CPS][10]) ([0-9]*) ([0-9]*): ([10]*)");

    /**
     * Base regex for a "cannot decode" error example : gsmstack.c:301 cannot
     * decode fnr=0x12965a (1218138) ts=2
     */
    public static Pattern RGX_CANNOT_DEC
            = Pattern.compile("gsmstack.c:[0-9]* cannot decode fnr=0x[0-9a-fA"
                    + "-F]* \\(([0-9]*)\\) ts=[0-9]");

    /**
     * Base regex for a "parity error" example : cch.c:419 error: sacch: parity
     * error (-1 fn=1218138)
     */
    public static Pattern RGX_PARITY
            = Pattern.compile("cch.c:[0-9]* error: sacch: parity error \\(-1 fn=([0-9]*)\\)");

    /**
     * Base regex for a "WRN" error example : WRN: errors=18 fn=1218189
     */
    public static Pattern RGX_WRN_ERR
            = Pattern.compile("(.*)(WRN: errors=[0-9]* fn=[0-9]*)$");

    /**
     * Base regex for a "conv_decode" error example : sch.c:260 ERR: conv_decode
     * 1
     */
    public static Pattern RGX_CONVDEC_ERR
            = Pattern.compile("(.*)(sch.c:[0-9]* ERR: conv_decode [0-9]*)$");

    /**
     * Base regex for a decoded frame example : 1218142 2: 03 42 0d 06 0d 00 6d
     * .... d9 39 45 b9 c5 b1 55
     */
    public static Pattern RGX_FRAME_DEC
            = Pattern.compile("[0-9]* [0-9]: [0-9a-fA-F ]*");

    /**
     * Convert a bin file (capture with rtl_sdr command) to a readable cfile for
     * airprobe (gnuradio)
     *
     * @param binfile the binary capture file
     * @throws java.io.IOException I/O error
     */
    public static void binToCfile(File binfile) throws IOException {
        // TODO : ne pas emcombrer la mémoire et libérer l'espace au fur 
              // et a mesure (que 2 bytes occuper à la fois normalement)

        int i = 0;
        int byte1, byte2;
        int temp1, temp2;
        byte[] bytes = new byte[4];
        int bits;
        InputStream buffy = new BufferedInputStream(new FileInputStream(binfile));
        LittleEndianOutputStream dataOut = 
                new LittleEndianOutputStream(new BufferedOutputStream(new FileOutputStream(binfile.getAbsoluteFile() + ".cfile")));
        while ((byte1 = buffy.read()) != -1) {
            if ((byte2 = buffy.read()) == -1) {
                dataOut.close();
                break;
            }
            dataOut.writeFloat(((float)byte1 - 127) * 1 / 128);
            dataOut.writeFloat(((float)byte2 - 127) * 1 / 128);
    
        }
        dataOut.close();
    }


    /**
     * Determine if a file is a cfile
     *
     * @return true is the file is a cfile, false otherwhise
     * @param file the file to test
     */
    public static boolean isCfile(File file) {
        try {
            InputStream buffy = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            return false;
        }
        // test only the first 20 bytes
        for (int i = 0; i < 20; i++) {
            // TODO : find the best way to make this test
        }
        return true;
    }

    /**
     * Use rtl_sdr command to sniff a GSM tower
     *
     * @param dir the current working dir
     */
    public static ProcessBuilder rtlSdrSnif(String dir, String frequency, String gain, String samplerate) {
        ProcessBuilder pb = new ProcessBuilder("rtl_sdr", "-f",
                frequency,
                "-g", gain,
                frequency + "_AIRPROBE_OUTPUT_BIN",
                "-s", samplerate);
        pb.directory(new File(dir));
        return pb;
    }

    /**
     * Read a file
     *
     * @param file the file
     * @return an ArrayList of String
     */
    public static ArrayList<String> readFile(String file) {
        ArrayList<String> fichier = new ArrayList<String>();

        try {
            InputStream ips = new FileInputStream(file);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String ligne = br.readLine();
            @SuppressWarnings("unused")
            int i = 0;
            while (ligne != null) {
                fichier.add(ligne.toString());
                i++;
                ligne = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            // user has to give this output if he gets problem from I/O
            System.out.println(e.toString() + e);
        }
        return fichier;
    }

    /**
     * Read a file
     *
     * @param file the file
     * @return an ArrayList of String
     */
    public static ArrayList<String> readFile(File file) {
        ArrayList<String> fichier = new ArrayList<String>();

        try {
            InputStream ips = new FileInputStream(file);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String ligne = br.readLine();
            @SuppressWarnings("unused")
            int i = 0;
            while (ligne != null) {
                fichier.add(ligne.toString());
                ligne = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            // user has to give this output if he gets problem from I/O
            System.out.println(e.toString() + e);
        }
        return fichier;
    }

    /**
     * Write a file
     *
     * @param array the array to write into a file
     * @param fileName absolute path et and output file name
     */
    public static void writeFile(ArrayList<String> array, String fileName) {

        try {
            FileWriter fw = new FileWriter(fileName);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter outputFile = new PrintWriter(bw);
            for (int i = 0; i < array.size(); i++) {
                outputFile.println(array.get(i));
            }
            outputFile.close();
        } catch (Exception e) {
            // user has to give this output if he gets problem from I/O
            System.out.println(e.toString());
        }
    }

    /**
     * Write a file
     *
     * @param array the array to write
     * @param fileName absolute path and output file name
     * @param param 1 : write all index to same line, 2 : write one index per
     * line
     */
    public static void writeFileWithArray(ArrayList<String[]> array, String fileName, int param) {

        try {
            FileWriter fw = new FileWriter(fileName);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter outputFile = new PrintWriter(bw);
            if (param == 1) {
                for (int i = 0; i < array.size(); i++) {
                    for (int j = 0; j < array.get(i).length; j++) {
                        if (j == array.get(i).length - 1) {
                            outputFile.println(array.get(i)[j]);
                        } else {
                            outputFile.print(array.get(i)[j] + " : ");
                        }
                    }
                }
            } else if (param == 2) {
                for (int i = 0; i < array.size(); i++) {
                    for (int j = 0; j < array.get(i).length; j++) {
                        // don't write empty or null value
                        if(!(array.get(i)[j] == null || array.get(i)[j].equals(""))) 
                                outputFile.println(array.get(i)[j]);
                    }
                    outputFile.println("-------------------");
                }
            }

            outputFile.close();
        } catch (Exception e) {
            // user has to give this output if he gets problem from I/O
            System.out.println(e.toString());
        }
    }

    /**
     * Detect if a cfile has already been processed
     *
     * @param cfile the cfile file
     * @return true if it has already been processed, false if not
     */
    public static boolean alreadyDone(File cfile) {
        if (new File(cfile.getAbsolutePath() + "_" + Configuration.BTSCONF).exists()) {
            for (int i = 0; i < 7; i++) {
                if (new File(cfile.getAbsolutePath() + "_" + Integer.toString(i) + "S").exists()) {
                    timeslot = Integer.toString(i);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Clean an airprobe output (with S parameter)
     *
     * @param aTraite the "file" to clean (into an ArrayList)
     * @return the cleaned ArrayList
     */
    public static ArrayList<String> cleanAirprobeOutput(ArrayList<String> aTraite) {

        ArrayList<String> aTraiter = aTraite;

        for (int liste = 0; liste < aTraiter.size(); liste++) {
            String i = aTraiter.get(liste);
            // if  :
            if (RGX_FRAME_DEC.matcher(i).matches()) {
                // If it's a decoded frame: do nothing
            }
            // We place PARITY ERROR on an unique line (if not)
            if (!(RGX_FRAME_CCCH.matcher(i).matches())) {
                Pattern pat = Pattern.compile("(.*)(" + RGX_PARITY + ")");
                // if it's a malformed frame
                Matcher recup_err = pat.matcher(i);
                if (recup_err.find() && !(recup_err.group(1).equals(""))) {

                    //int indice = aTraiter.indexOf(aTraiter.get(liste));
                    // Taille = taille - taille(msg_err)
                    aTraiter.set(liste, i.substring(0, i.length()
                            - recup_err.group(2).length()));
                    aTraiter.add(++liste, recup_err.group(2));

                }
            }
            // Delete "conv_decode" error
            if (RGX_CONVDEC_ERR.matcher(i).matches()) {
                Matcher recup_err = RGX_CONVDEC_ERR.matcher(i);
                if (recup_err.find()) {

                    if (i.length() - recup_err.group(2).length() == 0) { // if the line is just a conv_decode error
                        aTraiter.remove(liste);
                        liste--; // avoid to jump a line
                    } else { // if the conv_decode error is not an unique line (something before)
                        aTraiter.set(liste, i.substring(0, i.length()
                                - recup_err.group(2).length()));
                    }
                }
            }
            // Delete "WRN" error
            if (RGX_WRN_ERR.matcher(i).matches()) {
                Matcher recup_err = RGX_WRN_ERR.matcher(i);
                if (recup_err.find()) {

                    if (i.length() - recup_err.group(2).length() == 0) {
                        aTraiter.remove(liste);
                        liste--; // avoid to jump a line
                    } else {
                        aTraiter.set(liste, i.substring(0, i.length()
                                - recup_err.group(2).length()));
                    }
                }
            }
        }
        // We correct this kind of problem :
		/*
         * 	C0 1228670 1897390: 01101001110111110010101100001
         *	cch.c:419 error: sacch: parity error (-1 fn=1228671)
         *	gsmstack.c:301 cannot decode fnr=0x12bf7f (1228671) ts=2
         *	cch.c:419 error: sacch: parity error (-1 fn=1228699)
         *	gsmstack.c:301 cannot decode fnr=0x12bf9b (1228699) ts=2
         *	1110010110111011110000011110001001011101100110000100000101000000111010010000110101111
         *  
         */
        for (int liste = 0; liste < aTraiter.size(); liste++) {
            String i = aTraiter.get(liste);
            // frame lenght compared to frame number (will not be the same 
            // if the fn is 111 or 12255 for exemple)
            int framelenght;
            // if  :
            if (i.length() < 133
                    && !(RGX_WRN_ERR.matcher(i).matches()
                    || RGX_CONVDEC_ERR.matcher(i).matches()
                    || RGX_FRAME_DEC.matcher(i).matches()
                    || RGX_PARITY.matcher(i).matches()
                    || RGX_CANNOT_DEC.matcher(i).matches())) {
                System.out.println(i + " capté");
                for (int subList = liste; liste + 20 < aTraiter.size() && subList < liste + 20; subList++) {

                    if (!(RGX_WRN_ERR.matcher(aTraiter.get(subList)).matches()
                            || RGX_CONVDEC_ERR.matcher(aTraiter.get(subList)).matches()
                            || RGX_FRAME_DEC.matcher(aTraiter.get(subList)).matches()
                            || RGX_PARITY.matcher(aTraiter.get(subList)).matches()
                            || RGX_CANNOT_DEC.matcher(aTraiter.get(subList)).matches())) {

                        if (RGX_FRAME_CCCH.matcher(i + aTraiter.get(subList)).matches()) {
                            // We concatenate frame correctly

                            aTraiter.set(liste, i + aTraiter.get(subList));
                            aTraiter.remove(subList);
                        }
                    }
                }
            }
        }

        // TODO : place parity error (for encrypted frame) just after the frame
        return aTraiter;

    }

    /**
     * Get from a cfile airprobe output for B and S configuration
     *
     * @param file the cfile t
     * @throws Exception Not Immediate Assignment found
     */
    public static void getAirprobeOutput(File file) throws Exception {

        // check if principal output exist
        //File f = new File(fichier + "_0B"); TODO : intégrer la détection mais demandé à l'utilisateur une confirmation de pas faire les output
        // TODO : utilisé la méthode alreadyDone (gui?)
        // pour finir l'itération pour trouvé le channel dédié utilisé par la tour
        boolean finish = false;

        //if(!(f.exists() && !f.isDirectory())) {  
        // Extract 
        ProcessBuilder pb = new ProcessBuilder("sh", "go.sh", file.getAbsolutePath(), Configuration.DEC_RATE, Configuration.BTSCONF);
        pb.directory(new File(Configuration.gsmReceivePath + "/src/python/"));
        pb.redirectOutput(new File(file.getAbsolutePath() + "_" + Configuration.BTSCONF));

        // avoid infinite time out with big cfile
        pb.redirectErrorStream(true);

        Process p = pb.start();
        p.waitFor();
        p.destroy();
        p.destroyForcibly();
        //	}
        // We get broadcast channel
        broadcastChannelTab = Broadcast.lignesToTab(readFile(file.getAbsolutePath() + "_" + Configuration.BTSCONF));

        // Potential Immediate Assignment index
        ArrayList<Integer> imAs = Broadcast.extractImAs(broadcastChannelTab);

        if (imAs.isEmpty()) {
            throw new Exception("Sorry, don't find any Immediate Assignment on this cfile.\n"
                    + START_LINE + "Please choose an other file, or sniff again a GSM tower.\n");
        }
        // else : if there is Immediate Assignment

        for (int i = 0; i < imAs.size() && finish == false; i++) {
            if ("1".equals(Broadcast.extractTsConf(broadcastChannelTab.get(imAs.get(i))).get(1))) {
                // if an immediate assignment redirects to a dedicated channel
                // pb.redirectOutput(new File(fichier.getAbsolutePath() + "0B"));
                timeslot = Broadcast.extractTsConf(broadcastChannelTab.get(imAs.get(i))).get(0);
                pb = new ProcessBuilder("sh", "go.sh",
                        file.getAbsolutePath(),
                        Configuration.DEC_RATE,
                        timeslot + "S"
                );
                pb.directory(new File(Configuration.gsmReceivePath + "/src/python/"));
                pb.redirectOutput(new File(file.getAbsolutePath() + "_" + timeslot + "S"));
                pb.redirectErrorStream(true);
                p = pb.start();
                p.waitFor();
                p.destroy();
                p.destroyForcibly();
                finish = true;
            }
        }
        dedicatedChannelTab = Dedicated.lignesToTab(readFile(file.getAbsolutePath() + "_" + timeslot + "S"));
        // debug line TODO : delete
        System.out.println("fichier " + file.getAbsolutePath() + "_" + timeslot + "S" + " traité");
    }

    /**
     * XOR two array (ind 0 with ind 0, ind 1 with ind 1, ...)
     *
     * @param beginBursts the array that contains bursts
     * @param endBursts the second array that contains bursts (and a5/1 fn) [encrypted]
     * @param fn the plaintext frame number
     * @param fnEnc the encrypted frame number
     * @return  all bursts (4 at maximum) xored
     */
    public static String[] xorBursts(String[] beginBursts, String[] endBursts, String fn, String fnEnc) {
        String[] xoredBursts = new String[10];
        xoredBursts[8] = fn;
        xoredBursts[9] = fnEnc;

        StringBuilder oneXoredBurst;
        
        // number of bursts that cannot be xored
        int imposs = 0;

        for (int j = 0; j < 4; j++) {
            if (isABurst(beginBursts[j]) && isABurst(endBursts[j])) {
                oneXoredBurst = new StringBuilder();
                for (int i = 0; i < 114; i++) {
                    oneXoredBurst.append(beginBursts[j].charAt(i) ^ endBursts[j].charAt(i));
                }
                xoredBursts[j] = oneXoredBurst.toString();
                
            } else {
                imposs++;
                xoredBursts[j] = "Incorrect bursts from capture, can't xor them.";
            }
        }
        
        if(imposs >= 3) 
            return new String[0];
        
        // add a5/1 frame number from encrypted frame to simplify crack steps
        for(int i = 4; i < 8 ;i++) {
            if(isInteger(endBursts[i])) {
                xoredBursts[i] = "a5/1 burst fn = " + endBursts[i];
            } 
        }
        // we reorganize the array to match bursts with a5/1 fn
        // TODO ; make something more algorithmic
        String temp2 = xoredBursts[1];
        String temp3 = xoredBursts[2];
        String temp4 = xoredBursts[3];
        
        xoredBursts[1] = xoredBursts[4];
        xoredBursts[2] = temp2;
        xoredBursts[3] = xoredBursts[5];
        xoredBursts[4] = temp3;
        xoredBursts[5] = xoredBursts[6];
        xoredBursts[6] = temp4;

        return xoredBursts;
    }

    /**
     * XOR two array (ind 0 with ind 0, ind 1 with ind 1, ...) without any other
     * information exept xored bits
     *
     * @param beginBursts the array that containt bursts
     * @param endBursts the second array that containt bursts
     * @return all bursts (4 at maximum) xored
     */
    public static String[] xorBursts(String[] beginBursts, String[] endBursts) {
        String[] xoredBursts = new String[4];
        StringBuilder oneXoredBurst;

        for (int j = 0; j < 4; j++) {
            if (isABurst(beginBursts[j]) && isABurst(endBursts[j])) {
                oneXoredBurst = new StringBuilder();
                for (int i = 0; i < 114; i++) {
                    oneXoredBurst.append(beginBursts[j].charAt(i) ^ endBursts[j].charAt(i));
                }
                xoredBursts[j] = oneXoredBurst.toString();
            } else {
                xoredBursts[j] = "This burst is not correct, cannot xor it.";
            }
        }
        int numberOfGoodBursts = 0;
        for (int i = 0; i < xoredBursts.length; i++) {
            if (xoredBursts[i].length() == 114) {
                numberOfGoodBursts++;
            }
        }
        // delete bad burst (This burst is not correct, .. etc)
        String[] xoredBurstsOnly = new String[numberOfGoodBursts];
        for (int i = 0, j = 0; i < 4; i++) {
            if (xoredBursts[i].length() == 114) {
                xoredBurstsOnly[j] = xoredBursts[i];
                j++;
            }
        }
        return xoredBurstsOnly;
    }

    /**
     * Check if the String is a burst or not
     *
     * @param toTest the string to test
     * @return true if the String seems to be a bursts, false otherwise
     */
    public static boolean isABurst(String toTest) {
        if (toTest.length() == 114 && toTest.matches("[01]*")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retourne a binary String from and hexadecimal String
     *
     * @param s hexadecimal String
     * @return binary String
     */
    public static String hexToBin(String s) {
        return new BigInteger(s, 16).toString(2);
    }

    /**
     * toString(ArrayList<String[]>)
     *
     * @param liste
     * @return listeString frames into an unique String ("\n" separator)
     */
    public static String toStringALtabStr(ArrayList<String[]> liste) {
        String listeString = "";

        for (int i = 0; i < liste.size(); i++) {
            for (int j = 0; j < liste.get(i).length; j++) {
                listeString = listeString + liste.get(i)[j];
            }
            listeString = listeString + "\n";
        }
        return listeString;
    }

    /**
     * ArrayList of String array to ArrayList of String
     *
     * @param liste
     * @return listeString
     */
    public static ArrayList<String> toArraylistString(ArrayList<String[]> liste) {
        ArrayList<String> listeString = new ArrayList<String>();
        StringBuilder temp = new StringBuilder();

        for (int i = 0; i < liste.size(); i++) {
            for (int j = 0; j < liste.get(i).length; j++) {
                temp.append(liste.get(i)[j]);
            }
            listeString.add(temp.toString());
        }
        return listeString;
    }

    /**
     * Check if a String is a number
     *
     * @param s the string to test
     * @return true if the string is a number, false otherwhise
     */
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

}
