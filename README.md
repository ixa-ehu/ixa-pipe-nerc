
ixa-pipe-nerc
=============

ixa-pipe-nerc is multilingual Named Entity Recognition and Classification tagger. 
ixa-pipe-nerc is part of IXA pipes, a multilingual NLP pipeline developed 
by the IXA NLP Group [http://ixa2.si.ehu.es/ixa-pipes]. 

Please go to [http://ixa2.si.ehu.es/ixa-pipes] for general information about the IXA
pipes tools but also for **official releases, including source code and binary
packages for all the tools in the IXA pipes toolkit**.

This document is intended to be the **usage guide of ixa-pipe-nerc**. If you really need to clone
and install this repository instead of using the releases provided in
[http://ixa2.si.ehu.es/ixa-pipes], please scroll down to the end of the document for
the [installation instructions](#installation).

## TABLE OF CONTENTS

1. [Overview of ixa-pipe-nerc](#overview)
  + [Available features](#features)
  + [List of distributed models](#models)
2. [Usage of ixa-pipe-nerc](#usage)
  + [NERC tagging](#tagging)
  + [Training your own models](#training)
  + [Evaluation](#evaluation)

## OVERVIEW

ixa-pipe-nerc provides NERC English, Spanish, Dutch, German and Italian. The named entity types are based on:

+ **CONLL**: LOCATION, MISC, ORGANIZATION and PERSON. See [CoNLL 2002](http://www.clips.ua.ac.be/conll2002/ner/)
and [CoNLL 2003](http://www.clips.ua.ac.be/conll2003/ner/) for more information. 
+ **ONTONOTES 4.0**: 18 Named Entity types: TIME, LAW, GPE, NORP, LANGUAGE,
PERCENT, FACILITY, PRODUCT, ORDINAL, LOCATION, PERSON, WORK_OF_ART, MONEY, DATE, EVENT, QUANTITY, ORGANIZATION, CARDINAL.

We provide very fast models trained on local features only, similar to those of Zhang and Johnson (2003) with several differences: We do not use POS
tags, chunking or gazetteers in our baseline models but we do use
bigrams, trigrams and character ngrams. We also provide some models with
external knowledge (with the "dict" keyword in its properties file). 
To avoid duplication of efforts, we use the machine
learning API provided by the [Apache OpenNLP project](http://opennlp.apache.org).

**ixa-pipe-nerc models and resources**: 

  + The [nerc-resources.tgz](http://ixa2.si.ehu.es/ixa-pipes/models/nerc-resources.tgz) package, which contains **every model and resource** available.

### Features

**A description of every feature is provided in the trainParams.prop properties
file** distributed with ixa-pipe-nerc. As most functionality is configured in
properties files, please do check this document. For each model distributed,
there is a prop file which describes the training of the model.

### Models

We distribute the following models

+ **English Models**: we offer a variety of Perceptron based models (Collins 2002):
  
  + **CoNLL 2003 models**: We distribute models trained with local features
  (84.53 F1) and with external knowledge (87.11 F1). Furthermore, we also
  distribute opennlp compatible models (check for "opennlp" in the props
  files).
 
  + **Ontonotes 4.0**: 
    + Trained on the **full corpus** with the **18 NE types**, suitable **for production use**.
    + **Using 5K sentences at random for testset** from the corpus and leaving the rest (90K
      aprox) for training.
      + Ontonotes CoNLL 4 NE types with local features: F1 86.21
      + Ontonotes 3 NE types with local features: F1 89.41

+ **Spanish Models**: we obtained better results overall with Maximum Entropy
  models (Ratnapharki 1999). The best results are obtained when a c0 (cutoff 0)
  is used, but those models are slower for production than when a c4 (cutoff 4)
  is used. Therefore, we provide both types for opennlp and local features

  + CoNLL 2002 opennlp cutoff 0: F1 80.01
  + CoNLL 2002 opennlp cutoff 4: F1 77.85
  + CoNLL 2002 local features cutoff 0: F1 80.25
  + CoNLL 2002 local features cuttoff 4: F1 79.73

+ **Dutch Models**: 
  + CoNLL 2002 local features: F1 79.40

+ **German Models**: We distribute the following CoNLL02 models:
  + CoNLL 2003 local features: F1 71.93

+ **Italian Models**: Currently we distribute models trained with Evalita07 and Evalita09: 
  + Evalita07 local features: F1 70.79
  + Evalita09 local features: F1 74.97

**Summarizing**, and even though the best way of knowing which model to use is
to try them, for production use, we recommend using: 
  + English CoNLL 03 with dict features or if speed is required English CoNLL
  2003 with local features.
  + Spanish local features with cutoff 4 model.

## USAGE

ixa-pipe-nerc provides 3 basic functionalities:

1. **tag**: reads a NAF document containing *wf* and *term* elements and tags named
   entities.
2. **train**: trains new model for English or Spanish with several options
   available.
3. **eval**: evaluates a trained model with a given test set.

Each of these functionalities are accessible by adding (tag|train|eval) as a
subcommand to ixa-pipe-nerc-$version.jar. Please read below and check the -help
parameter: 

````shell
java -jar target/ixa-pipe-nerc-$version.jar (tag|train|eval) -help
````
**Every option for tagging, training and evaluation is well
documented in the trainParams.prop properties file distributed with
ixa-pipe-nerc**. Please do read that file!! 

Also remember that the [nerc-resources.tgz](http://ixa2.si.ehu.es/ixa-pipes/models/nerc-resources.tgz)
package contains **every model and properties file** available. 

### Tagging 

If you are in hurry, just execute: 

````shell
cat file.txt | ixa-pipe-tok | ixa-pipe-pos | java -jar $PATH/target/ixa-pipe-nerc-$version.jar tag -p paramsFile.prop
````

If you want to know more, please follow reading.

ixa-pipe-nerc reads NAF documents (with *wf* and *term* elements) via standard input and outputs NAF
through standard output. The NAF format specification is here:

(http://wordpress.let.vupr.nl/naf/)

You can get the necessary input for ixa-pipe-nerc by piping 
[ixa-pipe-tok](https://github.com/ixa-ehu/ixa-pipe-tok) and 
[ixa-pipe-pos](https://github.com/ixa-ehu/ixa-pipe-pos) as shown in the
example. 

There are several options to tag with ixa-pipe-nerc. In addition to passing the
properties file via the -p parameter, there is a CLI swith to use rule based tagging:

+ **lexer**: switches on the rule-based DFA for NERC tagging. Currently we only provide
  one option **numeric**, which identifies "numeric entities" such as DATE,
  TIME, MONEY and PERCENT for all the languages currently in ixa-pipe-nerc.

**Example**: 

````shell
cat file.txt | ixa-pipe-tok | ixa-pipe-pos | java -jar $PATH/target/ixa-pipe-nerc-$version.jar tag -p nerc-resources/en/en-local-conll03-testa.prop
````

### Training

To train a new model, you just need to pass a training parameters file as an
argument. As it has been already said, the options are documented in the
template trainParams.prop file. 

**Example**:

````shell
java -jar target/ixa.pipe.nerc-1.0.jar train -p trainParams.txt
````

### Evaluation

As for the training option, the eval only requires to pass the appropriate
properties file:  

**Example**:

````shell
java -jar target/ixa.pipe.nerc-$version.jar eval -p nerc-resources/en/en-local-conll03-testa.prop
````

## JAVADOC

It is possible to generate the javadoc of the module by executing:

````shell
cd ixa-pipe-nerc/
mvn javadoc:jar
````

Which will create a jar file core/target/ixa-pipe-nerc-$version-javadoc.jar

## Module contents

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module
    + src/                    java source code of the module and required resources
    + Furthermore, the installation process, as described in the README.md, will generate another directory:
    target/                 it contains binary executable and other directories
    + trainParams.prop      A template properties file containing documention
    for every available option


## INSTALLATION

Installing the ixa-pipe-nerc requires the following steps:

If you already have installed in your machine the Java 1.7+ and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

### 1. Install JDK 1.7

If you do not install JDK 1.7 in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java7
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java17
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your JDK is 1.7

### 2. Install MAVEN 3

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

You should see reference to the MAVEN version you have just installed plus the JDK 7 that is using.

### 3. Get module source code

If you must get the module source code from here do this:

````shell
git clone https://github.com/ixa-ehu/ixa-pipe-nerc
````

### 4. Compile

````shell
cd ixa-pipe-nerc
mvn clean package
````

This step will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

ixa-pipe-nerc-$version.jar

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
rodrigo.agerri@ehu.es
````
