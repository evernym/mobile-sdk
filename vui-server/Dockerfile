# !!! Download files from https://accelerator.evernym.com/portal/resources.html to your local folder !!!
FROM ubuntu:18.04
RUN apt-get update && \
    apt-get install -y \
    build-essential \
    curl \
    python3 \
    vim 

COPY libs/*  /tmp/
WORKDIR /tmp

# Add nodejs repo bits
RUN . /etc/os-release && \
    curl -f -s https://deb.nodesource.com/gpgkey/nodesource.gpg.key | apt-key add - && \
    echo "deb https://deb.nodesource.com/node_10.x ${UBUNTU_CODENAME} main" > /etc/apt/sources.list.d/nodesource.list && \
    apt-get update && \
    apt-get install -y nodejs

RUN apt install -y \
    ./libindy_*.deb \
    ./libnullpay_*.deb \
    ./indy-cli_*.deb \
    ./libvcx_*.deb

ARG USER=indy
RUN useradd -Um -d /home/$USER -s /bin/bash $USER

WORKDIR /home/$USER
RUN cp /tmp/node-vcx-wrapper_*.tgz node-vcx-wrapper.tgz && \
    npm install readline-sync node-vcx-wrapper.tgz 2>/dev/null

#Provision Faber agent
RUN /usr/share/libvcx/provision_agent_keys.py --enterprise-seed ABCDEFGHIJK012345678901234567890 https://eas01.pps.evernym.com 12345 > vcxconfig.json
RUN sed -i -e 's!"institution_name": "<CHANGE_ME>"!"institution_name": "'ThriftBank'"!' \
           -e 's!"institution_logo_url": "<CHANGE_ME>"!"institution_logo_url": "'https://freeiconshop.com/wp-content/uploads/edd/bank-flat.png'"!' \
           -e 's!"genesis_path": "<CHANGE_ME>"!"genesis_path": "'/home/indy/genesis.txn'"!' \
           -e 's!"genesis_path"!"author_agreement": "{\\"taaDigest\\": \\"8cee5d7a573e4893b08ff53a0761a22a1607df3b3fcd7e75b98696c92879641f\\",\\"acceptanceMechanismType\\":\\"on_file\\",\\"timeOfAcceptance\\": '"$(date +%s)"'}",\n  "genesis_path"!' vcxconfig.json

RUN npm install qrcode-terminal

#Copy scripts
COPY --chown=indy /*.js /genesis.txn /home/indy/

RUN node createSchemaAndCredDef.js
