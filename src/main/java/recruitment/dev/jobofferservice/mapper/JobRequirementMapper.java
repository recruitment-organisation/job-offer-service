package recruitment.dev.jobofferservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import recruitment.dev.jobofferservice.dto.JobRequirementDto;
import recruitment.dev.jobofferservice.entities.JobRequirement;

@Mapper(componentModel = "spring")
public interface JobRequirementMapper {

    JobRequirementDto toDto(JobRequirement entity);
    @Mapping(target = "jobOffer", ignore = true)

    JobRequirement toEntity(JobRequirementDto dto);
}