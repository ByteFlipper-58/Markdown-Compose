package com.byteflipper.markdown

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ScrollState // Импорт ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Импорт rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byteflipper.markdown.ui.theme.MarkdownComposeSampleTheme
import com.byteflipper.markdown_compose.MarkdownText
import com.byteflipper.markdown_compose.model.MarkdownStyleSheet
import com.byteflipper.markdown_compose.model.defaultMarkdownStyleSheet
import kotlinx.coroutines.launch // Импорт для корутин

class MainActivity : ComponentActivity() {
    // RequiresApi needed because MarkdownText now requires it due to the parser change
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
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

// Annotation required because this Composable calls others that require it
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Default Style", "Custom Style")

    // --- Create states here ---
    val scrollState = rememberScrollState() // State for the scroll
    // Use SnapshotStateMap for integration with Compose State
    val footnotePositions = remember { mutableStateMapOf<String, Float>() }
    val coroutineScope = rememberCoroutineScope() // For launching scroll animation

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

            // --- Wrap in a Scrollable Column ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp) // Horizontal padding here
                    .verticalScroll(scrollState) // APPLY SCROLL STATE
                    .padding(vertical = 16.dp)   // Vertical padding inside for content margins
            ) {
                when (selectedTab) {
                    // Pass states to both views
                    0 -> DefaultMarkdownView(footnotePositions, scrollState)
                    1 -> CustomMarkdownView(footnotePositions, scrollState)
                }
                Spacer(Modifier.height(50.dp)) // Bottom spacer
            }
        }
    }
}

// Receives states as parameters
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun DefaultMarkdownView(
    footnotePositions: MutableMap<String, Float>, // Map for MarkdownText to update
    scrollState: ScrollState // State to perform scroll
) {
    val coroutineScope = rememberCoroutineScope()
    MarkdownText(
        markdown = SampleMarkdown.content,
        modifier = Modifier.fillMaxWidth().padding(8.dp), // Inner padding
        footnotePositions = footnotePositions, // Pass map
        onLinkClick = { url ->
            Log.d("DefaultMarkdownView", "Link clicked: $url")
            // Handle link click if needed (default handler already opens browser)
        },
        onFootnoteReferenceClick = { identifier ->
            Log.d("DefaultMarkdownView", "Footnote reference clicked: [^$identifier]")
            // --- Scroll Logic ---
            val position = footnotePositions[identifier]
            if (position != null) {
                Log.d("DefaultMarkdownView", "Scrolling to position: $position for id: $identifier")
                coroutineScope.launch {
                    scrollState.animateScrollTo(position.toInt()) // Scroll to the measured Y position
                }
            } else {
                Log.w("DefaultMarkdownView", "Position not found for footnote id: $identifier")
            }
        }
    )
}

// Receives states as parameters
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun CustomMarkdownView(
    footnotePositions: MutableMap<String, Float>,
    scrollState: ScrollState
) {
    val coroutineScope = rememberCoroutineScope()
    val defaults = defaultMarkdownStyleSheet()
    // --- Create customStyleSheet (as before) ---
    val customStyleSheet = defaults.copy(
        textStyle = defaults.textStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),
        headerStyle = defaults.headerStyle.copy(
            h1 = defaults.headerStyle.h1.copy(color = MaterialTheme.colorScheme.tertiary),
            h2 = defaults.headerStyle.h2.copy(color = MaterialTheme.colorScheme.secondary),
            bottomPadding = 12.dp
        ),
        listStyle = defaults.listStyle.copy(
            indentPadding = 12.dp,
            bulletChars = listOf("* ", "+ ", "- "),
            itemSpacing = 6.dp
        ),
        taskListItemStyle = defaults.taskListItemStyle.copy(
            checkedTextStyle = SpanStyle(
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        ),
        tableStyle = defaults.tableStyle.copy(
            borderColor = MaterialTheme.colorScheme.primary,
            borderThickness = 2.dp,
            cellPadding = 10.dp,
            outerBorderShape = RoundedCornerShape(8.dp)
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
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
            textStyle = defaults.codeBlockStyle.textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            codeBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            showLanguageLabel = true,
            languageLabelTextStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
            languageLabelBackground = MaterialTheme.colorScheme.surfaceVariant,
            languageLabelPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            showInfoBar = true,
            infoBarTextStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            infoBarBackground = MaterialTheme.colorScheme.surfaceVariant,
            infoBarPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            showCopyButton = true,
            copyIconTint = MaterialTheme.colorScheme.primary,
            showLineCount = true,
            showCharCount = true
        ),
        inlineCodeStyle = defaults.inlineCodeStyle.copy(
            background = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        ),
        linkStyle = defaults.linkStyle.copy(
            color = MaterialTheme.colorScheme.secondary,
            textDecoration = TextDecoration.Underline
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
        // --- Custom Footnote Styles ---
        footnoteReferenceStyle = defaults.footnoteReferenceStyle.copy(
            color = MaterialTheme.colorScheme.secondary,
            baselineShift = BaselineShift.None, // Keep it on baseline
            textDecoration = TextDecoration.Underline // Underline the ref number
        ),
        footnoteDefinitionStyle = defaults.footnoteDefinitionStyle.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        ),
        footnoteBlockPadding = 24.dp,
        // --- Spacing ---
        blockSpacing = 20.dp,
        lineBreakSpacing = 10.dp
    )

    MarkdownText(
        markdown = SampleMarkdown.content,
        styleSheet = customStyleSheet,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        footnotePositions = footnotePositions, // Pass map
        onLinkClick = { url ->
            Log.d("CustomMarkdownView", "Link clicked: $url")
        },
        onFootnoteReferenceClick = { identifier ->
            Log.d("CustomMarkdownView", "Footnote reference clicked: [^$identifier]")
            // --- Scroll Logic ---
            val position = footnotePositions[identifier]
            if (position != null) {
                Log.d("CustomMarkdownView", "Scrolling to position: $position for id: $identifier")
                coroutineScope.launch {
                    scrollState.animateScrollTo(position.toInt())
                }
            } else {
                Log.w("CustomMarkdownView", "Position not found for footnote id: $identifier")
            }
        }
    )
}


