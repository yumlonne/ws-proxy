FROM hseeberger/scala-sbt

WORKDIR /root/project
RUN git clone https://github.com/yumlonne/ws-proxy
WORKDIR /root/project/ws-proxy
RUN sbt compile

EXPOSE 9000
CMD cd /root/project/ws-proxy && sbt `echo $CMD`
