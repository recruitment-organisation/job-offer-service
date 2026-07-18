package recruitment.dev.jobofferservice.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import recruitment.dev.jobofferservice.entities.JobRequirement;

public interface JobRequirementRepository extends JpaRepository<JobRequirement, Long> {
}
