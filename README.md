# CalmEd

### Language
- [Serbian](README.sr.md)

### Requirements
- JDK 21 (https://www.azul.com/downloads/?version=java-21-lts&architecture=x86-64-bit&package=jdk#zulu)
- Android SDK _must be installed via Android Studio_ (https://developer.android.com/studio)
- PostgreSQL (https://www.postgresql.org/download/)
- IDE _preferably IntelliJ_ (https://www.jetbrains.com/idea/download/)

### Setup
- {backend_root_dir}: CalmEd/backend/calmed-backend
- {frontend_root_dir}: CalmEd/frontend/calmed-frontend-tourettes
- Open each project separately in IntelliJ.
- On the backend, make sure to point the EngineMain run configuration to the .env file ({backend_root_dir}/src/resources/.env). EngineMain -> Edit Configurations -> Environment Variables
- In pgAdmin, create a database "calmed". Or through the psql command line: createdb -U postgres -h localhost calmed (password: postgres).
- Run the Server, then run the Client.