FROM ubuntu:16.04

COPY docker-setup.sh /
COPY hadoop-setup.sh /
COPY hadoop-start.sh /

RUN /docker-setup.sh

CMD ["/bin/bash"]
