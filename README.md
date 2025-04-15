# Markdown Compose Lib

![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-%2300C853.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Coil](https://img.shields.io/badge/Coil-%23FFA500.svg?style=for-the-badge&logo=coil&logoColor=white)

**Markdown Compose Lib** — это удобная библиотека для рендеринга Markdown в Jetpack Compose. Она позволяет легко интегрировать поддержку Markdown в Android-приложения, обеспечивая гибкость, простоту использования и широкие возможности кастомизации.

## 📌 Возможности

✅ Поддержка базовых элементов Markdown:
- Заголовки (H1-H6)
- Списки:
  - Маркированные (`-`, `*`, `+`)
  - Нумерованные (`1.`, `2.`, ...)
  - ✅ **Списки задач** (`- [ ] текст`, `- [x] текст`) с **интерактивными чекбоксами**, поддержкой ссылок/сносок и настраиваемым стилем.
  - ✅ **Списки определений** (термин на одной строке, `:` + определение на следующей).
  - Вложенные списки
- Ссылки (`[текст](URL)`)
- **Жирный текст** (`**текст**`)
- *Курсив* (`*текст*`)
- ~~Зачеркнутый текст~~ (`~~текст~~`)
- `Inline code` (`` `код` ``)
- Блоки кода с отображением языка, кнопкой копирования, счетчиками строк/символов и настройками стилей (подсветка синтаксиса в планах).
- > Блочные цитаты (`> текст`)
- 📊 Таблицы с различными стилями и настраиваемыми границами.
- Разделители (`---`, `***`, `___`)
- ✅ **Изображения** (`![alt текст](URL)`) с возможностью кастомизации рендеринга (например, для интеграции Coil/Glide).
- ✅ **Ссылки-изображения** (`[![alt текст](img URL)](link URL)`).
- ✅ **Сноски (Footnotes)** (`[^id]` и `[^id]: Text`) с настраиваемым стилем и **прокруткой к определению** при клике.
- Настраиваемое расстояние между блоками и строками.
- ✅ **Расширяемый рендеринг:** Возможность передать собственные Composable-функции для отображения конкретных элементов Markdown (заголовков, изображений, блоков кода, списков определений и т.д.).
- ✅ **Совместимость:** Не требует высокого уровня API Android.
- 🎨 **Темы оформления** (светлая, тёмная, кастомные стили через `MarkdownStyleSheet`).
- ⚡ **Оптимизированный рендеринг** (базовый).

*В планах:*
- Подсветка синтаксиса для блоков кода.
- Базовая поддержка HTML тегов.
- Улучшенная поддержка GFM (GitHub Flavored Markdown).

## 🚀 Установка

Добавьте зависимость в `build.gradle.kts` вашего модуля:

```kotlin
dependencies {
    // Убедитесь, что путь к проекту ':markdown-compose' корректен
    implementation(project(":markdown-compose"))
}
```

## 📖 Пример использования

```kotlin
// ... внутри вашего Composable

// Состояния для скролла и позиций сносок
val scrollState = rememberScrollState()
val footnotePositions = remember { mutableStateMapOf<String, Float>() }
val coroutineScope = rememberCoroutineScope()

// Пример состояния для отслеживания изменений в Task List
var markdownContent by remember { mutableStateOf("""
    # Пример Markdown
    Это **жирный** текст и *курсив*.

    - [x] Выполненная задача со [ссылкой](https://example.com)
    - [ ] Невыполненная задача со сноской[^task]
    - [ ] Еще одна задача

    Вот еще одна сноска[^1].

    [^1]: Описание первой сноски.
    [^task]: Сноска для задачи.
""".trimIndent()) }

// Оберните MarkdownText в скроллируемый контейнер
Column(modifier = Modifier.verticalScroll(scrollState)) {
    MarkdownText(
        markdown = markdownContent, // Используем состояние
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        footnotePositions = footnotePositions, // Передаем карту для записи позиций
        onFootnoteReferenceClick = { identifier ->
            // Ищем позицию по ID сноски
            footnotePositions[identifier]?.let { position ->
                // Запускаем плавный скролл
                coroutineScope.launch {
                    scrollState.animateScrollTo(position.toInt())
                }
            }
        },
        onTaskCheckedChange = { taskNode: TaskListItemNode, isChecked: Boolean ->
            // Обработка изменения состояния чекбокса
            // ВАЖНО: Эта лямбда только уведомляет. Вам нужно обновить исходный markdownContent.
            // Это ПРОСТОЙ пример обновления строки. В реальном приложении может потребоваться
            // более сложная логика для поиска и замены строки задачи.
            val taskText = taskNode.content.joinToString("") { node ->
                 // Упрощенное получение текста узла (может быть неточным для сложных inline)
                 when(node) {
                     is TextNode -> node.text
                     is BoldTextNode -> node.text
                     is ItalicTextNode -> node.text
                     is StrikethroughTextNode -> node.text
                     is CodeNode -> "`" + node.code + "`"
                     is LinkNode -> "[${node.text}](${node.url})"
                     is FootnoteReferenceNode -> "[^${node.identifier}]"
                     else -> ""
                 }
            }
            val oldTaskLine = "- [${if (isChecked) " " else "x"}] $taskText"
            val newTaskLine = "- [${if (isChecked) "x" else " "}] $taskText"

            if (markdownContent.contains(oldTaskLine)) {
                 markdownContent = markdownContent.replace(oldTaskLine, newTaskLine)
                 println("Task state changed: $newTaskLine")
            } else {
                 println("Warning: Could not find task line to update: $oldTaskLine")
            }
        }
        // Можно передать кастомные renderers и styleSheet здесь
        // renderers = customRenderers,
        // styleSheet = customStyleSheet
    )
}
```

### Пример списка определений

```markdown
Термин 1
: Определение 1

Термин 2 с `кодом`
: Определение 2а
: Определение 2б
```

## 🎨 Кастомизация стилей (`MarkdownStyleSheet`)

Вы можете легко настроить внешний вид элементов, передав собственный `MarkdownStyleSheet`. Используйте `defaultMarkdownStyleSheet()` как основу и модифицируйте нужные стили с помощью `copy()`:

```kotlin
// ...

val customStyleSheet = defaultMarkdownStyleSheet().copy(
    textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground),
    headerStyle = defaultMarkdownStyleSheet().headerStyle.copy(
        h1 = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
        bottomPadding = 12.dp
    ),
    codeBlockStyle = defaultMarkdownStyleSheet().codeBlockStyle.copy(
        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
        codeBackground = Color.DarkGray.copy(alpha = 0.1f)
    ),
    inlineCodeStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        fontSize = 13.sp
    ),
    taskListItemStyle = defaultMarkdownStyleSheet().taskListItemStyle.copy(
        checkedTextStyle = SpanStyle(textDecoration = TextDecoration.LineThrough, color = Color.Gray)
    ),
    footnoteReferenceStyle = SpanStyle( // Стиль для ссылки [1]
        color = MaterialTheme.colorScheme.secondary,
        baselineShift = BaselineShift.Superscript, // Делаем верхним индексом
        fontSize = 12.sp // Меньше размер
    ),
    footnoteDefinitionStyle = TextStyle( // Стиль для текста определения [1]: ...
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp
    ),
    definitionListStyle = defaultMarkdownStyleSheet().definitionListStyle.copy(
        termTextStyle = TextStyle(fontWeight = FontWeight.Bold),
        detailsIndent = 20.dp,
        itemSpacing = 10.dp
    )
)

// Затем передайте его в MarkdownText:
// MarkdownText(..., styleSheet = customStyleSheet)
```

## 🔧 Расширяемый рендеринг (`MarkdownRenderers`)

Для полной кастомизации отображения конкретных элементов (например, для использования Coil для загрузки изображений или добавления своей подсветки кода), вы можете передать собственный объект `MarkdownRenderers`.

```kotlin
// ...

val customRenderers = defaultMarkdownRenderers().copy(
    // Переопределяем рендеринг изображений для использования Coil
    renderImage = { node: ImageNode, styleSheet: MarkdownStyleSheet, modifier: Modifier ->
        AsyncImage(
            model = node.url,
            contentDescription = node.altText,
            modifier = modifier
                .fillMaxWidth() // Пример модификатора
                .then(styleSheet.imageStyle.modifier), // Применяем стили из StyleSheet
            contentScale = styleSheet.imageStyle.contentScale,
            placeholder = styleSheet.imageStyle.placeholder,
            error = styleSheet.imageStyle.error
        )
    },
    // Можно переопределить и другие рендереры, например, renderCodeBlock, renderDefinitionList
    // renderCodeBlock = { node, styleSheet, modifier -> /* Ваша реализация */ },
    // renderDefinitionList = { node, styleSheet, modifier, footnoteMap, linkHandler, footnoteClickHandler -> /* Ваша реализация */ }
)

// Затем передайте его в MarkdownText:
// MarkdownText(..., renderers = customRenderers)

```

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте новую ветку (`git checkout -b feature/your-feature-name`)
3. Внесите изменения и закоммитьте их (`git commit -m 'Add feature'`)
4. Отправьте изменения (`git push origin feature/your-feature-name`)
5. Откройте пулл-реквест

⭐ Если вам понравился проект, поддержите его звездочкой на GitHub!
