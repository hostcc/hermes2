FROM centos:latest
LABEL hermes2="latest"

RUN yum install -y unzip curl sqlite

# Apache Tomcat
# http://archive.apache.org/dist/tomcat/tomcat-6/v${TOMCAT_VER}/bin/apache-tomcat-${TOMCAT_VER}.zip
ENV TOMCAT_VER 6.0.18
ENV TOMCAT_DESTDIR /opt

WORKDIR ${TOMCAT_DESTDIR}
COPY apache-tomcat-${TOMCAT_VER}.zip .
RUN unzip apache-tomcat-${TOMCAT_VER}.zip; rm -f apache-tomcat-${TOMCAT_VER}.zip
RUN find apache-tomcat-${TOMCAT_VER}/bin/ -name '*.sh' -exec chmod +x {} ';'
COPY tomcat-users.xml apache-tomcat-${TOMCAT_VER}/conf/tomcat-users.xml

# Oracle JRE
ENV JAVA_FILEVER jre-6u7
ENV JAVA_VER jre1.6.0_07
ENV JAVA_DESTDIR /opt

WORKDIR ${JAVA_DESTDIR}
COPY ${JAVA_FILEVER}-linux-x64.bin .
RUN echo "yes" | sh ${JAVA_FILEVER}-linux-x64.bin; rm -f ${JAVA_FILEVER}-linux-x64.bin
ENV JAVA_HOME ${JAVA_DESTDIR}/${JAVA_VER}

WORKDIR ${JAVA_HOME}
ENV JCE_POLICY jce_policy-6.zip
# Unlimited JCE
COPY ${JCE_POLICY} .
RUN unzip ${JCE_POLICY} -d lib/security *.jar; rm -f ${JCE_POLICY}

ENV CORVUS_DESTDIR /corvus

WORKDIR ${CORVUS_DESTDIR}
# Application
COPY Corvus/dist/corvus.war ${TOMCAT_DESTDIR}/apache-tomcat-${TOMCAT_VER}/webapps
# Plugins
COPY */dist/*.spa plugins/
# SQL scripts
COPY sql/* sql/
# Log directory
RUN mkdir logs
# AS2 repository
RUN mkdir repository
# Create AS2 SQLite database
RUN sqlite3 as2.db < sql/as2.sql
# Create Ebms SQLite database
RUN sqlite3 ebms.db < sql/ebms.sql

EXPOSE 8080
CMD ${TOMCAT_DESTDIR}/apache-tomcat-${TOMCAT_VER}/bin/catalina.sh run
