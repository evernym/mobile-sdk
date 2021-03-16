FROM python:3.7-stretch

RUN apt-get update && apt-get install -y \
  maven openjdk-8-jdk \
  jq

ADD e2e-automation/appium-launcher/requirements.txt requirements.txt
RUN pip install -r requirements.txt

# Install Ngrok
RUN curl -O -s https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.zip && \
    unzip ngrok-stable-linux-amd64.zip && \
    cp ngrok /usr/local/bin/.

