package com.aichat.app.ui.markdown

sealed interface MarkdownBlock {
    data class TextLines(val lines: List<String>) : MarkdownBlock
    data class CodeBlock(val language: String?, val code: String) : MarkdownBlock
}

object MarkdownParser {
    fun parse(content: String): List<MarkdownBlock> {
        if (content.isBlank()) return emptyList()

        val blocks = mutableListOf<MarkdownBlock>()
        val textBuffer = mutableListOf<String>()
        val codeBuffer = StringBuilder()
        var inCodeBlock = false
        var codeLanguage: String? = null

        fun flushText() {
            if (textBuffer.isNotEmpty()) {
                blocks += MarkdownBlock.TextLines(textBuffer.toList())
                textBuffer.clear()
            }
        }

        fun flushCode() {
            blocks += MarkdownBlock.CodeBlock(
                language = codeLanguage?.takeIf { it.isNotBlank() },
                code = codeBuffer.toString().trimEnd('\n'),
            )
            codeBuffer.clear()
            codeLanguage = null
        }

        content.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    flushCode()
                    inCodeBlock = false
                } else {
                    flushText()
                    inCodeBlock = true
                    codeLanguage = trimmed.removePrefix("```").trim()
                }
                return@forEach
            }

            if (inCodeBlock) {
                codeBuffer.append(line).append('\n')
            } else if (trimmed.isEmpty()) {
                flushText()
            } else {
                textBuffer += line
            }
        }

        if (inCodeBlock) flushCode()
        flushText()
        return blocks
    }
}
