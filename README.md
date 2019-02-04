A SEO Spider in Java

How can I create an executable JAR with dependencies using Maven?
mvn clean compile assembly:single

run crawler
java -jar SEO-Spider-1.0.0-SNAPSHOT-jar-with-dependencies.jar https://is.net.sa 10000 20 1 1

drop connections
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'TARGET_DB' -- ← change this to your DB
  AND pid <> pg_backend_pid();
  
Current problems :-

Url decoding problem (accept arabic chars)
https://is.net.sa/%d8%a3%d8%b9%d9%85%d8%a7%d9%84%d9%86%d8%a7-%d8%a7%d9%84%d9%85%d8%aa%d9%85%d9%8a%d8%b2%d8%a9/
https://is.net.sa/%D8%A3%D8%B9%D9%85%D8%A7%D9%84%D9%86%D8%A7-%D8%A7%D9%84%D9%85%D8%AA%D9%85%D9%8A%D8%B2%D8%A9/

https://is.net.sa/%d8%a3%d8%b9%d9%85%d8%a7%d9%84%d9%86%d8%a7-%d8%a7%d9%84%d9%85%d8%aa%d9%85%d9%8a%d8%b2%d8%a9/
https://is.net.sa/%D8%A3%D8%B9%D9%85%D8%A7%D9%84%D9%86%D8%A7-%D8%A7%D9%84%D9%85%D8%AA%D9%85%D9%8A%D8%B2%D8%A9/