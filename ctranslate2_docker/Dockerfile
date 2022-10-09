FROM ubuntu:22.04

RUN apt-get -y update
RUN apt-get -y install python3 pip
RUN pip install ctranslate2 OpenNMT-py sentencepiece

COPY src/translate.py /usr/bin/
WORKDIR /var/opt/ctranslate2

ENTRYPOINT [ "translate.py" ]
CMD [ "Hello world!" ]