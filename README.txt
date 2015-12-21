To test USSD Gateway with mobicents ss7-simulator make sure you follow the below configuration changes and the execute SS7 Command's

1) Copy folder ussd-mobicents-jainslee-x.y.z.FINAL-jboss-5.1.0.GA/extra/mobicents-ss7/ss7/mobicents-ss7-service and paste to ussd-mobicents-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/jboss-5.1.0.GA/server/default/deploy
 
2) Copy below Resource Adaptors from ussd-mobicents-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/resources/ and paste to ussd-mobicents-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/jboss-5.1.0.GA/server/default/deploy

	a) map/mobicents-slee-ra-map-du-1.0.0.CR3.jar
	b) http-client/http-client-ra-DU-2.5.0.FINAL.jar
	c) jdbc/mobicents-slee-ra-jdbc-DU-1.0.0.FINAL.jar

3) If you are deploying USSD Gateway from source code, set JBOSS_HOME variable to point to "ussd-mobicents-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/jboss-5.1.0.GA" and execute "mvn clean install" command, it should create "mobicents-ussd-gateway" and ussdhttpdemo.war in /deploy folder of JBoss. 

5) Execute below commands in SS7 CLI to setup SS7 

	5.1) sctp association create Assoc1 CLIENT 127.0.0.1 8012 127.0.0.1 8011 

	5.2) m3ua as create AS1 IPSP mode SE ipspType CLIENT rc 101 traffic-mode loadshare

	5.3) m3ua asp create ASP1 Assoc1

	5.4) m3ua as add AS1 ASP1

	5.5) m3ua route add AS1 2 -1 -1

	5.6) m3ua asp start ASP1

	5.7) sccp sap create 1 1 1 2

	5.8) sccp dest create 1 1 2 2 0 255 255

	5.9) sccp rsp create 1 2 0 0

	5.10) sccp rss create 1 2 8 0

//USSD Settings

	5.11) ussd scrule create *519# http://127.0.0.1:8080/ussddemo/test

	5.12) ussd set noroutingruleconfigerrmssg Please dial valid short code

	5.13) ussd set dialogtimeouterrmssg Request timedout please try again

	5.14) ussd set servererrmssg Server error. Please try again later
	
//USSD Settings for PUSH

ussd set scgt 923330053058

ussd set scssn 8

ussd set hlrssn 6

ussd set mscssn 8

ussd set maxmapv 3

6) Assuming mobicents ss7-simulator is started already you should see on jboss console "10:21:28,046 WARN  [SccpStackImpl-SccpStack] Rx : MTP-RESUME: AffectedDpc=2" indicating that USSD Gateway M3UA layer is now connected with ss7-simulator

7) Dial *519# on ss7-simulator and you should see USSD getting exchanged between simulator and server



//SS7 configs to connect to MSC/HLR

sctp association create <assoc-name> <CLIENT | SERVER> <server-name> <peer-ip> <peer-port> <host-ip> <host-port>
sctp association create Assoc2 CLIENT 127.0.0.1 2775 127.0.0.1 2776

m3ua as create <as-name> <AS | SGW | IPSP> mode <SE | DE> ipspType <client | server> rc <routing-context> traffic-mode <traffic mode> 
m3ua as create AS2 IPSP mode SE ipspType client rc 100

m3ua asp create ASP2 Assoc2

m3ua as add AS2 ASP2

m3ua route add AS2 3 -1 -1

//Rule for incoming SCCP message
sccp primary_add create <id> <address-indicator> <point-code> <subsystem-number> <translation-type> <numbering-plan>  
<nature-of-address-indicator> <digits>

sccp primary_add create 1 19 1 8 0 1 4 923330053058 

sccp rule create <id> <mask> <address-indicator> <point-code> <subsystem-number> <translation-type> <numbering-plan>  
<nature-of-address-indicator> <digits> <ruleType> <primary-address-id> <backup-address-id> <loadsharing-algorithm>

sccp rule create 1 K 18 0 8 0 1 4 923330053058 solitary 1 

//Rule for all out going
sccp primary_add create 2 19 3 0 0 1 4 - 
sccp rule create 2 K 18 0 0 0 1 4 * solitary 2 

//sccp rsp create <id> <remote-spc> <rspc-flag> <mask> 
sccp rsp create 2 3 0 0

//sccp rss create <id> <remote-spc> <remote-ssn> <rss-flag> <mark-prohibited-when-spc-resuming>
sccp rss create 2 3 8 0
sccp rss create 3 3 6 0

m3ua asp start ASP2





//Simulator Setting
sctp server create SCTPServer1 127.0.0.1 2775
sctp server start SCTPServer1
sctp association create SCTPAssoc1 SERVER SCTPServer1 127.0.0.1 2776

m3ua as create AS1 IPSP mode SE ipspType server rc 100

m3ua asp create ASP1 SCTPAssoc1

m3ua as add AS1 ASP1

m3ua route add AS1 1 -1 -1

m3ua asp start ASP1

//Rule for outgoing SCCP message
sccp primary_add create <id> <address-indicator> <point-code> <subsystem-number> <translation-type> <numbering-plan> <nature-of-address-indicator> <digits>
sccp primary_add create 1 19 1 8 0 1 4 923330053058

sccp rule create <id> <mask> <address-indicator> <point-code> <subsystem-number> <translation-type> <numbering-plan>  
<nature-of-address-indicator> <digits> <ruleType> <primary-address-id> <backup-address-id> <loadsharing-algorithm>
sccp rule create 1 K 18 0 8 0 1 4 923330053058 solitary 1 

//Rule for all incoming
sccp primary_add create 2 19 3 0 0 1 4 - 
sccp rule create 2 K 18 0 0 0 1 4 * solitary 2

sccp rsp create 1 1 0 0

sccp rss create 1 1 8 0

sccp sap create 1 1 3 2

sccp dest create 1 1 1 1 0 255 255
