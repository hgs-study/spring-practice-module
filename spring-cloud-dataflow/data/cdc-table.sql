-- Source 테이블 (CDC가 감시할 테이블)
CREATE TABLE source_table (
                              id SERIAL PRIMARY KEY,
                              name VARCHAR(255),
                              email VARCHAR(255),
);

-- Target 테이블 (JDBC sink가 저장할 테이블)
CREATE TABLE target_table (
                              id INTEGER PRIMARY KEY,
                              name VARCHAR(255),
                              email VARCHAR(255),
);

-- replication 권한 부여
ALTER TABLE source_table REPLICA IDENTITY FULL;