package com.aichat.app.ui.markdown

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownMessage(
    content: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(content) { MarkdownParser.parse(content) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.TextLines -> TextLines(block.lines)
                is MarkdownBlock.CodeBlock -> CodeBlock(block)
            }
        }
    }
}

@Composable
private fun TextLines(lines: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("### ") -> Heading(line.removePrefix("### "), small = true)
                line.startsWith("## ") -> Heading(line.removePrefix("## "), small = false)
                line.startsWith("# ") -> Heading(line.removePrefix("# "), small = false)
                line.startsWith("- ") || line.startsWith("* ") -> BulletLine(line.drop(2))
                numberedLineRegex.matches(line) -> {
                    val number = line.substringBefore(".")
                    val text = line.substringAfter(".").trimStart()
                    NumberedLine(number = number, text = text)
                }
                else -> Text(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun Heading(text: String, small: Boolean) {
    Text(
        text = text,
        style = if (small) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun BulletLine(text: String) {
    Row {
        Text("•", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NumberedLine(number: String, text: String) {
    Row {
        Text("$number.", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CodeBlock(block: MarkdownBlock.CodeBlock) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (!block.language.isNullOrBlank()) {
                Text(
                    text = block.language,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = block.code,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            )
        }
    }
}

private val numberedLineRegex = Regex("""\d+\.\s+.+""")
