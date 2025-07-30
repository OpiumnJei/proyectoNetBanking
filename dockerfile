# Usa una imagen base oficial que ya tiene Java 17 (JDK) en una versión ligera de Linux (Alpine).
FROM openjdk:17-jdk-alpine

# Copia el archivo .jar compilado de tu proyecto desde la carpeta 'target' al interior de la imagen y lo renombra.
COPY target/proyectoNetBanking-0.0.1-SNAPSHOT.jar netbanking-api-rest.jar

# Define el comando por defecto que se ejecutará al iniciar el contenedor: arrancar la aplicación Java.
ENTRYPOINT ["java", "-jar", "netbanking-api-rest.jar"]