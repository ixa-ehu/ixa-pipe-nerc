package ixa.pipe.nerc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

public class Dictionaries {
  
  InputStream allFile;
  InputStream locationFile;
  InputStream organizationFile;
  InputStream peopleFile;
  
  InputStream knownLocationFile;
  InputStream knownOrganizationFile;
  InputStream knownPeopleFile;
  
  public final Set<String> all = new HashSet<String>();
  public final Set<String> location = new HashSet<String>();
  public final Set<String> organization = new HashSet<String>();
  public final Set<String> person = new HashSet<String>();
  public final Set<String> knownLocation = new HashSet<String>();
  public final Set<String> knownOrganization = new HashSet<String>();
  public final Set<String> knownPerson = new HashSet<String>();
  
  
  private void loadAllList(InputStream file) throws IOException {
    LineIterator lineIterator = IOUtils.lineIterator(file, "UTF-8");
    try {
      while (lineIterator.hasNext()) {
        String line = lineIterator.nextLine();
        all.add(line.trim());
      }
    } finally {
      LineIterator.closeQuietly(lineIterator);
    }
  }
  
  private void loadLocationList(InputStream file) throws IOException {
    LineIterator lineIterator = IOUtils.lineIterator(file, "UTF-8");
    try {
      while (lineIterator.hasNext()) {
        String line = lineIterator.nextLine();
        location.add(line.toLowerCase().trim());
      }
    } finally {
      LineIterator.closeQuietly(lineIterator);
    }
  }
  
  private void loadKnownLocationList(InputStream file) throws IOException {
    LineIterator lineIterator = IOUtils.lineIterator(file, "UTF-8");
    try {
      while (lineIterator.hasNext()) {
        String line = lineIterator.nextLine();
        knownLocation.add(line.toLowerCase().trim());
      }
    } finally {
      LineIterator.closeQuietly(lineIterator);
    }
  }
  
  private void loadOrganizationList(InputStream file) throws IOException {
    LineIterator lineIterator = IOUtils.lineIterator(file, "UTF-8");
    try {
      while (lineIterator.hasNext()) {
        String line = lineIterator.nextLine();
        organization.add(line.toLowerCase().trim());
      }
    } finally {
      LineIterator.closeQuietly(lineIterator);
    }
  }
  
  private void loadKnownOrganizationList(InputStream file) throws IOException {
    LineIterator lineIterator = IOUtils.lineIterator(file, "UTF-8");
    try {
      while (lineIterator.hasNext()) {
        String line = lineIterator.nextLine();
        knownOrganization.add(line.toLowerCase().trim());
      }
    } finally {
      LineIterator.closeQuietly(lineIterator);
    }
  }
 
  
  private void loadPeopleList(InputStream file) throws IOException {
    LineIterator lineIterator = IOUtils.lineIterator(file, "UTF-8");
    try {
      while (lineIterator.hasNext()) {
        String line = lineIterator.nextLine();
        person.add(line.toLowerCase().trim());
      }
    } finally {
      LineIterator.closeQuietly(lineIterator);
    }
  }
  
  private void loadKnownPeopleList(InputStream file) throws IOException {
    LineIterator lineIterator = IOUtils.lineIterator(file, "UTF-8");
    try {
      while (lineIterator.hasNext()) {
        String line = lineIterator.nextLine();
        knownPerson.add(line.toLowerCase().trim());
      }
    } finally {
      LineIterator.closeQuietly(lineIterator);
    }
  }
  
  public InputStream getAllDictionary(String lang) {
    if (lang.equals("en")) {
      allFile = getClass().getResourceAsStream("/en-all-nes.txt");
    }
    return allFile;
  }
  
  public InputStream getLocationDictionary(String lang) {
    if (lang.equals("en")) {
      locationFile = getClass().getResourceAsStream("/en-wikilocation.lst");
    }
    return locationFile;
  }
  
  public InputStream getKnownLocationDictionary(String lang) {
    if (lang.equals("en")) {
      knownLocationFile = getClass().getResourceAsStream("/en-known-location.txt");
    }
    return knownLocationFile;
  }
  
  
  public InputStream getOrganizationDictionary(String lang) {
    if (lang.equals("en")) {
      organizationFile = getClass().getResourceAsStream("/en-wikiorganization.lst");
    }
    return organizationFile;
  }
  
  public InputStream getKnownOrganizationDictionary(String lang) {
    if (lang.equals("en")) {
      knownOrganizationFile = getClass().getResourceAsStream("/en-known-organization.txt");
    }
    return knownOrganizationFile;
  }
  
  public InputStream getPersonDictionary(String lang) {
    if (lang.equals("en")) {
      peopleFile = getClass().getResourceAsStream("/en-wikipeople.lst");
    }
    return peopleFile;
  }
  
  public InputStream getKnownPersonDictionary(String lang) {
    if (lang.equals("en")) {
      knownPeopleFile = getClass().getResourceAsStream("/en-known-people.txt");
    }
    return knownPeopleFile;
  }

  public Dictionaries(String lang) {
    if (lang.equalsIgnoreCase("en")) {
      allFile = getAllDictionary(lang);
      locationFile = getLocationDictionary(lang);
      organizationFile = getOrganizationDictionary(lang);
      peopleFile = getPersonDictionary(lang);
      knownLocationFile = getKnownLocationDictionary(lang);
      knownOrganizationFile = getKnownOrganizationDictionary(lang);
      knownPeopleFile = getKnownPersonDictionary(lang);
      
      try {
        loadAllList(allFile);
        loadLocationList(locationFile);
        loadOrganizationList(organizationFile);
        loadPeopleList(peopleFile);
        loadKnownLocationList(knownLocationFile);
        loadKnownOrganizationList(knownOrganizationFile);
        loadKnownPeopleList(knownPeopleFile);
      } catch (IOException e) {
        e.getMessage();
      }
    }
  }
}
