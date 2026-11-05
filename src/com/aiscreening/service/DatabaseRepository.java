package com.aiscreening.service;

import com.aiscreening.model.AdminUser;
import com.aiscreening.model.CandidateResume;
import com.aiscreening.model.JobOpening;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistence Repository demonstrating:
 * - Collections Framework (ConcurrentHashMap, LinkedHashMap, ArrayList)
 * - JDBC connectivity support (with automatic in-memory fallback when no external MySQL daemon is running)
 * - Pre-loaded realistic benchmark dataset matching the project specification.
 */
public class DatabaseRepository {

    private final Map<String, AdminUser> usersByUsername = new ConcurrentHashMap<>();
    private final Map<String, JobOpening> jobsById = new LinkedHashMap<>();
    private final Map<String, CandidateResume> candidatesById = new LinkedHashMap<>();

    // Optional JDBC configuration (defaults to embedded memory store so app works out-of-the-box)
    private final String jdbcUrl = System.getProperty("jdbc.url", "jdbc:mysql://localhost:3306/ai_resume_screening");
    private final String jdbcUser = System.getProperty("jdbc.user", "root");
    private final String jdbcPassword = System.getProperty("jdbc.password", "");

    private final ResumeParserService parserService;

    public DatabaseRepository(ResumeParserService parserService) {
        this.parserService = parserService;
        seedDefaultAdminUsers();
        seedDefaultJobs();
        seedDefaultCandidates();
    }

    private void seedDefaultAdminUsers() {
        AdminUser admin = new AdminUser("admin", "admin123", "Priya Sharma (Lead HR Manager)", "HR_ADMIN");
        AdminUser recruiter = new AdminUser("hr", "hr123", "Rohan Verma (Technical Recruiter)", "RECRUITER");
        usersByUsername.put(admin.getUsername(), admin);
        usersByUsername.put(recruiter.getUsername(), recruiter);
    }

    private void seedDefaultJobs() {
        // Primary Job from the user's specification: Java, Spring Boot, MySQL, Hibernate, REST API
        Map<String, Integer> job1Skills = new LinkedHashMap<>();
        job1Skills.put("Java", 5);
        job1Skills.put("Spring Boot", 5);
        job1Skills.put("MySQL", 4);
        job1Skills.put("Hibernate", 4);
        job1Skills.put("REST API", 4);

        JobOpening job1 = new JobOpening(
                "JOB-101",
                "Senior Java Backend Developer",
                "Core Platform Engineering",
                "Design and build scalable enterprise backend services using Java, Spring Boot, MySQL, Hibernate ORM, and RESTful APIs.",
                job1Skills,
                3,
                "Bachelor's Degree"
        );

        Map<String, Integer> job2Skills = new LinkedHashMap<>();
        job2Skills.put("Java", 5);
        job2Skills.put("Spring Boot", 5);
        job2Skills.put("Microservices", 5);
        job2Skills.put("Docker", 4);
        job2Skills.put("Kubernetes", 4);
        job2Skills.put("Kafka", 3);

        JobOpening job2 = new JobOpening(
                "JOB-102",
                "Cloud Microservices Architect",
                "Cloud Infrastructure",
                "Lead distributed microservices architecture on Java & Spring Cloud with Docker, Kubernetes, and Apache Kafka event streaming.",
                job2Skills,
                5,
                "Master's Degree"
        );

        Map<String, Integer> job3Skills = new LinkedHashMap<>();
        job3Skills.put("Java", 5);
        job3Skills.put("JDBC", 4);
        job3Skills.put("MySQL", 4);
        job3Skills.put("SQL", 4);
        job3Skills.put("Git", 3);

        JobOpening job3 = new JobOpening(
                "JOB-103",
                "Junior Associate Java Engineer",
                "Application Development",
                "Entry-level Java engineering role focusing on Core Java, OOP, Collections Framework, JDBC database programming, and MySQL.",
                job3Skills,
                1,
                "Bachelor's Degree"
        );

        jobsById.put(job1.getId(), job1);
        jobsById.put(job2.getId(), job2);
        jobsById.put(job3.getId(), job3);
    }

