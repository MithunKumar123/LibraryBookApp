package com.app.librarybooks.utils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ExtractJWT {

    public static String payloadJWTExtraction(String token, String extraction){
        token.replace("Bearer", "");
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        String[] payloadDetails = payload.split(",");
        Map<String, String> payloadMap = new HashMap<>();
        for(String entry: payloadDetails){
            String[] keyValue = entry.split(":");
            if(keyValue[0].equalsIgnoreCase(extraction)){
                int remove = 1;
                if(keyValue[1].endsWith("}")){
                    remove = 2;
                }

                keyValue[1] = keyValue[1].substring(0, keyValue[1].length() - remove);
                keyValue[1] = keyValue[1].substring(1);

                payloadMap.put(keyValue[0], keyValue[1]);
            }
        }
        if(payloadMap.containsKey(extraction)){
            return payloadMap.get(extraction);
        }
        return null;
    }

}
