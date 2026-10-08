package com.stayfinder.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stayfinder.backend.entity.Hostel;

@Repository
public interface HostelRepository extends JpaRepository<Hostel, Long> {

}