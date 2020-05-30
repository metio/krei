FROM maven:slim

LABEL org.opencontainers.image.authors="Sebastian Hoß <seb@hoß.de>"
LABEL org.opencontainers.image.vendor="metio.wtf"
LABEL org.opencontainers.image.url="https://krei.projects.metio.wtf/"
LABEL org.opencontainers.image.title="krei-build-env"
LABEL org.opencontainers.image.description="Image which contains all required software to build the entire krei project"

RUN apt-get update && apt-get install hugo -y; \
    java --version; \
    javac --version; \
    mvn --version; \
    hugo version;

CMD ["jshell"]
