# Shipment Q&A
A sample Shipment Q&A application demonstrating the usage of RAG(Retreival Augmented generation) in Java based applications.

It consist of 2 modules

* shipment-qna-app
* shipment-qna-ui

# shipment-qna-ui

A basic react SPA with a chatbox window. This application provides interfaces for users to ask queries regarding the policies for their shipments.

### How to run?
**This will require node installed on your machine**
```
npm start
```
# shipment-qna-app

A spring boot REST api, which exposes two endpoints which is called from the UI application. This application allows users to upload the policies documents, which will be stored in the vector database. The users can then ask questions against those uploaded documents.

### How to run?
**This will require a docker runtime on your machine.**
1. go to project root directory on the command line and run the following command
    ```
    docker build -t shipment-qna-app .
    ```
2. Run the folowing command. 
    ```
    docker run -p 8080:8080 shipment-qna-app
    ```
