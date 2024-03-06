# 개요

* 경기데이터드림 및 공공데이터포털 API를 이용하여 서울 및 경기도 버스 노선 수집 및 적재
* 사전에 정의한 표준 데이터 구조로 수집된 데이터 적재
* 프로젝트 초기 커밋 내 개인 정보 포함으로 인해 커밋 로그가 존재하지 않음 😭

### 표준 데이터 구조

- 노선 ID
- 버스번호 → 노선 번호
- 다음 둘 중 하나 필수(optional null), 둘 다 없을 수도 있음
    - 배차간격 (평일,토요일,공휴일)
        - 경기도(optional) → 최소 최대 배차시간으로 되어 있어서 평균값으로 저장함
    - 운행횟수
- 첫차/막차(서울o, 경기?)
- 기점 (정류장 이름)
- 종점

### 실행방법

1. MySQL 설치 및 실행
    1. MySQL 설치 및 실행
        ```text
        docker run --name bus-route-information -e MYSQL_ROOT_PASSWORD=root!23$ -d -p 3306:3306 mysql:latest
        ```
    2. Root 계정 접근 권한 부여
        ```sql 
        GRANT ALL PRIVILEGES ON *.* to 'root'@'%';
        flush privileges;
        ``` 
    2. bus_route_information database 생성
        ```sql
        CREATE DATABASE bus_route_information;
        ``` 

2. 경기데이터드림/공공데이터포털 API Key 발급

3. configuration 정의
    1. 최초 실행 시 spring.jpa.hibernate.ddl-auto: create

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_URL:localhost:3306/bus_route_information}?allowMultiQueries=true&useSSL=false&useUnicode=yes&characterEncoding=UTF-8&characterSetResults=UTF-8
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:root!23$}
  jpa:
    hibernate:
      ddl-auto: none

logging:
  level:
    root: info

data-portal:
  service-key: data-portal-service-key  // 공공데이터포털 API Key
data-dream:
  service-key: data-dream-service-key   // 경기데이터드림 API Key
```