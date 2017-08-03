# Beachfront Data Migration Utility

Migration tool for moving existing Beachfront Piazza Jobs into the new PostgreSQL database. 

## Web Application

This project is hosted as a web application such that it could be deployed into Cloud Foundry (assuming one creates a manifest) and run the Migration from that environment.

## Usage

This application targets a running Piazza instance to fetch data. It does not use MongoDB (or any existing data store) to fetch data.

This will store the data in the specified database, as configured in the `application.properties` file. 

To run the migration, start the server with `clean install -U spring-boot:run` and hit the running endpoint at `http://localhost:8080/migrate/int?doCommit=true`  You can change the environment from `int` to `stage` to `prod`. The `doCommit` flag can be used in order to test the migration _without_ actually committing to the database. *Only set this flag to true if you know what you are doing* as this will affect any database that you have configured.
