package recruitment.dev.jobofferservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import recruitment.dev.jobofferservice.dto.JobOfferDto;
import recruitment.dev.jobofferservice.dto.JobRequirementDto;
import recruitment.dev.jobofferservice.dto.JobSkillDto;
import recruitment.dev.jobofferservice.entities.EmploymentType;
import recruitment.dev.jobofferservice.entities.ExperienceLevel;
import recruitment.dev.jobofferservice.entities.JobOffer;
import recruitment.dev.jobofferservice.entities.JobRequirement;
import recruitment.dev.jobofferservice.entities.JobSkills;
import recruitment.dev.jobofferservice.entities.JobStatus;
import recruitment.dev.jobofferservice.exception.JobOfferNotFoundException;
import recruitment.dev.jobofferservice.mapper.JobOfferMapper;
import recruitment.dev.jobofferservice.respositories.JobOfferRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobOfferServiceImplTest {

    @Test
    void shouldRejectClosingDateBeforeOpeningDate() {
        JobOfferServiceImpl service = new JobOfferServiceImpl(new InMemoryJobOfferRepository(), new SimpleJobOfferMapper());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createJobOffer(buildDto(
                        LocalDate.of(2026, 7, 30),
                        LocalDate.of(2026, 7, 29),
                        List.of(JobRequirementDto.builder().requirement("Java").build()),
                        List.of(JobSkillDto.builder().skillName("Spring Boot").mandatory(true).build())
                ))
        );

        assertEquals("Closing date must be after opening date", exception.getMessage());
    }

    @Test
    void shouldRejectMissingRequirements() {
        JobOfferServiceImpl service = new JobOfferServiceImpl(new InMemoryJobOfferRepository(), new SimpleJobOfferMapper());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createJobOffer(buildDto(
                        LocalDate.of(2026, 7, 29),
                        LocalDate.of(2026, 8, 15),
                        List.of(),
                        List.of(JobSkillDto.builder().skillName("Spring Boot").mandatory(true).build())
                ))
        );

        assertEquals("At least one requirement is required", exception.getMessage());
    }

    @Test
    void shouldRejectMissingSkills() {
        JobOfferServiceImpl service = new JobOfferServiceImpl(new InMemoryJobOfferRepository(), new SimpleJobOfferMapper());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createJobOffer(buildDto(
                        LocalDate.of(2026, 7, 29),
                        LocalDate.of(2026, 8, 15),
                        List.of(JobRequirementDto.builder().requirement("Java").build()),
                        List.of()
                ))
        );

        assertEquals("At least one skill is required", exception.getMessage());
    }

    @Test
    void shouldThrowWhenJobOfferDoesNotExist() {
        JobOfferServiceImpl service = new JobOfferServiceImpl(new InMemoryJobOfferRepository(), new SimpleJobOfferMapper());

        JobOfferNotFoundException exception = assertThrows(
                JobOfferNotFoundException.class,
                () -> service.getJobOfferById(99L)
        );

        assertEquals("Job offer not found with id: 99", exception.getMessage());
    }

    @Test
    void shouldCreateJobOfferAndLinkItsChildren() {
        InMemoryJobOfferRepository repository = new InMemoryJobOfferRepository();
        JobOfferServiceImpl service = new JobOfferServiceImpl(repository, new SimpleJobOfferMapper());

        JobOfferDto result = service.createJobOffer(buildDto(
                LocalDate.of(2026, 7, 29),
                LocalDate.of(2026, 8, 15),
                List.of(JobRequirementDto.builder().requirement("Java").build()),
                List.of(JobSkillDto.builder().skillName("Spring Boot").mandatory(true).build())
        ));

        JobOffer saved = repository.findById(result.getId()).orElseThrow();
        assertSame(saved, saved.getRequirements().getFirst().getJobOffer());
        assertSame(saved, saved.getSkills().getFirst().getJobOffer());
        assertEquals("Java", result.getRequirements().getFirst().getRequirement());
        assertEquals("Spring Boot", result.getSkills().getFirst().getSkillName());
    }

    @Test
    void shouldReplaceChildrenWhenUpdatingJobOffer() {
        InMemoryJobOfferRepository repository = new InMemoryJobOfferRepository();
        JobOfferServiceImpl service = new JobOfferServiceImpl(repository, new SimpleJobOfferMapper());
        JobOffer existing = new JobOffer();
        existing.setId(1L);
        existing.setRequirements(new ArrayList<>(List.of(JobRequirement.builder().requirement("Legacy").build())));
        existing.setSkills(new ArrayList<>(List.of(JobSkills.builder().skillName("Legacy").mandatory(false).build())));
        repository.save(existing);

        JobOfferDto result = service.updateJobOffer(1L, buildDto(
                LocalDate.of(2026, 7, 29),
                LocalDate.of(2026, 8, 15),
                List.of(JobRequirementDto.builder().requirement("Docker").build()),
                List.of(JobSkillDto.builder().skillName("Kubernetes").mandatory(true).build())
        ));

        JobOffer updated = repository.findById(1L).orElseThrow();
        assertEquals("Docker", updated.getRequirements().getFirst().getRequirement());
        assertEquals("Kubernetes", updated.getSkills().getFirst().getSkillName());
        assertSame(updated, updated.getRequirements().getFirst().getJobOffer());
        assertSame(updated, updated.getSkills().getFirst().getJobOffer());
        assertEquals("Kubernetes", result.getSkills().getFirst().getSkillName());
    }

    @Test
    void shouldDeleteExistingJobOffer() {
        InMemoryJobOfferRepository repository = new InMemoryJobOfferRepository();
        JobOfferServiceImpl service = new JobOfferServiceImpl(repository, new SimpleJobOfferMapper());
        JobOffer existing = new JobOffer();
        existing.setId(1L);
        repository.save(existing);

        service.deleteJobOffer(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void shouldReturnMappedPageOfJobOffers() {
        InMemoryJobOfferRepository repository = new InMemoryJobOfferRepository();
        JobOfferServiceImpl service = new JobOfferServiceImpl(repository, new SimpleJobOfferMapper());
        JobOffer first = new JobOffer();
        first.setId(1L);
        first.setRequirements(new ArrayList<>());
        first.setSkills(new ArrayList<>());
        JobOffer second = new JobOffer();
        second.setId(2L);
        second.setRequirements(new ArrayList<>());
        second.setSkills(new ArrayList<>());
        repository.save(first);
        repository.save(second);

        Page<JobOfferDto> result = service.getAllJobOffers(Pageable.ofSize(2));

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(1L, 2L), result.getContent().stream().map(JobOfferDto::getId).toList());
    }

    private JobOfferDto buildDto(
            LocalDate openingDate,
            LocalDate closingDate,
            List<JobRequirementDto> requirements,
            List<JobSkillDto> skills
    ) {
        return JobOfferDto.builder()
                .id(1L)
                .title("Backend Engineer")
                .description("Build APIs")
                .location("Tunis")
                .employmentType(EmploymentType.FULL_TIME)
                .experienceLevel(ExperienceLevel.JUNIOR)
                .openingDate(openingDate)
                .closingDate(closingDate)
                .status(JobStatus.OPEN)
                .requirements(requirements)
                .skills(skills)
                .build();
    }

    private static class InMemoryJobOfferRepository implements JobOfferRepository {

        private final Map<Long, JobOffer> offers = new LinkedHashMap<>();
        private long nextId = 1;

        @Override
        public Page<JobOffer> findByStatus(JobStatus status, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public Page<JobOffer> findByExperienceLevel(ExperienceLevel level, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public Page<JobOffer> findByEmploymentType(EmploymentType type, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public List<JobOffer> findAll() {
            return new ArrayList<>(offers.values());
        }

        @Override
        public List<JobOffer> findAllById(Iterable<Long> longs) {
            return List.of();
        }

        @Override
        public <S extends JobOffer> List<S> saveAll(Iterable<S> entities) {
            return List.of();
        }

        @Override
        public void flush() {
        }

        @Override
        public <S extends JobOffer> S saveAndFlush(S entity) {
            return entity;
        }

        @Override
        public <S extends JobOffer> List<S> saveAllAndFlush(Iterable<S> entities) {
            return List.of();
        }

        @Override
        public void deleteAllInBatch(Iterable<JobOffer> entities) {
        }

        @Override
        public void deleteAllByIdInBatch(Iterable<Long> longs) {
        }

        @Override
        public void deleteAllInBatch() {
        }

        @Override
        public JobOffer getOne(Long aLong) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JobOffer getById(Long aLong) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JobOffer getReferenceById(Long aLong) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <S extends JobOffer> List<S> findAll(org.springframework.data.domain.Example<S> example) {
            return List.of();
        }

        @Override
        public <S extends JobOffer> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) {
            return List.of();
        }

        @Override
        public List<JobOffer> findAll(org.springframework.data.domain.Sort sort) {
            return List.of();
        }

        @Override
        public Page<JobOffer> findAll(Pageable pageable) {
            return new PageImpl<>(findAll(), pageable, offers.size());
        }

        @Override
        public <S extends JobOffer> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(nextId++);
            }
            offers.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<JobOffer> findById(Long aLong) {
            return Optional.ofNullable(offers.get(aLong));
        }

        @Override
        public boolean existsById(Long aLong) {
            return false;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(Long aLong) {
        }

        @Override
        public void delete(JobOffer entity) {
            offers.remove(entity.getId());
        }

        @Override
        public void deleteAllById(Iterable<? extends Long> longs) {
        }

        @Override
        public void deleteAll(Iterable<? extends JobOffer> entities) {
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public <S extends JobOffer> Optional<S> findOne(org.springframework.data.domain.Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends JobOffer> Page<S> findAll(org.springframework.data.domain.Example<S> example, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public <S extends JobOffer> long count(org.springframework.data.domain.Example<S> example) {
            return 0;
        }

        @Override
        public <S extends JobOffer> boolean exists(org.springframework.data.domain.Example<S> example) {
            return false;
        }

        @Override
        public <S extends JobOffer, R> R findBy(
                org.springframework.data.domain.Example<S> example,
                java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction
        ) {
            throw new UnsupportedOperationException();
        }
    }

    private static class SimpleJobOfferMapper implements JobOfferMapper {

        @Override
        public JobOfferDto toDto(JobOffer entity) {
            return JobOfferDto.builder()
                    .id(entity.getId())
                    .title(entity.getTitle())
                    .description(entity.getDescription())
                    .location(entity.getLocation())
                    .employmentType(entity.getEmploymentType())
                    .experienceLevel(entity.getExperienceLevel())
                    .openingDate(entity.getOpeningDate())
                    .closingDate(entity.getClosingDate())
                    .status(entity.getStatus())
                    .requirements(entity.getRequirements().stream()
                            .map(requirement -> JobRequirementDto.builder()
                                    .id(requirement.getId())
                                    .requirement(requirement.getRequirement())
                                    .build())
                            .toList())
                    .skills(entity.getSkills().stream()
                            .map(skill -> JobSkillDto.builder()
                                    .id(skill.getId())
                                    .skillName(skill.getSkillName())
                                    .mandatory(skill.isMandatory())
                                    .build())
                            .toList())
                    .build();
        }

        @Override
        public JobOffer toEntity(JobOfferDto dto) {
            JobOffer entity = new JobOffer();
            updateEntity(dto, entity);
            return entity;
        }

        @Override
        public void updateEntity(JobOfferDto dto, JobOffer entity) {
            entity.setId(dto.getId());
            entity.setTitle(dto.getTitle());
            entity.setDescription(dto.getDescription());
            entity.setLocation(dto.getLocation());
            entity.setEmploymentType(dto.getEmploymentType());
            entity.setExperienceLevel(dto.getExperienceLevel());
            entity.setOpeningDate(dto.getOpeningDate());
            entity.setClosingDate(dto.getClosingDate());
            entity.setStatus(dto.getStatus());

            List<JobRequirement> requirements = new ArrayList<>();
            for (JobRequirementDto requirementDto : dto.getRequirements()) {
                requirements.add(JobRequirement.builder()
                        .id(requirementDto.getId())
                        .requirement(requirementDto.getRequirement())
                        .build());
            }
            entity.setRequirements(requirements);

            List<JobSkills> skills = new ArrayList<>();
            for (JobSkillDto skillDto : dto.getSkills()) {
                skills.add(JobSkills.builder()
                        .id(skillDto.getId())
                        .skillName(skillDto.getSkillName())
                        .mandatory(skillDto.isMandatory())
                        .build());
            }
            entity.setSkills(skills);
        }
    }
}
