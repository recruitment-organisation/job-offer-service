package recruitment.dev.jobofferservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import recruitment.dev.jobofferservice.dto.JobOfferDto;
import recruitment.dev.jobofferservice.entities.JobOffer;

@Mapper(
        componentModel = "spring",
        uses = {
                JobRequirementMapper.class,
                JobSkillMapper.class
        }
)
public interface JobOfferMapper {

    JobOfferDto toDto(JobOffer entity);

    JobOffer toEntity(JobOfferDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requirements", ignore = true)
    @Mapping(target = "skills", ignore = true)
    void updateEntity(JobOfferDto dto,
                      @MappingTarget JobOffer entity);
}
