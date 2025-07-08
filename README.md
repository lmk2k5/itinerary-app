| Path                             | Purpose                                                     |
| -------------------------------- | ----------------------------------------------------------- |
| `MainVerticle.java`              | App entry point where Vert.x is bootstrapped.               |
| `config/AppConfig.java`          | Loads and parses config from `config.json`.                 |
| `auth/AuthRouter.java`           | REST routes for sign-up and login.                          |
| `auth/JwtUtil.java`              | Handles JWT creation and validation.                        |
| `db/MongoService.java`           | Initializes MongoDB client and provides DB utility methods. |
| `handlers/ItineraryHandler.java` | CRUD logic for itinerary items.                             |
| `models/User.java`               | Represents a user document in MongoDB.                      |
| `models/ItineraryItem.java`      | Represents a location entry in the user's itinerary.        |
| `resources/config.json`          | Your "dotenv" equivalent; contains DB URL, secret key, etc. |
| `.gitignore`                     | Ignore `target/`, IDE folders, `.env`, etc.                 |
| `pom.xml`                        | Maven project configuration.                                |