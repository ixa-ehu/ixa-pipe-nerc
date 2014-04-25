
package es.ehu.si.ixa.pipe.nerc.train;

import es.ehu.si.ixa.pipe.nerc.Dictionary;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.BigramNameFeatureGenerator;
import opennlp.tools.util.featuregen.CachedFeatureGenerator;
import opennlp.tools.util.featuregen.OutcomePriorFeatureGenerator;
import opennlp.tools.util.featuregen.PreviousMapFeatureGenerator;
import opennlp.tools.util.featuregen.SentenceFeatureGenerator;
import opennlp.tools.util.featuregen.SuffixFeatureGenerator;
import opennlp.tools.util.featuregen.TokenClassFeatureGenerator;
import opennlp.tools.util.featuregen.TokenFeatureGenerator;
import opennlp.tools.util.featuregen.WindowFeatureGenerator;

/**
 * Training NER based on Apache OpenNLP Machine Learning API 
 * This class implements Gazetteer features as explained in 
 * Ratinov, Lev and Dan Roth. Design Challenges and Misconceptions in 
 * Named Entity Recognition. In CoNLL 2009  
 * 
 * @author ragerri 2014/03/19
 * 
 */

public class DictLbjNameFinderTrainer extends AbstractNameFinderTrainer {

  Dictionary cardinalNumber;
  Dictionary currencyFinal;
  Dictionary knownCorporations;
  Dictionary knownCountry;
  Dictionary knownJobs;
  Dictionary knownName;
  Dictionary knownNamesBig;
  Dictionary knownNationalities;
  Dictionary knownPlace;
  Dictionary knownState;
  Dictionary knownTitle;
  Dictionary measurements;
  Dictionary ordinalNumber;
  Dictionary temporalWords;
  Dictionary wikiArtWork;
  Dictionary wikiArtWorkRedirects;
  Dictionary wikiCompetitionsBattlesEvents;
  Dictionary wikiCompetitionsBattlesEventsRedirects;
  Dictionary wikiFilms;
  Dictionary wikiFilmsRedirects;
  Dictionary wikiLocations;
  Dictionary wikiLocationRedirects;
  Dictionary wikiManMadeObjectNames;
  Dictionary wikiManMadeObjectNamesRedirects;
  Dictionary wikiOrganizations;
  Dictionary wikiOrganizationsRedirects;
  Dictionary wikiPeople;
  Dictionary wikiPeopleRedirects;
  Dictionary wikiSongs;
  Dictionary wikiSongsRedirects;
  
  
  public DictLbjNameFinderTrainer(String trainData, String testData, String lang, int beamsize, String corpusFormat, String netypes) throws IOException {
    super(trainData,testData,lang,beamsize,corpusFormat, netypes);
    
    InputStream cardinalNumberFile = getClass().getResourceAsStream("/lbj/cardinalNumber.txt");
    cardinalNumber = new Dictionary(cardinalNumberFile);
    InputStream currencyFinalFile = getClass().getResourceAsStream("/lbj/currencyFinal.txt");
    currencyFinal = new Dictionary(currencyFinalFile);
    InputStream knownCorporationsFile = getClass().getResourceAsStream("/lbj/known_corporations.lst");
    knownCorporations = new Dictionary(knownCorporationsFile);
    InputStream knownCountryFile = getClass().getResourceAsStream("/lbj/known_country.lst");
    knownCountry = new Dictionary(knownCountryFile);
    InputStream knownJobsFile = getClass().getResourceAsStream("/lbj/known_jobs.lst");
    knownJobs = new Dictionary(knownJobsFile);
    InputStream knownNameFile = getClass().getResourceAsStream("/lbj/known_name.lst");
    knownName = new Dictionary(knownNameFile);
    InputStream knownNameBigFile = getClass().getResourceAsStream("/lbj/known_names.big.lst");
    knownNamesBig = new Dictionary(knownNameBigFile);
    InputStream knownNationalitiesFile = getClass().getResourceAsStream("/lbj/known_nationalities.lst");
    knownNationalities = new Dictionary(knownNationalitiesFile);
    InputStream knownPlaceFile = getClass().getResourceAsStream("/lbj/known_place.lst");
    knownPlace = new Dictionary(knownPlaceFile);
    InputStream knownStateFile = getClass().getResourceAsStream("/lbj/known_state.lst");
    knownState = new Dictionary(knownStateFile);
    InputStream knownTitleFile = getClass().getResourceAsStream("/lbj/known_title.lst");
    knownTitle = new Dictionary(knownTitleFile);
    InputStream measurementsFile = getClass().getResourceAsStream("/lbj/measurments.txt");
    measurements = new Dictionary(measurementsFile);
    InputStream ordinalNumberFile = getClass().getResourceAsStream("/lbj/ordinalNumber.txt");
    ordinalNumber = new Dictionary(ordinalNumberFile);
    InputStream temporalWordsFile = getClass().getResourceAsStream("/lbj/temporal_words.txt");
    temporalWords = new Dictionary(temporalWordsFile);
    InputStream wikiArtWorkFile = getClass().getResourceAsStream("/lbj/WikiArtWork.lst");
    wikiArtWork = new Dictionary(wikiArtWorkFile);
    InputStream wikiArtWorkRedirectsFile = getClass().getResourceAsStream("/lbj/WikiArtWorkRedirects.lst");
    wikiArtWorkRedirects = new Dictionary(wikiArtWorkRedirectsFile);
    InputStream wikiCompetitionsBattlesEventsFile = getClass().getResourceAsStream("/lbj/WikiCompetitionsBattlesEvents.lst");
    wikiCompetitionsBattlesEvents = new Dictionary(wikiCompetitionsBattlesEventsFile);
    InputStream wikiCompetitionsBattlesEventsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiCompetitionsBattlesEventsRedirects.lst");
    wikiCompetitionsBattlesEventsRedirects = new Dictionary(wikiCompetitionsBattlesEventsRedirectsFile);
    InputStream wikiFilmsFile = getClass().getResourceAsStream("/lbj/WikiFilms.lst");
    wikiFilms = new Dictionary(wikiFilmsFile);
    InputStream wikiFilmsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiFilmsRedirects.lst");
    wikiFilmsRedirects = new Dictionary(wikiFilmsRedirectsFile);
    InputStream wikiLocationsFile = getClass().getResourceAsStream("/lbj/WikiLocations.lst");
    wikiLocations = new Dictionary(wikiLocationsFile);
    InputStream wikiLocationsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiLocationsRedirects.lst");
    wikiLocationRedirects = new Dictionary(wikiLocationsRedirectsFile);
    InputStream wikiManMadeObjectNamesFile = getClass().getResourceAsStream("/lbj/WikiManMadeObjectNames.lst");
    wikiManMadeObjectNames = new Dictionary(wikiManMadeObjectNamesFile);
    InputStream wikiManMadeObjectNamesRedirectsFile = getClass().getResourceAsStream("/lbj/WikiManMadeObjectNamesRedirects.lst");
    wikiManMadeObjectNamesRedirects = new Dictionary(wikiManMadeObjectNamesRedirectsFile);
    InputStream wikiOrganizationsFile = getClass().getResourceAsStream("/lbj/WikiOrganizations.lst");
    wikiOrganizations = new Dictionary(wikiOrganizationsFile);
    InputStream wikiOrganizationsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiOrganizationsRedirects.lst");
    wikiOrganizationsRedirects = new Dictionary(wikiOrganizationsRedirectsFile);
    InputStream wikiPeopleFile = getClass().getResourceAsStream("/lbj/WikiPeople.lst");
    wikiPeople = new Dictionary(wikiPeopleFile);
    InputStream wikiPeopleRedirectsFile = getClass().getResourceAsStream("/lbj/WikiPeopleRedirects.lst");
    wikiPeopleRedirects = new Dictionary(wikiPeopleRedirectsFile);
    InputStream wikiSongsFile = getClass().getResourceAsStream("/lbj/WikiSongs.lst");
    wikiSongs = new Dictionary(wikiSongsFile);
    InputStream wikiSongsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiSongsRedirects.lst");
    wikiSongsRedirects = new Dictionary(wikiSongsRedirectsFile);
    
    features = createFeatureGenerator();
    
  }
  
