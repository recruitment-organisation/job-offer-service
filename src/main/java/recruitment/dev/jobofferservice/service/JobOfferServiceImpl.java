package recruitment.dev.jobofferservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import recruitment.dev.jobofferservice.dto.JobOfferDto;
import recruitment.dev.jobofferservice.entities.*;
import recruitment.dev.jobofferservice.mapper.JobOfferMapper;
import recruitment.dev.jobofferservice.respositories.JobOfferRepository;
@Service
@RequiredArgsConstructor
@Transactional
public class JobOfferServiceImpl implements JobOfferService {

    private final JobOfferRepository repository;
    private final JobOfferMapper mapper;

    @Override
    public JobOfferDto createJobOffer(JobOfferDto dto) {

        validateDates(dto);

        JobOffer jobOffer = mapper.toEntity(dto);

        linkChildren(jobOffer);

        return mapper.toDto(repository.save(jobOffer));
    }

    @Override
    public JobOfferDto updateJobOffer(Long id, JobOfferDto dto) {

        validateDates(dto);

        JobOffer jobOffer = findJobOffer(id);

        mapper.updateEntity(dto, jobOffer);

        updateRequirements(jobOffer, dto);

        updateSkills(jobOffer, dto);

        linkChildren(jobOffer);

        return mapper.toDto(repository.save(jobOffer));
    }

    @Override
    @Transactional(readOnly = true)
    public JobOfferDto getJobOfferById(Long id) {
        return mapper.toDto(findJobOffer(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobOfferDto> getAllJobOffers(Pageable pageable) {

        return repository.findAll(pageable)
                .map(mapper::toDto);
    }

    @Override
    public void deleteJobOffer(Long id) {

        repository.delete(findJobOffer(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobOfferDto> getJobOffersByStatus(JobStatus status,
                                                  Pageable pageable) {

        return repository.findByStatus(status, pageable)
                .map(mapper::toDto);
    }



    @Override
    @Transactional(readOnly = true)
    public Page<JobOfferDto> getJobOffersByExperienceLevel(
            ExperienceLevel level,
            Pageable pageable) {

        return repository.findByExperienceLevel(level, pageable)
                .map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobOfferDto> getJobOffersByEmploymentType(
            EmploymentType type,
            Pageable pageable) {

        return repository.findByEmploymentType(type, pageable)
                .map(mapper::toDto);
    }

    private JobOffer findJobOffer(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Job offer not found"));
    }

    private void validateDates(JobOfferDto dto) {

        if (dto.getClosingDate().isBefore(dto.getOpeningDate())) {
            throw new RuntimeException(
                    "Closing date must be after opening date");
        }
    }

    private void linkChildren(JobOffer jobOffer) {

        jobOffer.getRequirements()
                .forEach(r -> r.setJobOffer(jobOffer));

        jobOffer.getSkills()
                .forEach(s -> s.setJobOffer(jobOffer));
    }

    private void updateRequirements(JobOffer jobOffer,
                                    JobOfferDto dto) {

        jobOffer.getRequirements().clear();

        dto.getRequirements().forEach(r -> {

            JobRequirement requirement = JobRequirement.builder()
                    .requirement(r.getRequirement())
                    .jobOffer(jobOffer)
                    .build();

            jobOffer.getRequirements().add(requirement);
        });
    }

    private void updateSkills(JobOffer jobOffer,
                              JobOfferDto dto) {

        jobOffer.getSkills().clear();

        dto.getSkills().forEach(s -> {

            JobSkills skill = JobSkills.builder()
                    .skillName(s.getSkillName())
                    .mandatory(s.isMandatory())
                    .jobOffer(jobOffer)
                    .build();

            jobOffer.getSkills().add(skill);
        });
    }
}