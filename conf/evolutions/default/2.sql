# --- !Ups
CREATE TABLE users (
       id serial,
       username varchar(255) NOT NULL,
       github_id integer NOT NULL,
       created_at timestamp NOT NULL
);

# --- !Downs

DROP TABLE users;
