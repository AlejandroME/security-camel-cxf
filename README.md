# Security with Camel

Security is everywhere like also for Integration Architectures. Apache Camel framework
and related technologies (WebServices/Rest - Apache CXF, Middleware - Aapche ActiveMQ,
OSGI Multi-containers Apache ServiceMix/Karaf) support different standards JAAS, WS-Security, PKI,
SSL, ... and allow to secure not only the components/endpoints (HTTP(S), CXF, ...) but also
to encrypt/decrypt the payload or authorize access to [camel routes](http://camel.apache.org/security.html).

This project will explore through a collection of different examples the different possibilities
offered to implement in a secure way an Integration Camel project

## Authentication & authorization

Everything about security with Camel &amp; CXF (WS-Security, Spring Security, JAAS, ...)