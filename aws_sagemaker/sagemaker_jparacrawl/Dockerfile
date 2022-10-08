FROM nvidia/cuda:11.7.1-runtime-ubuntu20.04

RUN apt-get -y update
RUN apt-get -y install python3 pip

RUN pip --no-cache-dir install OpenNMT-py

ENV PYTHONUNBUFFERED=TRUE
ENV PYTHONDONTWRITEBYTECODE=TRUE
ENV PATH="/opt/program:${PATH}"

COPY src /opt/program
WORKDIR /opt/program
