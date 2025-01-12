FROM openjdk:17-slim
WORKDIR /
RUN mkdir /usr/local/agnos/ /usr/local/agnos/Cubes
ENV AGNOS_CUBES_DIR=/usr/local/agnos/Cubes
ENV CUBES_MEMORY_IN_BYTES=4000000000
ADD ./AgnosCubeServer.jar cube-server.jar
EXPOSE 7979
CMD java -jar -Dspring.profiles.active=prod cube-server.jar