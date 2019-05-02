# Asciidoctor/Reveal.js presentation template

This is a template project for Asciidoctor+Reveal.js presentations.
To use it, just fork this repository and update the presentation as you wish.

## Configuration

Minimally, update the `build.gradle.kts` file to set your GitHub username:

```
presentation {
    githubUserName.set("yourUserName")
}
```

and change the name of the project in `settings.gradle.kts`:

```
rootProject.name = "my-awesome-presentation"
```

## Generating the presentation

Run:

```
./gradlew asciidoctor
```

If you want to enable the watch mode, you can also run:

```
./gradlew asciidoctor --continous
```

## Uploading the slides on GitHub pages

Run:

```
./gradlew publishGhPages
```

If your repository name is different from the name of the project in `settings.gradle.kts`, update the configuration in `build.gradle.kts`:

```
presentation {
    githubUserName.set("yourUserName")
    githubRepoName.set("yourRepoName")
}
```

Then the presentation is going to be available at `https://yourUserName.github.io/yourRepoName/`

## Screencasts

This template supports screencasts in the [asciinema](https://asciinema.org/) format.
Include your JSON files in `src/docs/asciidoc/screencasts`, then include them in your presentation using:

```
screencast:my_file[]
```

## Exporting the presentation

This template supports exporting the presentation to PDF, JPEG and PNG.
You'll need a JDK which bundles JavaFX to do this.
Run this task:

```
./gradlew exportToPdf
```

to generate a PDF, or:

```
./gradlew export
```

to export to all formats.
