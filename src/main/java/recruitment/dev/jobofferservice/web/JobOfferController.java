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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.AccessDeniedException;

@RestController
@RequestMapping("/job-offers")
@RequiredArgsConstructor
public class JobOfferController {

    private final JobOfferService jobOfferService;

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/create")
    public ResponseEntity<JobOfferDto> createJobOffer(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody JobOfferDto jobOfferDto) {
        jobOfferDto.setCompanyId(requiredCompany(jwt));
        JobOfferDto saved = jobOfferService.createJobOffer(jobOfferDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PreAuthorize("hasRole('HR')")
    @PutMapping("/update/{id}")
    public ResponseEntity<JobOfferDto> updateJobOffer(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody JobOfferDto jobOfferDto) {
        ensureTenant(jwt, jobOfferService.getJobOfferById(id));
        jobOfferDto.setCompanyId(requiredCompany(jwt));
        return ResponseEntity.ok(
                jobOfferService.updateJobOffer(id, jobOfferDto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'CANDIDATE', 'MANAGER')")
    @GetMapping("/get/{id}")
    public ResponseEntity<JobOfferDto> getJobOfferById(
            @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        JobOfferDto offer = jobOfferService.getJobOfferById(id);
        if (hasRole(jwt, "HR") || hasRole(jwt, "MANAGER")) ensureTenant(jwt, offer);
        return ResponseEntity.ok(offer);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'CANDIDATE', 'MANAGER')")
    @GetMapping("/getall")
    public ResponseEntity<Page<JobOfferDto>> getAllJobOffers(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return ResponseEntity.ok(hasRole(jwt, "ADMIN") || hasRole(jwt, "CANDIDATE")
                ? jobOfferService.getAllJobOffers(pageable)
                : jobOfferService.getAllJobOffersForCompany(requiredCompany(jwt), pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'CANDIDATE', 'MANAGER')")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<JobOfferDto>> getByStatus(
            @PathVariable JobStatus status,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return ResponseEntity.ok(hasRole(jwt, "ADMIN") || hasRole(jwt, "CANDIDATE")
                ? jobOfferService.getJobOffersByStatus(status, pageable)
                : jobOfferService.getJobOffersByCompanyAndStatus(requiredCompany(jwt), status, pageable));
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
            @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ensureTenant(jwt, jobOfferService.getJobOfferById(id));
        jobOfferService.deleteJobOffer(id);

        return ResponseEntity.noContent().build();
    }

    private Long requiredCompany(Jwt jwt) {
        Object claim = jwt == null ? null : jwt.getClaim("companyId");
        if (claim instanceof Number number) return number.longValue();
        if (claim instanceof String value && !value.isBlank()) try { return Long.valueOf(value); } catch (NumberFormatException ignored) { }
        throw new AccessDeniedException("No company is associated with this account");
    }

    private void ensureTenant(Jwt jwt, JobOfferDto offer) {
        if (hasRole(jwt, "ADMIN")) return;
        if (offer.getCompanyId() == null || !offer.getCompanyId().equals(requiredCompany(jwt))) throw new AccessDeniedException("Cross-company access denied");
    }

    @SuppressWarnings("unchecked")
    private boolean hasRole(Jwt jwt, String role) {
        if (jwt == null || jwt.getClaim("realm_access") == null) return false;
        Object roles = ((java.util.Map<String, Object>) jwt.getClaim("realm_access")).get("roles");
        return roles instanceof java.util.Collection<?> values && values.contains(role);
    }
}
