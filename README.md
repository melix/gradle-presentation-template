# Asciidoctor/Reveal.js presentation template

This is a template project for Asciidoctor+Reveal.js presentations.
To use it, just clone this repository and update the presentation as you wish.

## Configuration

Minimally, update the `build.gradle.kts` file to set your GitHub username:

```
presentation {
    githubUserName.set("yourUserName")
}
```

## Generating the presentation

Run:

```
./gradlew asciidoctor
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
