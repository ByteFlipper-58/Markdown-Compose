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
    - ✅ **Списки задач (Checkboxes)** (`- [ ] текст`, `- [x] текст`) с настраиваемым внешним видом текста и самого `Checkbox`.
    - Вложенные списки (с настраиваемыми отступами)
- Ссылки (`[текст](URL)`):
    - ✅ **Кликабельные ссылки** с возможностью кастомной обработки (`onLinkClick`) или открытием в браузере по умолчанию.
- **Жирный текст** (`**текст**` или `__текст__`)
- *Курсив* (`*текст*` или `_текст_`)
- ~~Зачеркнутый текст~~ (`~~текст~~`)
- `Inline code` (`` `код` ``)
- Блоки кода (`` ```код``` ``):
    - Определение языка (`` ```kotlin ... ``` ``)
    - Отображение метки языка сверху блока (настраиваемое)
    - Нижняя панель с кнопкой копирования кода, счетчиком строк/символов (настраиваемое)
    - Гибкая настройка фона, отступов, стилей текста для блока, метки языка и инфо-панели.
- > Блочные цитаты (`> текст`) с кастомным фоном/полосой и поддержкой **кликабельных ссылок** внутри.
- 📊 Таблицы:
    - Различные стили выравнивания.
    - Настраиваемые границы и скругление углов.
    - ✅ Поддержка **кликабельных ссылок** внутри ячеек.
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

## 📖 Пример использования

### Базовый пример

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

        [Больше информации](https://developer.android.com/jetpack/compose)

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

        ### Таблица с ссылкой
        | Ресурс         | Ссылка                             | Описание                          |
        |----------------|------------------------------------|-----------------------------------|
        | Google         | [Перейти на Google](https://google.com) | Поисковая система                 |
        | Compose Dev    | [Документация](https://developer.android.com/jetpack/compose) | Официальная документация Compose |
        | Kotlin Lang    | [*Сайт Kotlin*](https://kotlinlang.org) | Информация о языке Kotlin     |

    """.trimIndent(),
    modifier = Modifier.fillMaxWidth().padding(16.dp)
    // styleSheet = defaultMarkdownStyleSheet() // Используется по умолчанию
    // onLinkClick = { url -> /* ... обработать клик ... */ } // По умолчанию открывает в браузере
)
```

### Обработка кликов по ссылкам

По умолчанию ссылки открываются в браузере. Вы можете переопределить это поведение, передав лямбда-функцию в параметр `onLinkClick`:


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
        bulletChars = listOf("* ", "+ ", "- "),
        itemSpacing = 6.dp
    ),

    // --- Настраиваем СПИСКИ ЗАДАЧ (Чекбоксы) ---
    taskListItemStyle = defaults.taskListItemStyle.copy(
        // Стиль ТЕКСТА для выполненных задач (например, серый + зачеркнутый)
        checkedTextStyle = SpanStyle(
            color = MaterialTheme.colorScheme.outline, // Нейтральный цвет для текста выполненного пункта
            textDecoration = TextDecoration.LineThrough
        ),
        // --- Настройка ЦВЕТОВ самого Checkbox ---
        checkedCheckboxContainerColor = MaterialTheme.colorScheme.secondary,
        checkedCheckboxIndicatorColor = MaterialTheme.colorScheme.onSecondary,
        uncheckedCheckboxBorderColor = MaterialTheme.colorScheme.secondary,
    ),

    // Настраиваем таблицы
    tableStyle = defaults.tableStyle.copy(
        borderColor = MaterialTheme.colorScheme.primary,
        borderThickness = 2.dp, // Утолщаем границы
        cellPadding = 10.dp, // Увеличиваем отступы в ячейках
        outerBorderShape = RoundedCornerShape(8.dp) // Скругляем углы таблицы!
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

    // Настраиваем БЛОКИ КОДА
    codeBlockStyle = defaults.codeBlockStyle.copy(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
        textStyle = defaults.codeBlockStyle.textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        codeBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        // Верхняя метка языка
        languageLabelTextStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
        languageLabelBackground = MaterialTheme.colorScheme.surfaceVariant,
        // Нижняя инфо-панель
        infoBarTextStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        infoBarBackground = MaterialTheme.colorScheme.surfaceVariant,
        copyIconTint = MaterialTheme.colorScheme.primary,
    ),

    // Настраиваем INLINE КОД
    inlineCodeStyle = defaults.inlineCodeStyle.copy(
        background = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        color = MaterialTheme.colorScheme.onTertiaryContainer
    ),

    // Настраиваем ссылки
    linkStyle = defaults.linkStyle.copy(
        color = Color(0xFFB00020) // Например, красный цвет для ссылок
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

        > Цитата с другим фоном, полосой и [ссылкой внутри](https://example.com).

        * Маркер списка '*'
            + Маркер списка '+'
                - Маркер списка '-'

        **Кастомные списки задач:**
        - [x] Выполнено (Стиль текста изменен, Checkbox цвета Secondary)
        - [ ] Не выполнено (Рамка Checkbox цвета Secondary)
        - [x] Еще [выполнено со ссылкой](https://example.com/done)

        ```kotlin
        // Блок кода с меткой языка, кнопкой копирования и счетчиками
        data class User(val id: Int, val name: String)

        fun processUser(user: User) {
           println("Processing ${user.name}...")
        }
        ```

        | Таблица    | Со скруглением | Ссылка                             |
        |------------|----------------|------------------------------------|
        | Границы    | Толще (2dp)    | [Jetpack](https://developer.android.com/jetpack) |
        | Отступы    | Больше (10dp)  | [*Курсив* ссылка](https://kotlinlang.org) |

        --- (Красный разделитель)

        [Это красная ссылка](...)
    """.trimIndent(),
    styleSheet = customStyleSheet,
    modifier = Modifier.fillMaxWidth().padding(16.dp),
    // onLinkClick = { url -> /* ... */ } // Можно добавить обработчик
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
*   `tableStyle`: Отступы ячеек, толщина/цвет границ, форма внешних границ (для скругления).
*   `horizontalRuleStyle`: Цвет и толщина горизонтального разделителя.
*   `linkStyle`: Цвет и подчеркивание для ссылок.
*   `blockSpacing`: Вертикальный отступ между блочными элементами.
*   `lineBreakSpacing`: Вертикальный отступ, добавляемый при явном разрыве строки.

Для полного списка свойств обратитесь к исходному коду файла `MarkdownStyleSheet.kt`.

## 🔧 Разработка

Проект находится в активной разработке.

## 🤝 Вклад в проект

Будем рады вашему участию в развитии проекта! Вы можете:

1.  Форкнуть репозиторий
2.  Создать новую ветку (`git checkout -b feature-branch`)
3.  Внести изменения и закоммитить их (`git commit -m 'Add feature'`)
4.  Отправить изменения (`git push origin feature-branch`)
5.  Открыть пулл-реквест

---

⭐ Если вам понравился проект, поддержите его звездочкой на GitHub!