    private void seedDefaultCandidates() {
        // Candidate A: 5/5 (100% Match for JOB-101) — also demonstrates Synonym detection ("Spring MVC", "JPA", "RESTful Web Services")
        addSeedCandidate("CAND-01", "Candidate_A_Aarav_Mehta.pdf",
                "Aarav Mehta (Candidate A)\n" +
                "Email: aarav.mehta@techmail.in | Phone: +91-98201-11223\n" +
                "Education: M.Tech in Computer Science, IIT Bombay\n" +
                "Experience: 5.5 years of experience as Senior Backend Engineer.\n" +
                "Technical Skills: Java Programming, Spring Boot, Spring MVC, Hibernate ORM, JPA, MySQL, RESTful Web Services, Microservices, Docker, Git, JUnit.\n" +
                "Certifications: Oracle Certified Professional (OCP Java), Spring Professional Certification.\n" +
                "Work History: Architected high-throughput payment microservices and REST APIs using Spring Boot and MySQL.");

        // Candidate B: 4/5 (80% Match for JOB-101 — matches Java, Spring Boot, MySQL, REST API; missing Hibernate)
        addSeedCandidate("CAND-02", "Candidate_B_Ananya_Iyer.docx",
                "Ananya Iyer (Candidate B)\n" +
                "Email: ananya.iyer@devmail.com | Phone: +91-98450-22334\n" +
                "Education: B.Tech in Information Technology, NIT Trichy\n" +
                "Experience: 4 years of experience as Software Developer.\n" +
                "Technical Skills: Core Java, Spring Boot, MySQL, REST API, Docker, Maven, Git, JUnit, Agile.\n" +
                "Certifications: AWS Certified Developer.\n" +
                "Work History: Built enterprise REST APIs and microservices using Java 17, Spring Boot, and MySQL databases.");

        // Candidate C: 3/5 (60% Match for JOB-101 — matches Java, Spring Boot, MySQL; missing Hibernate, REST API)
        addSeedCandidate("CAND-03", "Candidate_C_Vikram_Rao.pdf",
                "Vikram Rao (Candidate C)\n" +
                "Email: vikram.rao@candi.org | Phone: +91-97111-33445\n" +
                "Education: B.E. in Computer Engineering, Pune University\n" +
                "Experience: 3 years of experience as Application Developer.\n" +
                "Technical Skills: Java, Spring Framework, MySQL, JDBC, HTML/CSS, Git, Maven.\n" +
                "Work History: Maintained monolithic Java web portals and MySQL stored procedures.");

        // Candidate D: 2/5 (40% Match for JOB-101 — matches Java, MySQL; missing Spring Boot, Hibernate, REST API)
        addSeedCandidate("CAND-04", "Candidate_D_Sneha_Patel.docx",
                "Sneha Patel (Candidate D)\n" +
                "Email: sneha.patel@mail.in | Phone: +91-99001-44556\n" +
                "Education: B.Sc in Computer Science\n" +
                "Experience: 1.5 years of experience as Junior Programmer.\n" +
                "Technical Skills: Core Java, MySQL, SQL, JDBC, HTML/CSS.\n" +
                "Work History: Developed desktop utilities and JDBC reporting queries on MySQL.");

        // Candidate E: 5/5 (100% Match for JOB-101 via Synonyms + Cloud Native skills)
        addSeedCandidate("CAND-05", "Candidate_E_Karthik_Nair.pdf",
                "Karthik Nair (Candidate E)\n" +
                "Email: karthik.nair@clouddev.io | Phone: +91-98860-55667\n" +
                "Education: M.Tech in Software Systems, BITS Pilani\n" +
                "Experience: 6 years of experience as Lead Java Engineer.\n" +
                "Technical Skills: Java 21, Spring Boot, Hibernate, MySQL, PostgreSQL, REST API, Microservices, Docker, Kubernetes, Kafka, AWS.\n" +
                "Certifications: Oracle Certified Professional (OCP Java), AWS Certified Solutions Architect, CKAD.\n" +
                "Work History: Led cloud migration of banking backend using Java, Spring Boot, Hibernate, and Kubernetes.");

        // Candidate F: 4/5 (80% Match for JOB-101 — Java, Spring MVC -> Spring Boot, JPA -> Hibernate, RESTful API -> REST API; uses PostgreSQL instead of MySQL)
        addSeedCandidate("CAND-06", "Candidate_F_Meera_Krishnan.docx",
                "Meera Krishnan (Candidate F)\n" +
                "Email: meera.k@techcorp.in | Phone: +91-94441-66778\n" +
                "Education: MCA (Master of Computer Applications)\n" +
                "Experience: 3.5 years of experience as Backend Engineer.\n" +
                "Technical Skills: Java Development, Spring MVC, JPA, PostgreSQL, RESTful Services, JUnit, Git, Maven.\n" +
                "Certifications: Scrum Master Certified (CSM).\n" +
                "Work History: Designed RESTful backend services using Java, Spring MVC, and JPA.");

        // Candidate G: 1/5 (20% Match for JOB-101 — Python/ML profile with basic SQL)
        addSeedCandidate("CAND-07", "Candidate_G_Arjun_Das.pdf",
                "Arjun Das (Candidate G)\n" +
                "Email: arjun.das@datasci.ai | Phone: +91-98301-77889\n" +
                "Education: M.Sc in Data Science\n" +
                "Experience: 2 years of experience as Data Analyst.\n" +
                "Technical Skills: Python, Machine Learning, MySQL, SQL, Docker, Git.\n" +
                "Work History: Built NLP classification pipelines in Python and queried MySQL data warehouses.");
    }

