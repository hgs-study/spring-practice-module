### Gradle 의존성 확인
```shell
./gradlew dependencies --configuration compileClasspath

---

Welcome to Gradle 7.6.1!

Here are the highlights of this release:
 - Added support for Java 19.
 - Introduced `--rerun` flag for individual task rerun.
 - Improved dependency block for test suites to be strongly typed.
 - Added a pluggable system for Java toolchains provisioning.

For more details see https://docs.gradle.org/7.6.1/release-notes.html


> Task :dependencies

------------------------------------------------------------
Root project 'pir'
------------------------------------------------------------

compileClasspath - Compile classpath for source set 'main'.
+--- org.projectlombok:lombok -> 1.18.26
+--- org.springframework.boot:spring-boot-starter-web -> 2.7.9
|    +--- org.springframework.boot:spring-boot-starter:2.7.9
|    |    +--- org.springframework.boot:spring-boot:2.7.9
|    |    |    +--- org.springframework:spring-core:5.3.25
|    |    |    |    \--- org.springframework:spring-jcl:5.3.25
|    |    |    \--- org.springframework:spring-context:5.3.25
|    |    |         +--- org.springframework:spring-aop:5.3.25

```

