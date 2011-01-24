package com.heychinaski.historyhack.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.heychinaski.historyhack.model.GeoEventPage;

public class JsonInputGEPProvider implements GeoEventPageProvider {
    
    String currentLine = "";
    String nextLine = null;
    private BufferedReader reader;
    
    int currentYear = -1;

    public JsonInputGEPProvider(String filename) {
        try {
            File input = new File(filename);
            reader = new BufferedReader(new FileReader(input));
    
            readNextLine();
        } catch(Exception e) {
            throw new RuntimeException("Couldn't parse file", e);
        }

    }

    private void readNextLine() {
        try {
            nextLine = reader.readLine();
        } catch(Exception e) {
            throw new RuntimeException("Couldn't read line from file", e);
        }
    }
    
    @Override
    public List<GeoEventPage> getNextFrame() {
        
        List<GeoEventPage> pages = new ArrayList<GeoEventPage>();
        
        GeoEventPage nextGeoEventPage = new Gson().fromJson(nextLine, GeoEventPage.class);
        GeoEventPage initialGeoEventPage = nextGeoEventPage;
        
        int year;
        if(initialGeoEventPage != null) {
            year = initialGeoEventPage.getYear();
        } else {
            // empty list of pages
            return pages;
        }
        
        // The page that has come back has a year in advance of the
        // anticipated current year. There was a gap in the data. 
        // increment year and return empty list.
        if(year - currentYear > 1) {
            currentYear ++;
            return pages;
        } else {
            currentYear = year;
        }
        
        // Stop when we run out or change years
        while(nextGeoEventPage != null && initialGeoEventPage.getYear() == nextGeoEventPage.getYear()) {
            pages.add(nextGeoEventPage);
            
            readNextLine();
            if(nextLine != null) {
                nextGeoEventPage = new Gson().fromJson(nextLine, GeoEventPage.class);
            } else {
                nextGeoEventPage = null;
            }
        }
        return pages;
    }

    @Override
    public boolean hasMoreEvents() {
        boolean b = nextLine != null;
        if(!b) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return b;
    }

    @Override
    public int getCurrentYear() {
        return currentYear;
    }

    @Override
    public Map<Integer, List<GeoEventPage>> allPages() {
        Map<Integer, List<GeoEventPage>> allPages = new HashMap<Integer, List<GeoEventPage>>();
        while (this.hasMoreEvents()) {
            List<GeoEventPage> pages = getNextFrame();
            allPages.put(getCurrentYear(), pages);
        }
        System.out.println(allPages.size());
        System.out.println(allPages.get(1865).size());
        return allPages;
    }

}
