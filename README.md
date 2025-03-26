# Markdown Compose Lib

![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-%2300C853.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)

**Markdown Compose Lib** — это удобная библиотека для рендеринга Markdown в Jetpack Compose. Она позволяет легко интегрировать поддержку Markdown в Android-приложения, обеспечивая гибкость, простоту использования и широкие возможности кастомизации.

## 📌 Возможности

✅ Поддержка базовых элементов Markdown:
- Заголовки (H1-H6)
- Списки:
    - Маркированные (неупорядоченные) (`-`, `*`, `+`)
    - Нумерованные (упорядоченные) (`1.`, `2.`, ...)
    - Вложенные списки (с настраиваемыми отступами)
- Ссылки (`[текст](URL)`)
- **Жирный текст** (`**текст**` или `__текст__`)
- *Курсив* (`*текст*` или `_текст_`)
- ~~Зачеркнутый текст~~ (`~~текст~~`)
- `Inline code` (`` `код` ``)
- Блоки кода (`` ```код``` ``) - однострочные и многострочные (базовая стилизация)
- > Блочные цитаты (`> текст`) с кастомным фоном/полосой
- 📊 Таблицы с различными стилями выравнивания, настраиваемыми границами и скруглением углов
- Разделитель (`---`, `***`, `___`) с настраиваемой толщиной/цветом
- Настраиваемое расстояние между блоками и строками

## 🚀 Установка

На данный момент библиотека на этапе активной разработки. Для использования добавьте зависимость в ваш `build.gradle.kts`:

```kotlin
dependencies {
    // Замените 'version' на актуальную версию, когда она будет доступна
    // implementation("com.byteflipper:markdown-compose:version")

    // Локально (если модуль находится в вашем проекте):
    implementation(project(":markdown-compose"))
}
```

## 📖 Пример использования (базовый)

Использовать библиотеку очень просто. Достаточно вызвать Composable-функцию `MarkdownText`, передав ей Markdown-строку:

```kotlin
// ... внутри вашего Composable

MarkdownText(
    markdown = """
        # Привет, Мир!
        Это **простой** пример использования `MarkdownText`.

        - Пункт 1
        - Пункт 2
            - Вложенный пункт

        [Больше информации](https://...)
    """.trimIndent(),
    modifier = Modifier.fillMaxWidth().padding(16.dp)
    // styleSheet = defaultMarkdownStyleSheet() // Используется по умолчанию
)
```

## 🎨 Кастомизация стилей

Библиотека позволяет гибко настраивать внешний вид всех элементов Markdown с помощью `MarkdownStyleSheet`.

**1. Использование стилей по умолчанию:**

По умолчанию `MarkdownText` использует стили, основанные на текущей `MaterialTheme` вашего приложения. Их можно получить с помощью `defaultMarkdownStyleSheet()`.

```kotlin
MarkdownText(
    markdown = "...",
    styleSheet = defaultMarkdownStyleSheet() // Явно передаем стиль по умолчанию
)
```

**2. Создание кастомного стиля:**

Вы можете легко переопределить стили по умолчанию, скопировав их и изменив нужные параметры.

