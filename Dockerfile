FROM reg.bitgadak.com/base:0.1

EXPOSE 10080

RUN mkdir -p /home/deployer/deploy
COPY ./build/libs/gateway.jar /home/deployer/deploy

ENTRYPOINT java -jar -Dspring.profiles.active=$PROFILE /home/deployer/deploy/gateway.jar
