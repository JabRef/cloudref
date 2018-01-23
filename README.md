# CloudRef [![License:MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://tldrlegal.com/license/mit-license) [![Build Status](https://travis-ci.org/JabRef/cloudref.svg?branch=master)](https://travis-ci.org/JabRef/cloudref)

CloudRef is a cloud-based tool for managing bibliographical references. 
It supports collaborative work and quality assurance of references.
The software consists of an Angular application at the front end and a Java application at the back end.
For the front end the [admin template "ng2-admin" by Akveo](https://akveo.github.io/ng2-admin) is used.

![CloudRef Screenshot](https://user-images.githubusercontent.com/14543255/32387802-e67fa39a-c0c5-11e7-98bc-b69c441e4300.jpg)

## Start using Docker

1. `docker run jabref/cloudref`
2. Open <http://localhost:8080/>

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
