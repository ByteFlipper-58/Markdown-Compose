package com.byteflipper.markdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byteflipper.markdown.ui.theme.MarkdownComposeSampleTheme
import com.byteflipper.markdown_compose.MarkdownText
import com.byteflipper.markdown_compose.model.defaultMarkdownStyleSheet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarkdownComposeSampleTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Default Style", "Custom Style")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Markdown Compose Demo") })
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> DefaultMarkdownView()
                    1 -> CustomMarkdownView()
                }
            }
        }
    }
}

@Composable
fun DefaultMarkdownView() {
    MarkdownText(
        markdown = SampleMarkdown.content,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(8.dp)
    )
}

@Composable
fun CustomMarkdownView() {
    val customStyleSheet = defaultMarkdownStyleSheet().let { defaults ->
        defaults.copy(
            textStyle = defaults.textStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),
            headerStyle = defaults.headerStyle.copy(
                h1 = defaults.headerStyle.h1.copy(color = MaterialTheme.colorScheme.tertiary),
                h2 = defaults.headerStyle.h2.copy(color = MaterialTheme.colorScheme.secondary)
            ),
            listStyle = defaults.listStyle.copy(
                indentPadding = 12.dp, // More indent
                bulletChars = listOf("* ", "+ ", "- ") // Different bullets
            ),
            tableStyle = defaults.tableStyle.copy(
                borderColor = MaterialTheme.colorScheme.primary,
                borderThickness = 2.dp, // Thicker border
                cellPadding = 10.dp, // More padding
                outerBorderShape = RoundedCornerShape(8.dp) // Rounded corners!
            ),
            horizontalRuleStyle = defaults.horizontalRuleStyle.copy(
                color = MaterialTheme.colorScheme.error,
                thickness = 2.dp
            ),
            blockQuoteStyle = defaults.blockQuoteStyle.copy(
                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                verticalBarColor = MaterialTheme.colorScheme.primary
            ),
            codeBlockStyle = defaults.codeBlockStyle.copy(
                textStyle = defaults.codeBlockStyle.textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                padding = 12.dp, // More padding
                backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
            linkStyle = defaults.linkStyle.copy(
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.LineThrough // Strikethrough for links
            ),
            strikethroughTextStyle = defaults.strikethroughTextStyle.copy(
                color = MaterialTheme.colorScheme.error, // Custom color for strikethrough text
                textDecoration = TextDecoration.LineThrough
            ),
            boldTextStyle = defaults.boldTextStyle.copy(
                color = MaterialTheme.colorScheme.primary, // Custom color for bold text
                fontWeight = FontWeight.ExtraBold
            ),
            italicTextStyle = defaults.italicTextStyle.copy(
                color = MaterialTheme.colorScheme.secondary
            ),
            blockSpacing = 20.dp, // More space between blocks
            lineBreakSpacing = 20.dp // More space for line breaks
        )
    }

    MarkdownText(
        markdown = SampleMarkdown.content,
        styleSheet = customStyleSheet,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            .padding(8.dp)
    )
}


object SampleMarkdown {
    val content = """
        # Привет, Мир!
        ## Это Markdown
        ### Подзаголовок 3
        #### Подзаголовок 4

        Это обычный текст параграфа с **жирным**, *курсивом* и ~~зачеркнутым~~ текстом.
        А также `inline code`.

        ---

        **Неупорядоченный список:**
        - ✅ Поддержка заголовков
        - ✅ Списков
            - Вложенный элемент 1
                - Еще глубже `code`
            - Вложенный элемент 2
        - ✅ **Жирного** и *курсива*

        **Упорядоченный список:**
        1. Первый пункт
        2. Второй пункт
            1. Вложенный нумерованный 1
            2. Вложенный нумерованный 2
        3. Третий пункт

        **Смешанные списки:**
        - Пункт 1
            1. Нумерованный подпункт 1
            2. Нумерованный подпункт 2
                - Вложенный пунктовый под-подпункт
        - Пункт 2

        [Ссылка на ByteFlipper](https://byteflipper.web.app/)

        > Это блочная цитата.
        > Она может содержать **форматирование** и *курсив*.
        > И даже несколько строк.

        ### Пример таблицы

        | Заголовок 1 | Заголовок 2 | Заголовок 3 |
        |-------------|:-----------:|------------:|
        | Данные 1    | Центр 2     | Справа 3    |
        | Данные 4    | Центр 5     | Справа 6    |
        | `Код` в яч. | **Жирный**  | *Курсив*    |

        ---

        Конец примера.
        Еще один параграф текста для проверки отступов.
        """.trimIndent()
}


@Preview(showBackground = true, name = "Light Mode Preview")
@Composable
fun MainScreenPreviewLight() {
    MarkdownComposeSampleTheme(darkTheme = false) {
        MainScreen()
    }
}

@Preview(showBackground = true, name = "Dark Mode Preview")
@Composable
fun MainScreenPreviewDark() {
    MarkdownComposeSampleTheme(darkTheme = true) {
        MainScreen()
    }
}