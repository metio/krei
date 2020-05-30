FROM alpine:edge

LABEL org.opencontainers.image.authors="Sebastian Hoß <seb@hoß.de>"
LABEL org.opencontainers.image.vendor="metio.wtf"
LABEL org.opencontainers.image.url="https://krei.projects.metio.wtf/"
LABEL org.opencontainers.image.title="krei-build-env"
LABEL org.opencontainers.image.description="Image which contains all required software to build the entire krei project"

ENV JAVA_HOME /opt/openjdk-15
ENV PATH $JAVA_HOME/bin:$PATH

ENV JAVA_VERSION 15-ea+10
ENV JAVA_URL https://download.java.net/java/early_access/alpine/10/binaries/openjdk-15-ea+10_linux-x64-musl_bin.tar.gz
ENV JAVA_SHA256 15a5e8002e24ed129b82bfe55ffe4bdbf3cfd0a7e5ad3399879cdd44175bfd06

RUN set -eux; \
    \
    wget -O /openjdk.tgz "$JAVA_URL"; \
    echo "$JAVA_SHA256 */openjdk.tgz" | sha256sum -c -; \
    \
    mkdir -p "$JAVA_HOME"; \
    tar --extract --file /openjdk.tgz --directory "$JAVA_HOME" --strip-components 1; \
    rm /openjdk.tgz; \
    \
# https://github.com/docker-library/openjdk/issues/212#issuecomment-420979840
# https://openjdk.java.net/jeps/341
    java -Xshare:dump; \
    \
    apk add maven hugo; \
    java --version; \
    javac --version; \
    mvn --version; \
    hugo version;

CMD ["jshell"]
