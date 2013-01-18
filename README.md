# Security with Camel

  Security is everywhere like also for Integration Architectures. Apache Camel framework
and related technologies (WebServices/Rest - Apache CXF, Middleware - Aapche ActiveMQ,
OSGI Multi-containers Apache ServiceMix/Karaf) support different standards JAAS, WS-Security, PKI,
SSL, ... and allow to secure not only the components/endpoints (HTTP(S), CXF, ...) but also
to encrypt/decrypt the payload or authorize access to [camel routes](http://camel.apache.org/security.html).

  This project will explore through a collection of different examples the different possibilities
offered to implement in a secure way Integration Camel projects.

## Component

  As Apache Camel provide a huge list of components, it is almost impossible to explore each one individually
and explain how to secure the different endpoints. Nevertheless, we will focus our efforts on some of them which are the
 more relevants (CXF, Jetty, Mina, FTP, ...) and investigate different approaches (SSL, JAAS, ...)

### CXF - WS-Security & JAAS

  As WebServices rely on HTTP server and HTTP requests, different mechanisms exist to authenticate a user
using security directly managed by a HTTP Server or simply by redirecting the HTTP request to a proxy/server. With
this example, we will use the [WS-Security](http://en.wikipedia.org/wiki/WS-Security) standard. This WS-*
standard allows not only to authenticate a user but also to sign/encrypt SOAP message or attach a security token.

The project contains 2 unit tests which can be used to test locally ws-security with camel using JAAS and [CalBackhandler](http://docs.oracle.com/javase/6/docs/technotes/guides/security/jaas/JAASRefGuide.html)
or JAAS & [Security Module - LoginModule](http://docs.oracle.com/javase/6/docs/api/javax/security/auth/spi/LoginModule.html)

mvn test -Dtest=WebServiceAuthenticateCustomerTest
mvn test -Dtest=WebServiceAuthenticateCustomerUsingRealmTest


Instructions to run the JAAS or JAAS-Realm example
--------------------------------------------------

1) cd camel-cxf-jaas
2) Start camel plugin using the command
    mvn -Pjaas camel:run
3) Open [SoapUI client](http://www.soapui.org/) and create a project (name = camel-ws-security-jaas) using the wsdl (http://127.0.0.1:9090/training/WebService?wsdl)
4) Apply [security](http://www.soapui.org/SOAP-and-WSDL/applying-ws-security.html) by adding a Username (charles/charlespassword) and TimeStamp
   Increase Time to Live to 6000
   PAssword type should be passwordText
5) Configure outgoing traffic
5) Create a SOAP request to getAllCustomers

<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
   <soapenv:Header/>
   <soapenv:Body/>
</soapenv:Envelope>

6) Authenticate the user

<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
   <soap:Header>
      <wsse:Security soap:mustUnderstand="1" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
         <wsu:Timestamp wsu:Id="TS-4">
            <wsu:Created>2013-01-18T11:43:01.841Z</wsu:Created>
            <wsu:Expires>2013-01-18T20:03:01.841Z</wsu:Expires>
         </wsu:Timestamp>
         <wsse:UsernameToken wsu:Id="UsernameToken-3">
            <wsse:Username>charles</wsse:Username>
            <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">charlespassword</wsse:Password>
            <wsse:Nonce EncodingType="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary">8g5+qgpGs4Q/W6AxQRjpLQ==</wsse:Nonce>
            <wsu:Created>2013-01-18T11:43:01.841Z</wsu:Created>
         </wsse:UsernameToken>
      </wsse:Security>
   </soap:Header>
   <soap:Body>
      <ns2:getAllCustomersResponse xmlns:ns2="http://example.fuse.redhat.com/">
         <return>
            <name>Fuse</name>
            <address>FuseSource Office</address>
            <numOrders>87</numOrders>
            <revenue>6824.0</revenue>
            <test>100.0</test>
            <type>BUSINESS</type>
         </return>
      </ns2:getAllCustomersResponse>
   </soap:Body>
</soap:Envelope>

7) Repeat 2) to 6) but using now mvn -Pjaas-realm camel:run


### CXF - REST

### Jetty - SSL

## Payload



## Route

