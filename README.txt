To test USSD Gateway with mobicents ss7-simulator make sure you follow the below configuration changes and the execute SS7 Command's

1) Copy folder ussd-mobicents-jainslee-x.y.z.FINAL-jboss-5.1.0.GA/extra/mobicents-ss7/ss7/mobicents-ss7-service and paste to ussd-mobicents-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/jboss-5.1.0.GA/server/default/deploy
 
2) Copy below Resource Adaptors from ussd-mobicents-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/resources/ and paste to ussd-mobicents-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/jboss-5.1.0.GA/server/default/deploy

	a) map/mobicents-slee-ra-map-du-1.0.0.CR3.jar
	b) http-client/http-client-ra-DU-2.5.0.FINAL.jar
	c) jdbc/mobicents-slee-ra-jdbc-DU-1.0.0.FINAL.jar

3) Change "localSpc" property in jboss-5.1.0.GA/server/default/deploy/mobicents-ss7-service/META-INF/jboss-beans.xml to 1

4) If you are deploying USSD Gateway from source code, set JBOSS_HOME variable to point to "ussd-mobicents-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/jboss-5.1.0.GA" and execute "mvn clean install" command, it should create "mobicents-ussd-gateway" and ussdhttpdemo.war in /deploy folder of JBoss. 

5) Execute below commands in SS7 CLI to setup SS7 

	a) sctp association create Assoc1 CLIENT 127.0.0.1 2905 127.0.0.1 2904 

	b) m3ua as create AS1 AS mode SE rc 100 traffic-mode loadshare

	c) m3ua asp create ASP1 Assoc1

	d) m3ua as add AS1 ASP1

	e) m3ua route add AS1 2 -1 -1

	f) m3ua asp start ASP1

	g) sccp rsp create 1 2 0 0

	h) sccp rss create 1 2 8 0

6) Assuming mobicents ss7-simulator is started already you should see on jboss console "10:21:28,046 WARN  [SccpStackImpl-SccpStack] Rx : MTP-RESUME: AffectedDpc=2" indicating that USSD Gateway M3UA layer is now connected with ss7-simulator

7) Dial *456# on ss7-simulator and you should see USSD getting exchanged between simulator and server