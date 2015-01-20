## Setup

If not already installed, download and install the [Typesafe Activator][typesafe-activator] so the `activator` command is available. The minimal version is fine to download as it will fetch any additional dependencies as needed.

Clone the application:

```
$ git clone git@github.com:atsheehan/neon.git
$ cd neon
```

Create the database (requires PostgreSQL to be running):

```
$ psql < dbsetup.sql
```

Run the `activator` which will resolve any dependencies and open the console:

```
$ activator

[info] Loading project definition from /home/asheehan/work/neon/project
[info] Set current project to neon (in build file:/home/asheehan/work/neon/)
[neon] $
```

Issue the `run` command to start the server on port 9000:

```
[neon] $ run

--- (Running the application, auto-reloading is enabled) ---

[info] play - Listening for HTTP on /0:0:0:0:0:0:0:0:9000

(Server started, use Ctrl+D to stop and go back to the console...)
```

Visit the server running at [http://localhost:9000][localhost-9000].

[typesafe-activator]: https://typesafe.com/get-started
[localhost-9000]: http://localhost:9000
