package com.byteflipper.markdown

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byteflipper.markdown.screens.CustomRenderersScreen
import com.byteflipper.markdown.screens.CustomStyleScreen
import com.byteflipper.markdown.screens.DefaultScreen
import com.byteflipper.markdown.ui.theme.MarkdownComposeSampleTheme

// Remove unused imports from MainActivity
// import com.byteflipper.markdown_compose.MarkdownText
// import com.byteflipper.markdown_compose.model.*
// import com.byteflipper.markdown_compose.renderer.MarkdownRenderer
// import kotlinx.coroutines.launch

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
    val tabs = listOf("Default", "Custom Style", "Custom Renderers") // Keep tabs list

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
                // Call the appropriate screen based on the selected tab
                when (selectedTab) {
                    0 -> DefaultScreen(footnotePositions, scrollState)
                    1 -> CustomStyleScreen(footnotePositions, scrollState)
                    2 -> CustomRenderersScreen(footnotePositions, scrollState)
                }
                Spacer(Modifier.height(50.dp)) // Bottom spacer
            }
        }
    }
}

// --- SampleMarkdown object (moved here for simplicity, could be in its own file) ---
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

        ### Список определений

        Термин 1
        : Определение для Термина 1. Может содержать **форматирование**.

        Термин 2 с `кодом`
        : Определение для Термина 2.
        : Может быть несколько строк определения.

        Еще один термин
        : Его определение.
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
