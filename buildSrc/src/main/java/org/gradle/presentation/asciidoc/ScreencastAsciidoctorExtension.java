package org.gradle.presentation.asciidoc;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.InlineMacroProcessor;

import java.util.Map;

class ScreencastAsciidoctorExtension extends InlineMacroProcessor {
    ScreencastAsciidoctorExtension() {
        super("screencast");
    }

    @Override
    protected Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
        return "<asciinema-player src=\"/" + target + ".cast\" class=\"stretch\" theme=\"gradle\"></asciinema-player>";
    }
}
