import com.github.jrubygradle.JRubyPlugin
import org.ajoberstar.gradle.git.ghpages.GithubPagesPlugin
import org.ajoberstar.gradle.git.ghpages.GithubPagesPluginExtension
import org.asciidoctor.gradle.AsciidoctorExtension
import org.asciidoctor.gradle.AsciidoctorPlugin
import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.CopySpec
import org.gradle.api.file.Directory
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.util.PatternSet
import org.gradle.kotlin.dsl.delegateClosureOf
import org.gradle.kotlin.dsl.dependencies
import org.ysb33r.gradle.vfs.VfsPlugin
import org.ysb33r.gradle.vfs.VfsProxy
import java.io.File

class AsciidoctorPresentationPlugin : Plugin<Project> {
    companion object {
        const val GROUP = "Asciidoctor Presentation"
    }

    override
    fun apply(project: Project) = project.run {
        pluginManager.run {
            apply<JRubyPlugin>()
            apply<VfsPlugin>()
            apply<JavaBasePlugin>()
            apply<AsciidoctorPlugin>()
            apply<GithubPagesPlugin>()
        }
        val extension = extensions.create("presentation", PresentationExtension::class.java, this)

        registerGems()
        configureAsciidoctor(extension)

        val downloadsDir = objects.directoryProperty().convention(layout.buildDirectory.dir("download"))
        val templateDir = downloadsDir.dir("template")
        val revealJsDir = downloadsDir.dir("revealjs")

        val outputDir = objects.directoryProperty().convention(layout.buildDirectory.dir("asciidoc"))
        val asciidocRevealOut = outputDir.dir("revealjs")

        val downloadTask = createDownloadTask(extension, templateDir, revealJsDir, project)

        configureAsciidoctorTask(downloadTask, extension, templateDir, revealJsDir)
        configureGitHubPublishing(extension, asciidocRevealOut)
        createExportTasks(project, extension, outputDir, asciidocRevealOut)
    }

    private
    fun Project.createExportTasks(project: Project, extension: PresentationExtension, outputDir: Provider<Directory>, asciidocRevealOut: Provider<Directory>) {
        val exportConfig = configurations.maybeCreate("exportation")
        dependencies {
            "exportation"("me.champeau.deck2pdf:deck2pdf:0.3.0")
        }
        val exportTask = tasks.register("export") {
            group = GROUP
            description = "Exports the presentation to various formats (PDF, PNG, ...)"
        }
        listOf("pdf", "jpeg", "png").forEach { type ->
            val baseTypeDir = "${outputDir.get().asFile}/export/${type}"

            val gentask = tasks.register("exportTo${type.capitalize()}", JavaExec::class.java) {
                group = GROUP
                description = "Exports the presentation to ${type}"
                dependsOn("asciidoctor")
                main = "me.champeau.deck2pdf.Main"
                workingDir = asciidocRevealOut.get().asFile
                args = listOf("index.html", "$baseTypeDir/${project.name}.$type", "--profile=revealjs", "--width=${extension.width.get()}", "--height=${extension.height.get()}")
                classpath = exportConfig

                inputs.file("$workingDir/index.html")
                outputs.dir(baseTypeDir)

                doFirst {
                    file(baseTypeDir).mkdirs()
                }
            }
            exportTask.configure { dependsOn(gentask) }
        }
    }