    private void addSeedCandidate(String id, String fileName, String text) {
        CandidateResume resume = parserService.parseFromText(id, fileName, text);
        candidatesById.put(resume.getId(), resume);
    }

    public AdminUser authenticate(String username, String password) {
        if (username == null || password == null) return null;
        AdminUser user = usersByUsername.get(username.trim().toLowerCase());
        if (user != null && user.getPasswordHash().equals(password)) {
            return user;
        }
        return null;
    }

    public AdminUser registerAdmin(String username, String password, String fullName) {
        String key = username.trim().toLowerCase();
        if (usersByUsername.containsKey(key)) {
            return null;
        }
        AdminUser newUser = new AdminUser(key, password, fullName, "HR_ADMIN");
        usersByUsername.put(key, newUser);
        return newUser;
    }

    public synchronized void saveJob(JobOpening job) {
        jobsById.put(job.getId(), job);
        persistJobViaJdbcIfConfigured(job);
    }

    public synchronized void saveCandidate(CandidateResume candidate) {
        candidatesById.put(candidate.getId(), candidate);
        persistCandidateViaJdbcIfConfigured(candidate);
    }

    public synchronized void deleteCandidate(String id) {
        candidatesById.remove(id);
    }

    public Collection<JobOpening> getAllJobs() {
        return new ArrayList<>(jobsById.values());
    }

    public JobOpening getJobById(String id) {
        if (id == null || !jobsById.containsKey(id)) {
            return jobsById.values().stream().findFirst().orElse(null);
        }
        return jobsById.get(id);
    }

    public Collection<CandidateResume> getAllCandidates() {
        return new ArrayList<>(candidatesById.values());
    }

    public CandidateResume getCandidateById(String id) {
        return candidatesById.get(id);
    }

    /**
     * Demonstrates standard JDBCPreparedStatement persistence when an external MySQL database is configured.
     */
    private void persistCandidateViaJdbcIfConfigured(CandidateResume c) {
        if (!Boolean.getBoolean("enable.jdbc")) {
            return;
        }
        String sql = "INSERT INTO candidates (id, full_name, email, phone, education, experience_years, file_name) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getId());
            ps.setString(2, c.getCandidateName());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getHighestEducation());
            ps.setDouble(6, c.getExperienceYears());
            ps.setString(7, c.getFileName());
            ps.executeUpdate();
        } catch (SQLException ignored) {
            // Gracefully fall back to in-memory collection store if MySQL daemon is offline
        }
    }

    private void persistJobViaJdbcIfConfigured(JobOpening job) {
        if (!Boolean.getBoolean("enable.jdbc")) {
            return;
        }
        String sql = "INSERT INTO job_openings (id, title, department, min_experience, required_education) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, job.getId());
            ps.setString(2, job.getTitle());
            ps.setString(3, job.getDepartment());
            ps.setInt(4, job.getMinExperienceYears());
            ps.setString(5, job.getRequiredEducation());
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }
}
