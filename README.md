# 📘 Board-Service (Spring Boot 게시판 서비스)

Spring Boot 기반으로 개발한 **JWT 인증 게시판 서비스**입니다.  
게시글 CRUD, 댓글/대댓글, 좋아요/싫어요, 조회수 중복 방지, AWS S3 파일 업로드 등  
**실무에서 바로 사용할 수 있는 기능들**을 담은 백엔드 프로젝트입니다.

본 프로젝트는 **신입 백엔드 개발자 포트폴리오 기준 완성도 높은 아키텍처**를 목표로 제작되었습니다.

---

# 🚀 주요 기능 요약

## 🔐 인증/인가 (Spring Security + JWT)
- 회원가입 / 로그인
- JWT 발급 & 인증 필터 적용
- 인증된 사용자만 게시글 작성/수정/삭제 가능
- SecurityContext에서 principal(email) 기반 권한 인증

---

## 📝 게시글 기능
- 게시글 생성 / 조회 / 수정 / 삭제
- **제목 / 내용 / 작성자 통합 검색**
- **정렬 기능**
  - 최신순
  - 조회수순
  - 좋아요순
- **JPA Auditing** 자동 생성 시간/수정 시간 관리
- **조회수 중복 방지**
  - 동일 사용자 기준 날짜별 1회만 증가

---

## 💬 댓글/대댓글 기능
- 댓글 작성 / 조회 / 삭제
- 대댓글(depth 1) 구조 지원
- 게시글별 댓글 트리 응답
- 게시글 삭제 시 댓글 cascade 적용

---

## 👍 좋아요 / 싫어요
- 좋아요 / 싫어요 **토글 방식**
- 다시 누르면 취소됨
- likeCount / dislikeCount 실시간 반영
- PostLike 테이블 기반 중복 방지

---

## 📁 파일 업로드 (AWS S3)
- 게시글별 첨부파일 업로드
- 업로드된 파일 S3 저장 후 URL 반환
- S3 경로 구조  
```
posts/{postId}/{UUID}.확장자
```
- UploadedFile 엔티티에 메타정보 저장
  - originalName  
  - url  
  - size  
  - contentType  
  - uploadedAt  

---

## 🔎 검색/정렬 기능
- keyword 기반 통합 검색
- 최신순 / 조회수순 / 좋아요순 정렬 지원

---

## 🧪 테스트
- **좋아요 토글 테스트**
- **통합 검색 테스트**
- **조회수 중복 방지 테스트**(일부 작성)
- SpringBootTest + @Transactional 기반

---

# 🏛 아키텍처 구조
클린 아키텍처 기반의 표준 Spring MVC 구조

```
Controller → Service → Repository → JPA Entity → MySQL
```

---

# 🗂 프로젝트 구조
```
src/main/java/com/example/board_service
 ├── controller
 ├── service
 ├── repository
 ├── domain
 ├── dto
 ├── config
 ├── security
 ├── like
 ├── comment
 ├── file
 └── exception
```

---

# 📦 기술 스택

### Backend
- Java 21  
- Spring Boot 3.5.x  
- Spring Security  
- Spring Data JPA  
- MySQL 8  
- JWT (io.jsonwebtoken)  
- AWS S3 SDK  

### DevOps
- Docker / Docker Compose  
- Adminer  
- AWS EC2 배포 고려 구조  
- GitHub Actions(CI) 적용 가능 구조  

### Tools
- IntelliJ  
- GitHub  

---

# 🗄 ERD

### 📌 User
- id  
- email  
- password  
- nickname  

### 📌 Post
- id  
- title  
- content  
- author  
- viewCount  
- likeCount  
- dislikeCount  

### 📌 Comment
- id  
- content  
- author  
- parentId  
- postId  

### 📌 UploadedFile
- id  
- originalName  
- url  
- size  
- contentType  
- postId  

### 📌 ViewHistory
- userId  
- postId  
- viewedAt  

---

# 📚 API 문서
Swagger UI에서 전체 API 확인 가능  

➡ http://localhost:8080/swagger-ui/index.html

---

# 🌐 주요 API 예시

### 🔐 Auth
```
POST /api/auth/register
POST /api/auth/login
```

### 📝 Posts
```
POST   /api/posts
GET    /api/posts/{id}
PUT    /api/posts/{id}
DELETE /api/posts/{id}
```

### 🔎 Search & Sort
```
GET /api/posts?keyword=재순&sort=latest
GET /api/posts?keyword=테스트&sort=views
GET /api/posts?keyword=치이카와&sort=likes
```

### 📁 File Upload
```
POST /api/posts/{postId}/files
```

---

# 🧪 예외 처리(GlobalExceptionHandler)
모든 오류는 다음 형태로 응답:
```
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "message": "게시글을 찾을 수 없습니다."
}
```

---

# 🔧 실행 방법

### 1) Docker MySQL 실행
```
docker compose up -d
```

### 2) Spring Boot 실행
```
./gradlew bootRun
```

### 3) Swagger 접속
```
http://localhost:8080/swagger-ui/index.html
```

---

# 🚀 배포 (예정)
- AWS EC2 + Docker 기반 배포 구조
- GitHub Actions CI/CD 적용 가능

---

# 🎯 프로젝트 목표 달성도

| 기능 | 완료 여부 |
|------|-----------|
| JWT 인증 | ✅ |
| 게시글 CRUD | ✅ |
| 댓글/대댓글 | ✅ |
| 좋아요/싫어요 | ✅ |
| 조회수 중복 방지 | ✅ |
| 파일 업로드(S3) | ✅ |
| 검색/정렬 | ✅ |
| Swagger | ✅ |
| Docker | ✅ |
| 테스트 코드 | 🔶 일부 완료 |

---

# 🙋‍♂️ 개발자

**이재욱 (Backend Developer)**  
- Java · Spring Boot 기반 백엔드 개발자  
- AWS · Docker · DevOps 관심  
- 게임 개발/프레임워크 개발 병행  

---

