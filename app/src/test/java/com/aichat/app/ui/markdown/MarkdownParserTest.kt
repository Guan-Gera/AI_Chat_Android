package com.aichat.app.ui.markdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {
    @Test
    fun parse_splitsTextAndCodeBlocks() {
        val blocks = MarkdownParser.parse(
            """
            # 标题
            下面是代码：
            
            ```kotlin
            val name = "AI Chat"
            println(name)
            ```
            
            - 完成
            """.trimIndent(),
        )

        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.TextLines)
        assertTrue(blocks[1] is MarkdownBlock.CodeBlock)
        assertTrue(blocks[2] is MarkdownBlock.TextLines)
        assertEquals("kotlin", (blocks[1] as MarkdownBlock.CodeBlock).language)
    }

    @Test
    fun parse_keepsUnclosedCodeBlock() {
        val blocks = MarkdownParser.parse(
            """
            ```json
            {"ok": true}
            """.trimIndent(),
        )

        assertEquals(1, blocks.size)
        val block = blocks.first() as MarkdownBlock.CodeBlock
        assertEquals("json", block.language)
        assertEquals("""{"ok": true}""", block.code)
    }
}
