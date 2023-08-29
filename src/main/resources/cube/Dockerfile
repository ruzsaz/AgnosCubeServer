FROM openjdk:17-slim
WORKDIR /
RUN mkdir /usr/local/agnos/ /usr/local/agnos/Products
ENV AGNOS_HOME=/usr/local/agnos/Products
ADD ./AgnosCubeServer.jar cube-server.jar
EXPOSE 7979
CMD java -jar cube-server.jar