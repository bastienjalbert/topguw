/* Configuration.java
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
package gsm.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * General configuration for the application
 * @author bastien enjalbert
 */
public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    /**
     * Decimation rate for airprobe 
     * For example a rtl-sdr device works with 64
     */
    public static String DEC_RATE = "64";

    /**
     * BTS Configuration (0C -> combined , 0B -> non-combined)
     */
    public static String BTSCONF = "0B";
    
    // gsm-receive Path from Airprobe
    public static String gsmReceivePath = "/root/airprobe/gsm-receiver/";
    // gsmframecoder Path (test folder) 
    public static String gsmFrameCoder = "/root/gsmframecoder/gsmframecoder/test/";


    public static void saveProperties() {
        Properties prop = new Properties();
        prop.put("dec_rate",Configuration.DEC_RATE);
        prop.put("btsconf",Configuration.BTSCONF);
        prop.put("gsmReceivePath",Configuration.gsmReceivePath);
        prop.put("gsmFrameCoder",Configuration.gsmFrameCoder);
        try {
            prop.store(new FileOutputStream("config.properties"),"");
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void loadProperties() {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        Configuration.DEC_RATE = prop.getProperty("dec_rate","64");
        Configuration.BTSCONF = prop.getProperty("btsconf","0B");
        Configuration.gsmReceivePath = prop.getProperty("gsmReceivePath", "/root/airprobe/gsm-receiver/");
        Configuration.gsmFrameCoder = prop.getProperty("gsmFrameCoder", "/root/gsmframecoder/gsmframecoder/test/");

    }
}
