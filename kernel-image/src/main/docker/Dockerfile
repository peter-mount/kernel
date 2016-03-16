FROM area51/java:serverjre-8
MAINTAINER Peter Mount <peter@retep.org>

# Where the jars are located
ENV KERNEL_LIB     /opt/kernel

ADD ${project.build.finalName}-assembly.tar /opt

CMD ["/opt/kernel/start"]
