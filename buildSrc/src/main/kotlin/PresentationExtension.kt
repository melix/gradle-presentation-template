import org.gradle.api.Project

open class PresentationExtension(project: Project) {

    val revealjsVersion = project.convention("3.5.0")
    val asciidoctorBackendVersion = project.convention("2.0.0")
    val asciidoctorJVersion = project.convention("1.5.6")
    val githubUserName = project.property<String>()
    val githubRepoName = project.convention(project.name)
    val width = project.convention(1280)
    val height = project.convention(700)
    val theme = project.convention("gradle")
    val highlighter = project.convention("highlightjs")

    private
    inline fun <reified T> Project.property() = objects.property(T::class.java)

    private
    inline fun <reified T> Project.convention(defaultValue: T) = objects.property(T::class.java).convention(defaultValue)
}