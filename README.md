# RDBMS with Oracle & Spring Boot

Bu repo, i2i Academy staj programı kapsamında geliştirilen ilişkisel veri tabanı (RDBMS) ödevini içermektedir. 

Projede dışarıdan gelen ham veriler, Java (Spring Boot) üzerinden Oracle veritabanına iletilir. Verilerin XML/JSON formatlarına ayrıştırılıp ilişkili tablolara (Authors, Publishers, Books) yazılması işlemleri tamamen PL/SQL (XMLTABLE, JSON_TABLE) ile veritabanı katmanında çözülmüştür. Ayrıca tablo üzerindeki değişiklikleri loglayan bir trigger mekanizması barındırır.

## Kullanılan Teknolojiler
* Java & Spring Boot
* Oracle Database XE (Docker)
* Flyway (Migration)
* HikariCP (Connection Pool)
* PL/SQL (Stored Procedures, Functions, Triggers)

## Kurulum ve Çalıştırma

1. **Veritabanını Başlatın:**
   Proje ana dizininde aşağıdaki komutla Oracle konteynerini ayağa kaldırın:
   ```bash
   docker-compose up -d