    private
    fun Project.configureAsciidoctorTask(downloadTask: TaskProvider<Task>,
                                         extension: PresentationExtension,
                                         templateDir: Provider<Directory>,
                                         revealJsDir: Provider<Directory>) {
        afterEvaluate {
            tasks.withType(AsciidoctorTask::class.java).configureEach {
                group = GROUP
                sources(delegateClosureOf<PatternSet> {
                    include("index.adoc")
                })
                resources(delegateClosureOf<CopySpec> {
                    from(sourceDir) {
                        include("images/**")
                    }
                    from("${revealJsDir.get().asFile}/reveal.js-${extension.revealjsVersion.get()}") {
                        into("reveal.js")
                    }
                    from("src/docs/theme") {
                        into("reveal.js/css/theme")
                    }
                    from("src/docs/fonts") {
                        into("reveal.js/css/fonts")
                    }
                    from("src/docs/asciidoc/screencasts")
                })
                backends("revealjs")
                dependsOn(downloadTask, tasks.getByName("jrubyPrepare"))

                attributes(mapOf(
                        "source-highlighter" to extension.highlighter.get(),
                        "imagesdir" to "./images",
                        "buildsdir" to "../../../scripts",
                        "toc" to "left",
                        "icons" to "font",
                        "setanchors" to "true",
                        "idprefix" to "",
                        "idseparator" to "-",
                        "basedir" to projectDir,
                        "docinfo1" to "",
                        "width" to extension.width.get(),
                        "height" to extension.height.get(),
                        "project-version" to "1.0",
                        "revealjs_transition" to "linear",
                        "revealjs_history" to "true",
                        "revealjs_slideNumber" to "true",
                        "revealjs_theme" to extension.theme.get(),
                        "examples" to file("${projectDir}/examples")))

                val slimTemplatesDir = File(templateDir.get().asFile, "asciidoctor-reveal.js-${extension.asciidoctorBackendVersion.get()}/templates")
                options(mapOf(
                        "template_dirs" to listOf(slimTemplatesDir.absolutePath)
                ))

            }
        }
    }

    private
    fun Project.configureGitHubPublishing(extension: PresentationExtension, assemblePresentation: Provider<Directory>) {
        tasks.named("publishGhPages").configure {
            group = GROUP
            dependsOn("asciidoctor")
        }
        afterEvaluate {
            extensions.getByType(GithubPagesPluginExtension::class.java).run {
                setRepoUri("git@github.com:${extension.githubUserName.get()}/${extension.githubRepoName.get()}.git")
                pages.from(assemblePresentation)
            }
        }
    }

    private
    fun Project.createDownloadTask(extension: PresentationExtension, templateDir: Provider<Directory>, revealJsDir: Provider<Directory>, project: Project) = tasks.register("downloadTooling") {
        group = GROUP
        description = "Downloads extra deckjs/reveal.js resources"

        inputs.property("asciidoctorBackendVersion", extension.asciidoctorBackendVersion)
        inputs.property("revealjsVersion", extension.revealjsVersion)

        outputs.dir(templateDir)
        outputs.dir(revealJsDir)
        doLast {
            val templates = templateDir.get().asFile
            val reveal = revealJsDir.get().asFile
            mkdir(templates)
            mkdir(reveal)
            VfsProxy.request(project).run {
                val options = mapOf("recursive" to true, "overwrite" to true)
                cp(options, "zip:https://github.com/asciidoctor/asciidoctor-reveal.js/archive/v${extension.asciidoctorBackendVersion.get()}.zip!asciidoctor-reveal.js-${extension.asciidoctorBackendVersion.get()}", templates)
                cp(options, "zip:https://github.com/hakimel/reveal.js/archive/${extension.revealjsVersion.get()}.zip!reveal.js-${extension.revealjsVersion.get()}", reveal)
            }
        }
    }

    private
    fun Project.configureAsciidoctor(extension: PresentationExtension) {
        afterEvaluate {
            extensions.getByType(AsciidoctorExtension::class.java).version = extension.asciidoctorJVersion.get()
        }
    }

    private
    fun Project.registerGems() {
        repositories.run {
            maven {
                setUrl("http://rubygems-proxy.torquebox.org/releases")
            }
        }
        dependencies {
            "gems"("rubygems:slim:3.0.8")
            "gems"("rubygems:thread_safe:0.3.5")
        }
    }

    private
    inline fun <reified T> PluginManager.apply() = apply(T::class.java)

}
