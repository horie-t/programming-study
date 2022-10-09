FROM nvidia/cuda:11.7.1-runtime-ubuntu20.04

RUN apt-get -y update
RUN DEBIAN_FRONTEND=noninteractive apt-get -y install \
        python3-pip \
        python3-setuptools \
        ffmpeg \
        git \
         nginx \
         ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN pip --no-cache-dir install git+https://github.com/openai/whisper.git setuptools-rust flask gunicorn

ENV PYTHONUNBUFFERED=TRUE
ENV PYTHONDONTWRITEBYTECODE=TRUE
ENV PATH="/opt/program:${PATH}"

COPY src /opt/program
WORKDIR /opt/program
