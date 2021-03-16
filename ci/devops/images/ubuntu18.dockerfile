FROM ubuntu:18.04

# Install libraries
RUN apt-get update -y && apt-get install -y \
    git \
    curl

# Install Nodejs
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash - \
    && apt-get install -y nodejs
    
# Install Yarn
RUN npm install -g yarn
