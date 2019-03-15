plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    compile("com.github.jruby-gradle:jruby-gradle-plugin:1.4.0")
    compile("org.asciidoctor:asciidoctor-gradle-plugin:1.5.9.2")
    compile("org.ysb33r.gradle:vfs-gradle-plugin:1.0")
    compile("commons-httpclient:commons-httpclient:3.1")
    compile("org.ajoberstar:gradle-git:1.1.0")
    compile("me.champeau.deck2pdf:deck2pdf:0.3.0")
    compileOnly("org.asciidoctor:asciidoctorj:1.5.6")

    components.all(RemoveGroovyRule::class.java)

    configurations.all {
        resolutionStrategy.force("com.jcraft:jsch:0.1.54")
    }
}

gradlePlugin {
    plugins {
        register("presentation") {
            id = "org.gradle.presentation.asciidoctor"
            implementationClass = "AsciidoctorPresentationPlugin"
        }
    }
}

class RemoveGroovyRule: ComponentMetadataRule {

    override
    fun execute(context: ComponentMetadataContext) = context.details.allVariants {
        withDependencies {
            removeAll {
                it.group == "org.codehaus.groovy"
            }
        }
    }

}