FROM ubuntu:latest
LABEL authors="kiava"

ENTRYPOINT ["top", "-b"]