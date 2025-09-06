# 2025_SEASONTHON_TEAM_99_BE  

2025 [kakao X goorm] 시즌톤 - 청정 (BE)  

# 프로젝트 컨벤션 가이드
  
<br>

## 🌿 브랜치 전략
- main(배포)
- develop(개발진행중)
- 작업 브랜치 작명 -> 예) feat/post, feat/post-vote
<br>

### 🔖 이슈 관리 라벨

- `feat/` 기능 추가
- `fix/` 오류 수정
- `refactor/` 리팩토링
- `hotfix/` 긴급 수정
- `devops/` 데브옵스
- `docs/` 문서 파일 추가
- `chore/` 기존 코드 수정
<br>
  
## 💬 커밋 메세지 컨벤션

- **형식**: `종류(기능명): 설명`
- **예시**:  
  ```bash
  feat/post: add like count API
  ```
  <br>

## 🏗 프로젝트 패키지 아키텍처
- 도메인 기반 디렉터리 구조(DDD)
<img width="292" height="758" alt="image" src="https://github.com/user-attachments/assets/af407255-ed0c-4f98-9f66-eaec60e1629b" />


<br>
  
## ✏ 네이밍 컨벤션
| **종류**             | **방식**               | **예시**                                |
|----------------------|------------------------|-----------------------------------------|
| 클래스 / 인터페이스   | `PascalCase`           | `PostService`, `UserController`         |
| 패키지 / 변수 / 메서드 | `camelCase`            | `getUserById()`, `postList`             |
| 상수                  | `SCREAMING_SNAKE_CASE` | `MAX_SIZE`, `API_KEY` 
<br>
  
## 🗄 DB 네이밍 컨벤션

- 스네이크 케이스

- 테이블: post_likes
- 컬럼: created_at
- FK: post_likes_id
<br>
  
## 📡 API 규약
- GET    /posts
- POST   /posts
- PATCH  /posts/{id}
- DELETE /posts/{id}
<br>
  
## 🛠 Tech Stack

| **Category** | **Tech Stack** |
|--------------|----------------|
| 🔐 Security  | ![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?logo=springsecurity&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-black?logo=jsonwebtokens&logoColor=white) |
| 🗄 Database  | ![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white) ![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?logo=spring&logoColor=white) ![RDS](https://img.shields.io/badge/AWS%20RDS-527FFF?logo=amazonrds&logoColor=white) |
| ☁ Infra      | ![AWS ECS](https://img.shields.io/badge/AWS%20ECS-FF9900?logo=amazonecs&logoColor=white) ![AWS ECR](https://img.shields.io/badge/Amazon%20ECR-FF9900?logo=amazonaws&logoColor=white) ![MinIO](https://img.shields.io/badge/MinIO-C72E49?logo=minio&logoColor=white) ![S3](https://img.shields.io/badge/AWS%20S3-569A31?logo=amazons3&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white) |
| 🚀 Deployment | ![CodeDeploy](https://img.shields.io/badge/AWS%20CodeDeploy-6DB33F?logo=aws&logoColor=white) |
| 📡 API       | ![OpenAPI](https://img.shields.io/badge/OpenAPI-6BA539?logo=openapiinitiative&logoColor=white) ![공공데이터](https://img.shields.io/badge/OpenAPI%20Public%20Data-blue) ![Perplexity LLM](https://img.shields.io/badge/Perplexity%20LLM-7B68EE?logo=openai&logoColor=white) |
| 💻 Language  | ![Java](https://img.shields.io/badge/Java-007396?logo=java&logoColor=white) |
| 📱 Framework | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white) ![Flutter](https://img.shields.io/badge/Flutter-02569B?logo=flutter&logoColor=white) |
| 🤝 CI/CD & Collaboration | ![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white) ![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?logo=githubactions&logoColor=white) |
<br>
  
## Infra Structure
<img width="1404" height="750" alt="infra" src="https://github.com/user-attachments/assets/50895906-1073-4819-8d4c-da2afa2eb3c7" />
