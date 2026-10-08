package com.stayfinder.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stayfinder.backend.entity.Hostel;
import com.stayfinder.backend.repository.HostelRepository;
import com.stayfinder.backend.service.GeocodingService;

@RestController
@RequestMapping("/api/geocoding")
@CrossOrigin(origins = "*")
public class GeocodingController {

    private final HostelRepository hostelRepository;
    private final GeocodingService geocodingService;

    public GeocodingController(
            HostelRepository hostelRepository,
            GeocodingService geocodingService) {

        this.hostelRepository = hostelRepository;
        this.geocodingService = geocodingService;
    }

    @PostMapping("/update")
    public String updateLocations() {

        List<Hostel> hostels = hostelRepository.findAll();

        int updated = 0;
        int notFound = 0;

        for (Hostel hostel : hostels) {

            // Skip properties that already have GPS coordinates
            if (hostel.getLatitude() != null &&
                    hostel.getLongitude() != null) {
                continue;
            }

            boolean found = false;

            try {

                /*
                 * ------------------------------------------------
                 * TRY 1
                 * Full address + property name
                 * ------------------------------------------------
                 */
                String address = clean(hostel.getAddress());
                String name = clean(hostel.getName());

                if (address != null && !address.isBlank()) {

                    String searchText =
                            name + ", " + address;

                    System.out.println();
                    System.out.println(
                            "Searching GPS for: "
                                    + searchText
                    );

                    double[] coordinates =
                            geocodingService
                                    .getCoordinates(searchText);

                    if (coordinates != null) {

                        saveCoordinates(
                                hostel,
                                coordinates
                        );

                        updated++;
                        found = true;

                        System.out.println(
                                "GPS FOUND: "
                                        + hostel.getName()
                                        + " -> "
                                        + coordinates[0]
                                        + ", "
                                        + coordinates[1]
                        );
                    }
                }

                /*
                 * ------------------------------------------------
                 * TRY 2
                 * Address only
                 * ------------------------------------------------
                 */
                if (!found &&
                        address != null &&
                        !address.isBlank()) {

                    System.out.println(
                            "Trying address only for: "
                                    + hostel.getName()
                    );

                    double[] coordinates =
                            geocodingService
                                    .getCoordinates(address);

                    if (coordinates != null) {

                        saveCoordinates(
                                hostel,
                                coordinates
                        );

                        updated++;
                        found = true;

                        System.out.println(
                                "GPS FOUND: "
                                        + hostel.getName()
                                        + " -> "
                                        + coordinates[0]
                                        + ", "
                                        + coordinates[1]
                        );
                    }
                }

                /*
                 * IMPORTANT:
                 *
                 * We do NOT use only "Madhapur",
                 * "Gachibowli", "Kondapur", etc.
                 *
                 * That could give a locality centre,
                 * not the actual hostel.
                 */

                if (!found) {

                    notFound++;

                    System.out.println(
                            "GPS NOT FOUND: "
                                    + hostel.getName()
                    );
                }

                // Keep requests slow and respectful
                Thread.sleep(1100);

            } catch (Exception e) {

                notFound++;

                System.out.println(
                        "Geocoding failed for "
                                + hostel.getName()
                                + ": "
                                + e.getMessage()
                );
            }
        }

        return "Geocoding completed. "
                + "Updated: " + updated
                + ", Not found: " + notFound;
    }

    private void saveCoordinates(
            Hostel hostel,
            double[] coordinates) {

        hostel.setLatitude(coordinates[0]);
        hostel.setLongitude(coordinates[1]);

        hostelRepository.save(hostel);
    }

    private String clean(String text) {

        if (text == null) {
            return null;
        }

        String cleaned = text.trim();

        // Remove surrounding quotes
        cleaned = cleaned.replaceAll("^\"|\"$", "");

        // Remove duplicated quotes
        cleaned = cleaned.replace("\"\"", "\"");

        // Remove extra spaces
        cleaned = cleaned.replaceAll("\\s+", " ");

        return cleaned.trim();
    }
}