package com.stayfinder.backend.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeocodingService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private long lastRequestTime = 0;

    public double[] getCoordinates(String searchText) throws Exception {

        if (searchText == null || searchText.trim().isEmpty()) {
            return null;
        }

        String cleanedText = cleanText(searchText);

        System.out.println("Finding location for: \"" + cleanedText + "\"");

        // Try 1: Exact name/address + Hyderabad
        double[] coordinates = searchNominatim(
                cleanedText + ", Hyderabad, Telangana, India"
        );

        if (coordinates != null) {
            System.out.println(
                    "GPS found: " +
                    coordinates[0] +
                    ", " +
                    coordinates[1]
            );

            return coordinates;
        }

        // Try 2: Search without Telangana/India
        coordinates = searchNominatim(
                cleanedText + ", Hyderabad"
        );

        if (coordinates != null) {
            System.out.println(
                    "GPS found: " +
                    coordinates[0] +
                    ", " +
                    coordinates[1]
            );

            return coordinates;
        }

        // Try 3: Search only the cleaned text
        coordinates = searchNominatim(cleanedText);

        if (coordinates != null) {
            System.out.println(
                    "GPS found: " +
                    coordinates[0] +
                    ", " +
                    coordinates[1]
            );

            return coordinates;
        }

        System.out.println(
                "GPS not found for: \"" + cleanedText + "\""
        );

        return null;
    }

    private double[] searchNominatim(String query) throws Exception {

        waitForRateLimit();

        String encodedQuery =
                URLEncoder.encode(
                        query,
                        StandardCharsets.UTF_8
                );

        String url =
                "https://nominatim.openstreetmap.org/search"
                        + "?q=" + encodedQuery
                        + "&format=jsonv2"
                        + "&limit=1"
                        + "&countrycodes=in";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header(
                                "User-Agent",
                                "StayFinder/1.0 (development)"
                        )
                        .header(
                                "Accept",
                                "application/json"
                        )
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            System.out.println(
                    "Nominatim returned HTTP status: "
                            + response.statusCode()
            );

            return null;
        }

        JsonNode results =
                objectMapper.readTree(response.body());

        if (!results.isArray() || results.isEmpty()) {
            return null;
        }

        JsonNode firstResult = results.get(0);

        JsonNode latNode = firstResult.get("lat");
        JsonNode lonNode = firstResult.get("lon");

        if (latNode == null || lonNode == null) {
            return null;
        }

        double latitude = latNode.asDouble();
        double longitude = lonNode.asDouble();

        return new double[]{
                latitude,
                longitude
        };
    }

    private String cleanText(String text) {

        String cleaned = text.trim();

        // Remove surrounding quotation marks
        cleaned = cleaned.replaceAll("^\"|\"$", "");

        // Remove repeated quotation marks
        cleaned = cleaned.replace("\"\"", "\"");

        // Remove extra spaces
        cleaned = cleaned.replaceAll("\\s+", " ");

        return cleaned.trim();
    }

    private synchronized void waitForRateLimit()
            throws InterruptedException {

        long currentTime =
                System.currentTimeMillis();

        long elapsed =
                currentTime - lastRequestTime;

        long minimumDelay = 1100;

        if (elapsed < minimumDelay) {

            long waitTime =
                    minimumDelay - elapsed;

            Thread.sleep(waitTime);
        }

        lastRequestTime =
                System.currentTimeMillis();
    }
}