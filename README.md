# backend_repository

Git branch 전략: GitHub Flow 방식
추후 GitHub Actions를 통하여 AWS beanstalk EC2 배포 전략

<h2> ERD </h2>  </br>
<img width="1336" alt="스크린샷 2024-12-14 오전 9 27 09" src="https://github.com/user-attachments/assets/94c1740a-dded-407b-bc61-44bd916fbdaf" />

<h2>주요 기능</h2> 

- 회원 
  - smtp 이메일 인증(java mailsender)을 통한 숙명여대 도메인 검증 기능
  - JWT token과 Spring Security 활용 인가 기능
  - Redis를 통한 빠른 refresh token 액세스
  - 회원은 USER, ADMIN 두 가지 역할 구분
  - 추후 신고 기능을 넣기 위하여 회원 계정 상태 enum 
  
- 커뮤니티 
  - 게시글에는 텍스트와 이미지 첨부 가능(AWS S3 활용한 이미지)
  - 댓글과 대댓글 기능
  - 각 게시글과 댓글은 좋아요 기능 

- 커리어 카드
  - 커피챗을 위한 각 회원의 소개 카드
  - 카드를 스와이프하며 채팅 혹은 보관하기(커리어카드 보관함)
  - 첫 회원 가입 시 커리어카드는 생성 필수

- 채팅, 알람
  - WebSocket 활용 실시간 채팅 기능
  - RabbitMQ를 통한 여러 인스턴스에서도 공유 가능
  - SSE를 통한 알람 기능
    
- 회원 문의
  - 고객 문의함 기능
  
- 마이 페이지
  - 비밀번호 변경, 회원 탈퇴 등의 계정 관리
  - 내가 쓴 커리어카드 및 커뮤니티 활동 표시

<h2>기술 스택</h2> 
Front: React
Server: Spring
DB: MySQL,Redis
Cloud/infra: Elasitc Cache, EC2, GitHub Actions, Docker
