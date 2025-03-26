package com.byteflipper.markdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.byteflipper.markdown.ui.theme.MarkdownComposeSampleTheme
import com.byteflipper.markdown_compose.MarkdownText

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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Markdown Compose") }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val textColor = MaterialTheme.colorScheme.onBackground

            MarkdownText("""
                # Привет, Мир!
                ## Это Markdown
                ### Подзаголовок 3
                #### Подзаголовок 4
            
                Это обычный текст параграфа.
            
                **Неупорядоченный список:**
                - ✅ Поддержка заголовков
                - ✅ Списков
                    - Вложенный элемент 1
                        - Еще глубже
                    - Вложенный элемент 2
                - ✅ **Жирного** и *курсива*
                - ✅ ~~Зачеркнутый текст~~
                - ✅ `Моноширинный текст`
            
            
                **Упорядоченный список:**
                1. Первый пункт
                2. Второй пункт
                    1. Вложенный нумерованный пункт 1
                    2. Вложенный нумерованный пункт 2
                3. Третий пункт


                **Смешанные списки:**
                - Пункт 1
                    1. Нумерованный подпункт 1
                    2. Нумерованный подпункт 2
                        - Вложенный пунктовый под-подпункт
                - Пункт 2

                [Ссылка на ByteFlipper](https://byteflipper.web.app/)


                > Цитата
                > **жирный текст в цитате** и *курсив*.
            
                ### Пример таблицы

                | Заголовок 1 | Заголовок 2 | Заголовок 3 |
                |-------------|-------------|-------------|
                | Данные 1    | Данные 2    | Данные 3    |
                | Данные 4    | Данные 5    | Данные 6    |
            
                ### Пример таблицы со стилями

                | Column 1 | Column 2 | Column 3 |
                |:---------|:--------:|---------:|
                | Left     | Center   | Right    |
                | **Bold** | *Italic* | Regular  |

                ### Пример разделителя

                ---

                Конец примера.
            """.trimIndent(),
                modifier = Modifier.fillMaxWidth(),
                textColor = textColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MarkdownComposeSampleTheme {
        MainScreen()
    }
}