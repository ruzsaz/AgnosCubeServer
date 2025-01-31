FROM openjdk:17-slim

WORKDIR /
RUN mkdir /usr/local/agnos/ /usr/local/agnos/Cubes
ENV AGNOS_CUBES_DIR=/usr/local/agnos/Cubes
ADD ./AgnosCubeServer.jar cube-server.jar
EXPOSE 7979

CMD java -Xmx"${CUBESERVER_XMX}" -jar -Dspring.profiles.active=prod cube-server.jar
