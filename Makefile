.PHONY: test build server docker

test:
	mvn test

build:
	mvn clean package

client:
	mvn clean javafx:run -Djavax.net.ssl.trustStore=src/main/resources/mytruststore.jks -Djavax.net.ssl.trustStorePassword=husksheets

server:
	mvn exec:java -Dexec.mainClass="com.group12.husksheets.HusksheetsServer"

docker:
	docker build -t husksheets-server .
	docker run -p 9443:9443 husksheets-server