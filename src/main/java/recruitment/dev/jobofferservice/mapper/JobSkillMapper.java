package recruitment.dev.jobofferservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import recruitment.dev.jobofferservice.dto.JobSkillDto;
import recruitment.dev.jobofferservice.entities.JobSkills;

@Mapper(componentModel = "spring")
public interface JobSkillMapper {

    JobSkillDto toDto(JobSkills entity);
    @Mapping(target = "jobOffer", ignore = true)

    JobSkills toEntity(JobSkillDto dto);
}