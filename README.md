
IXA-pipe-nerc
=============

This module uses Apache OpenNLP programatically to perform Named Entity Recognition.
English models have been trained using CoNLL 2003 dataset (84.80 F1). Spanish models
using CoNLL 2002 dataset (79.92 F1). This module is part of IXA-Pipeline ("is a pipeline"),
a multilingual NLP pipeline developed by the IXA NLP Group (ixa.si.ehu.es).


Contents
========

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module
    + src/                    java source code of the module
    + Furthermore, the installation process, as described in the README.md, will generate another directory:
    target/                 it contains binary executable and other directories


INSTALLATION
============

Installing the ixa-pipe-nerc requires the following steps:

If you already have installed in your machine JDK6 and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

1. Install JDK 1.6
-------------------

If you do not install JDK 1.6 in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java6
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java16
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your jdk is 1.6

2. Install MAVEN 3
------------------

Download MAVEN 3 from

````shell
wget http://apache.rediris.es/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz
````

Now you need to configure the PATH. For Bash Shell:

````shell
export MAVEN_HOME=/home/ragerri/local/apache-maven-3.0.5
export PATH=${MAVEN_HOME}/bin:${PATH}
````

For tcsh shell:

````shell
setenv MAVEN3_HOME ~/local/apache-maven-3.0.5
setenv PATH ${MAVEN3}/bin:{PATH}
````

If you re-login into your shell and run the command

````shell
mvn -version
````

You should see reference to the MAVEN version you have just installed plus the JDK 6 that is using.

3. Get module source code
--------------------------

````shell
git clone git@github.com:ixa-ehu/ixa-pipe-tok.git
````

4. Download Models
------------------

You will need to download the trained models and copy them to ixa-pipe-nerc/src/main/resources/
for the module to work properly.

Download the models and untar the archive into the src/main/resources directory:

````shell
cd ixa-pipe-nerc/src/main/resources
wget http://ixa2.si.ehu.es/ragerri/ixa-pipeline-models/nerc-resources.tgz
tar xvzf nerc-resources.tgz
````
Note that if you change the name of the models you will need to modify also the source code in Models.java.

5. Move into main directory
---------------------------

````shell
cd ixa-pipe-nerc
````

6. Install module using maven
-----------------------------

````shell
mvn clean package
````

This step will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

ixa-pipe-nerc-1.0.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.6 installed.

To install the module in the local maven repository, usually located in ~/.m2/, execute:

````shell
mvn clean install
````

7. USING ixa-pipe-nerc
=========================

The program takes KAF documents (with <wf> and <term> elements) as standard input and outputs KAF.

http://kyoto-project.eu/www2.let.vu.nl/twiki/pub/Kyoto/TechnicalPapers/WP002_TR009_KAF_Framework.pdf

You can get the necessary input for ixa-pipe-nerc by running ixa-pipe-tok and ixa-pipe-pos in a pipeline.

To run the program execute:

````shell
cat wfterms.kaf | java -jar $PATH/target/ixa-pipe-nerc-1.0.jar -l $lang
````

GENERATING JAVADOC
==================

You can also generate the javadoc of the module by executing:

````shell
mvn javadoc:jar
````

Which will create a jar file core/target/ixa-pipe-nerc-1.0-javadoc.jar


Contact information
===================

````shell
Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
rodrigo.agerri@ehu.es
````
