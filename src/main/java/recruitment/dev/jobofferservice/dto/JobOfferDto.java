package recruitment.dev.jobofferservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import recruitment.dev.jobofferservice.entities.EmploymentType;
import recruitment.dev.jobofferservice.entities.ExperienceLevel;
import recruitment.dev.jobofferservice.entities.JobStatus;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOfferDto {

    private Long id;
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;
    @NotBlank(message = "Description is required")
    @Size(max = 3000, message = "Description must not exceed 3000 characters")
    private String description;
    @NotBlank(message = "Location is required")

    private String location;
    @NotNull(message = "Employment type is required")

    private EmploymentType employmentType;
    @NotNull(message = "Experience level is required")

    private ExperienceLevel experienceLevel;



    @NotNull(message = "Opening date is required")
    private LocalDate openingDate;

    @NotNull(message = "Closing date is required")
    private LocalDate closingDate;

    @NotNull(message = "Status is required")
    private JobStatus status;


    @Valid
    @NotEmpty(message = "At least one requirement is required")
    private List<JobRequirementDto> requirements;

    @Valid
    @NotEmpty(message = "At least one skill is required")
    private List<JobSkillDto> skills;
}