package utils.dependency.footnotes.internal;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import utils.dependency.footnotes.Footnote;
import utils.dependency.footnotes.FootnoteBlock;
import utils.dependency.footnotes.FootnoteExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FootnoteNodeRenderer implements PhasedNodeRenderer {

    private final FootnoteRepository footnoteRepository;
    private final FootnoteOptions options;
    private final boolean recheckUndefinedReferences;

    public FootnoteNodeRenderer(DataHolder options) {
        this.options = new FootnoteOptions(options);
        this.footnoteRepository = FootnoteExtension.FOOTNOTES.get(options);
        this.recheckUndefinedReferences = HtmlRenderer.RECHECK_UNDEFINED_REFERENCES.get(options);
        this.footnoteRepository.resolveFootnoteOrdinals();
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        return new HashSet<>(Arrays.asList(
                new NodeRenderingHandler<>(Footnote.class, this::render),
                new NodeRenderingHandler<>(FootnoteBlock.class, this::render)
        ));
    }

    @Override
    public Set<RenderingPhase> getRenderingPhases() {
        Set<RenderingPhase> set = new HashSet<>();
        set.add(RenderingPhase.BODY_TOP);
        set.add(RenderingPhase.BODY_BOTTOM);
        return set;
    }

    @Override
    public void renderDocument(NodeRendererContext context, HtmlWriter html,
                               Document document, RenderingPhase phase) {
        if (phase == RenderingPhase.BODY_TOP) {
            if (recheckUndefinedReferences) {
                // need to see if have undefined footnotes that were defined after parsing
                boolean[] hadNewFootnotes = {false};
                NodeVisitor visitor = new NodeVisitor(
                        new VisitHandler<>(Footnote.class, node -> {
                            if (!node.isDefined()) {
                                FootnoteBlock footonoteBlock =
                                        node.getFootnoteBlock(footnoteRepository);

                                if (footonoteBlock != null) {
                                    footnoteRepository.addFootnoteReference(footonoteBlock, node);
                                    node.setFootnoteBlock(footonoteBlock);
                                    hadNewFootnotes[0] = true;
                                }
                            }
                        })
                );

                visitor.visit(document);
                if (hadNewFootnotes[0]) {
                    this.footnoteRepository.resolveFootnoteOrdinals();
                }
            }
        }

        if (phase == RenderingPhase.BODY_BOTTOM) {
            // here we dump the footnote blocks that were referenced in the document body, ie.
            // ones with footnoteOrdinal > 0
            if (footnoteRepository.getReferencedFootnoteBlocks().size() > 0) {
                html.attr("class", "footnotes-sep").withAttr().tagVoid("hr");
                html.attr("class", "footnotes").withAttr().tagIndent("section", () -> {
                    html.attr("class", "footnotes-list").withAttr().tagIndent("ol", () -> {
                        for (FootnoteBlock footnoteBlock : footnoteRepository
                                .getReferencedFootnoteBlocks()) {
                            int footnoteOrdinal = footnoteBlock.getFootnoteOrdinal();
                            html.attr("id", "fn" + footnoteOrdinal)
                                    .attr("class", "footnote-item");
                            html.withAttr().tagIndent("li", () -> {
                                context.renderChildren(footnoteBlock);
                                int lineIndex = html.getLineCount() - 1;
                                BasedSequence line = html.getLine(lineIndex);
                                if (line.lastIndexOf("</p>") > -1) {
                                    int iMax = footnoteBlock.getFootnoteReferences();
                                    for (int i = 0; i < iMax; i++) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(" <a href=\"#fnref").append(footnoteOrdinal)
                                                .append(i == 0 ? "" : String
                                                        .format(Locale.US, ":%d", i)).append("\"");
                                        if (!options.footnoteBackLinkRefClass.isEmpty()) {
                                            sb.append(" class=\"")
                                                    .append(options.footnoteBackLinkRefClass)
                                                    .append("\"");
                                        }
                                        sb.append(">").append(options.footnoteBackRefString)
                                                .append("</a>");
                                        html.setLine(html.getLineCount() - 1, "",
                                                line.insert(line.lastIndexOf("</p"), sb.toString()));
                                    }
                                } else {
                                    int iMax = footnoteBlock.getFootnoteReferences();
                                    for (int i = 0; i < iMax; i++) {
                                        html.attr("href", "#fnref" + footnoteOrdinal
                                                + (i == 0 ? "" : String.format(Locale.US, ":%d", i)));
                                        if (!options.footnoteBackLinkRefClass.isEmpty()) {
                                            html.attr("class", options.footnoteBackLinkRefClass);
                                        }
                                        html.line().withAttr().tag("a");
                                        html.raw(options.footnoteBackRefString);
                                        html.tag("/a");
                                    }
                                }
                            });
                        }
                    });
                });
            }
        }
    }

    private void render(FootnoteBlock node, NodeRendererContext context, HtmlWriter html) {

    }

    private void render(Footnote node, NodeRendererContext context, HtmlWriter html) {
        FootnoteBlock footnoteBlock = node.getFootnoteBlock();
        if (footnoteBlock == null) {
            //just text
            html.raw("[^");
            context.renderChildren(node);
            html.raw("]");
        } else {
            int footnoteOrdinal = footnoteBlock.getFootnoteOrdinal();
            int i = node.getReferenceOrdinal();

            html.attr("class", "footnote-ref");
            html.srcPos(node.getChars()).withAttr()
                    .tag("sup", false, false, () -> {
                        // if (!options.footnoteLinkRefClass.isEmpty())
                        // html.attr("class", options.footnoteLinkRefClass);
                        String ordinal = footnoteOrdinal + (i == 0 ? "" : String.format(Locale.US,
                                ":%d", i));
                        html.attr("id", "fnref"
                                + ordinal);
                        html.attr("href", "#fn" + footnoteOrdinal);
                        html.withAttr().tag("a");
                        html.raw("[" + ordinal + "]");
                        html.tag("/a");
                    });
        }
    }

    public static class Factory implements NodeRendererFactory {


        @Override
        public NodeRenderer apply(DataHolder options) {
            return new FootnoteNodeRenderer(options);
        }
    }
}
