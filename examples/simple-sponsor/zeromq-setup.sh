#!/usr/bin/bash

wget https://github.com/zeromq/libzmq/releases/download/v4.2.5/zeromq-4.2.5.tar.gz
tar xvzf zeromq-4.2.5.tar.gz
cd zeromq-4.2.5
./configure
make install
ldconfig