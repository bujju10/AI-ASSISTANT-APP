package com.smarttravel.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class SearchFreeService {

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
        } catch (Exception ex) {
            System.err.println("enrich error: " + ex.getMessage());
        }
        return out;
    }
}
