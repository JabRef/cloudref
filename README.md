# CloudRef [![License:MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://tldrlegal.com/license/mit-license) [![Build Status](https://travis-ci.org/JabRef/cloudref.svg?branch=master)](https://travis-ci.org/JabRef/cloudref) [![Help Contribute to Open Source](https://www.codetriage.com/jabref/cloudref/badges/users.svg)](https://www.codetriage.com/jabref/cloudref) [![Join Open Source Firday](https://img.shields.io/badge/Open%20Source-Friday-60B37A.svg)](https://opensourcefriday.com/)

> CloudRef is a web-based tool for managing bibliographical references.

> Scientific publication: [Oliver Kopp, Uwe Breitenbücher, Tamara Müller:
CloudRef – Towards Collaborative Reference Management in the Cloud. ZEUS 2018, CEUR-WS.org](http://ceur-ws.org/Vol-2072/paper10.pdf)

CloudRef supports collaborative work and quality assurance of references.
The software consists of an Angular application at the front end and a Java application at the back end.
For the front end the [admin template "ng2-admin" by Akveo](https://akveo.github.io/ng2-admin) is used.

![CloudRef Screenshot](https://user-images.githubusercontent.com/14543255/32387802-e67fa39a-c0c5-11e7-98bc-b69c441e4300.jpg)

## Start using Docker

1. `docker run -p 127.0.0.1:8080:8080 jabref/cloudref`
2. Open <http://localhost:8080/>
4. Use `maintainer`/`developer` as login

The user `maintainer` has full rights for merging references.
Other users can just be created via the login form and cannot merge if the threshold of 3 was not reached.

Note that the data is stored inside the Docker container and might get lost.
To connect a local folder (e.g., `D:\CloudRef`) to docker, use following command:

    docker run --rm -v"D:/CloudRef:/root/CloudRef" -p 127.0.0.1:8080:8080 jabref/cloudref

### Local build and start

1. `docker build -t cloudref .`
2. `docker run -p 127.0.0.1:8080:8080 cloudref`
3. Open <http://localhost:8080/>
4. Use `maintainer`/`developer` as login

## Installation

### Installation of required software

1. Install [node.js](https://nodejs.org/en/)
2. Install [Java JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

**Node version >= 6.0 and NPM version >= 3 required!**

Versions can be checked with:

```
node -v
npm -v
```

### Get application and install dependencies

1. Clone repository or download .zip file
2. Navigate into the "frontend" folder of the project
3. Install dependencies

       npm install
       
### Create CloudRef.sqlite in your users home directory

1. Download the SQLite Command Line Shell ("sqlite-tools") from <https://sqlite.org/download.html>.
2. Copy `sqlite3.exe` into the folder where the database is stored `{USER_DIRECTORY}/CloudRef`
3. Run `sqlite3.exe`
4. Run
```
    .open CloudRef.sqlite
```
5. Run the statements from user-maintainer.sql in the sqlite3 shell


### Start application

1. Front end: run following command in the "frontend" folder

       npm start

2. Back end: run following command in the "backend" folder

       ./gradlew run

The application is available at <http://localhost:4200> and a Swagger definition of the RESTful web service of the back end at <http://localhost:8080/swagger.json>.

## Set role of user to 'MAINTAINER'

A user with the role 'MAINTAINER' can additionally edit a suggestion for modification.
Furthermore, he can accept and reject suggestions directly.
The role of a user cannot be changed through the user interface but in the database.

Possibility to change the role:

1. Download the SQLite Command Line Shell ("sqlite-tools") from <https://sqlite.org/download.html>.
2. Copy `sqlite3.exe` into the folder where the database is stored `{USER_DIRECTORY}/CloudRef`
3. Open `sqlite3.exe` file
4. Run

       .open CloudRef.sqlite
       UPDATE User
       SET role = 'MAINTAINER'
       WHERE username = {username};

## Development

We needed to change the `basePath` in `DefaultApi.ts` as follows

    protected basePath = location.protocol + '//' + location.hostname + ':' + location.port === '4200' ? '8080' : location.port;

In case you regenerate `DefaultApi.ts`, please patch this line.
