package recruitment.dev.jobofferservice.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import recruitment.dev.jobofferservice.dto.JobOfferDto;
import recruitment.dev.jobofferservice.entities.EmploymentType;
import recruitment.dev.jobofferservice.entities.ExperienceLevel;
import recruitment.dev.jobofferservice.entities.JobStatus;
import recruitment.dev.jobofferservice.service.JobOfferService;

@RestController
@RequestMapping("/job-offers")
@RequiredArgsConstructor
public class JobOfferController {

    private final JobOfferService jobOfferService;

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/create")
    public ResponseEntity<JobOfferDto> createJobOffer(
            @Valid @RequestBody JobOfferDto jobOfferDto) {

        JobOfferDto saved = jobOfferService.createJobOffer(jobOfferDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PreAuthorize("hasRole('HR')")
    @PutMapping("/update/{id}")
    public ResponseEntity<JobOfferDto> updateJobOffer(
            @PathVariable Long id,
            @Valid @RequestBody JobOfferDto jobOfferDto) {

        return ResponseEntity.ok(
                jobOfferService.updateJobOffer(id, jobOfferDto));
    }

    @PreAuthorize("hasAnyRole('HR', 'CANDIDATE')")
    @GetMapping("/get/{id}")
    public ResponseEntity<JobOfferDto> getJobOfferById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                jobOfferService.getJobOfferById(id));
    }

    @PreAuthorize("hasAnyRole('HR', 'CANDIDATE')")
    @GetMapping("/getall")
    public ResponseEntity<Page<JobOfferDto>> getAllJobOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                jobOfferService.getAllJobOffers(Pageable.ofSize(size).withPage(page)));
    }

    @PreAuthorize("hasAnyRole('HR', 'CANDIDATE')")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<JobOfferDto>> getByStatus(
            @PathVariable JobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                jobOfferService.getJobOffersByStatus(status, Pageable.ofSize(size).withPage(page)));
    }



    @PreAuthorize("hasRole('HR')")
    @GetMapping("/employment-type/{employmentType}")
    public ResponseEntity<Page<JobOfferDto>> getByEmploymentType(
            @PathVariable EmploymentType employmentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                jobOfferService.getJobOffersByEmploymentType(employmentType, Pageable.ofSize(size).withPage(page)));
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/experience-level/{experienceLevel}")
    public ResponseEntity<Page<JobOfferDto>> getByExperienceLevel(
            @PathVariable ExperienceLevel experienceLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                jobOfferService.getJobOffersByExperienceLevel(experienceLevel, Pageable.ofSize(size).withPage(page)));
    }

    @PreAuthorize("hasRole('HR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobOffer(
            @PathVariable Long id) {

        jobOfferService.deleteJobOffer(id);

        return ResponseEntity.noContent().build();
    }
}
