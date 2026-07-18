package recruitment.dev.jobofferservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequirementDto {

    private Long id;

    @NotBlank(message = "Requirement is required")
    @Size(max = 255, message = "Requirement must not exceed 255 characters")
    private String requirement;
}