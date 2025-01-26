Goldbook

Goldbook is an Android application designed to assist users in managing and tracking their gold investments. The app provides real-time gold price updates, investment portfolio management, and analytical tools to help users make informed decisions.

Description

The Goldbook application offers a user-friendly interface with the following key features:
	•	Real-Time Gold Prices: Stay updated with the latest gold prices sourced from reliable financial APIs.
	•	Investment Portfolio Management: Add, update, and monitor your gold assets, including purchase dates, quantities, and current valuations.
	•	Analytical Tools: Access charts and insights to analyze investment performance over time.

Interesting Techniques

This project employs several noteworthy techniques:
	•	Model-View-ViewModel (MVVM) Architecture: Separates the development of the graphical user interface from the business logic, enhancing code maintainability and testability.
	•	Data Binding: Facilitates automatic synchronization between the UI components and the underlying data models, reducing boilerplate code.
	•	LiveData: Ensures that the UI reflects the latest data by observing data changes in a lifecycle-aware manner.

Libraries and Technologies

In addition to standard Android development tools, Goldbook integrates the following libraries:
	•	Retrofit: A type-safe HTTP client for Android and Java, used for network operations.
	•	Glide: An image loading and caching library for Android, utilized for efficient image handling.
	•	Room: A persistence library that provides an abstraction layer over SQLite, streamlining database access.

Project Structure

The repository is organized as follows:

<pre>
.
├── .idea/                  # Project-specific settings for the IDE
├── app/                    # Main application source code
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── example/
│   │   │   │           └── goldbook/
│   │   │   │               ├── ui/             # UI components and activities
│   │   │   │               ├── data/           # Data models and repositories
│   │   │   │               ├── viewmodel/      # ViewModel classes
│   │   │   │               └── utils/          # Utility classes
│   │   │   ├── res/                            # Resource files (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml             # Application manifest file
│   ├── build.gradle                            # Module-level Gradle build file
├── gradle/
│   └── wrapper/            # Gradle wrapper files
├── .gitignore              # Specifies files to ignore in version control
├── build.gradle            # Build configuration script
├── gradle.properties       # Gradle properties
├── gradlew                 # Unix shell script to run Gradle
├── gradlew.bat             # Windows batch script to run Gradle
└── settings.gradle         # Settings for the Gradle build
</pre>


Highlights in the Structure
	•	.idea/: Contains IDE-specific configurations and settings.
	•	app/: Houses the main application code, including source files and resources.
	•	gradle/wrapper/: Contains the Gradle Wrapper files, enabling consistent build environments.
