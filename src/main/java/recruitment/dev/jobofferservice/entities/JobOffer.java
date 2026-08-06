package recruitment.dev.jobofferservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    /** Business area used to group and search job offers. */
    @Column(length = 100)
    private String domain;

    @Column(length = 3000)
    private String description;

    private String location;
    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;


    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;



    private LocalDate openingDate;

    private LocalDate closingDate;

    @Enumerated(EnumType.STRING)
    private JobStatus status;



    @OneToMany(
            mappedBy = "jobOffer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<JobRequirement> requirements = new ArrayList<>();

    @OneToMany(
            mappedBy = "jobOffer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<JobSkills> skills = new ArrayList<>();
}
