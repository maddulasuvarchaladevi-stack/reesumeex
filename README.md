# AI-Based Resume Screening System (100% Pure Java, Zero JavaScript)

An advanced full-stack **Java 21** web application that automates resume parsing, NLP skill & synonym extraction, weighted job-resume matching, multithreaded candidate scoring, and bias-free candidate ranking — built **entirely in Java without a single line of JavaScript**.

## Key Features & Modules
1. **Authentication Module (`/login`, `/register`, `/logout`)**: HR Admin and Recruiter login & registration with HTTP-only session cookies.
2. **Job Management Module (`/jobs`)**: Create and manage job openings with weighted skill requirements (`1–5`) and automatic NLP extraction from job descriptions.
3. **Resume Management & Parser (`/upload`, `/upload-text`)**:
   - Native `.docx` parser using `ZipInputStream` (`word/document.xml` extraction).
   - Native `.pdf` text stream extractor & `.txt` parser.
   - Extracts candidate name, email, phone, education level, years of experience, and certifications.
4. **NLP Skill & Synonym Analyzer (`SkillAnalyzerService.java`)**:
   - Recognizes 20+ canonical technical skills and 100+ synonyms/related sub-technologies (e.g., *Spring MVC* &rarr; `Spring Boot`, *JPA / ORM* &rarr; `Hibernate`, *RESTful Services* &rarr; `REST API`, *Java Programming / J2EE* &rarr; `Java`).
   - **Bias-Free Fairness Guarantee**: Explicitly scrubs and ignores personal/demographic attributes (gender, age, marital status, ethnicity).
5. **AI Matching & Multithreaded Ranking Engine (`/rankings`, `/dashboard`)**:
   - Computes **Exact Skill Ratio** (`5/5 = 100%`, `4/5 = 80%`, `3/5 = 60%`, `2/5 = 40%`) and **Composite AI Score** (75% Weighted Skill + 15% Experience + 10% Education).
   - Evaluates candidates concurrently using Java `ExecutorService` (`Callable`/`Future`) and filters/sorts via the **Java Stream API**.
6. **Individual Candidate Evaluation Report (`/candidate`)**:
   - Displays explainable AI rationales, synonym evidence badges, missing skills, bonus skills, and extracted raw text.
7. **Recruitment Analytics Dashboard (`/analytics`) & REST API (`/api/rankings`)**:
   - Pure-CSS skill distribution bar charts, NLP taxonomy viewer, and JSON REST API endpoint.

## How to Run
```powershell
cd C:\Users\suvar\.gemini\antigravity\scratch\ai-resume-screening
.\run.bat
```
Then open **http://localhost:8080** in your browser (Default login: `admin` / `admin123`).
