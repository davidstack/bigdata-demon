FROM registry.iop.com:5000/library/java:8u45-jdk
WORKDIR /root
RUN mkdir -p /root/app & \
    mkdir -p /root/app/libs/ & \
    mkdir -p /root/app/resources/
COPY target/bigdata-0.0.1-SNAPSHOT.jar /root/app/
COPY target/dependency/* /root/app/libs/
COPY src/main/resources/* /root/app/resources/
COPY start.sh /usr/local/bin/
RUN chmod 755 /usr/local/bin/start.sh
CMD ["/usr/local/bin/start.sh"]