  public DictLbjNameFinderTrainer(int beamsize) {
    super(beamsize);
    
    InputStream cardinalNumberFile = getClass().getResourceAsStream("/lbj/cardinalNumber.txt");
    cardinalNumber = new Dictionary(cardinalNumberFile);
    InputStream currencyFinalFile = getClass().getResourceAsStream("/lbj/currencyFinal.txt");
    currencyFinal = new Dictionary(currencyFinalFile);
    InputStream knownCorporationsFile = getClass().getResourceAsStream("/lbj/known_corporations.lst");
    knownCorporations = new Dictionary(knownCorporationsFile);
    InputStream knownCountryFile = getClass().getResourceAsStream("/lbj/known_country.lst");
    knownCountry = new Dictionary(knownCountryFile);
    InputStream knownJobsFile = getClass().getResourceAsStream("/lbj/known_jobs.lst");
    knownJobs = new Dictionary(knownJobsFile);
    InputStream knownNameFile = getClass().getResourceAsStream("/lbj/known_name.lst");
    knownName = new Dictionary(knownNameFile);
    InputStream knownNameBigFile = getClass().getResourceAsStream("/lbj/known_names.big.lst");
    knownNamesBig = new Dictionary(knownNameBigFile);
    InputStream knownNationalitiesFile = getClass().getResourceAsStream("/lbj/known_nationalities.lst");
    knownNationalities = new Dictionary(knownNationalitiesFile);
    InputStream knownPlaceFile = getClass().getResourceAsStream("/lbj/known_place.lst");
    knownPlace = new Dictionary(knownPlaceFile);
    InputStream knownStateFile = getClass().getResourceAsStream("/lbj/known_state.lst");
    knownState = new Dictionary(knownStateFile);
    InputStream knownTitleFile = getClass().getResourceAsStream("/lbj/known_title.lst");
    knownTitle = new Dictionary(knownTitleFile);
    InputStream measurementsFile = getClass().getResourceAsStream("/lbj/measurments.txt");
    measurements = new Dictionary(measurementsFile);
    InputStream ordinalNumberFile = getClass().getResourceAsStream("/lbj/ordinalNumber.txt");
    ordinalNumber = new Dictionary(ordinalNumberFile);
    InputStream temporalWordsFile = getClass().getResourceAsStream("/lbj/temporal_words.txt");
    temporalWords = new Dictionary(temporalWordsFile);
    InputStream wikiArtWorkFile = getClass().getResourceAsStream("/lbj/WikiArtWork.lst");
    wikiArtWork = new Dictionary(wikiArtWorkFile);
    InputStream wikiArtWorkRedirectsFile = getClass().getResourceAsStream("/lbj/WikiArtWorkRedirects.lst");
    wikiArtWorkRedirects = new Dictionary(wikiArtWorkRedirectsFile);
    InputStream wikiCompetitionsBattlesEventsFile = getClass().getResourceAsStream("/lbj/WikiCompetitionsBattlesEvents.lst");
    wikiCompetitionsBattlesEvents = new Dictionary(wikiCompetitionsBattlesEventsFile);
    InputStream wikiCompetitionsBattlesEventsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiCompetitionsBattlesEventsRedirects.lst");
    wikiCompetitionsBattlesEventsRedirects = new Dictionary(wikiCompetitionsBattlesEventsRedirectsFile);
    InputStream wikiFilmsFile = getClass().getResourceAsStream("/lbj/WikiFilms.lst");
    wikiFilms = new Dictionary(wikiFilmsFile);
    InputStream wikiFilmsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiFilmsRedirects.lst");
    wikiFilmsRedirects = new Dictionary(wikiFilmsRedirectsFile);
    InputStream wikiLocationsFile = getClass().getResourceAsStream("/lbj/WikiLocations.lst");
    wikiLocations = new Dictionary(wikiLocationsFile);
    InputStream wikiLocationsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiLocationsRedirects.lst");
    wikiLocationRedirects = new Dictionary(wikiLocationsRedirectsFile);
    InputStream wikiManMadeObjectNamesFile = getClass().getResourceAsStream("/lbj/WikiManMadeObjectNames.lst");
    wikiManMadeObjectNames = new Dictionary(wikiManMadeObjectNamesFile);
    InputStream wikiManMadeObjectNamesRedirectsFile = getClass().getResourceAsStream("/lbj/WikiManMadeObjectNamesRedirects.lst");
    wikiManMadeObjectNamesRedirects = new Dictionary(wikiManMadeObjectNamesRedirectsFile);
    InputStream wikiOrganizationsFile = getClass().getResourceAsStream("/lbj/WikiOrganizations.lst");
    wikiOrganizations = new Dictionary(wikiOrganizationsFile);
    InputStream wikiOrganizationsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiOrganizationsRedirects.lst");
    wikiOrganizationsRedirects = new Dictionary(wikiOrganizationsRedirectsFile);
    InputStream wikiPeopleFile = getClass().getResourceAsStream("/lbj/WikiPeople.lst");
    wikiPeople = new Dictionary(wikiPeopleFile);
    InputStream wikiPeopleRedirectsFile = getClass().getResourceAsStream("/lbj/WikiPeopleRedirects.lst");
    wikiPeopleRedirects = new Dictionary(wikiPeopleRedirectsFile);
    InputStream wikiSongsFile = getClass().getResourceAsStream("/lbj/WikiSongs.lst");
    wikiSongs = new Dictionary(wikiSongsFile);
    InputStream wikiSongsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiSongsRedirects.lst");
    wikiSongsRedirects = new Dictionary(wikiSongsRedirectsFile);
    
    features = createFeatureGenerator();
  }
  
