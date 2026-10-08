package com.stayfinder.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.stayfinder.backend.entity.Hostel;
import com.stayfinder.backend.repository.HostelRepository;

@Service
public class HostelService {

    private final HostelRepository hostelRepository;

    public HostelService(HostelRepository hostelRepository) {
        this.hostelRepository = hostelRepository;
    }

    public List<Hostel> getAllHostels() {
        return hostelRepository.findAll();
    }

    public Optional<Hostel> getHostelById(Long id) {
        return hostelRepository.findById(id);
    }

    public Hostel createHostel(Hostel hostel) {
        return hostelRepository.save(hostel);
    }

    public Hostel updateHostel(Long id, Hostel hostelDetails) {

        Hostel hostel = hostelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hostel not found"));

        hostel.setName(hostelDetails.getName());
        hostel.setType(hostelDetails.getType());
        hostel.setGender(hostelDetails.getGender());
        hostel.setLocation(hostelDetails.getLocation());
        hostel.setAddress(hostelDetails.getAddress());
        hostel.setRent(hostelDetails.getRent());
        hostel.setRating(hostelDetails.getRating());
        hostel.setRooms(hostelDetails.getRooms());
        hostel.setAmenities(hostelDetails.getAmenities());
        hostel.setLatitude(hostelDetails.getLatitude());
        hostel.setLongitude(hostelDetails.getLongitude());
        hostel.setImageUrl(hostelDetails.getImageUrl());
        hostel.setVerified(hostelDetails.getVerified());
        hostel.setAvailable(hostelDetails.getAvailable());
        hostel.setDescription(hostelDetails.getDescription());

        return hostelRepository.save(hostel);
    }

    public void deleteHostel(Long id) {
        hostelRepository.deleteById(id);
    }
}