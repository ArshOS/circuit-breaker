CS549 (Design Patterns for highly Scalable System)
Assignment-4

Members:
1. Md Arshad Jamal
2. Aditya Pandey
3. Nalini Singh

#################################################################################################
To run the project execute below command in terminal from the directory containing pom.xml
mvn compile exec:java -Dexec.mainClass="docs.http.javadsl".JacksonExampleTest
##################################################################################################

Monitoring the process using pid for memory consumption.

arsh@arsh-ubuntu:~$ jps
10546 Jps
9635 Launcher
9638 JacksonExampleTest
2615 Main

arsh@arsh-ubuntu:~$ jstat -gc 9638
 S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT    CGC    CGCT     GCT
 0.0   4096.0  0.0   4096.0 38912.0   9216.0   33792.0     6835.6   45312.0 43697.1 5888.0 5189.3      5    0.052   0      0.000   4      0.021    0.074

arsh@arsh-ubuntu:~$ jstat -gc 9638 | tail -n 1 | awk '{split($0,a," "); sum=a[3]+a[4]+a[6]+a[8]; print sum}'
20147.6
##############################################################################################################