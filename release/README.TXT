===========================
 Mobicents USSD Gateway
===========================
               v@VERSION@

 Welcome to the Mobicents SS7 Simulator v@VERSION@ release!

--------------
 Introduction
--------------

 This is a standalone bundle of USSD Gateway. It consists of following elements:
  * USSD Gateway
  * SS7 Simulator
  * SS7 Service
  * Simple USSD application
  * JSLEE container
 

------------------
 Release Contents
------------------

  * USSD Gateway v@VERSION@
  * SS7 Simulator v@SIMULATOR_VERSION@
  * SS7 v@SS7_VERSION@
  * JSLEE v@JSLEE_VERSION@ 
  

  
------------------
SVN Trunk Checkout
------------------

http://mobicents.googlecode.com/svn/trunk/applications/ss7-simulator
http://mobicents.googlecode.com/svn/trunk/applications/ussdgateway
http://mobicents.googlecode.com/svn/trunk/protocols/ss7
http://mobicents.googlecode.com/svn/trunk/servers/jain-slee

------------------
SVN Trunk Browse
------------------

http://code.google.com/p/mobicents/source/browse/#svn/trunk/applications/ss7-simulator
http://code.google.com/p/mobicents/source/browse/#svn/trunk/applications/ussdgateway
http://code.google.com/p/mobicents/source/browse/#svn/trunk/protocols/ss7
http://code.google.com/p/mobicents/source/browse/#svn/trunk/servers/jain-slee  
  
------------------
 SVN Tag Checkout
------------------

http://mobicents.googlecode.com/svn/tags/applications/ss7-simulator/@SIMULATOR_VERSION@
http://mobicents.googlecode.com/svn/tags/applications/ussdgateway/@VERSION@
http://mobicents.googlecode.com/svn/tags/protocols/ss7/ss7-@SS7_VERSION@
http://mobicents.googlecode.com/svn/tags/servers/jain-slee/2.x.y/

  
  
---------------
 Important !!  
---------------

 USSD Gateway is JSLEE based application. It is assumed that you are familiar with JSLEE concepts and Resource Adaptors like:
 * MAP
 * SIP
 * HTTP-Client
 * HTTP-Servlet
 
 Also it is assumed that you are familiar with Jboss AS.  
 
 NOTE: this is @VERSION@ release and as such it does not cover corner use cases!!!
  
----------------
 How to Use it?
----------------

 Please refer to specific project documentation. To configure interfaces, like SIP, please refer to JSLEE RA documentation.
 
----------------
 How to run it? 
----------------

 USSD Gateway is predeployed in JSLEE container. To run it, input following command:
 
 './bin/run.sh -b 127.0.0.1 -c ussd'
 
-----------------
 How to run example? 
-----------------

This bundle contains simple USSD application. It can be deployed via script found in example directory. To start the example please specify
following command:
 './bin/run.sh -b 127.0.0.2 -c examples -Djboss.service.binding.set=ports-01'

NOTE: 127.0.0.1 and 127.0.0.2 IPs are to be used for out of the box example experience.


------------------
 More Information
------------------

 For more detailed information, please visit the following sites:

  * Mobicents Google Group, for posting questions (http://groups.google.com/group/mobicents-public)

  * Mobicents Homepage (http://www.mobicents.org)

