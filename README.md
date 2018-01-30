# RestComm USSD Gateway
Enables web developers to build server side interactive messaging apps for mobile phones over SS7 infrastructure.

## Introduction 

USSD stands for Unstructured Supplementary Service Data what is a capability of GSM mobile phone much like the Short Message Service (SMS)

USSD information is sent from mobile handset directly to an application platform handling service. USSD establishes a real time session between mobile handsets and the application handling a service. The concept of real time session is very useful for constructing an interactive menu driven application.

RestComm USSD Gateway is built on [RestComm SS7](https://github.com/RestComm/jss7) and RestComm JSLEE Server. It offers RESTful HTTP interface that allows web developers to build interactive apps for feature mobile phones.

## Documentation

Read the [Online SIP Servlets Documentation](http://docs.telestax.com/ussd-homepage/) or it is also contained in the download binary

## Downloads

Download the latest binary from the [Restcomm site Downloads section](https://www.restcomm.com/downloads/), or clone this repo and build from source.

## Want to Contribute ? 

[See our Contributors Guide](https://github.com/RestComm/Restcomm-Core/wiki/Contribute-to-RestComm)

## Issue Tracking and Roadmap

[Issue Tracker](https://github.com/RestComm/sip-servlets/issues)

## Questions ?

Please ask your question on [StackOverflow](http://stackoverflow.com/questions/tagged/restcomm) or the Google [public forum](http://groups.google.com/group/restcomm)

## License

RestComm USSD Gateway is lead by [TeleStax](http://www.telestax.com/), Inc. and developed collaboratively by a community of individual and enterprise contributors.

RestComm USSD Gateway is licensed under dual license policy. The default license is the Free Open Source GNU Affero GPL v3.0. Alternatively a commercial license can be obtained from Telestax ([contact form](http://www.telestax.com/contactus/#InquiryForm))


[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fussdgateway.svg?type=large)](https://app.fossa.io/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fussdgateway?ref=badge_large)

## Continuous Integration and Delivery

[![RestComm USSD Gateway Continuous Job](http://www.cloudbees.com/sites/default/files/Button-Built-on-CB-1.png)](https://mobicents.ci.cloudbees.com/job/RestComm-USSD-Gateway//)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fussdgateway.svg?type=shield)](https://app.fossa.io/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fussdgateway?ref=badge_shield)

## Acknowledgements
[See who has been contributing to RestComm](http://www.telestax.com/opensource/acknowledgments/)

## Maven Repository

Artifacts are available at [Sonatype Maven Repo](https://oss.sonatype.org/content/repositories/releases/org/mobicents) which are also synched to central

## Wiki

Read our [RestComm jSS7 wiki](https://github.com/RestComm/ussdgateway/wiki) 

# Testing 
To test USSD Gateway with RestComm ss7-simulator make sure you follow the below configuration changes and the execute SS7 Command's
Assume you are using USSD GW version 3.0.4

1) Download and extract mobicents-ussd-3.0.4-1601192026.zip form [Sonatype USSD GW Repo](https://mobicents.ci.cloudbees.com/job/RestComm-USSD-Gateway/4/artifact/release/)
 
2) Set JBOSS_HOME to mobicents-ussd-3.0.4/jboss-5.1.0.GA

	2.1) export JBOSS_HOME=/path/to/mobicents-ussd-3.0.4/jboss-5.1.0.GA

3) If you are deploying RestComm USSD Gateway from source code, set JBOSS_HOME variable to point to "jboss-5.1.0.GA/" and execute "mvn clean install" command, it should create "mobicents-ussd-gateway" directory and ussdhttpdemo.war in server/default/deploy/ directory of JBoss. 

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
// USSD Settings

// Remember that USSD demo is not included in release build, so you should make it from source

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

6) Assuming RestComm ss7-simulator is started already you should see on jboss console "10:21:28,046 WARN  [SccpStackImpl-SccpStack] Rx : MTP-RESUME: AffectedDpc=2" indicating that USSD Gateway M3UA layer is now connected with ss7-simulator

7) Dial *519# on ss7-simulator and you should see USSD getting exchanged between simulator and server

8) Use this config for JSS7 [USSD SIM](https://github.com/RestComm/ussdgateway/wiki/SS7-Simulator-Configuration-for-USSD-demo)
