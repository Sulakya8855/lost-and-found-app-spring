FROM mysql:latest

ENV MYSQL_DATABASE=lost_and_found_db
ENV MYSQL_USER=myuser
ENV MYSQL_PASSWORD=mypassword
ENV MYSQL_RANDOM_ROOT_PASSWORD=yes
# MYSQL_ROOT_PASSWORD is also required, but for simplicity in this setup,
# we'll let the image use a generated one or you can set it explicitly if needed.
# For example: ENV MYSQL_ROOT_PASSWORD=supersecret

EXPOSE 3306 