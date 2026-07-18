package recruitment.dev.jobofferservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import recruitment.dev.jobofferservice.dto.JobOfferDto;
import recruitment.dev.jobofferservice.entities.EmploymentType;
import recruitment.dev.jobofferservice.entities.ExperienceLevel;
import recruitment.dev.jobofferservice.entities.JobStatus;

public interface JobOfferService {

    JobOfferDto createJobOffer(JobOfferDto jobOfferDto);

    JobOfferDto updateJobOffer(Long id, JobOfferDto jobOfferDto);

    JobOfferDto getJobOfferById(Long id);

    Page<JobOfferDto> getAllJobOffers(Pageable pageable);

    void deleteJobOffer(Long id);

    Page<JobOfferDto> getJobOffersByStatus(JobStatus status, Pageable pageable);



    Page<JobOfferDto> getJobOffersByExperienceLevel(ExperienceLevel level, Pageable pageable);

    Page<JobOfferDto> getJobOffersByEmploymentType(EmploymentType type, Pageable pageable);

}
