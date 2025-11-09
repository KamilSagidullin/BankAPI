FROM ubuntu:latest
LABEL authors="kamilsagidullin"

ENTRYPOINT ["top", "-b"]