package com.stayfinder.backend.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.stayfinder.backend.entity.Hostel;
import com.stayfinder.backend.repository.HostelRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final HostelRepository hostelRepository;

    public DataLoader(HostelRepository hostelRepository) {
        this.hostelRepository = hostelRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        InputStream inputStream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream("data/hostels.csv");

        if (inputStream == null) {
            System.out.println("hostels.csv not found.");
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     inputStream,
                                     StandardCharsets.UTF_8))) {

            // Skip CSV header
            reader.readLine();

            String line;

            // Load existing hostels once
            List<Hostel> existingHostels =
                    hostelRepository.findAll();

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = parseCsvLine(line);

                if (data.length < 15) {
                    System.out.println(
                            "Skipping invalid CSV row: " + line
                    );
                    continue;
                }

                String name = data[0].trim();

                // Check if hostel already exists
                boolean alreadyExists =
                        existingHostels.stream()
                                .anyMatch(hostel ->
                                        hostel.getName() != null
                                                && hostel.getName()
                                                .equalsIgnoreCase(name)
                                );

                if (alreadyExists) {
                    continue;
                }

                Hostel hostel = new Hostel();

                hostel.setName(name);
                hostel.setType(data[1].trim());
                hostel.setGender(data[2].trim());
                hostel.setLocation(data[3].trim());
                hostel.setAddress(data[4].trim());

                hostel.setRent(
                        parseDouble(data[5])
                );

                hostel.setRating(
                        parseDouble(data[6])
                );

                hostel.setRooms(
                        data[7].trim()
                );

                hostel.setAmenities(
                        data[8].trim()
                );

                hostel.setLatitude(
                        parseDouble(data[9])
                );

                hostel.setLongitude(
                        parseDouble(data[10])
                );

                hostel.setImageUrl(
                        data[11].trim()
                );

                hostel.setVerified(
                        parseBoolean(data[12])
                );

                hostel.setAvailable(
                        parseBoolean(data[13])
                );

                hostel.setDescription(
                        data[14].trim()
                );

                hostelRepository.save(hostel);

                existingHostels.add(hostel);

                System.out.println(
                        "Imported hostel: "
                                + hostel.getName()
                );
            }
        }

        System.out.println(
                "CSV data loading completed."
        );
    }

    private String[] parseCsvLine(String line) {

        return line.split(
                ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",
                -1
        );
    }

    private Double parseDouble(String value) {

        try {

            if (value == null ||
                    value.trim().isEmpty()) {

                return null;
            }

            return Double.parseDouble(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }

    private Boolean parseBoolean(String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return null;
        }

        return Boolean.parseBoolean(
                value.trim()
        );
    }
}