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
  - ✅ **Списки задач** (`- [ ] текст`, `- [x] текст`) с настраиваемым стилем.
  - Вложенные списки
- Ссылки (`[текст](URL)`)
- **Жирный текст** (`**текст**`)
- *Курсив* (`*текст*`)
- ~~Зачеркнутый текст~~ (`~~текст~~`)
- `Inline code` (`` `код` ``)
- Блоки кода с подсветкой (в планах), кнопкой копирования и настройками стилей
- > Блочные цитаты (`> текст`)
- 📊 Таблицы с различными стилями и настраиваемыми границами (поддержка Canvas)
- Разделители (`---`, `***`, `___`)
- ✅ **Изображения** (`![alt текст](URL)`) с поддержкой загрузки (Coil) и кастомизации.
- ✅ **Ссылки-изображения** (`[![alt текст](img URL)](link URL)`).
- ✅ **Сноски (Footnotes)** (`[^id]` и `[^id]: Text`) с настраиваемым стилем и **прокруткой к определению** при клике.
- Настраиваемое расстояние между блоками и строками.
- 📌 **Поддержка HTML внутри Markdown** (базовая, в планах)
- 🎨 **Темы оформления** (светлая, тёмная, кастомные стили)
- ⚡ **Оптимизированный рендеринг** (базовый)

## 🚀 Установка

На данный момент библиотека находится в разработке. Добавьте зависимость в `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":markdown-compose"))
}
```

## 📖 Пример использования (с обработкой сносок)

```kotlin
// Состояния для скролла и позиций сносок
val scrollState = rememberScrollState()
val footnotePositions = remember { mutableStateMapOf<String, Float>() }
val coroutineScope = rememberCoroutineScope()

// Оберните MarkdownText в скроллируемый контейнер
Column(modifier = Modifier.verticalScroll(scrollState)) {
    MarkdownText(
        markdown = """
            # Привет, Мир!
            Это **простой** пример использования `MarkdownText`.

            Вот сноска[^1].

            - Пункт 1
            - Пункт 2 со сноской[^note]
                - Вложенный пункт

            ```kotlin
            fun greet(name: String) {
                println("Hello, $name!")
            }
            ```
            Много текста, чтобы было куда скроллить...
            ...
            Еще текст...
            ...

            [^1]: Это первая сноска.
            [^note]: Это вторая сноска с `кодом`.
        """.trimIndent(),
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
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    )
}
```

## 🎨 Кастомизация стилей

```kotlin
val customStyleSheet = defaultMarkdownStyleSheet().copy(
    textStyle = TextStyle(fontSize = 15.sp),
    headerStyle = defaultMarkdownStyleSheet().headerStyle.copy(
        h1 = TextStyle(color = MaterialTheme.colorScheme.primary)
    ),
    tableStyle = defaultMarkdownStyleSheet().tableStyle.copy(
        borderColor = Color.Gray,
        borderThickness = 2.dp
    ),
    // --- Стили сносок ---
    footnoteReferenceStyle = SpanStyle( // Стиль для ссылки [1]
        color = MaterialTheme.colorScheme.tertiary,
        baselineShift = BaselineShift.Superscript, // Делаем верхним индексом
        fontSize = 12.sp // Меньше размер
    ),
    footnoteDefinitionStyle = TextStyle( // Стиль для текста определения [1]: ...
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp
    ),
    footnoteBlockPadding = 24.dp // Отступ перед блоком определений
)
```

## 🔧 Разработка

Проект находится в активной разработке. Планируется улучшение поддержки таблиц, добавление синтаксической подсветки кода, поддержка HTML и расширений Markdown (GFM).

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте новую ветку (`git checkout -b feature/your-feature-name`)
3. Внесите изменения и закоммитьте их (`git commit -m 'Add feature'`)
4. Отправьте изменения (`git push origin feature/your-feature-name`)
5. Откройте пулл-реквест

⭐ Если вам понравился проект, поддержите его звездочкой на GitHub!