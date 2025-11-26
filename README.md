# Board Service 프로젝트

간단한 게시판 API 서버로, 게시글 CRUD, 파일 업로드(S3), 댓글/대댓글, 좋아요/싫어요, 조회수 증가, 정렬/검색 기능 등을 구현한 Spring Boot 기반 포트폴리오 프로젝트입니다.

## 🚀 기술 스택

- **Backend**: Java 21, Spring Boot 3.3
- **Security**: Spring Security + JWT
- **Database**: MySQL 8 (로컬/도커)
- **ORM**: Spring Data JPA
- **Infra**: AWS S3 (파일 저장)
- **Docs**: Swagger(springdoc-openapi)
- **CI/CD**: GitHub Actions (빌드 자동화)
- **Test**: JUnit5, SpringBootTest

---

## 📌 주요 기능 정리

### 1. 게시글 기능
- 게시글 작성 / 조회 / 수정 / 삭제
- 페이징 처리
- 최신순 / 조회순 / 좋아요순 정렬
- 제목/내용/작성자 통합 검색
- 조회수 24시간 1회 증가 제한(LocalDate 기준 유저 단위)

### 2. 댓글 & 대댓글
- 댓글 작성 / 조회 / 삭제
- 대댓글(대댓글의 depth=1 구조)
- 게시글 삭제 시 댓글 cascade 삭제 설정

### 3. 좋아요 / 싫어요
- 게시글 좋아요 토글(한 번 누르면 +1, 다시 누르면 -1)
- 테스트 코드로 정상 동작 검증 완료

### 4. 파일 업로드
- AWS S3에 업로드 (폴더 구조: `posts/{postId}/{uuid}.확장자`)
- 게시글 상세 조회 시 파일 목록 포함
- 이미지 URL S3 주소로 조회 가능

### 5. 사용자 기능
- 회원가입 / 로그인(JWT)
- 이메일 중복 체크
- 로그인 유저 기준 글쓰기/좋아요/조회수 증가 기능 수행

---

## 🧪 테스트 코드
### 작성된 테스트 목록
- `PostServiceTest`
  - 조회수 중복 증가 방지
  - 제목/내용/작성자 통합검색 검증

- `PostLikeServiceTest`
  - 좋아요 토글(+1 → 0) 정상 동작 검증

---

## 🗄️ ERD (요약)

### 주요 테이블  
- users  
- posts  
- comments  
- post_likes  
- files  

관계:  
- user 1 : N post  
- post 1 : N comment  
- post 1 : N file  
- user/post : post_likes(중간 테이블)

---

## 📂 프로젝트 구조

```
src/main/java/com/example/board_service
 ├── controller
 ├── domain
 ├── dto
 ├── like
 ├── comment
 ├── file
 ├── repository
 ├── security
 └── service
```

---

## 📘 Swagger 문서 주소

http://localhost:8080/swagger-ui.html

---

## 🛠️ 실행 방법

### 1. 환경 변수 설정
```
AWS_ACCESS_KEY=xxx
AWS_SECRET_KEY=xxx
S3_BUCKET_NAME=board-service-jaewook-files
```

### 2. MySQL docker compose 실행
```
docker compose up -d
```

### 3. Spring Boot 실행
```
./gradlew bootRun
```

---

## 👨‍💻 Author
- Backend Developer: **이재욱(재순)**
- Email: wodnrdl7013@gmail.com

---

## 📄 License
MIT