// --- SampleMarkdown object (with extra paragraphs for scrolling) ---
object SampleMarkdown {
    val content = """
        # Привет, Мир!
        ## Это Markdown
        ### Подзаголовок 3
        #### Подзаголовок 4

        Это обычный текст параграфа с **жирным**, *курсивом* и ~~зачеркнутым~~ текстом.
        А также `inline code`.

        Вот пример сноски[^1]. Вот еще одна[^примечание]. Можно ссылаться на ту же сноску[^1] несколько раз.
        Неопределенная сноска[^missing] будет просто текстом.

        ---

        **Неупорядоченный список:**
        - ✅ Поддержка заголовков
        - ✅ Списков [^1]
            - Вложенный элемент 1 со сноской [^примечание].
            - Вложенный элемент 2
        - ✅ **Жирного** и *курсива*

        **Упорядоченный список:**
        1. Первый пункт
        2. Второй пункт
            1. Вложенный нумерованный 1
            2. Вложенный нумерованный 2
            3. [Ссылка на ByteFlipper](https://byteflipper.web.app/)
        3. Третий пункт со сноской[^третий].

        **Списки задач (Checkboxes):**
        - [x] Завершённая задача [^1]
        - [ ] Невыполненная задача
        - [X] Другая завершённая (Caps X)
            - [ ] Вложенная невыполненная
            - [x] Вложенная выполненная

        [Ссылка на ByteFlipper](https://byteflipper.web.app/)

        > Это блочная цитата.
        > Она может содержать **форматирование** и *курсив*.
        > И даже несколько строк, включая сноску[^примечание].

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
        | Данные 4    | Центр 5[^примечание]| Справа 6    |
        | `Код` в яч. | **Жирный**  | *Курсив*    |

        ---

        ## ФОТАЧКИ

        Простое изображение:
        ![Compose Logo](https://cdn-icons-png.flaticon.com/512/1509/1509974.png)

        Изображение-ссылка:
        [![GitHub Logo](https://cdn-icons-png.flaticon.com/512/1509/1509974.png)](https://github.com)

        Конец примера.
        Еще один параграф текста для проверки отступов.

        [^1]: Это определение **первой** сноски. Она может содержать *форматирование*, [ссылку](https://example.com) и даже другую сноску[^примечание].
        [^примечание]: Это вторая сноска. Она также содержит `код`. Клик на [^1] здесь тоже сработает.
        [^третий]: Определение для третьей сноски. Длинный текст, чтобы проверить высоту.
        """.trimIndent()
}


// --- Preview Functions ---
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Preview(showBackground = true, name = "Light Mode Preview")
@Composable
fun MainScreenPreviewLight() {
    MarkdownComposeSampleTheme(darkTheme = false) {
        Surface {
            MainScreen()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Preview(showBackground = true, name = "Dark Mode Preview")
@Composable
fun MainScreenPreviewDark() {
    MarkdownComposeSampleTheme(darkTheme = true) {
        Surface {
            MainScreen()
        }
    }
}