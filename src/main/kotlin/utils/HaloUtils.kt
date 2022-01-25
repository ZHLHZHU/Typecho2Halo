package utils

import java.util.regex.Matcher
import java.util.regex.Pattern

class HaloUtils {
    companion object {
        private const val RE_HTML_MARK = "(<[^<]*?>)|(<[\\s]*?/[^<]*?>)|(<[^<]*?/[\\s]*?>)"

        private val BLANK_PATTERN = Pattern.compile("\\s")

        fun cleanHtmlTag(content: String): String? {
            return if (content.isEmpty()) {
                null
            } else content.replace(RE_HTML_MARK.toRegex(), "")
        }

        fun htmlFormatWordCount(htmlContent: String?): Long {
            if (htmlContent == null) {
                return 0
            }
            val cleanContent = cleanHtmlTag(htmlContent)
            val matcher: Matcher = cleanContent?.let { BLANK_PATTERN.matcher(it) } ?: return 0
            var count = 0
            while (matcher.find()) {
                count++
            }
            return (cleanContent.length - count).toLong()
        }
    }

}