package com.byteflipper.markdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        ) {
            val textColor = MaterialTheme.colorScheme.onBackground

            MarkdownText(
                text = """
                    # **Привет, Мир!** No Bold
                    ## *Это Markdown*
                    - ✅ Поддержка заголовков
                    - ✅ Списков
                    - ✅ **Жирного** и *курсива*
                    - [Ссылка на Google](https://google.com)
                    
                    **Жирдяй**
                    
                    *Курсив*
                    
                    
                    ```
                    print("log");
                    ```
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
