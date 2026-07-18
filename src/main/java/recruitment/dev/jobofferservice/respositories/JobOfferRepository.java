package recruitment.dev.jobofferservice.respositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import recruitment.dev.jobofferservice.entities.EmploymentType;
import recruitment.dev.jobofferservice.entities.ExperienceLevel;
import recruitment.dev.jobofferservice.entities.JobOffer;
import recruitment.dev.jobofferservice.entities.JobStatus;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
    Page<JobOffer> findByStatus(JobStatus status, Pageable pageable);


    Page<JobOffer> findByEmploymentType(EmploymentType type, Pageable pageable);

    Page<JobOffer> findByExperienceLevel(ExperienceLevel level, Pageable pageable);

}


