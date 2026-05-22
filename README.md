# 📊 Архитектура распределенной системы интеграции и анализа данных (Inter-Service Integration)

🚀 **Проект по интеграции микросервисов, асинхронному обмену данными и автоматизированному анализу артефактов.**

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![MinIO](https://img.shields.io/badge/MinIO-C72C48?style=for-the-badge&logo=minio&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JaCoCo](https://img.shields.io/badge/Coverage-90.5%25-brightgreen?style=for-the-badge)

---

## 📝 1. Подробное описание проделанной работы

В рамках данного проекта спроектирована и реализована отказоустойчивая микросервисная архитектура для загрузки, распределенного хранения и последующего асинхронного анализа студенческих работ.

### Основные компоненты системы:

**1. `gateway-service` (API Gateway)** Единая точка входа, обеспечивающая абстракцию микросервисной архитектуры для клиента.
* Прозрачно перенаправляет запросы на целевые микросервисы (`storing-service` или `analysis-service`), скрывая внутреннюю топологию сети.
* Выступает централизованным узлом для управления трафиком, логирования запросов и масштабирования системы без внесения изменений в бизнес-логику отдельных сервисов.

**2. `storing-service` (Сервис хранения)** Реализует REST API для приема файлов и сопутствующих метаданных.
* Обеспечивает персистентность данных: метаданные транзакционно сохраняются в реляционную СУБД **PostgreSQL**, а физические файлы отправляются в S3-совместимое объектное хранилище **MinIO**.
* Инициирует событийно-ориентированное взаимодействие, публикуя сообщения о новых артефактах в брокер **RabbitMQ**.

<p align="center">
  <img src="photo/swager1.png" alt="Swagger: POST-запрос на загрузку работы" width="800">
  <br>
  <i>Рисунок 1. Успешный POST-запрос на отправку файла с заданием и метаданными студента.</i>
</p>

<p align="center">
  <img src="photo/minio1.png" alt="Интерфейс бакета MinIO" width="800">
  <br>
  <i>Рисунок 2. Успешная загрузка физического файла в изолированный бакет MinIO (S3).</i>
</p>

<p align="center">
  <img src="photo/postgres1.png" alt="Таблица Works в PostgreSQL" width="800">
  <br>
  <i>Рисунок 3. Сохранение метаданных загруженной работы в таблицу реляционной БД.</i>
</p>

**3. `analysis-service` (Сервис анализа)** Работает в фоновом режиме, асинхронно потребляя сообщения из очереди RabbitMQ.
* Извлекает текстовое содержимое работ, выполняет синтаксический разбор и строгую валидацию данных.
* Интегрирован с внешним REST API (генерация облака тегов/ключевых слов через HTTP-клиент `WordCloudClient`) 

<p align="center">
  <img src="photo/rabbit1.png" alt="Мониторинг очереди RabbitMQ" width="800">
  <br>
  <i>Рисунок 4. Маршрутизация и доставка сообщения через Exchange в очередь RabbitMQ.</i>
</p>

<p align="center">
  <img src="photo/Proverka1.png" alt="Лог успешной валидации" width="800">
  <br>
  <i>Рисунок 5. Успешное принятие данных и прохождение этапа валидации.</i>
</p>

<p align="center">
  <img src="photo/proverka2.png" alt="Лог ошибок валидации" width="800">
  <br>
  <i>Рисунок 6. Обработка ошибки: валидация не пройдена, сформирован детализированный список нарушений.</i>
</p>

<p align="center">
  <img src="photo/api.png" alt="Результат интеграции с QuickChart API" width="800">
  <br>
  <i>Рисунок 7. Интеграция с внешним API (quickchart.io/wordcloud): сохранение метаданных в БД и сгенерированного .png отчета в S3.</i>
</p>

<p align="center">
  <img src="photo/swager2.png" alt="Swagger: GET-запрос результатов" width="800">
  <br>
  <i>Рисунок 8. GET-запрос для получения итоговой информации о результатах проверки и привязанных метаданных.</i>
</p>

---



## 🔄 2. Технические сценарии и взаимодействие (Data Flow)

Система построена на принципах слабой связанности (Loose Coupling) и асинхронного взаимодействия.

### Пошаговый технический сценарий:
1. **Загрузка (Client -> REST):** Клиент отправляет `Multipart` HTTP POST-запрос на эндпоинт `storing-service`.
2. **Изоляция данных:** Бизнес-логика разделяет запрос: бинарный поток (файл) загружается в корзину **MinIO**. Метаданные (информация о студенте, задании, времени отправки, статусе) валидируются через `jakarta.validation` и сохраняются в **PostgreSQL** в рамках единой ACID-транзакции.
3. **Публикация события:** После успешного коммита в БД, `storing-service` отправляет JSON-сообщение в Exchange брокера **RabbitMQ**. Клиент мгновенно получает ответ `201 Created`, не дожидаясь окончания тяжелого анализа.
4. **Асинхронное потребление:** `analysis-service` через `@RabbitListener` извлекает событие из очереди.
5. **Внешняя интеграция:** Сервис анализа запрашивает данные, подготавливает текст и вызывает внешнее API через `WordCloudClient` для построения визуального отчета. При падении сети срабатывает защитный `catch`-блок, сохраняющий консистентность системы.
6. **Обновление статуса:** Результаты анализа фиксируются в базе данных, переводя статус работы в финальное состояние.

---

## 🗺️ 3. Модели и диаграммы системы

### Диаграмма взаимодействия микросервисов (Sequence Diagram)
Ниже представлена интерактивная диаграмма последовательности, описывающая полный жизненный цикл обработки запросов в системе.

```mermaid
sequenceDiagram
    autonumber
   actor Teaсher as Teacher
    actor Student as Student
    participant GW as Gateway
    participant SS as StoringService
    participant MinIO as MinIO_S3
    participant DB as PostgreSQL
    participant RMQ as RabbitMQ
    participant AS as AnalysisService
    participant QC as QuickChart_API
      
    Student->>GW: POST /works/upload
    GW->>SS: Forward Request
    activate SS
    SS->>MinIO: Upload binary file
    SS->>DB: INSERT into METADATA in works table
    SS->>RMQ: Publish event (work_id)
    SS-->>GW: HTTP 201 Created
    deactivate SS
    GW-->>Student: Response Success
      
    Note over RMQ, AS: Asynchronous Background Process
    RMQ->>AS: Deliver message (work_id)
    activate AS
    AS->>QC: HTTP GET /chart
    QC-->>AS: Return image bytes
   AS->>MinIO: Save file from API
    AS->>DB: INSERT into report table
   Teaсher->>GW: GET /report/*
   GW->>AS: Forward Request
   AS->>DB: Select METADATA in reports table
   AS-->>GW: HTTP 200 OK
   GW-->>Teaсher: Response Success
    deactivate AS
```

### Покрытие кода тестами (JaCoCo Report)
Локальные метрики верификации качества кода исключают служебные структуры данных, гарантируя проверку только бизнес-логики.

<p align="center">
  <img src="photo/tests.png" alt="Отчет JaCoCo покрытия тестами" width="600">
  <br>
  <i>Рисунок 10. Интерактивный отчёт покрытия кода тестами JaCoCo (Total Coverage).</i>
</p>

