package recruitment.dev.jobofferservice.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import recruitment.dev.jobofferservice.entities.JobSkills;

public interface JobSkillsRepository extends JpaRepository<JobSkills, Long> {
}
