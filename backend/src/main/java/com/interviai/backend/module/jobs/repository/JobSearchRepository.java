package com.interviai.backend.module.jobs.repository;

import com.interviai.backend.module.jobs.entity.JobSearch;
import com.interviai.backend.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobSearchRepository extends JpaRepository<JobSearch, UUID> {

    List<JobSearch> findByUserOrderByCreatedAtDesc(User user);

    Optional<JobSearch> findByIdAndUser(UUID id, User user);
}
