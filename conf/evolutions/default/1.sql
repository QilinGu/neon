# --- !Ups
CREATE TABLE questions (
       id serial,
       title varchar(255) NOT NULL,
       body text NOT NULL,
       created_at timestamp NOT NULL
);

# --- !Downs

DROP TABLE questions;
