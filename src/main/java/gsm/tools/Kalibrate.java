/* Kalibrate.java
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Link for kalibrate-rtl tool and java
 *
 * @author bastien enjalbert
 */
public class Kalibrate {

    /**
     * base regex for a kalibrate output group 1 nothing group 2 cell fequency
     * group 3 freq type (- or +) group 4 freq correction group 5 power
     */
    public static Pattern RGX_KAL
            = Pattern.compile(".*chan: [0-9]* \\(([0-9]*.[0-9]*)MHz (-+) ([0-9]*.[0-9]*)kHz\\)	power: ([0-9]*.[0-9]*)");

    /**
     * Start kalibrate-rtl (kal) to get GSM tower
     *
     * @param whichGsm GSM type (900, 1800, ..)
     * @param gain
     * @return an arraylist containing GSM tower detected by kal index 1 : freq
     * (corrected) index 2 : power
     * @throws Exception if RTL-SDR device is not plugged
     */
    public static ArrayList<String[]> getGsmCell(String whichGsm, String gain) throws Exception {
        ArrayList<String[]> gsmCells = new ArrayList<String[]>();

        ProcessBuilder pb = new ProcessBuilder("kal", "-s", whichGsm, "-g", gain);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        p.getOutputStream().flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String ligne = new String();
        while ((ligne = reader.readLine()) != null) {
            System.out.println("debug : " + ligne); // TODO : delete (DEBUG LINE)
            if (ligne.equals("No supported devices found.")) {
                throw new Exception("Please plug-in your RTL-SDR device (not detected).");
            }
            Matcher m = RGX_KAL.matcher(ligne);
            if (m.matches()) {
                // add the correct frequency					
                String[] temp = new String[2];
                BigDecimal add = null;
                if (m.group(2).equals("+")) {
                    // TODO : capture with a long (parsing)
                    add = new BigDecimal(Double.parseDouble(m.group(3)));
                } else {
                    add = new BigDecimal(-(Double.parseDouble(m.group(3))));
                }
                BigDecimal big = new BigDecimal(Double.parseDouble(m.group(1)) * 1000000);
                big = big.add(add);
                //System.out.println("detected frequency : " + Double.toString(Double.parseDouble(m.group(1))*1000000));
                System.out.println(Long.toString(big.longValue()));
                temp[0] = Long.toString(big.longValue());
                temp[1] = m.group(4);
                gsmCells.add(temp);
            }
        }
        p.destroy();
        p.destroyForcibly();
        // assert p.getInputStream().read() == -1;
        
        // sort gsm tower by power detected
        Collections.sort(gsmCells, new Comparator<String[]>() {
            @Override
            public int compare(String[] a, String[] b) {
                return b[1].compareTo(a[1]);
            }
        });
        return gsmCells;
    }

}
