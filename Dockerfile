FROM openjdk:8-jdk
EXPOSE 8080:8080

RUN wget -qO - https://www.mongodb.org/static/pgp/server-5.0.asc | apt-key add - \
    && echo "deb http://repo.mongodb.org/apt/debian buster/mongodb-org/5.0 main" | tee /etc/apt/sources.list.d/mongodb-org-5.0.list \
    && apt-get update \
    && apt-get install -y mongodb-org

RUN mkdir /app
RUN mkdir /data

COPY ./build/install/pickarr/ /app/
COPY ./mongod.conf /app/bin/mongod.conf
COPY ./pickarr.sh /app/bin/pickarr.sh

WORKDIR /app/bin
CMD ["./pickarr.sh"]