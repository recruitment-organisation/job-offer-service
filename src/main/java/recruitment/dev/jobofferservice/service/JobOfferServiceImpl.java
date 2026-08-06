package recruitment.dev.jobofferservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import recruitment.dev.jobofferservice.dto.JobOfferDto;
import recruitment.dev.jobofferservice.entities.*;
import recruitment.dev.jobofferservice.exception.JobOfferNotFoundException;
import recruitment.dev.jobofferservice.mapper.JobOfferMapper;
import recruitment.dev.jobofferservice.respositories.JobOfferRepository;
import recruitment.dev.jobofferservice.outbox.OutboxEvent;
import recruitment.dev.jobofferservice.outbox.OutboxEventRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Service
@Transactional
public class JobOfferServiceImpl implements JobOfferService {

    private final JobOfferRepository repository;
    private final JobOfferMapper mapper;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public JobOfferServiceImpl(
            JobOfferRepository repository,
            JobOfferMapper mapper,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    JobOfferServiceImpl(JobOfferRepository repository, JobOfferMapper mapper) {
        this(repository, mapper, null, null);
    }

    @Override
    public JobOfferDto createJobOffer(JobOfferDto dto) {

        validateJobOffer(dto);

        JobOffer jobOffer = mapper.toEntity(dto);

        linkChildren(jobOffer);

        JobOffer saved = repository.save(jobOffer);
        if (saved.getStatus() == JobStatus.OPEN) {
            enqueueJobOfferEvent(saved, "job-offer.created");
        }
        return mapper.toDto(saved);
    }

    @Override
    public JobOfferDto updateJobOffer(Long id, JobOfferDto dto) {

        validateJobOffer(dto);

        JobOffer jobOffer = findJobOffer(id);
        JobStatus previousStatus = jobOffer.getStatus();

        mapper.updateEntity(dto, jobOffer);
        // The resource identifier comes from the path, never from the request body.
        jobOffer.setId(id);

        updateRequirements(jobOffer, dto);

        updateSkills(jobOffer, dto);

        linkChildren(jobOffer);

        JobOffer updated = repository.save(jobOffer);
        if (updated.getStatus() == JobStatus.OPEN) {
            enqueueJobOfferEvent(updated, previousStatus == JobStatus.OPEN ? "job-offer.updated" : "job-offer.created");
        }
        return mapper.toDto(updated);
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
                        new JobOfferNotFoundException(id));
    }

    private void validateJobOffer(JobOfferDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Job offer is required");
        }

        validateDates(dto);
        validateRequirements(dto.getRequirements());
        validateSkills(dto.getSkills());
    }

    private void validateDates(JobOfferDto dto) {
        if (dto.getOpeningDate() == null) {
            throw new IllegalArgumentException("Opening date is required");
        }

        if (dto.getClosingDate() == null) {
            throw new IllegalArgumentException("Closing date is required");
        }

        if (dto.getClosingDate().isBefore(dto.getOpeningDate())) {
            throw new IllegalArgumentException(
                    "Closing date must be after opening date");
        }
    }

    private void validateRequirements(List<recruitment.dev.jobofferservice.dto.JobRequirementDto> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            throw new IllegalArgumentException("At least one requirement is required");
        }
    }

    private void validateSkills(List<recruitment.dev.jobofferservice.dto.JobSkillDto> skills) {
        if (skills == null || skills.isEmpty()) {
            throw new IllegalArgumentException("At least one skill is required");
        }
    }

    private void linkChildren(JobOffer jobOffer) {
        if (jobOffer.getRequirements() == null) {
            jobOffer.setRequirements(new java.util.ArrayList<>());
        }

        if (jobOffer.getSkills() == null) {
            jobOffer.setSkills(new java.util.ArrayList<>());
        }

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

    private void enqueueJobOfferEvent(JobOffer jobOffer, String eventType) {
        if (outboxEventRepository == null || objectMapper == null) {
            return;
        }
        JobOfferEvent event = new JobOfferEvent(
                UUID.randomUUID().toString(),
                eventType,
                Instant.now().toString(),
                jobOffer.getId(),
                jobOffer.getTitle(),
                jobOffer.getLocation(),
                jobOffer.getStatus().name()
        );
        try {
            outboxEventRepository.save(new OutboxEvent(
                    "recruitment.job-offer.v1",
                    String.valueOf(jobOffer.getId()),
                    objectMapper.writeValueAsString(event)
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize " + eventType + " event", exception);
        }
    }

    private record JobOfferEvent(
            String eventId,
            String eventType,
            String occurredAt,
            Long jobOfferId,
            String title,
            String location,
            String status
    ) {
    }
}
