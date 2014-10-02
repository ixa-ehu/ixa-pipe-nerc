package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;

public class SentenceFeatureGenerator extends FeatureGeneratorAdapter {

 private final boolean isGenerateFirstWordFeature;
 private final boolean isGenerateLastWordFeature;
 
 public SentenceFeatureGenerator(boolean isGenerateFirstWordFeature,
     boolean isGenerateLastWordFeature) {
   this.isGenerateFirstWordFeature = isGenerateFirstWordFeature;
   this.isGenerateLastWordFeature = isGenerateLastWordFeature;
 }
 
 public void createFeatures(List<String> features, String[] tokens, int index,
     String[] previousOutcomes) {

   if (isGenerateFirstWordFeature && index == 0) {
     features.add("S=begin");
   }

   if (isGenerateLastWordFeature && tokens.length == index + 1) {
     features.add("S=end");
   }
 }

}
