package recruitment.dev.jobofferservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSkillDto {

    private Long id;

    @NotBlank(message = "Skill name is required")
    private String skillName;

    private boolean mandatory;}