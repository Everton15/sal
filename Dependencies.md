This page explains how to download and install SAL and its dependencies

# Pre-requisites #

In order for SAL to support a sensing technology, required software must be installed prior to compiling SAL. This section lists all currently supported technologies and explains how to obtain and install their dependencies.
**SAL expects to find all JAR files in `/usr/share/java`, except for the SunSPOT SDK (see SunSPOT section below).**

## log4j ##
SAL uses log4j to log debug statements. log4j can be obtained from  http://logging.apache.org/log4j/1.2 . On Debian/Ubuntu, you can simply `sudo apt-get install liblog4j1.2-java` .

## Registration with Bonjour ##
SAL agents register themselves with Bonjour so they can be automatically discovered by clients. SAL uses Avahi4J to handle Bonjour interactions. Avahi4J can be found at http://avahi4j.googlecode.com
Avahi4J must be installed before attempting to build SAL (see "Getting Started" page at Avahi4J website for more information).

## Serial devices ##
Communication with serial devices requires the GNU implementation of the Java Comm API, which can be obtained from [this page](http://rxtx.qbang.org/wiki/index.php/Main_Page). Under Debian/Ubuntu, you can simply `sudo apt-get install librxtx-java`.

## SNMP devices ##
In order to communicate with SNMP devices, SAL requires the Westhawk freeware implementation of the SNMP protocol which can be obtained [here](http://snmp.westhawk.co.uk). **However, there is no need to download the package as it is already shipped with SAL.**

## Video devices ##
In order for SAL to support video streaming, you must install [v4l4j](http://v4l4j.googlecode.com).
  * **Download v4l4j** either from source or binary package (see [Getting started](http://code.google.com/p/v4l4j/wiki/GettingStarted) page)
  * Make sure it is properly installed and configured by running:
```
java -Djava.library.path=/usr/lib/jni -jar /usr/share/java/v4l4j.jar
```
> You should be able to view a video stream from a connected video device. Note that SAL expects to find the JAR file in `/usr/share/java` and the JNI library in `/usr/lib/jni`.

## Hardware Asbtraction Layer ##
In order to detect hardware-related events, SAL uses the HAL java package to connected to the HAL daemon. See [this page](HALpackage.md) for information on how to download and install the HAL java package.

## SunSPOT ##
SunSPOT support requires the installation of the SunSPOT SDK available from [this page](http://www.sunspotworld.com). The SDK can only be downloaded via a Java WebStart application.

## 1-wire device ##
SAL deals with 1-wire devices using the OWFS software available from [this page](http://owfs.org/). Download and install OWFS.