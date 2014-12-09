
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

**NOTICE!!**: ixa-pipe-nerc is now in [Maven Central](http://search.maven.org/)
for easy access to its API.

## TABLE OF CONTENTS

1. [Overview of ixa-pipe-nerc](#overview)
  + [Available features](#features)
  + [List of distributed models](#models)
2. [Usage of ixa-pipe-nerc](#cli-usage)
  + [NERC tagging](#tagging)
  + [Training your own models](#training)
  + [Evaluation](#evaluation)
3. [API via Maven Dependency](#api)
4. [Git installation](#installation)

## OVERVIEW

ixa-pipe-nerc provides NERC English, Spanish, Dutch, German and Italian. The named entity types are based on:

+ **CONLL**: LOCATION, MISC, ORGANIZATION and PERSON. See [CoNLL 2002](http://www.clips.ua.ac.be/conll2002/ner/)
and [CoNLL 2003](http://www.clips.ua.ac.be/conll2003/ner/) for more information. 
+ **ONTONOTES 4.0**: 18 Named Entity types: TIME, LAW, GPE, NORP, LANGUAGE,
PERCENT, FACILITY, PRODUCT, ORDINAL, LOCATION, PERSON, WORK_OF_ART, MONEY, DATE, EVENT, QUANTITY, ORGANIZATION, CARDINAL.

The models are self-contained, that is, the prop files are not needed to use them. 
You will find for each model a prop and a log file. The log file
describes the training process that was performed. The prop file is used for training only. Please see the
traininParams.prop template file to all available training options and
documentation. 

We provide fast models trained on local features only: We do not use POS
tags, chunking or gazetteers in our baseline models but we do use
bigrams, trigrams and character ngrams. For English we also provide some models with
external knowledge, based on Brown and Clark and Word2Vec clustering, and dictionaries. 
To avoid duplication of efforts, we use and contribute to the machine learning API provided 
by the [Apache OpenNLP project](http://opennlp.apache.org).

### Features

**A description of every feature is provided in the trainParams.prop properties
file** distributed with ixa-pipe-nerc. As the training functionality is configured in
properties files, please do check this document. For each model distributed,
there is a prop file which describes the training of the model, as well as a
log file which provides details about the evaluation and training process.

### Models

**ixa-pipe-nerc models and resources**: 

  + **Latest models**: [nerc-models-latest.tgz](http://ixa2.si.ehu.es/ixa-pipes/models/nerc-models-1.3.3.tgz)
  + Release 3.2 models: [nerc-models-$version.tgz](http://ixa2.si.ehu.es/ixa-pipes/models/nerc-models-1.3.2.tgz)
  + The [nerc-resources.tgz](http://ixa2.si.ehu.es/ixa-pipes/models/nerc-resources.tgz)
  package, which contains **every resource** required to train the English models
  with clustering and dictionary features.

All models are trained with the averaged Perceptron algorithm as
described in (Collins 2002):

+ **English Models**: 
  
  + **CoNLL 2003 models**: We distribute models trained with local features
  with external knowledge. Furthermore, we also
  distribute opennlp compatible models (look for "opennlp" in the model name). 
  Each of the models improve in F1 but they get slower: 
    + CoNLL 2003 local features: F1 83.35
    + CoNLL 2003 Brown and Clark clusters as features: F1 89.76
    + CoNLL 2003 Brown, Clark and dictionaries: F1 90.29
 
+ **Spanish Models**: 

  + CoNLL 2002 local features: F1 79.50

+ **Dutch Models**: 
  + CoNLL 2002 local features: F1 78.13

+ **German Models**: 
  + CoNLL 2003 local features: F1 71.62

+ **Italian Models**: 
  + Evalita07 local features: F1 70.93
  + Evalita09 local features: F1 74.43

## CLI-USAGE

ixa-pipe-nerc provides 3 command-line basic functionalities:

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
**Every option for training is documented in the trainParams.prop properties file distributed with
ixa-pipe-nerc**. Please do read that file!! 

### Tagging 

If you are in hurry, just execute: 

````shell
cat file.txt | ixa-pipe-tok | ixa-pipe-pos | java -jar $PATH/target/ixa-pipe-nerc-$version.jar tag -m model.bin
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
+ **outputFormat**: Output annotation in a format: available CoNLL03, CoNLL02,
  OpenNLP native format and NAF. It defaults to NAF.
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
cat file.txt | ixa-pipe-tok | ixa-pipe-pos | java -jar $PATH/target/ixa-pipe-nerc-$version.jar tag -m nerc-models-$version/en/en-local-conll03.bin
````

### Training

To train a new model, you just need to pass a training parameters file as an
argument. As it has been already said, the options are documented in the
template trainParams.prop file. 

**Example**:

````shell
java -jar target/ixa.pipe.nerc-1.0.jar train -p trainParams.prop
````
**Training with Features using External Resources**: For training with dictionary or clustering
based features (Brown, Clark and Word2Vec) you need to pass the lexicon as
value of the respective feature in the prop file. This is only for training, as
for tagging or evaluation the model is serialized with all resources included.

### Evaluation

You can evaluate a trained model or a prediction data against a reference data
or testset. 

+ **language**: provide the language.
+ **model**: if evaluating a model, pass the model.
+ **testset**: the testset or reference set.
+ **corpusFormat**: the format of the reference set and of the prediction set
  if --prediction option is chosen.
+ **prediction**: evaluate against a  prediction corpus instead of against a
  model.
+ **evalReport**: detail of the evaluation report
  + **brief**: just the F1, precision and recall scores
  + **detailed**, the F1, precision and recall per class
  + **error**: the list of false positives and negatives

**Example**:

````shell
java -jar target/ixa.pipe.nerc-$version.jar eval -m nerc-models-$version/en/en-local-conll03.bin -l en -t conll03.testb
````

## API

The easiest way to use ixa-pipe-nerc programatically is via Apache Maven. Add
this dependency to your pom.xml:

````shell
<dependency>
    <groupId>es.ehu.si.ixa</groupId>
    <artifactId>ixa-pipe-nerc</artifactId>
    <version>1.3.3</version>
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

Execute this command to compile ixa-pipe-nerc:

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
