FROM ubuntu:bionic
ENV DEBIAN_FRONTEND="noninteractive" TZ="Europe/Moscow"
ARG module
RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    gnupg \
    pbuilder \
    ubuntu-dev-tools \
    apt-file \
    software-properties-common \
    ca-certificates

RUN update-ca-certificates -f -v

RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys CE7709D068DB5E88 &&\
    add-apt-repository "deb https://repo.sovrin.org/sdk/deb bionic stable"

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    python3 \
    python3-pip \
    pkg-config \
    build-essential \
    autoconf \
    automake \
    uuid-dev \
    wget && \
    # Install libvdrtools
    curl https://gitlab.com/evernym/verity/vdr-tools/-/package_files/27311920/download --output libvdrtools_0.8.4-bionic_amd64.deb && \
    apt-get install -y ./libvdrtools_0.8.4-bionic_amd64.deb

COPY . /app
WORKDIR /app
RUN sh zeromq-setup.sh

RUN pip3 install -r requirements.txt
EXPOSE 4321
CMD [ "python3", "server.py" ]
