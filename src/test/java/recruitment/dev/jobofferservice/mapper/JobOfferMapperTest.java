package recruitment.dev.jobofferservice.mapper;

import org.junit.jupiter.api.Test;
import recruitment.dev.jobofferservice.dto.JobOfferDto;
import recruitment.dev.jobofferservice.dto.JobRequirementDto;
import recruitment.dev.jobofferservice.dto.JobSkillDto;
import recruitment.dev.jobofferservice.entities.EmploymentType;
import recruitment.dev.jobofferservice.entities.ExperienceLevel;
import recruitment.dev.jobofferservice.entities.JobOffer;
import recruitment.dev.jobofferservice.entities.JobRequirement;
import recruitment.dev.jobofferservice.entities.JobSkills;
import recruitment.dev.jobofferservice.entities.JobStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JobOfferMapperTest {

    private final JobOfferMapper mapper = buildMapper();

    @Test
    void shouldMapEntityToDto() {
        JobOffer entity = new JobOffer();
        entity.setId(1L);
        entity.setTitle("Backend Engineer");
        entity.setDescription("Build APIs");
        entity.setLocation("Tunis");
        entity.setEmploymentType(EmploymentType.FULL_TIME);
        entity.setExperienceLevel(ExperienceLevel.JUNIOR);
        entity.setOpeningDate(LocalDate.of(2026, 7, 25));
        entity.setClosingDate(LocalDate.of(2026, 8, 25));
        entity.setStatus(JobStatus.OPEN);
        entity.setRequirements(List.of(
                JobRequirement.builder().id(10L).requirement("Java").build()
        ));
        entity.setSkills(List.of(
                JobSkills.builder().id(20L).skillName("Spring Boot").mandatory(true).build()
        ));

        JobOfferDto dto = mapper.toDto(entity);

        assertEquals(1L, dto.getId());
        assertEquals("Backend Engineer", dto.getTitle());
        assertEquals("Java", dto.getRequirements().getFirst().getRequirement());
        assertEquals("Spring Boot", dto.getSkills().getFirst().getSkillName());
        assertEquals(true, dto.getSkills().getFirst().isMandatory());
    }

    @Test
    void shouldMapDtoToEntityWithoutBackReference() {
        JobOfferDto dto = JobOfferDto.builder()
                .id(2L)
                .title("Platform Engineer")
                .description("Infrastructure")
                .location("Sfax")
                .employmentType(EmploymentType.FULL_TIME)
                .experienceLevel(ExperienceLevel.SENIOR)
                .openingDate(LocalDate.of(2026, 7, 25))
                .closingDate(LocalDate.of(2026, 8, 25))
                .status(JobStatus.OPEN)
                .requirements(List.of(
                        JobRequirementDto.builder().id(11L).requirement("Docker").build()
                ))
                .skills(List.of(
                        JobSkillDto.builder().id(21L).skillName("Kubernetes").mandatory(true).build()
                ))
                .build();

        JobOffer entity = mapper.toEntity(dto);

        assertEquals(2L, entity.getId());
        assertEquals("Platform Engineer", entity.getTitle());
        assertEquals("Docker", entity.getRequirements().getFirst().getRequirement());
        assertEquals("Kubernetes", entity.getSkills().getFirst().getSkillName());
        assertNull(entity.getRequirements().getFirst().getJobOffer());
        assertNull(entity.getSkills().getFirst().getJobOffer());
    }

    private JobOfferMapper buildMapper() {
        try {
            JobOfferMapperImpl mapper = new JobOfferMapperImpl();

            java.lang.reflect.Field requirementMapperField =
                    JobOfferMapperImpl.class.getDeclaredField("jobRequirementMapper");
            requirementMapperField.setAccessible(true);
            requirementMapperField.set(mapper, new JobRequirementMapperImpl());

            java.lang.reflect.Field skillMapperField =
                    JobOfferMapperImpl.class.getDeclaredField("jobSkillMapper");
            skillMapperField.setAccessible(true);
            skillMapperField.set(mapper, new JobSkillMapperImpl());

            return mapper;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize JobOfferMapper for tests", exception);
        }
    }
}
