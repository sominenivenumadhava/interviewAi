package com.interviai.backend.module.jobs.repository;

import com.interviai.backend.module.jobs.entity.SavedJob;
import com.interviai.backend.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, UUID> {

    List<SavedJob> findByUserOrderByCreatedAtDesc(User user);

    Optional<SavedJob> findByExternalJobIdAndUser(String externalJobId, User user);

    boolean existsByExternalJobIdAndUser(String externalJobId, User user);
}
