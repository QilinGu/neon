CREATE ROLE neon LOGIN;
ALTER ROLE neon WITH PASSWORD 'thenoblegas';

CREATE DATABASE neon_development;
ALTER DATABASE neon_development OWNER TO neon;
