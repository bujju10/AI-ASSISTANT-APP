package com.smarttravel.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SearchFreeService {

    @Autowired
    private RealTimeDataService realTimeDataService;

    // Use Nominatim to get place info (free) and Wikidata for structured facts
    public Map<String,String> enrichPlace(String place) {
        Map<String,String> out = new HashMap<>();
        try {
            RestTemplate rt = new RestTemplate();
            String nomUrl = "https://nominatim.openstreetmap.org/search?q=" + URLEncoder.encode(place, StandardCharsets.UTF_8) + "&format=json&limit=1&addressdetails=0";
            ResponseEntity<Object[]> r = rt.getForEntity(nomUrl, Object[].class);
            Object[] bodyArr = r.getBody();
            if (r.getStatusCode().is2xxSuccessful() && bodyArr != null && bodyArr.length>0) {
                Object b = bodyArr[0];
                if (b instanceof java.util.Map<?,?> m) {
                    Object dn = m.get("display_name");
                    Object lat = m.get("lat");
                    Object lon = m.get("lon");
                    if (dn!=null) out.put("display_name", String.valueOf(dn));
                    if (lat!=null) out.put("lat", String.valueOf(lat));
                    if (lon!=null) out.put("lon", String.valueOf(lon));
                    // try to fetch wikidata id if present in extratags
                    Object extr = m.get("extratags");
                    if (extr instanceof java.util.Map<?,?> extrMap) {
                        Object wikidata = extrMap.get("wikidata");
                        if (wikidata != null) out.put("wikidata", String.valueOf(wikidata));
                    }
                }
            }

            // If we have a wikidata id, fetch short description
            if (out.containsKey("wikidata")) {
                String wd = out.get("wikidata");
                String wdUrl = "https://www.wikidata.org/wiki/Special:EntityData/"+URLEncoder.encode(wd, StandardCharsets.UTF_8)+".json";
                ResponseEntity<java.util.Map> wdResp = rt.getForEntity(wdUrl, java.util.Map.class);
                if (wdResp.getStatusCode().is2xxSuccessful() && wdResp.getBody()!=null) {
                    java.util.Map<?,?> bodyMap = wdResp.getBody();
                    Object entitiesObj = bodyMap.get("entities");
                    if (entitiesObj instanceof java.util.Map<?,?> entities) {
                        Object entObj = entities.get(wd);
                        if (entObj instanceof java.util.Map<?,?> ent) {
                            Object descsObj = ent.get("descriptions");
                            if (descsObj instanceof java.util.Map<?,?> descs) {
                                Object enObj = descs.get("en");
                                if (enObj instanceof java.util.Map<?,?> en) {
                                    Object val = en.get("value");
                                    if (val!=null) out.put("wikidata_description", String.valueOf(val));
                                }
                            }
                        }
                    }
                }
            }

            // Add real-time data if we have coordinates
            if (out.containsKey("lat") && out.containsKey("lon")) {
                addRealTimeData(out, rt);
            }

        } catch (Exception ex) {
            System.err.println("enrich error: " + ex.getMessage());
        }
        return out;
    }

    private void addRealTimeData(Map<String,String> placeData, RestTemplate rt) {
        try {
            String lat = placeData.get("lat");
            String lon = placeData.get("lon");
            String placeName = placeData.get("display_name");
            
            // Get enhanced real-time data
            Map<String, String> realTimeData = realTimeDataService.getRealTimeData(lat, lon, placeName);
            placeData.putAll(realTimeData);
            
        } catch (Exception e) {
            System.err.println("Real-time data error: " + e.getMessage());
            // Add fallback data
            placeData.put("current_time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            placeData.put("traffic_note", "Real-time traffic data available with API integration");
            placeData.put("weather_note", "Weather data available with API key");
        }
    }

    // Enhanced method to get comprehensive place information
    public Map<String,String> getComprehensivePlaceInfo(String place) {
        Map<String,String> info = enrichPlace(place);
        
        // Add additional context
        info.put("query", place);
        info.put("enriched_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Add travel tips based on place type
        String displayName = info.get("display_name");
        if (displayName != null) {
            if (displayName.toLowerCase().contains("airport")) {
                info.put("travel_tip", "Airport detected - consider flight delays, security wait times, and ground transportation options");
            } else if (displayName.toLowerCase().contains("station")) {
                info.put("travel_tip", "Train station detected - check train schedules and platform information");
            } else if (displayName.toLowerCase().contains("hospital")) {
                info.put("travel_tip", "Medical facility detected - consider emergency access routes and parking availability");
            } else if (displayName.toLowerCase().contains("university") || displayName.toLowerCase().contains("college")) {
                info.put("travel_tip", "Educational institution detected - consider student traffic patterns and parking restrictions");
            } else {
                info.put("travel_tip", "General location - check local traffic patterns and parking availability");
            }
        }
        
        return info;
    }
}
