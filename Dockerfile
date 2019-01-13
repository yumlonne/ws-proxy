FROM hseeberger/scala-sbt

COPY project /root/project/ws-proxy/project
COPY build.sbt /root/project/ws-proxy/build.sbt
WORKDIR /root/project/ws-proxy
RUN sbt clean
RUN sbt reload update
COPY . /root/project/ws-proxy
WORKDIR /root/project/ws-proxy
RUN sbt clean
RUN sbt compile

EXPOSE 9000
CMD cd /root/project/ws-proxy && sbt `echo $CMD`
