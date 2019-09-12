# News

## Topguw v2 development is being started, code and more informations available here : https://github.com/bastienjalbert/topguw2

I'm working on an API that works with GR-GSM (not airprobe anymore), Topguw v1 is really dirty, this API will easily perform gsm analysis in Java and make application creation possible for anybody on the gsm protocol. If you want to help me : contact me, you can find sources here https://github.com/bastienjalbert/topguw_api/ .

# Topguw

I had to change the repository because of some local problems.. I'm sorry to those who forked and/or stared my project. This will be the last for Topguw.
Sorry again for the inconvenience.

### About
Topguw is a small piece of software that I made to help people who want to analyse and then crack GSM. 
Topguw works with airprobe and kalibrate-rtl. 
It takes some steps of the know-plaintext attack vector to GSM automatically (Karsten Nohl).

The actual version is currently in beta, bugs may occur but the software will work with good input data.
V0.1

## How to install
1. Dependencies 
  -  First, you have to install airprobe and gsmframecoder, (airprobe depends on gnu-radio **3.6**, so if you use Kali 2 for example you have 2 solutions -> downgrade to kali 1 or use the zmiana patch).
  -  You should install kalibrate-rtl too (Topguw uses kal command to scan GSM tower).
  -  Not really a dependencies, just to tell you that I use Oracle JDK 8 to run Topguw.

2. Run
  -  From a shell, you can start the jar with "java -jar Topguw.jar" 
  -  Use Netbean to import the project and run from Netbean, main class is Principal.java

## How to use

Watch [the demo](https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=3&cad=rja&uact=8&ved=2ahUKEwjvpIL5kMvkAhUBixoKHYDtA34QtwIwAnoECAkQAQ&url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3D--VIisKqVYk&usg=AOvVaw0YRlSNFXK8focbisF1a7F6) on youtube.

## Disclaimer

Copyright (c) 2015, Bastien Enjalbert All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies,
either expressed or implied, of the FreeBSD Project.