  public AdaptiveFeatureGenerator createFeatureGenerator() {
    return new CachedFeatureGenerator(new AdaptiveFeatureGenerator[] {
        new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
        new OutcomePriorFeatureGenerator(), new PreviousMapFeatureGenerator(),
        new BigramNameFeatureGenerator(),
        new SentenceFeatureGenerator(true, false),
        new Prefix34FeatureGenerator(),
        new SuffixFeatureGenerator(),
        new DictionaryFeatures("CARDINAL",cardinalNumber),
        new DictionaryFeatures("CURRENCY",currencyFinal),
        new DictionaryFeatures("CORPORATIONS",knownCorporations),
        new DictionaryFeatures("COUNTRY",knownCountry),
        new DictionaryFeatures("JOBS",knownJobs),
        new DictionaryFeatures("NAME",knownName),
        new DictionaryFeatures("NAMESBIG",knownNamesBig),
        new DictionaryFeatures("NATIONALITIES",knownNationalities),
        new DictionaryFeatures("PLACE",knownPlace),
        new DictionaryFeatures("STATE",knownState),
        new DictionaryFeatures("TITLE",knownTitle),
        new DictionaryFeatures("MEASURES",measurements),
        new DictionaryFeatures("ORDINAL",ordinalNumber),
        new DictionaryFeatures("TEMPORALS",temporalWords),
        new DictionaryFeatures("ART",wikiArtWork),
        new DictionaryFeatures("ARTRED",wikiArtWorkRedirects),
        new DictionaryFeatures("COMPETITIONS",wikiCompetitionsBattlesEvents),
        new DictionaryFeatures("COMPETITIONSRED",wikiCompetitionsBattlesEventsRedirects),
        new DictionaryFeatures("FILMS",wikiFilms),
        new DictionaryFeatures("FILMSRED",wikiFilmsRedirects),
        new DictionaryFeatures("LOCATIONS",wikiLocations),
        new DictionaryFeatures("LOCATIONSRED",wikiLocationRedirects),
        new DictionaryFeatures("OBJECTS",wikiManMadeObjectNames),
        new DictionaryFeatures("OBJECTSRED",wikiManMadeObjectNamesRedirects),
        new DictionaryFeatures("ORGANIZATIONS",wikiOrganizations),
        new DictionaryFeatures("ORGANIZATIONSRED",wikiOrganizationsRedirects),
        new DictionaryFeatures("PEOPLE",wikiPeople),
        new DictionaryFeatures("PEOPLERED",wikiPeopleRedirects),
        new DictionaryFeatures("SONGS",wikiSongs),
        new DictionaryFeatures("SONGSRED",wikiSongsRedirects)
        });
    }
   
}
