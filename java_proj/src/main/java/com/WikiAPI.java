package com;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A utility class to interact with the Wikipedia API to fetch and parse data about specific topics
 */
public class WikiAPI {
    private static final Logger logger = Logger.getLogger(WikiAPI.class.getName());
    private static final String urlStr = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&explaintext=true&redirects=1&titles=";
    private static int counter = 0;

    /**
     * Fetches data from the Wikipedia API for a given query
     *
     * @param query the topic to query in the Wikipedia API
     * @return a JsonObject containing the API response or null if an error occurs
     */
    private static JsonObject fetch(String query) {
        try {
            String encodedQuery = query.replace(" ", "%20");
            URL url = new URL(urlStr + encodedQuery);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonObject data = jsonResponse.getAsJsonObject("query").getAsJsonObject("pages");
            logger.info("WikiAPI data loaded successfully. Request #" + counter);
            counter++;

            return data;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while loading WikiAPI data", e);
            return null;
        }
    }

    /**
     * Checks the fetched data for content and returns the first paragraph if available
     *
     * @param data the JsonObject containing the API response
     * @return the first paragraph of the content or null if the page is missing or no content is found
     */
    private static String checkContentAndRet(JsonObject data) {
        JsonObject firstPage = data.entrySet().iterator().next().getValue().getAsJsonObject();

        if (firstPage.has("missing")) {
            return null;
        }
        return extractFirstParagraph(firstPage);
    }

    /**
     * Extracts the first paragraph from the "extract" field of the API response
     *
     * @param data the JsonObject representing a Wikipedia page
     * @return the first paragraph of the extract or null if not found
     */
    private static String extractFirstParagraph(JsonObject data) {
        if (data.has("extract")) {
            String extract = data.get("extract").getAsString();
            return extract.split("\n")[0];
        }
        return null;
    }

    /**
     * Queries the Wikipedia API for a topic and returns the first paragraph of the response
     *
     * @param query the topic to query in the Wikipedia API
     * @return the first paragraph of the topic or "No data found" if unavailable
     */
    public static String queryWiki(String query) {
        try {
            JsonObject data = fetch(query);
            String res = checkContentAndRet(data);
            if (res == null) {
                return "No data found";
            }
            return res;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while querying WikiAPI", e);
            return null;
        }
    }

    /**
     * Generates a Wikipedia URL for the specified topic
     *
     * @param query the topic to generate the URL for
     * @return the Wikipedia URL of the topic
     */
    public static String getWikiURL(String query) {
        return "https://en.wikipedia.org/wiki/" + query.replace(" ", "_");
    }
}
