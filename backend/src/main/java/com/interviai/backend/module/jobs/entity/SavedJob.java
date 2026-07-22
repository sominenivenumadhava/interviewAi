package com.interviai.backend.module.jobs.entity;

import com.interviai.backend.common.entity.BaseEntity;
import com.interviai.backend.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "saved_jobs", uniqueConstraints = {
    @UniqueConstraint(name = "uq_saved_jobs_user_external_id", columnNames = {"user_id", "external_job_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SavedJob extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "external_job_id", nullable = false)
    private String externalJobId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "location")
    private String location;

    @Column(name = "job_url", length = 1000)
    private String jobUrl;

    @Column(name = "raw_data_json", columnDefinition = "TEXT")
    private String rawDataJson;
}
