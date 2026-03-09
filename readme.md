# Bravery Alive - League of Legends Build Randomizer

A Spring Boot web application that generates random League of Legends builds with a p5.js frontend.

## Quick Start

### Prerequisites
- Java 19 or higher
- Maven 3.6+ (installed at `C:\DEV\apache-maven-3.9.13`)

### Build and Run

1. **Build the application:**
   ```cmd
   cd src
   build.bat package
   ```

2. **Run the server:**
   ```cmd
   build.bat run
   ```

3. **Open in browser:**
   ```
   http://localhost:8080
   ```

### Manual Commands

If you prefer to run Maven directly:

```cmd
# Build
"C:\DEV\apache-maven-3.9.13\bin\mvn.cmd" clean package -DskipTests

# Run
java -jar target\BraveryAlive-1.0-SNAPSHOT.jar
```

## Features

- **Random Build Generation**: Generates complete LoL builds with champion, mastery, summoner spells, and items
- **Web Interface**: Clean p5.js canvas interface with buttons for roll, copy, and export
- **Export Options**: Export builds to Discord format or League client JSON
- **Image Loading**: Automatically loads champion and item images from Riot's CDN

## Project Structure

```
src/
├── main/
│   ├── java/com/braveryalive/
│   │   ├── WebApplication.java          # Spring Boot entry point
│   │   ├── controller/
│   │   │   └── BuildController.java     # REST API endpoints
│   │   ├── service/
│   │   │   ├── BuildGenerator.java      # Build randomization logic
│   │   │   ├── DataLoader.java          # Riot API data loading
│   │   │   ├── BuildExporter.java       # Export interface
│   │   │   ├── DiscordExporter.java     # Discord format export
│   │   │   └── LeagueExporter.java      # League client export
│   │   ├── model/
│   │   │   ├── Build.java               # Domain model
│   │   │   ├── Pair.java                # Image/name pair
│   │   │   ├── BuildResponse.java       # API response DTO
│   │   │   └── ExportResponse.java      # Export response DTO
│   │   └── config/
│   │       ├── Skill.java               # Skill enum
│   │       ├── Mastery.java             # Mastery enum
│   │       └── Constants.java           # UI constants
│   └── resources/
│       └── static/
│           ├── index.html               # Main HTML page
│           └── sketch.js                # p5.js frontend
└── pom.xml                              # Maven configuration
```

## API Endpoints

- `GET /api/roll` - Generate a random build
- `GET /api/version` - Get current game version
- `GET /api/export/discord` - Export last build to Discord format
- `GET /api/export/league` - Export last build to League client format  
