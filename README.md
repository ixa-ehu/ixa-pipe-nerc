
ixa-pipe-nerc
=============
[![Build Status](https://travis-ci.org/ixa-ehu/ixa-pipe-nerc.svg?branch=master)](https://travis-ci.org/ixa-ehu/ixa-pipe-nerc)
[![GitHub license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/apache/opennlp/master/LICENSE)

ixa-pipe-nerc is a multilingual Named Entity tagger developed within the IXA pipes tools  [http://ixa2.si.ehu.es/ixa-pipes]. **Current version is 2.0.0**

**Please cite this paper** if you use the tagger:

R. Agerri, G. Rigau, Robust multilingual Named Entity Recognition with shallow semi-supervised features. Artificial Intelligence, 238 (2016) 63-82. (http://dx.doi.org/10.1016/j.artint.2016.05.003)

Please go to (http://ixa2.si.ehu.es/ixa-pipes) for general information about the IXA pipes tools but also for **official releases, including source code and binary packages for all the tools in the IXA pipes toolkit**.

This document is intended to be the **usage guide of ixa-pipe-nerc**. If you really need to clone
and install this repository instead of using the releases provided in
[http://ixa2.si.ehu.es/ixa-pipes], please scroll down to the end of the document for
the [installation instructions](#installation).

ixa-pipe-nerc is in [Maven Central](http://search.maven.org/) for easy access to its API.

## TABLE OF CONTENTS

1. [Overview of ixa-pipe-nerc](#overview)
  + [Available features](#features)
  + [NERC distributed models](#nerc-models)
2. [Usage of ixa-pipe-nerc](#cli-usage)
  + [NERC tagging](#tagging)
  + [Server mode](#server)
3. [API via Maven Dependency](#api)
4. [Git installation](#installation)

## OVERVIEW

ixa-pipe-nerc provides models for Named Entity Recognition for Basque, Dutch, English, Galician, German, Italian and Spanish. The named entity types are based on:
   + **CONLL**: LOCATION, MISC, ORGANIZATION and PERSON. See [CoNLL 2002](http://www.clips.ua.ac.be/conll2002/ner/) and [CoNLL 2003](http://www.clips.ua.ac.be/conll2003/ner/) for more information.
   + **Evalita 2009**: for Italian, LOCATION, GPE, ORGANIZATION and PERSON.
   + **SONAR-1**: for Dutch, six main types, including CoNLL types plus PRODUCT and EVENT.
   + **Ancora**: for Spanish, six main types, including CoNLL types plus DATE and NUMBER.

We provide competitive models based on robust local features and exploiting unlabeled data
via clustering features. The clustering features are based on Brown, Clark (2003)
and Word2Vec clustering plus some gazetteers in some cases.
To avoid duplication of efforts, we use and contribute to the API provided by the
[Apache OpenNLP project](http://opennlp.apache.org) with our own custom developed features for each of the three tasks.

### NERC-Models

These models are to be used with the official [IXA pipes 1.1.1 distribution](https://ixa2.si.ehu.es/ixa-pipes).

**Reproducing results with conlleval**:
Every result reported in Agerri and Rigau (2016) can be reproduced with the [conlleval script](http://www.cnts.ua.ac.be/conll2002/ner/bin/conlleval.txt) using the [conlleval-results](http://ixa2.si.ehu.es/ixa-pipes/models/results-conlleval.tar.gz) scripts and the ixa-pipe-nerc contained in the [IXA pipes 1.1.1 distribution](https://ixa2.si.ehu.es/ixa-pipes).

**NERC models**:

  + **Release 1.5.4** [685MB]: [nerc-models-latest.tgz](http://ixa2.si.ehu.es/ixa-pipes/models/nerc-models-1.5.4.tgz)

Every model is trained with the averaged Perceptron algorithm as described in (Collins 2002) and as implemented
in Apache OpenNLP.

+ **Basque**: eu-clusters model, trained on egunkaria dataset, F1 76.72 on 3 class evaluation and F1 75.40 on 4 classes.

+ **English Models**:

  + **CoNLL 2003 models**: We distribute models trained with local features
  and with external knowledge. Each of the models improve in F1 (reported on testb data)
  but they get somewhat slower:
    + CoNLL 2003 local + brown features: F1 88.50
    + CoNLL 2003 local + clark features: F1 88.97
    + CoNLL 2003 clusters + dicts: F1 91.36

  + **Combined models**: trained using Ontonotes 4.0, conll03 and muc 7 data, good for out of domain usage.

+ **Spanish Models**:

  + CoNLL 2002 clusters: F1 84.16
  + CoNLL 2002 clusters + dict: F1 84.30

+ **Dutch Models**:
  + CoNLL 2002 clusters: F1 84.23
  + CoNLL 2002 clusters + dict: F1 84.91

+ **German Models**:
  + CoNLL 2003 clusters + dict: F1 76.42

+ **Italian Models**:
  + Evalita09 clusters: F1 80.38

## CLI-USAGE

ixa-pipe-nerc provides a runable jar with the following command-line basic functionalities:

1. **server**: starts a TCP service loading the model and required resources.
2. **client**: sends a NAF document to a running TCP server.
3. **tag**: reads a NAF document containing *wf* and *term* elements and tags named
   entities.

Each of these functionalities are accessible by adding (server|client|tag) as a
subcommand to ixa-pipe-nerc-${version}-exec.jar. Please read below and check the -help
parameter:

````shell
java -jar target/ixa-pipe-nerc-${version}-exec.jar (tag|server|client) -help
````

### Tagging

If you are in hurry, just execute:

````shell
cat file.txt | java -jar target/ixa-pipe-tok-$version-exec.jar tok -l en | java -jar ixa-pipe-pos-1.5.0-exec.jar tag -m en-pos-perceptron-autodict01-conll09.bin -lm en-lemma-perceptron-conll09.bin | java -jar $PATH/target/ixa-pipe-nerc-${version}-exec.jar tag -m model.bin
````

If you want to know more, please follow reading.

ixa-pipe-nerc reads NAF documents (with *wf* and *term* elements) via standard input and outputs NAF
through standard output. The NAF format specification is here:

(http://wordpress.let.vupr.nl/naf/)

You can get the necessary input for ixa-pipe-nerc by piping
[ixa-pipe-tok](https://github.com/ixa-ehu/ixa-pipe-tok) and
[ixa-pipe-pos](https://github.com/ixa-ehu/ixa-pipe-pos) as shown in the
example.

There are several options to tag with ixa-pipe-nerc:

+ **model**: pass the model as a parameter.
+ **language**: pass the language as a parameter.
+ **outputFormat**: Output annotation in a format: available CoNLL03, CoNLL02, and NAF. It defaults to NAF.
+ **lexer**: switches on the rule-based DFA for NERC tagging. Currently we only provide
  one option **numeric**, which identifies "numeric entities" such as DATE,
  TIME, MONEY and PERCENT for all the languages currently in ixa-pipe-nerc.
+ **dictTag**: directly tag named entities contained in a gazetteer.
  + **tag**: with tag option, only dictionary entities are annotated.
  + **post**: with post option, the output of the statistical model is
    post-processed.
+ **dictPath**: the directory containing the gazetteers for the --dictTag
  option.

**Example**:

````shell
cat file.txt | java -jar target/ixa-pipe-tok-$version-exec.jar tok -l en | java -jar ixa-pipe-pos-1.5.0-exec.jar tag -m en-pos-perceptron-autodict01-conll09.bin -lm en-lemma-perceptron-conll09.bin | java -jar $PATH/target/ixa-pipe-nerc-${version}-exec.jar tag -m nerc-models-$version/en/en-local-conll03.bin
````

### Server

We can start the TCP server as follows:

````shell
java -jar target/ixa-pipe-nerc-${version}-exec.jar server -l en --port 2060 -m en-model-conll03.bin
````
Once the server is running we can send NAF documents containing (at least) the term layer like this:

````shell
 cat file.pos.naf | java -jar target/ixa-pipe-nerc-${version}-exec.jar client -p 2060
````

## API

The easiest way to use ixa-pipe-nerc programatically is via Apache Maven. Add
this dependency to your pom.xml:

````shell
<dependency>
    <groupId>eus.ixa</groupId>
    <artifactId>ixa-pipe-nerc</artifactId>
    <version>1.6.0</version>
</dependency>
````

## JAVADOC

The javadoc of the module is located here:

````shell
ixa-pipe-nerc/target/ixa-pipe-nerc-$version-javadoc.jar
````

## Module contents

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module
    + src/                    java source code of the module and required resources
    + Furthermore, the installation process, as described in the README.md, will generate another directory:
    target/                 it contains binary executable and other directories

## INSTALLATION

Installing the ixa-pipe-nerc requires the following steps:

If you already have installed in your machine the Java 1.8+ and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

### 1. Install JDK 1.8

If you do not install JDK 1.8+ in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java8
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java18
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your JDK is 1.8.

### 2. Install MAVEN 3

Download MAVEN 3.3.9+ from

````shell
https://maven.apache.org/download.cgi
````
Now you need to configure the PATH. For Bash Shell:

````shell
export MAVEN_HOME=/home/ragerri/local/apache-maven-3.3.9
export PATH=${MAVEN_HOME}/bin:${PATH}
````

For tcsh shell:

````shell
setenv MAVEN3_HOME ~/local/apache-maven-3.3.9
setenv PATH ${MAVEN3}/bin:{PATH}
````

If you re-login into your shell and run the command

````shell
mvn -version
````

You should see reference to the MAVEN version you have just installed plus the JDK that is using.

### 3. Get module source code

If you must get the module source code from here do this:

````shell
git clone https://github.com/ixa-ehu/ixa-pipe-nerc
````

### 4. Compile

Execute this command to compile ixa-pipe-nerc:

````shell
cd ixa-pipe-nerc
mvn clean package
````
This step will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

ixa-pipe-nerc-${version}-exec.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.7 installed.

To install the module in the local maven repository, usually located in ~/.m2/, execute:

````shell
mvn clean install
````

## Contact information

````shell
Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
rodrigo.agerri@ehu.eus
````
