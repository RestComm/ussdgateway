# RestComm USSD Gateway
Enables web developers to build server side interactive messaging apps for mobile phones over SS7 infrastructure.

## Introduction 

USSD stands for Unstructured Supplementary Service Data what is a capability of GSM mobile phone much like the Short Message Service (SMS)

USSD information is sent from mobile handset directly to an application platform handling service. USSD establishes a real time session between mobile handsets and the application handling a service. The concept of real time session is very useful for constructing an interactive menu driven application.

RestComm USSD Gateway is built on [RestComm SS7](https://github.com/RestComm/jss7) and RestComm JSLEE Server. It offers RESTful HTTP interface that allows web developers to build interactive apps for feature mobile phones.

## License

RestComm USSD Gateway is licensed under dual license policy. The default license is the Free Open Source [GNU Affero GPL v3.0](http://www.gnu.org/licenses/agpl-3.0.html). Alternatively a commercial license can be obtained from Telestax ([contact form](http://www.telestax.com/contactus/#InquiryForm))

RestComm USSD Gateway is lead by [TeleStax, Inc.](www.telestax.com) and developed collaboratively by a [community of individual and enterprise contributors](http://www.telestax.com/open-source-2/acknowledgments/).


## Downloads

Download binary from [here](https://github.com/RestComm/ussdgateway/releases) or Continuous Delivery builds from [CloudBees](https://mobicents.ci.cloudbees.com/job/Mobicents-USSD-Gateway/)

## Maven Repository

Artifacts are available at [Sonatype Maven Repo](https://oss.sonatype.org/content/repositories/releases/org/mobicents) which are also synched to central

## Wiki

Read our [RestComm jSS7 wiki](https://github.com/RestComm/ussdgateway/wiki) 

## All Open Source RestComm Projects

Open Source http://telestax.com/open-source-2/

# Testing 
To test USSD Gateway with RestComm ss7-simulator make sure you follow the below configuration changes and the execute SS7 Command's

1) Copy folder ussd-RestComm-jainslee-x.y.z.FINAL-jboss-5.1.0.GA/extra/RestComm-ss7/ss7/RestComm-ss7-service and paste to ussd-RestComm-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/jboss-5.1.0.GA/server/default/deploy
 
2) Copy below Resource Adaptors from ussd-RestComm-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/resources/ and paste to ussd-RestComm-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/jboss-5.1.0.GA/server/default/deploy

	a) map/RestComm-slee-ra-map-du-1.0.0.CR3.jar
	b) http-client/http-client-ra-DU-2.5.0.FINAL.jar
	c) jdbc/RestComm-slee-ra-jdbc-DU-1.0.0.FINAL.jar

3) If you are deploying USSD Gateway from source code, set JBOSS_HOME variable to point to "ussd-RestComm-jainslee-2.6.0.FINAL-jboss-5.1.0.GA/jboss-5.1.0.GA" and execute "mvn clean install" command, it should create "RestComm-ussd-gateway" and ussdhttpdemo.war in /deploy folder of JBoss. 

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

6) Assuming RestComm ss7-simulator is started already you should see on jboss console "10:21:28,046 WARN  [SccpStackImpl-SccpStack] Rx : MTP-RESUME: AffectedDpc=2" indicating that USSD Gateway M3UA layer is now connected with ss7-simulator

7) Dial *519# on ss7-simulator and you should see USSD getting exchanged between simulator and server
