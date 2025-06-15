docker stop synDns
docker rm synDns
docker rmi synDns
docker build -t syndns .
docker run -id --name=synDns --restart=always -p 8089:8089 syndns
