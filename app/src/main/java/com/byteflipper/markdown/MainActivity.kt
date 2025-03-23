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

            MarkdownText(
                markdown = """
                    # **Привет, Мир!**
                    ## *Это расширенный Markdown*
                    ### Заголовок 3
                    #### Заголовок 4
                    
                    - ✅ Поддержка заголовков
                    - ✅ Списков
                    - ✅ **Жирного** и *курсива*
                    - ✅ ~~Зачеркнутый текст~~
                    - ✅ `Моноширинный текст`
                    - ✅ [Ссылка на Google](https://google.com)
                    
                    > Это цитата с примером форматирования
                    > **жирный текст в цитате**
                    
                    **Жирный текст**
                    
                    *Курсивный текст*
                    
                    ~~Зачеркнутый текст~~
                    
                    ```kotlin
                    fun main() {
                        println("Hello, World!")
                    }
                    ```
                    
                    ---
                    
                    ### Пример таблицы
                    
                    | Заголовок 1 | Заголовок 2 | Заголовок 3 |
                    |-------------|-------------|-------------|
                    | Данные 1    | Данные 2    | Данные 3    |
                    | Данные 4    | Данные 5    | Данные 6    |
                    | Данные 7    | Данные 8    | Данные 9    |
                    
                    ### Пример таблицы со стилями
                    
                    | Column 1 | Column 2 | Column 3 |
                    |:---------|:--------:|---------:|
                    | Left     | Center   | Right    |
                    | **Bold** | *Italic* | Regular  |
                    
                    # Horizontal Rule Test
        
                    Text before rule
        
                    ---
        
                    Text between rules
        
                    ***
        
                    More text
        
                    ___
        
                    End of test
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