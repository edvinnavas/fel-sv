# FETCH BASIC IMAGE.
FROM maven:3.8.5-jdk-11

# APPLICATION PLACED INTO /opt/app.
RUN mkdir -p /FELSV3/py
RUN chmod -R 777 /FELSV3/py
RUN mkdir -p /FELSV3/pd
RUN chmod -R 777 /FELSV3/pd
RUN mkdir -p /FELSV3/pdf
RUN chmod -R 777 /FELSV3/pdf
RUN mkdir -p /FELSV3/pdf_pd
RUN chmod -R 777 /FELSV3/pdf_pd
RUN mkdir -p /FELSV3/json
RUN chmod -R 777 /FELSV3/json
RUN mkdir -p /FELSV3/json_pd
RUN chmod -R 777 /FELSV3/json_pd
RUN mkdir -p /opt/app
RUN echo "America/Guatemala" > /etc/timezone
WORKDIR /opt/app

# SELECTIVELY ADD THE POM FILE AND INSTALL DEPENDENCIES.
COPY pom.xml /opt/app/
RUN mvn install

# REST OF THE PROJECTS.
COPY src /opt/app/src
RUN mvn package

# LOCAL APPLICATION PORT
EXPOSE 8080

# EXECUTE IT
CMD ["mvn", "exec:java"]
