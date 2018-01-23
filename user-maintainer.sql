PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;
CREATE TABLE Comment (
        bibtexkey varchar(255) not null,
        id integer not null,
        alteration_date datetime,
        author varchar(255) not null,
        content varchar(255) not null,
        creation_date datetime not null,
        real_page_number varchar(255) not null,
        sequential_page_number integer not null,
        publish boolean not null,
        primary key (bibtexkey, id)
    );
CREATE TABLE Rating (
        bibtexkey varchar(255) not null,
        suggestion_id integer not null,
        rating integer not null,
        username varchar(255),
        primary key (bibtexkey, suggestion_id, username)
    );
CREATE TABLE User (
        username varchar(255) not null,
        email varchar(255) not null,
        first_name varchar(255) not null,
        last_name varchar(255) not null,
        password varchar(255) not null,
        salt blob not null,
        role varchar(255) not null,
        primary key (username)
    );
INSERT INTO User VALUES('maintainer','demo@example.org','Demo','Account','-2fa40176349855d6b07220f196f82200b876d9bfffa43168',X'41692618dc416d347bb448dcafe87d4d6df7a4c3','MAINTAINER');
COMMIT;
