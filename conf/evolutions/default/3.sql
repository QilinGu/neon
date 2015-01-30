# --- !Ups

ALTER TABLE questions ADD COLUMN user_id integer NOT NULL REFERENCES users(id);

CREATE INDEX questions_user_id ON questions (user_id);

# --- !Downs

DROP INDEX questions_user_id;

ALTER TABLE questions DROP COLUMN user_id;
