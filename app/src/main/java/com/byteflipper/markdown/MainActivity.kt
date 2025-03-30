package com.byteflipper.markdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
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
                Spacer(Modifier.height(50.dp))
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
            .padding(8.dp)
    )
}

@Composable
fun CustomMarkdownView() {
    // Get default styles as a starting point
    val defaults = defaultMarkdownStyleSheet()

    // Create custom styles based on defaults
    val customStyleSheet = defaults.copy(
        textStyle = defaults.textStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),
        headerStyle = defaults.headerStyle.copy(
            h1 = defaults.headerStyle.h1.copy(color = MaterialTheme.colorScheme.tertiary),
            h2 = defaults.headerStyle.h2.copy(color = MaterialTheme.colorScheme.secondary),
            bottomPadding = 12.dp // Increased padding after headers
        ),
        listStyle = defaults.listStyle.copy(
            indentPadding = 12.dp, // More indent for nested lists
            bulletChars = listOf("* ", "+ ", "- "), // Different bullet styles
            itemSpacing = 6.dp // Slightly more space between list items

        ),
        taskListItemStyle = defaults.taskListItemStyle.copy(
            checkedTextStyle = SpanStyle( // Custom style for checked items text
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) // Dimmer color
            )
            // uncheckedTextStyle remains null (inherits from baseTextStyle)
        ),
        tableStyle = defaults.tableStyle.copy(
            borderColor = MaterialTheme.colorScheme.primary,
            borderThickness = 2.dp, // Thicker border
            cellPadding = 10.dp, // More padding in cells
            outerBorderShape = RoundedCornerShape(8.dp) // Rounded corners!
        ),
        horizontalRuleStyle = defaults.horizontalRuleStyle.copy(
            color = MaterialTheme.colorScheme.error,
            thickness = 2.dp
        ),
        blockQuoteStyle = defaults.blockQuoteStyle.copy(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            verticalBarColor = MaterialTheme.colorScheme.primary,
            verticalBarWidth = 6.dp,
            padding = 12.dp
        ),
        codeBlockStyle = defaults.codeBlockStyle.copy(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)), // Rounded corners for the whole block
            textStyle = defaults.codeBlockStyle.textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            codeBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), // Semi-transparent outer bg
            // Language Label customization
            showLanguageLabel = true,
            languageLabelTextStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
            languageLabelBackground = MaterialTheme.colorScheme.surfaceVariant, // Match outer bg
            languageLabelPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            // Info Bar customization
            showInfoBar = true,
            infoBarTextStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            infoBarBackground = MaterialTheme.colorScheme.surfaceVariant, // Match outer bg
            infoBarPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            showCopyButton = true,
            copyIconTint = MaterialTheme.colorScheme.primary,
            showLineCount = true,
            showCharCount = true
        ),
        // --- Inline Code Style (using existing property) ---
        inlineCodeStyle = defaults.inlineCodeStyle.copy(
            background = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        ),
        linkStyle = defaults.linkStyle.copy(
            color = MaterialTheme.colorScheme.secondary,
            textDecoration = TextDecoration.Underline // Links underlined
        ),
        strikethroughTextStyle = defaults.strikethroughTextStyle.copy(
            color = MaterialTheme.colorScheme.error
        ),
        boldTextStyle = defaults.boldTextStyle.copy(
            color = MaterialTheme.colorScheme.primary
        ),
        italicTextStyle = defaults.italicTextStyle.copy(
            color = MaterialTheme.colorScheme.secondary
        ),
        blockSpacing = 20.dp,
        lineBreakSpacing = 10.dp
    )

    MarkdownText(
        markdown = SampleMarkdown.content,
        styleSheet = customStyleSheet,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}


object SampleMarkdown {
    // Updated sample content with code blocks
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
            - Вложенный элемент 2
        - ✅ **Жирного** и *курсива*

        **Упорядоченный список:**
        1. Первый пункт
        2. Второй пункт
            1. Вложенный нумерованный 1
            2. Вложенный нумерованный 2
            3. [Ссылка на ByteFlipper](https://byteflipper.web.app/)
        3. Третий пункт
        
        **Списки задач (Checkboxes):**
        - [x] Завершённая задача
        - [ ] Невыполненная задача
        - [X] Другая завершённая (Caps X)
            - [ ] Вложенная невыполненная
            - [x] Вложенная выполненная

        [Ссылка на ByteFlipper](https://byteflipper.web.app/)

        > Это блочная цитата.
        > Она может содержать **форматирование** и *курсив*.
        > И даже несколько строк.

        ### Блоки Кода

        **Блок кода без языка:**
        ```
        fun main() {
            println("Hello without language")
        }
        ```

        **Блок кода с языком Kotlin:**
        ```kotlin
        package com.example

        import androidx.compose.material3.Text
        import androidx.compose.runtime.Composable

        @Composable
        fun Greeting(name: String) {
            Text(text = "Hello, ByteFlipper!")
        }
        // This is a comment inside the code block
        val x = 10 * 5 + 3
        ```

        **Еще один блок (Python):**
        ```python
        def greet(name):
            print(f"Hello, {name}!")

        greet("Markdown User")
        # Simple python example
        ```

        ### Пример таблицы

        | Заголовок 1 | Заголовок 2 | Заголовок 3 |
        |-------------|:-----------:|------------:|
        | Данные 1    | Центр 2     | Справа 3    |
        | Данные 4    | Центр 5     | Справа 6    |
        | `Код` в яч. | **Жирный**  | *Курсив*    |

        ---
        
        ## ФОТАЧКИ

        Простое изображение:
        ![Compose Logo](https://cdn-icons-png.flaticon.com/512/1509/1509974.png)

        Изображение-ссылка:
        [![GitHub Logo](https://cdn-icons-png.flaticon.com/512/1509/1509974.png)](https://github.com)
        
        Конец примера.
        Еще один параграф текста для проверки отступов.
        """.trimIndent()
}


@Preview(showBackground = true, name = "Light Mode Preview")
@Composable
fun MainScreenPreviewLight() {
    MarkdownComposeSampleTheme(darkTheme = false) {
        Surface { // Wrap in Surface for background color
            MainScreen()
        }
    }
}

@Preview(showBackground = true, name = "Dark Mode Preview")
@Composable
fun MainScreenPreviewDark() {
    MarkdownComposeSampleTheme(darkTheme = true) {
        Surface { // Wrap in Surface for background color
            MainScreen()
        }
    }
}