```kotlin
// ... внутри вашего Composable

// Получаем стили по умолчанию
val defaultStyles = defaultMarkdownStyleSheet()

// Создаем кастомный стиль на основе дефолтного
val customStyleSheet = defaultStyles.copy(
    // Изменяем базовый стиль текста
    textStyle = defaultStyles.textStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),
    // Изменяем стили заголовков
    headerStyle = defaultStyles.headerStyle.copy(
        h1 = defaultStyles.headerStyle.h1.copy(color = MaterialTheme.colorScheme.tertiary),
        h2 = defaultStyles.headerStyle.h2.copy(color = MaterialTheme.colorScheme.secondary, fontSize = 26.sp),
        bottomPadding = 10.dp // Отступ после заголовков
    ),
    // Настраиваем списки
    listStyle = defaultStyles.listStyle.copy(
        indentPadding = 12.dp, // Увеличиваем отступ для вложенности
        bulletChars = listOf("* ", "+ ", "- ") // Используем другие символы маркеров
    ),
    // Настраиваем таблицы
    tableStyle = defaultStyles.tableStyle.copy(
        borderColor = MaterialTheme.colorScheme.primary,
        borderThickness = 2.dp, // Утолщаем границы
        cellPadding = 10.dp, // Увеличиваем отступы в ячейках
        outerBorderShape = RoundedCornerShape(8.dp) // Скругляем углы таблицы!
    ),
    // Настраиваем разделитель
    horizontalRuleStyle = defaultStyles.horizontalRuleStyle.copy(
         color = MaterialTheme.colorScheme.error,
         thickness = 2.dp
    ),
    // Настраиваем цитаты
    blockQuoteStyle = defaultStyles.blockQuoteStyle.copy(
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        verticalBarColor = MaterialTheme.colorScheme.primary,
        verticalBarWidth = 6.dp,
        padding = 12.dp
    ),
    // Настраиваем блоки кода
    codeBlockStyle = defaultStyles.codeBlockStyle.copy(
        backgroundColor = Color.DarkGray.copy(alpha=0.8f),
        textStyle = defaultStyles.codeBlockStyle.textStyle.copy(color = Color.White)
    ),
    // Настраиваем ссылки
    linkStyle = defaultStyles.linkStyle.copy(
        color = Color(0xFFB00020) // Ярко-красный цвет для ссылок
    ),
    // Настраиваем отступы между блоками
    blockSpacing = 20.dp
)

// Используем кастомный стиль
MarkdownText(
    markdown = """
        # Заголовок H1 (Tertiary)
        ## Заголовок H2 (Secondary)

        Текст с кастомным размером шрифта.

        > Цитата с другим фоном и полосой.

        * Маркер списка '*'
            + Маркер списка '+'
                - Маркер списка '-'

        | Таблица    | Со скруглением |
        |------------|----------------|
        | Границы    | Толще (2dp)    |
        | Отступы    | Больше (10dp)  |

        --- (Красный разделитель)

        `inline code`

        ```
        // Блок кода с темным фоном
        val theme = remember { CustomTheme() }
        ```
        [Это красная ссылка](...)
    """.trimIndent(),
    styleSheet = customStyleSheet,
    modifier = Modifier.fillMaxWidth().padding(16.dp)
)

```

**Настраиваемые элементы:**

Вы можете изменить стили для следующих категорий через `MarkdownStyleSheet`:

*   `textStyle`: Базовый стиль текста.
*   `boldTextStyle`, `italicTextStyle`, `strikethroughTextStyle`: Стили для форматирования текста.
*   `headerStyle`: Стили для заголовков (H1-H6) и отступа после них.
*   `listStyle`: Маркеры, форматирование номеров, отступы и интервалы для списков.
*   `blockQuoteStyle`: Стиль текста, цвет/ширина вертикальной полосы, фон и отступы для цитат.
*   `codeBlockStyle`: Стиль текста, фон и отступы для блоков кода и inline-кода.
*   `tableStyle`: Отступы ячеек, толщина/цвет границ, форма внешних границ (для скругления).
*   `horizontalRuleStyle`: Цвет и толщина горизонтального разделителя.
*   `linkStyle`: Цвет и подчеркивание для ссылок.
*   `blockSpacing`: Вертикальный отступ между блочными элементами (параграфами, списками, таблицами и т.д.).
*   `lineBreakSpacing`: Вертикальный отступ, добавляемый при явном разрыве строки (пустая строка в Markdown).

Для полного списка свойств обратитесь к исходному коду файла `MarkdownStyleSheet.kt`.

## 🔧 Разработка

Проект находится в активной разработке. В ближайших обновлениях планируется улучшение рендеринга таблиц и расширение поддержки Markdown.

## 🤝 Вклад в проект

Будем рады вашему участию в развитии проекта! Вы можете:

1.  Форкнуть репозиторий
2.  Создать новую ветку (`git checkout -b feature-branch`)
3.  Внести изменения и закоммитить их (`git commit -m 'Add feature'`)
4.  Отправить изменения (`git push origin feature-branch`)
5.  Открыть пулл-реквест

---

⭐ Если вам понравился проект, поддержите его звездочкой на GitHub!