FROM centos:6

COPY docker-setup.sh /
COPY hadoop-setup.sh /
COPY hadoop-start.sh /

RUN /docker-setup.sh

CMD ["/bin/bash"]
