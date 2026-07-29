package recruitment.dev.jobofferservice.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import recruitment.dev.jobofferservice.dto.JobOfferDto;
import recruitment.dev.jobofferservice.service.JobOfferService;

@RestController
@RequestMapping("/internal/job-offers")
@RequiredArgsConstructor
public class InternalJobOfferController {

    private final JobOfferService jobOfferService;

    @GetMapping("/get/{jobOfferId}")
    public JobOfferDto getJobOfferById(
            @PathVariable Long jobOfferId
    ) {
        return jobOfferService.getJobOfferById(jobOfferId);
    }
}