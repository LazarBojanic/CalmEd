# CalmEd

### Languages
- [English](README.md)
- [Serbian](README.sr.md)

### Preduslovi
- JDK 21 (https://www.azul.com/downloads/?version=java-21-lts&architecture=x86-64-bit&package=jdk#zulu)
- Android SDK _mora se instalirati preko Android Studia_ (https://developer.android.com/studio)
- PostgreSQL (https://www.postgresql.org/download/)
- IDE _poželjno Intellij_ (https://www.jetbrains.com/idea/download/)

### Podešavanje
- {backend_root_dir}: CalmEd/backend/calmed-backend
- {frontend_root_dir}: CalmEd/frontend/calmed-frontend-tourettes
- Otvoriti oba projekta posebno u Intellij-u.
- Na bekendu, obavezno usmerite konfiguraciju pokretanja EngineMain-a ka .env fajlu ({backend_root_dir}/src/resources/.env). EngineMain -> Edit Configurations -> Environment Variables
- U pgAdmin-u, kreirajte bazu podataka "calmed". Ili preko psql komandne linije: createdb -U postgres -h localhost calmed (password: postgres).
- Pokrenite server, a zatim pokrenite klijenta.