# Markdown Compose Lib

![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-%2300C853.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Coil](https://img.shields.io/badge/Coil-%23FFA500.svg?style=for-the-badge&logo=coil&logoColor=white)


**Markdown Compose Lib** — это удобная библиотека для рендеринга Markdown в Jetpack Compose. Она позволяет легко интегрировать поддержку Markdown в Android-приложения, обеспечивая гибкость, простоту использования и широкие возможности кастомизации.

## 📌 Возможности

✅ Поддержка базовых элементов Markdown:
- Заголовки (H1-H6)
- Списки:
    - Маркированные (неупорядоченные) (`-`, `*`, `+`)
    - Нумерованные (упорядоченные) (`1.`, `2.`, ...)
    - ✅ **Списки задач (Checkboxes)** (`- [ ] текст`, `- [x] текст`) с настраиваемым внешним видом текста и самого `Checkbox`.
    - Вложенные списки (с настраиваемыми отступами)
- Ссылки (`[текст](URL)`)
- **Жирный текст** (`**текст**` или `__текст__`)
- *Курсив* (`*текст*` или `_текст_`)
- ~~Зачеркнутый текст~~ (`~~текст~~`)
- `Inline code` (`` `код` ``)
- Блоки кода (`` ```код``` ``):
    - Определение языка (`` ```kotlin ... ``` ``)
    - Отображение метки языка сверху блока (настраиваемое)
    - Нижняя панель с кнопкой копирования кода, счетчиком строк/символов (настраиваемое)
    - Гибкая настройка фона, отступов, стилей текста для блока, метки языка и инфо-панели.
- > Блочные цитаты (`> текст`) с кастомным фоном/полосой
- 📊 Таблицы с различными стилями выравнивания, настраиваемыми границами и скруглением углов
- Разделитель (`---`, `***`, `___`) с настраиваемой толщиной/цветом
- ✅ **Изображения** (`![alt текст](URL)`) с поддержкой загрузки (Coil) и кастомизации (форма, масштаб, отступы, плейсхолдеры/ошибки).
- ✅ **Ссылки-изображения** (`[![alt текст](img URL)](link URL)`).
- Настраиваемое расстояние между блоками и строками

## 🚀 Установка

На данный момент библиотека на этапе активной разработки. Для использования добавьте зависимость в ваш `build.gradle.kts` (модуля `app`):

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

        **Список задач:**
        - [x] Что-то сделано
        - [ ] Что-то нужно сделать

        [Больше информации](https://...)

        Простой блок кода:
        ```
        val language = "Markdown"
        println("Render $language!")
        ```

        Блок кода с языком:
        ```kotlin
        fun greet(name: String) {
            // Комментарий
            println("Hello, $name!")
        }
        ```

        Простое изображение:
        ![Compose Logo](https://seeklogo.com/images/J/jetpack-compose-logo-8BC315158E-seeklogo.com.png)

        Изображение-ссылка (картинка будет кликабельной):
        [![GitHub Logo](https://github.githubassets.com/assets/GitHub-Mark-ea2971cee799.png)](https://github.com)

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
val defaults = defaultMarkdownStyleSheet()

val customStyleSheet = defaults.copy(
    // Изменяем базовый стиль текста
    textStyle = defaults.textStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),

  // Изменяем стили заголовков
    headerStyle = defaults.headerStyle.copy(
        h1 = defaults.headerStyle.h1.copy(color = MaterialTheme.colorScheme.tertiary),
        h2 = defaults.headerStyle.h2.copy(color = MaterialTheme.colorScheme.secondary, fontSize = 26.sp),
        bottomPadding = 10.dp
    ),

  // Настраиваем списки
    listStyle = defaults.listStyle.copy(
        indentPadding = 12.dp,
        bulletChars = listOf("* ", "+ ", "- ")
    ),

  // --- Настраиваем СПИСКИ ЗАДАЧ (Чекбоксы) ---
    taskListItemStyle = defaults.taskListItemStyle.copy(
        checkedTextStyle = SpanStyle(
            color = MaterialTheme.colorScheme.outline,
            textDecoration = TextDecoration.LineThrough
        ),
        checkedCheckboxContainerColor = MaterialTheme.colorScheme.secondary,
        checkedCheckboxIndicatorColor = MaterialTheme.colorScheme.onSecondary,
        uncheckedCheckboxBorderColor = MaterialTheme.colorScheme.secondary,
    ),

  // Настраиваем таблицы
    tableStyle = defaults.tableStyle.copy(
        borderColor = MaterialTheme.colorScheme.primary,
        borderThickness = 2.dp,
        cellPadding = 10.dp,
        outerBorderShape = RoundedCornerShape(8.dp)
    ),

  // Настраиваем разделитель
    horizontalRuleStyle = defaults.horizontalRuleStyle.copy(
         color = MaterialTheme.colorScheme.error,
         thickness = 2.dp
    ),

  // Настраиваем цитаты
    blockQuoteStyle = defaults.blockQuoteStyle.copy(
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        verticalBarColor = MaterialTheme.colorScheme.primary,
        verticalBarWidth = 6.dp,
        padding = 12.dp
    ),

  // ---- Настраиваем БЛОКИ КОДА (` ``` `) ----
    codeBlockStyle = defaults.codeBlockStyle.copy(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
        textStyle = defaults.codeBlockStyle.textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        codeBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        languageLabelTextStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
        languageLabelBackground = MaterialTheme.colorScheme.surfaceVariant,
        showInfoBar = true,
        infoBarTextStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        infoBarBackground = MaterialTheme.colorScheme.surfaceVariant,
        showCopyButton = true,
        copyIconTint = MaterialTheme.colorScheme.primary,
        showLineCount = true,
        showCharCount = true
    ),

  // ---- Настраиваем INLINE КОД (` `) ----
    inlineCodeStyle = defaults.inlineCodeStyle.copy(
        background = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        color = MaterialTheme.colorScheme.onTertiaryContainer
    ),

  // ---- Настраиваем ИЗОБРАЖЕНИЯ (`![]()`) ----
  imageStyle = defaults.imageStyle.copy(
      modifier = Modifier.padding(vertical = 8.dp), // Добавляем отступы вокруг изображений
      shape = RoundedCornerShape(12.dp), // Скругляем углы
      contentScale = ContentScale.Crop, // Обрезаем, чтобы заполнить фигуру (если изображение не квадратное)
      // Пример добавления плейсхолдера/ошибки (нужны соответствующие ресурсы в вашем app/drawable)
      // placeholder = painterResource(id = R.drawable.image_placeholder),
      // error = painterResource(id = R.drawable.image_error_placeholder)
  ),

  // Настраиваем ссылки
    linkStyle = defaults.linkStyle.copy(
        color = Color(0xFFB00020) // Красный цвет для ссылок
    ),

  // Настраиваем отступы между блоками
    blockSpacing = 20.dp
)

// Используем кастомный стиль
MarkdownText(
    markdown = """
        # Заголовок H1 (Tertiary)
        ## Заголовок H2 (Secondary)

        Текст с `inline code` стилем и кастомным размером шрифта.

        > Цитата с другим фоном и полосой.

        * Маркер списка '*'
            + Маркер списка '+'
                - Маркер списка '-'

        **Кастомные списки задач:**
        - [x] Выполнено (Стиль текста изменен, Checkbox цвета Secondary)
        - [ ] Не выполнено (Рамка Checkbox цвета Secondary)

        ```kotlin
        // Блок кода
        data class User(val id: Int, val name: String)
        ```

        | Таблица    | Со скруглением |
        |------------|----------------|
        | Границы    | Толще (2dp)    |
        | Отступы    | Больше (10dp)  |

        ![Изображение со скругленными углами](https://picsum.photos/seed/compose/400/200)

        --- (Красный разделитель)

        [Это красная ссылка](...)

        [![Круглое изображение-ссылка на picsum.photos](https://picsum.photos/seed/picsum/100/100)](https://picsum.photos/)
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
*   `taskListItemStyle`: Стили для элементов списка задач (`- [ ]`, `- [x]`).
*   `blockQuoteStyle`: Стиль текста, цвет/ширина вертикальной полосы, фон и отступы для цитат.
*   `inlineCodeStyle`: Стиль (`SpanStyle`) для инлайн-кода (`` `code` ``).
*   `codeBlockStyle`: Полная настройка для блоков кода (`` ```code``` ``).
*   `tableStyle`: Отступы ячеек, толщина/цвет границ, форма внешних границ.
*   `horizontalRuleStyle`: Цвет и толщина горизонтального разделителя.
*   `linkStyle`: Цвет и подчеркивание для ссылок.
*   **`imageStyle`**: Настройки для изображений (`![alt](url)` и внутри `[![alt](img)](link)`):
  *   `modifier`: Применяется к контейнеру `AsyncImage` (для отступов, явного размера и т.д.). *Примечание: `fillMaxWidth()` обычно применяется к `ImageComposable` в `MarkdownText`, но его можно переопределить здесь.*
  *   `shape`: Форма для обрезки изображения (например, `RectangleShape`, `CircleShape`, `RoundedCornerShape`).
  *   `contentScale`: Масштабирование контента (например, `ContentScale.Fit`, `ContentScale.Crop`).
  *   `placeholder`: `Painter` для отображения во время загрузки (например, через `painterResource`).
  *   `error`: `Painter` для отображения в случае ошибки загрузки (например, через `painterResource`).
*   `blockSpacing`: Вертикальный отступ между блочными элементами (параграфы, списки, таблицы, изображения и т.д.).
*   `lineBreakSpacing`: Вертикальный отступ, добавляемый при явном разрыве строки (пустая строка в Markdown).

Для полного списка свойств обратитесь к исходному коду файла `MarkdownStyleSheet.kt`.

## 🔧 Разработка

Проект находится в активной разработке.

## 🤝 Вклад в проект

Будем рады вашему участию в развитии проекта! Вы можете:

1.  Форкнуть репозиторий
2.  Создать новую ветку (`git checkout -b feature/your-feature-name`)
3.  Внести изменения и закоммитить их (`git commit -m 'Add feature'`)
4.  Отправить изменения (`git push origin feature/your-feature-name`)
5.  Открыть пулл-реквест

---

⭐ Если вам понравился проект, поддержите его звездочкой на GitHub!