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
- Блоки кода с подсветкой, кнопкой копирования и настройками стилей
- > Блочные цитаты (`> текст`)
- 📊 Таблицы с различными стилями и настраиваемыми границами (поддержка Canvas и Compose Tables)
- Разделители (`---`, `***`, `___`)
- ✅ **Изображения** (`![alt текст](URL)`) с поддержкой загрузки (Coil) и кастомизации.
- ✅ **Ссылки-изображения** (`[![alt текст](img URL)](link URL)`).
- Настраиваемое расстояние между блоками и строками.
- 📌 **Поддержка HTML внутри Markdown** (вставка `<div>`, `<span>` и других тегов)
- 🎨 **Темы оформления** (светлая, тёмная, кастомные стили)
- ⚡ **Оптимизированный рендеринг для больших документов**

## 🚀 Установка

На данный момент библиотека находится в разработке. Добавьте зависимость в `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":markdown-compose"))
}
```

## 📖 Пример использования

```kotlin
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

        ```kotlin
        fun greet(name: String) {
            println("Hello, $name!")
        }
        ```
    """.trimIndent(),
    modifier = Modifier.fillMaxWidth().padding(16.dp)
)
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
    )
)
```

## 🔧 Разработка

Проект находится в активной разработке. Мы работаем над улучшением поддержки таблиц, добавлением новых тем и оптимизацией производительности.

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте новую ветку (`git checkout -b feature/your-feature-name`)
3. Внесите изменения и закоммитьте их (`git commit -m 'Add feature'`)
4. Отправьте изменения (`git push origin feature/your-feature-name`)
5. Откройте пулл-реквест

⭐ Если вам понравился проект, поддержите его звездочкой на GitHub!