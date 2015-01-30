# --- !Ups
CREATE TABLE users (
       id serial PRIMARY KEY,
       username varchar(255) NOT NULL,
       github_id integer NOT NULL UNIQUE,
       created_at timestamp NOT NULL
);

# --- !Downs

DROP TABLE users;
