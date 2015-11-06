/* Principal.java
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
package gsm.gui;

import gsm.tools.Broadcast;
import gsm.tools.Dedicated;
import gsm.tools.General;
import gsm.conf.Configuration;
import gsm.tools.Kalibrate;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextArea;

import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionListener;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class Principal extends JPanel {

    // Line starting for the pseudo-shell
    protected static String START_LINE = "Topguw> ";

    protected static JTextArea localCmd;

    // the bin/cfile
    protected static File file;

    /**
     * 0B or 0C output for the cfile from airprobe
     */
    protected static ArrayList<String[]> broadcastChannelTab = null;

    /**
     * xS output for the cfile from airprobe
     */
    protected static ArrayList<String[]> dedicatedChannelTab = null;

    /**
     * Possible encrypted Sysytem Information ind 0 : SI plaintext Frame Number
     * ind 1 : SI plaintext Frame Number % 102 ind 2 : SI Encrypted possible
     * Frame Number ind 3 : SI Encrypted possible Frame Number % 102
     */
    protected static ArrayList<String[]> encryptedSiPosition = null;

    /**
     * System Information frame container ind 0 : frame number ind 1 : hex value
     * of the frame ind 3 : si type (5/5ter/6)
     */
    protected static ArrayList<String[]> systemInfo = null;

    /**
     * Cipher Mode Command frame container ind 0 : frame number ind 1 : hex
     * value of the frame
     */
    protected static ArrayList<String[]> cipherModCommand = null;

    /**
     * Current frequency to work in (snif)
     */
    protected String frequency = "";

    // Timeslot used for dedicated channel (s(a|d)cch channel)
    protected static String timeslot;

    /**
     * ************** JLABEL FILE LOADED ***********************
     */
    static JLabel lblLoadedFile = new JLabel();

    /**
     * *******************
     */
    private JFrame frmTopguw;

    
    /**
     * ************************************** BEGINNING
     * ***********************************
     */
    /**
     * Launch the application.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Principal window = new Principal();
                    window.frmTopguw.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public Principal() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {

        frmTopguw = new JFrame();
        frmTopguw.setResizable(true);
        frmTopguw.getContentPane().setFont(new Font("Dialog", Font.BOLD, 9));
        frmTopguw.setBackground(SystemColor.desktop);
        frmTopguw.getContentPane().setBackground(SystemColor.control);
        frmTopguw.setTitle("Topguw");
        frmTopguw.setBounds(100, 100, 800, 600);
        frmTopguw.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmTopguw.getContentPane().setLayout(null);

        /**
         * **************** DEFINITION OF THE "CMD" PANNEL (pseudo-shell)
         * *******
         */
        JTextArea localCmd = new JTextArea();
        //localCmd.setPreferredSize(new Dimension(700, 255));
        localCmd.setFont(new Font("DejaVu Sans", Font.BOLD, 11));
        localCmd.setEditable(false);
        localCmd.setBounds(57, 145, 700, 255);
        localCmd.setText("Welcome on Topguw, a help to analyze GSM.\n");
        localCmd.append("Topguw is currently in beta version.\n");
        localCmd.append("Bastien Enjalbert, bastien.enjalbert@gmail.com\n\n");
        localCmd.setLineWrap(true);
        localCmd.setWrapStyleWord(true);

        /**
         * ************** SEPARATOR ************
         */
        JSeparator separator = new JSeparator();
        separator.setBounds(57, 187, 700, 7);

        /**
         * ************** JLABEL FILE LOADED ***********************
         */
        lblLoadedFile.setBounds(206, 67, 426, 25);
        lblLoadedFile.setVisible(false);

        /**
         * ************** GSM CHOOSE FOR KALIBRATE ************
         */
        String[] typeGsm = {"GSM900", "GSM1800"};
        @SuppressWarnings({"unchecked", "rawtypes"})
        JComboBox mnGsmBand = new JComboBox(typeGsm);
        mnGsmBand.setName("BAND GSM");
        mnGsmBand.setBounds(546, 30, 91, 24);
        mnGsmBand.setVisible(false);

        // Gain (LABEL)
        JLabel lblGain = new JLabel("Gain :");
        lblGain.setBounds(206, 35, 70, 15);
        lblGain.setVisible(false);
        frmTopguw.getContentPane().add(lblGain);

        /**
         * ************** GAIN CHOOSE FOR KALIBRATE *************
         */
        JSlider sliderGainKal = new JSlider();
        sliderGainKal.setValue(0);
        sliderGainKal.setName("gainKal");
        sliderGainKal.setMaximum(50);
        sliderGainKal.setBounds(263, 22, 271, 33);
        sliderGainKal.setMajorTickSpacing(5);
        sliderGainKal.setMinorTickSpacing(5);
        sliderGainKal.setPaintLabels(true);
        sliderGainKal.setVisible(false);

        /**
         * ************** BROWSE FILE ****************
         */
        JFileChooser cfile_file = new JFileChooser();
        cfile_file.setVisible(false);
        cfile_file.setMultiSelectionEnabled(false);
        cfile_file.setDialogTitle("Choose a cfile");
        cfile_file.setApproveButtonToolTipText("");
        cfile_file.setApproveButtonText("Open");
        cfile_file.setBounds(476, -293, 400, 300);

        /**
         * **************** BUTTONS DEFINITION ******************
         */
        /**
         * FIND ENCRYPTED SI BUTTON
         */
        JButton btnExtractEncryptedSi = new JButton("Find encrypted SI");
        btnExtractEncryptedSi.setBounds(575, 109, 182, 25);
        btnExtractEncryptedSi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                localCmd.append(START_LINE + "Start finding SI (encrypted/ciphered version), please wait...\n");
                localCmd.update(localCmd.getGraphics());

                encryptedSiPosition = new ArrayList<String[]>();
                try {
                    encryptedSiPosition = Dedicated.getEncryptedSi();
                } catch (Exception e1) {
                    localCmd.append(START_LINE + "Error : " + e1);
                    localCmd.update(localCmd.getGraphics());
                }

                localCmd.append(START_LINE + encryptedSiPosition.size() + " possible ciphered SI position found.\n");
                localCmd.update(localCmd.getGraphics());
                General.writeFileWithArray(encryptedSiPosition, file.getAbsolutePath() + "_ENCRYPTED_SI_POS", 1);

            }
        });
        btnExtractEncryptedSi.setEnabled(false);

        /**
         * GET SI PLAINTEXT (BURSTS) WITHOUT TIME ADVANCE BUTTON
         */
        JButton btnGetCleanBursts = new JButton("Get SI Bursts");
        btnGetCleanBursts.setBounds(57, 150, 192, 25);
        btnGetCleanBursts.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                localCmd.append(START_LINE + "Start extracting SI bursts without Time Advance, please wait...\n");
                localCmd.update(localCmd.getGraphics());

                //ArrayList<String[]> siBursts = new ArrayList<String[]>();
                //TODO : CHECK THAT systemInfo have been initialized
                // we get only one frame of SI 5, 5TER and 6 because they are (almost) the same for a gsm Tower
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath() + "_SI_BURSTS_NO_TA")));
                    for (int i = 0; i < systemInfo.size(); i++) {

                        String[] frameBursts = Dedicated.getBursts(systemInfo.get(i)[1], systemInfo.get(i)[0], "SI5");
                        //siBursts.add(frameBursts);

                        out.println(systemInfo.get(i)[2] + " fn : " + systemInfo.get(i)[0] + " hex frame : " + systemInfo.get(i)[1]);
                        out.println("BURSTS without Timing Advance: ");
                        for (int o = 0; o < frameBursts.length; o++) {
                            if (frameBursts[o] != null) {
                                out.println(frameBursts[o]);
                            }
                        }
                    }
                    out.close();
                } catch (Exception e1) {
                }

                localCmd.append(START_LINE + "SI bursts without Time Advance have been extracted.\n");
                localCmd.update(localCmd.getGraphics());

            }
        });
        btnGetCleanBursts.setEnabled(false);

        /**
         * EXTRACT CIPHERING MODE COMMAND BUTTON
         */
        JButton btnExtractCiphMod = new JButton("Extract Ciphering Mode Command");
        btnExtractCiphMod.setBounds(292, 109, 271, 25);
        btnExtractCiphMod.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                localCmd.append(START_LINE + "Start getting Ciphering Mode Command, please wait...\n");
                localCmd.update(localCmd.getGraphics());
                ArrayList<String[]> cipherModPosition = Dedicated.getCipherModCmd(dedicatedChannelTab);
                if (cipherModPosition.isEmpty()) {
                    localCmd.append(START_LINE + "Sorry but no Ciphering Mode Command have been discovered.\n");
                    localCmd.append(START_LINE + "Maybe you should sniff longer the GSM tower.\n");
                    localCmd.update(localCmd.getGraphics());
                } else {
                    for (int i = 0; i < cipherModPosition.size(); i++) {
                        localCmd.append(START_LINE + "fn : " + cipherModPosition.get(i)[0] + " frame (hex) : "
                                + cipherModPosition.get(i)[1] + "\n");
                        localCmd.update(localCmd.getGraphics());
                    }
                }
                // save cipher mod command position
                General.writeFileWithArray(cipherModPosition, file.getAbsolutePath() + "_CIPHER_MOD_CMD", 1);

                localCmd.append("debug : " + "Getting Ciphering Mode Command is done.\n");
                localCmd.update(localCmd.getGraphics());
            }
        });
        btnExtractCiphMod.setEnabled(false);

        /**
         * GET POSSIBLE KEYSTREAMS BUTTON
         */
        JButton btnGetPossibleKeystream = new JButton("Get possible keystreams");
        btnGetPossibleKeystream.setBounds(263, 150, 210, 25);
        btnGetPossibleKeystream.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                localCmd.append(START_LINE + "Start getting possible keystreams, please wait...\n");
                localCmd.update(localCmd.getGraphics());

                // TODO : check if encryptedSiPosition is initialized (ou dans la méthode ?)
                ArrayList<String[]> possibleKeystream = new ArrayList<>();

                /* Contains bursts from extracted SI (5/5ter/6) */
                ArrayList<String[]> plaintextBursts = new ArrayList<>();

                // get bursts without time advance
                for (int i = 0; i < systemInfo.size(); i++) {

                    String[] frameBursts = new String[6];
                    try {
                        frameBursts = Dedicated.getBursts(systemInfo.get(i)[1], systemInfo.get(i)[0], systemInfo.get(i)[2]);
                        plaintextBursts.add(frameBursts);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

                String[] temp;

                for (int i = 0; i < encryptedSiPosition.size(); i++) {
                    for (int j = 0; j < plaintextBursts.size(); j++) {
                        //System.out.println("plaintext frame number = " + Integer.parseInt(plaintextBursts.get(j)[4]));
                        //System.out.println("with encrypted  = " + encryptedSiPosition.get(i)[2]);
                        if (encryptedSiPosition.get(i)[0].equals(plaintextBursts.get(j)[4])) {
                            // WITH SOME OTHER INFORMATIONS
                            try {
                                temp = General.xorBursts(plaintextBursts.get(j),
                                        Dedicated.getBurstsFromFn(encryptedSiPosition.get(i)[2]),
                                        "know : " + plaintextBursts.get(j)[4],
                                        "enc : " + encryptedSiPosition.get(i)[2]
                                );
                                for (int jr = 0; jr < temp.length; jr++) // TODO : delete debug 
                                {
                                    System.out.println("temp " + jr + " = " + temp[jr]);
                                }
                                possibleKeystream.add(temp);
                            } catch (NumberFormatException e2) {
                                localCmd.append(START_LINE + "Sorry, an error happened while trying to extract keystream. \n");
                                localCmd.update(localCmd.getGraphics());
                            }
                            /**
                             * ******** WITHOUT OTHER INFORMATIONS
                             * *************
                             */
                            /* temp = General.xorBursts(plaintextBursts.get(j),
                             Dedicated.getBurstsFromFn(encryptedSiPosition.get(i)[2]));
                             /**
                             * ***********************************************
                             */

                        }

                    }
                }

                localCmd.append(START_LINE + "Getting possible keystreams is done.\n");
                localCmd.update(localCmd.getGraphics());
                General.writeFileWithArray(possibleKeystream, file.getAbsolutePath() + "_POSSIBLE_KEYSTREAMs", 2);
            }
        });
        btnGetPossibleKeystream.setEnabled(false);

        /**
         * EXTRACT SI PLAINTEXT BUTTON
         */
        JButton btnExtractSiPlaintext = new JButton("Extract SI plaintext version");
        btnExtractSiPlaintext.setBounds(57, 109, 223, 25);
        btnExtractSiPlaintext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                localCmd.append(START_LINE + "Start getting SI plaintext, please wait...\n");
                localCmd.update(localCmd.getGraphics());
                ArrayList<String[]> tempSiPosition = Dedicated.getSysInfo(dedicatedChannelTab);
                if (tempSiPosition.isEmpty()) {
                    localCmd.append(START_LINE + "Sorry but no SI plaintext have been discovered.\n");
                    localCmd.append(START_LINE + "Maybe you should sniff longer the GSM tower.\n");
                    localCmd.update(localCmd.getGraphics());
                } else {
                    for (int i = 0; i < tempSiPosition.size(); i++) {
                        localCmd.append(START_LINE + tempSiPosition.get(i)[2] + " found, fn : " + tempSiPosition.get(i)[0] + " frame (hex) : "
                                + tempSiPosition.get(i)[1] + "\n");
                        localCmd.update(localCmd.getGraphics());
                    }
                }
                // save plaintext SI found
                General.writeFileWithArray(tempSiPosition, file.getAbsolutePath() + "_PLAINTEXT_SI", 1);

                localCmd.append("debug : " + "Getting SI plaintext is done.\n");
                localCmd.update(localCmd.getGraphics());
            }
        });
        btnExtractSiPlaintext.setEnabled(false);

        /**
         * BOUTON EXTRACT SI POSITIONS
         */
        /*	JButton btnExtractSiPositions = new JButton("Extract SI positions");
         btnExtractSiPositions.setBounds(57, 109, 170, 25);
         btnExtractSiPositions.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
				
         ArrayList<String[]> tempSiPosition = Dedicated.findSysInfo(dedicatedChannelTab);
		
         // Et on les enregistre TODO : demander à l'utilisateur si il souhaite enregistrer dans un fichier
         General.ecrireCfileWithTab(tempSiPosition, fichier.getAbsolutePath() + "_SI_POS");
         // TODO : signaler qu'on a arrêté à l'utilisateur
         for(int i = 0 ; i < tempSiPosition.size() ; i++) {
         localCmd.append(DEB_LIGNE + "fn : " + tempSiPosition.get(i)[0] + " frame (hex) : " 
         + tempSiPosition.get(i)[1]);
         localCmd.update(localCmd.getGraphics());
										
         }
         }
         });
         btnExtractSiPositions.setEnabled(false);*/
        /**
         * OPEN CFILE BUTTON
         */
        JButton btnOuvrirCfile = new JButton("Open a file ...");
        btnOuvrirCfile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cfile_file.setVisible(true);
                int returnVal = cfile_file.showOpenDialog(Principal.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    setFile(cfile_file.getSelectedFile());
                    
                    if (JOptionPane.showConfirmDialog(null, "Is the file is a cfile ?", "Cfile or Binary file",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                        == JOptionPane.YES_OPTION) {
                    } else {
                        localCmd.append(START_LINE + "Converting the binary file into a cfile, please wait...\n");
                        localCmd.update(localCmd.getGraphics());
                        try {
                            General.binToCfile(file);
                        } catch (IOException ex) {
                            localCmd.append(START_LINE + "Sorry, an error happened while trying to converting the file.\n");
                            localCmd.update(localCmd.getGraphics());
                        }
                        
                    }
                    
                    try {
                        localCmd.append(START_LINE + "Getting channel outputs for the cfile, please wait...\n");
                        localCmd.update(localCmd.getGraphics());
                        if (General.alreadyDone(file)) {
                            int dialogResult = JOptionPane.showConfirmDialog(null,
                                    "This file seem to have been already loaded before, would you want to "
                                    + "get airprobe output again ?",
                                    "Notice", 0);
                            if (dialogResult == JOptionPane.YES_OPTION) {
                                General.getAirprobeOutput(file);
                            } else {
                                broadcastChannelTab = Broadcast.lignesToTab(General.readFile(file.getAbsolutePath() + "_" + Configuration.BTSCONF));
                                dedicatedChannelTab = Dedicated.lignesToTab(General.readFile(file.getAbsolutePath() + "_" + timeslot + "S"));

                            }
                        } else {
                            General.getAirprobeOutput(file);
                        }
                    } catch (Exception e1) {
                        localCmd.append(START_LINE + "An error occur when trying to clean the output for dedicated chanel : \n");
                        localCmd.append(START_LINE + e1.getMessage());
                        localCmd.update(localCmd.getGraphics());
                    }

                    // Clean the output file if user wants
                    if (JOptionPane.showConfirmDialog(null, "Do you want to try to correct go.sh output for dedicated Chanel?\nIt will help to analyze GSM datas.", "Correct datas?",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                            == JOptionPane.YES_OPTION) {
                        localCmd.append(START_LINE + "Making dirty cleaner, please wait...\n");
                        localCmd.update(localCmd.getGraphics());
                        ArrayList<String> dedicatedTemp = General.cleanAirprobeOutput(General.readFile(file.getAbsolutePath() + "_" + timeslot + "S"));
                        dedicatedChannelTab = Dedicated.lignesToTab(dedicatedTemp);
                        General.writeFile(dedicatedTemp, file.getAbsolutePath() + "_" + timeslot + "S_Topguw-corrected");

                    } else {
                        // if he doesn't want to clean the output file
                    }
                    // files are good, we can start processing data to other services
                    //  btnExtractSiPositions.setEnabled(true);
                    btnExtractSiPlaintext.setEnabled(true);
                    btnExtractCiphMod.setEnabled(true);
                    btnExtractEncryptedSi.setEnabled(true);
                    btnGetCleanBursts.setEnabled(true);
                    btnGetPossibleKeystream.setEnabled(true);

                    localCmd.append(START_LINE + "Files correctly loaded.\n");
                    localCmd.update(localCmd.getGraphics());

                    lblLoadedFile.setVisible(true);

                }
            }
        });
        btnOuvrirCfile.setBounds(57, 67, 131, 25);

        /**
         * START TO SCAN BUTTON
         */
        JButton btnGo = new JButton("Start");
        btnGo.setBackground(new Color(189, 189, 189));
        btnGo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {
                    if (frequency.equals("")) {

                        localCmd.append(START_LINE + "Scanning for " + mnGsmBand.getSelectedItem().toString() + " towers with "
                                + String.valueOf(sliderGainKal.getValue()) + " of gain, please wait.\n");
                        localCmd.update(localCmd.getGraphics());
                        ArrayList<String[]> cells = new ArrayList<>();
                        try {
                            cells = Kalibrate.getGsmCell(mnGsmBand.getSelectedItem().toString(), String.valueOf(sliderGainKal.getValue()));
                        } catch (Exception e1) {
                            localCmd.append(START_LINE + "An error occur while scanning : \n");
                            localCmd.append(START_LINE + e1.getMessage() + "\n");
                            localCmd.update(localCmd.getGraphics());
                        }

                        if (!(cells.isEmpty())) {
                            // Format frequency 
                            BigDecimal freq;

                            Object[] frequencies = new Object[cells.size()];
                            DecimalFormat df = new DecimalFormat("#########");

                            for (int i = 0; i < cells.size(); i++) {
                                // extracted frequency 

                                freq = new BigDecimal(cells.get(i)[0]);
                                frequencies[i] = df.format(freq) + " " + cells.get(i)[1];

                                localCmd.append(START_LINE + "Cell [freq : " + cells.get(i)[0] + "Hz, power : " + cells.get(i)[1] + "]\n");
                                localCmd.update(localCmd.getGraphics());
                            }
                            String s = (String) JOptionPane.showInputDialog(
                                    frmTopguw,
                                    "Which frequency would you like to sniff ?\n Hz                Power ",
                                    "Customized Dialog",
                                    JOptionPane.PLAIN_MESSAGE, null, frequencies,
                                    "GSM Tower choice");
                            frequency = s.split(" ")[0];
                        } else {
                            localCmd.append(START_LINE + "No gsm tower found, disconnet and reconnect RTL-SDR and try again.\n");
                            localCmd.append(START_LINE + "Maybe kalibrate-rtl is not working correctly too.\n");
                        }

                        // Start sniffing
                    }
                    Process p = General.rtlSdrSnif(System.getProperty("user.dir"), frequency, Integer.toString(sliderGainKal.getValue()), "1e6").start();

                    // Allow user to stop sniffing when he wants to
                    Object[] options = {"Stop sniffing"};
                    int n = JOptionPane.showOptionDialog(frmTopguw,
                            "Sniffing in progress ... ",
                            "Sniffing " + frequency + "Hz",
                            JOptionPane.PLAIN_MESSAGE,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                    p.destroy();
                    p.destroyForcibly();
                    localCmd.append(START_LINE + "Start converting the binary file into a cfile.\n");
                    localCmd.update(localCmd.getGraphics());
                    General.binToCfile(new File(frequency + "_AIRPROBE_OUTPUT_BIN"));
                    localCmd.append(START_LINE + "Bin file converted.\n");

                } catch (IOException ex) {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                }

                localCmd.update(localCmd.getGraphics());

            }

        });
        btnGo.setVisible(
                false);
        btnGo.setBounds(
                650, 30, 108, 25);

        /**
         * SCAN GSM BUTTON
         */
        JButton btnScanGsmCell = new JButton("Scan/sniff GSM cell");

        btnScanGsmCell.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(null, "Do you want to try scan for"
                        + " GSM tower[YES] or sniff a frequency[NO].", "Scan or snif",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                        == JOptionPane.YES_OPTION) {
                    lblGain.setVisible(true);
                    sliderGainKal.setVisible(true);
                    mnGsmBand.setVisible(true);
                    btnGo.setVisible(true);
                } else {
                    String fr = JOptionPane.showInputDialog(frmTopguw,
                            "Which frequency do you want to snif (in Hz) ?");
                    if (General.isInteger(fr) && fr.length() == 9) {
                        frequency = fr;
                        lblGain.setVisible(true);
                        sliderGainKal.setVisible(true);
                        mnGsmBand.setVisible(true);
                        btnGo.setVisible(true);
                        localCmd.append(START_LINE + "Frequency " + fr + " is correctly setup.\n");
                    } else {
                        localCmd.append(START_LINE + "Specified frequency seems not to be valid.\n");
                    }
                    localCmd.update(localCmd.getGraphics());
                }

            }
        }
        );
        btnScanGsmCell.setBounds(57, 30, 131, 25);

        /**
         * CONFIG BUTTON
         */
        JButton btnConfigure = new JButton("Configure");
        btnConfigure.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                JTextField gsmReceivePathChamp = new JTextField(Configuration.gsmReceivePath.toString());
                JTextField gsmFrameCoderPathChamp = new JTextField(Configuration.gsmFrameCoder.toString());
                JTextField gsmDecimalRate = new JTextField(Configuration.DEC_RATE.toString());
                JTextField gsmBroadcastType = new JTextField(Configuration.BTSCONF.toString());
                JPanel config = new JPanel(new GridLayout(0, 1));
                config.add(new JLabel("Path for gsm-receive folder (airprobe):"));
                config.add(gsmReceivePathChamp);

                config.add(new JLabel("Path for gsmframecoder (test) folder :"));
                config.add(gsmFrameCoderPathChamp);
                
                config.add(new JLabel("Decimal rate :"));
                config.add(gsmDecimalRate);
                
                config.add(new JLabel("BTS Broadcast channel conf :"));
                config.add(gsmBroadcastType);                

                int result = JOptionPane.showConfirmDialog(null, config, "Configuration",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    Configuration.gsmReceivePath = new File(gsmReceivePathChamp.getText());
                    Configuration.gsmFrameCoder = new File(gsmFrameCoderPathChamp.getText());
                    Configuration.DEC_RATE = gsmDecimalRate.getText();
                    Configuration.BTSCONF = gsmBroadcastType.getText();
                    localCmd.append(START_LINE + "Configuration has successfuly changed.\n");
                } else {
                }
            }
        });
        btnConfigure.setBounds(650, 67, 107, 25);

        // Add all components to the principal window
        frmTopguw.getContentPane().add(cfile_file);

        frmTopguw.getContentPane().add(btnOuvrirCfile);
        frmTopguw.getContentPane().add(separator);
        frmTopguw.getContentPane().add(btnConfigure);
        frmTopguw.getContentPane().add(btnScanGsmCell);
        frmTopguw.getContentPane().add(sliderGainKal);
        frmTopguw.getContentPane().add(mnGsmBand);
        frmTopguw.getContentPane().add(btnGo);
        //frmTopguw.getContentPane().add(btnExtractSiPositions);
        frmTopguw.getContentPane().add(btnExtractSiPlaintext);
        frmTopguw.getContentPane().add(lblLoadedFile);
        frmTopguw.getContentPane().add(btnExtractCiphMod);
        frmTopguw.getContentPane().add(btnExtractEncryptedSi);
        frmTopguw.getContentPane().add(btnGetCleanBursts);
        frmTopguw.getContentPane().add(btnGetPossibleKeystream);

        // Add scrolling for the main text area (pseudo-shell)
        JScrollPane scrollBar = new JScrollPane(localCmd);
        scrollBar.setPreferredSize(new Dimension(700, 255));
        scrollBar.setBounds(57, 206, 700, 334);
        scrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frmTopguw.getContentPane().add(scrollBar);

        // We check that all process are exited before quit
        frmTopguw.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // TODO : delete 
                // not really a problem, nothing stuck after exit (still if something is working)
                // reassess this
            }
        });

    }

    /**
     * @return the file
     */
    public static File getFichier() {
        return file;
    }

    /**
     * loaded cfile's label
     *
     * @param fichier the fichier to set
     */
    public static void setFile(File fichier) {
        Principal.file = fichier;
        lblLoadedFile.setText(fichier.getName());
        lblLoadedFile.setVisible(true);
    }
}
