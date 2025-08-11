package com.example.techanalysisapp3.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {
    public static String extractAndFormatUrl(String input) {
        input = input.trim();

        if (input.startsWith("http://") || input.startsWith("https://")) {
            try {
                java.net.URI uri = java.net.URI.create(input);
                String path = uri.getPath();
                if (path != null) {
                    String[] segments = path.split("/");
                    for (String seg : segments) {
                        if (seg.matches("\\d{8}")) {
                            return "https://olx.ba/artikal/" + seg;
                        }
                    }
                }
            } catch (Exception e) {
                // fall through to regex
            }
        }

        Matcher matcher = Pattern.compile("\\b(\\d{8})\\b").matcher(input);
        if (matcher.find()) {
            return "https://olx.ba/artikal/" + matcher.group(1);
        }

        return null;
    }
}