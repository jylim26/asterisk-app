FROM debian:bookworm-slim

ENV DEBIAN_FRONTEND=noninteractive
ENV ASTERISK_VERSION=22

RUN apt-get update && apt-get install -y \
    build-essential \
    wget \
    curl \
    gnupg \
    libedit-dev \
    libjansson-dev \
    libsqlite3-dev \
    libxml2-dev \
    libxslt1-dev \
    libncurses5-dev \
    libssl-dev \
    libsrtp2-dev \
    uuid-dev \
    libspeex-dev \
    libspeexdsp-dev \
    libopus-dev \
    libcurl4-openssl-dev \
    libnewt-dev \
    libpq-dev \
    unixodbc-dev \
    libvorbis-dev \
    libogg-dev \
    libresample1-dev \
    pkg-config \
    python3 \
    subversion \
    git \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src

RUN wget -q https://downloads.asterisk.org/pub/telephony/asterisk/asterisk-${ASTERISK_VERSION}-current.tar.gz \
    && tar xzf asterisk-${ASTERISK_VERSION}-current.tar.gz \
    && rm asterisk-${ASTERISK_VERSION}-current.tar.gz \
    && mv asterisk-${ASTERISK_VERSION}.* asterisk

WORKDIR /usr/src/asterisk

RUN ./configure --with-jansson-bundled --with-pjproject-bundled \
    && make menuselect.makeopts \
    && menuselect/menuselect \
        --enable codec_opus \
        --enable CORE-SOUNDS-EN-GSM \
        --enable EXTRA-SOUNDS-EN-GSM \
        menuselect.makeopts \
    && make -j$(nproc) \
    && make install \
    && make samples \
    && make config

RUN ldconfig

RUN adduser --system --group --no-create-home asterisk \
    && chown -R asterisk:asterisk /etc/asterisk /var/lib/asterisk /var/log/asterisk /var/spool/asterisk /var/run/asterisk

WORKDIR /etc/asterisk

EXPOSE 5060/udp 5060/tcp 5061/tcp 8088/tcp 8089/tcp 10000-10100/udp

CMD ["asterisk", "-f", "-U", "asterisk", "-G", "asterisk", "-vvvvv"]
