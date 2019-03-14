package org.gradle.presentation.asciidoc;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.InlineMacroProcessor;

import java.util.Map;

class ScreencastAsciidoctorExtension extends InlineMacroProcessor {
    private int count = 0;

    ScreencastAsciidoctorExtension() {
        super("screencast");
    }

    @Override
    protected Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
        String pid = "player-container-" + count++;
        return "\n" +
                "<div id=\"" + pid + "\"></div>\n" +
                "  <script>\n" +
                "    asciinema_player.core.CreatePlayer('" + pid + "', '" + target + ".json', { width: 114, height: 24, fontSize: 'medium' });\n" +
                "  </script>";
    }
}
