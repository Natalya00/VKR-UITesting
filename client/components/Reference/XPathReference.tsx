import React from 'react';

const codeStyle: React.CSSProperties = {
  background: '#e8f0f8',
  padding: '2px 6px',
  borderRadius: '3px',
  fontFamily: "'Courier New', monospace",
  fontSize: '0.9em'
};

const preStyle: React.CSSProperties = {
  background: '#f5f5f5',
  color: '#333',
  padding: '15px',
  borderRadius: '5px',
  overflowX: 'auto' as const,
  margin: '10px 0'
};

const noteStyle: React.CSSProperties = {
  background: '#d4e5f7',
  borderLeft: '4px solid #6096BA',
  padding: '10px 15px',
  margin: '15px 0'
};

const warningStyle: React.CSSProperties = {
  background: '#eef5fb',
  borderLeft: '4px solid #6096BA',
  padding: '10px 15px',
  margin: '15px 0'
};

export const XPathBasics: React.FC = () => {
  return (
    <div className="reference-section">
      <h2>Модуль XPATH</h2>

      <h3>Основы</h3>

      <p>
        XPath — язык запросов для навигации по элементам и атрибутам в документах XML и HTML.
        CSS-селектор — это шаблон, который позволяет обратиться к элементу или группе элементов веб-страницы, чтобы применить к ним стили CSS.
      </p>

      <p>
        XPath и CSS-селекторы представляют собой формальные языки запросов, предназначенные для поиска и идентификации узлов в иерархической структуре документа (DOM). В контексте автоматизированного тестирования данные инструменты используются для точного нахождения элементов интерфейса.
      </p>

      <h4>Выбор инструмента: CSS или XPath</h4>
      <p>В большинстве случаев предпочтение следует отдавать CSS-селекторам. Они:</p>
      <ul>
        <li>быстрее работают в браузере</li>
        <li>проще читаются</li>
        <li>короче записываются</li>
        <li>лучше подходят для поиска по атрибутам и структуре сверху вниз</li>
      </ul>

      <p>XPath следует использовать:</p>
      <ul>
        <li>если нужно искать элемент по тексту</li>
        <li>если требуется навигация к родителю или предку</li>
        <li>если необходимы сложные логические условия</li>
        <li>если нужно работать с динамическими атрибутами через функции</li>
      </ul>

      <h4>В DOM существуют разные типы узлов:</h4>
      <ul>
        <li><strong>element</strong> — элемент (div, span, input)</li>
        <li><strong>attribute</strong> — атрибут (id, class, name)</li>
        <li><strong>text</strong> — текстовый узел</li>
        <li><strong>comment</strong> — комментарий</li>
      </ul>

      <p>Пример:</p>
      <pre style={preStyle}>{`<div class="card">
    <span>Текст</span>
</div>`}</pre>
      <ul>
        <li>div и span — элементы</li>
        <li>class — атрибут</li>
        <li>"Текст" — текстовый узел</li>
      </ul>

      <p>Понимание структуры DOM критично для построения корректных локаторов. При создании локатора рекомендуется придерживаться следующего приоритета (чем ниже пункт в списке — тем менее устойчив локатор.):</p>
      <ol>
        <li>id (если стабилен)</li>
        <li>data-testid / data-qa и другие тестовые атрибуты</li>
        <li>name</li>
        <li>уникальный атрибут</li>
        <li>комбинация атрибутов</li>
        <li>текст (если неизменяемый)</li>
        <li>иерархия (родитель + потомок)</li>
        <li>индекс (в крайнем случае)</li>
      </ol>

      <h4>Базовый синтаксис</h4>
      <p><strong>CSS-селекторы:</strong></p>
      <ul>
        <li><code style={codeStyle}>tag</code> — поиск по названию тега (например, button).</li>
        <li><code style={codeStyle}>.class</code> — поиск по классу (аналог [class="..."]).</li>
        <li><code style={codeStyle}>#id</code> — поиск по уникальному идентификатору.</li>
        <li><code style={codeStyle}>div p</code> — любой потомок (p где-то внутри div).</li>
        <li><code style={codeStyle}>${`div > p`}</code> — прямой потомок (p строго внутри div).</li>
      </ul>

      <p><strong>XPath:</strong></p>
      <ul>
        <li><code style={codeStyle}>//</code> — относительный путь (искать по всему документу).</li>
        <li><code style={codeStyle}>/</code> — прямой путь (от корня или текущего элемента).</li>
        <li><code style={codeStyle}>[]</code> — фильтр (условие), например <code style={codeStyle}>//input[@name='login']</code>.</li>
        <li><code style={codeStyle}>@</code> — обращение к атрибуту.</li>
        <li><code style={codeStyle}>*</code> — универсальный селектор (любой тег).</li>
        <li><code style={codeStyle}>and, or, not()</code> — логические операторы.</li>
      </ul>

      <h4>Логические операторы</h4>
      <p>Если одного атрибута недостаточно, условия можно комбинировать.</p>

      <p><strong>AND:</strong> В XPath пишется явно <code style={codeStyle}>//tag[@attr1='v1' and @attr2='v2']</code>. В CSS условия просто пишутся подряд без пробела: <code style={codeStyle}>tag[attr1='v1'][attr2='v2']</code>.</p>

      <p><strong>OR:</strong> В XPath используется оператор or: <code style={codeStyle}>//tag[@a='1' or @b='2']</code>. В CSS элементы перечисляются через запятую: <code style={codeStyle}>tag1, tag2</code>.</p>

      <p><strong>NOT:</strong> В XPath это функция <code style={codeStyle}>not()</code>: <code style={codeStyle}>//tag[not(@disabled)]</code>, в CSS — псевдокласс <code style={codeStyle}>:not()</code>.</p>

      <h4>Работа с текстом (только XPath)</h4>
      <p>CSS-селекторы не умеют искать элементы по их текстовому содержимому. В XPath для этого есть следующие инструменты:</p>
      <ul>
        <li><code style={codeStyle}>text()</code> — находит точное совпадение текста.</li>
        <li><code style={codeStyle}>contains(text(), 'фраза')</code> — ищет частичное вхождение.</li>
        <li><code style={codeStyle}>normalize-space()</code> — удаляет лишние пробелы в начале и конце текста, а также заменяет переносы строк на пробелы.</li>
        <li><code style={codeStyle}>starts-with(@attr, 'val')</code> — ищет элементы, атрибут которых начинается с определенной строки.</li>
      </ul>

      <div style={noteStyle}>
        При проектировании локаторов следует отдавать приоритет стабильным уникальным атрибутам (например, id или data-*), обращаясь к поиску по текстовому содержанию только в случае их отсутствия. Использование точного совпадения текста (функция text()) нежелательно, если содержимое элемента подвержено динамическим изменениям; в таких сценариях, а также при наличии в верстке неявных пробелов и переносов строк, рекомендуется применять функцию normalize-space().
      </div>

      <h4>Иерархия и оси</h4>
      <p>Элементы в DOM-дереве связаны как родственники.</p>

      <p><strong>Потомки:</strong> В XPath <code style={codeStyle}>/</code> — это прямой потомок (ребенок), а <code style={codeStyle}>//</code> — любой потомок на любом уровне вложенности. В CSS пробел означает любого потомка, а символ ${`>`} — только прямого.</p>

      <p><strong>Родители:</strong> В XPath можно подняться вверх с помощью <code style={codeStyle}>..</code> или оси <code style={codeStyle}>parent::</code> и <code style={codeStyle}>ancestor::</code>. CSS не умеет подниматься к родителям.</p>

      <p><strong>Основные оси XPath:</strong></p>
      <ul>
        <li><code style={codeStyle}>parent::</code> — родитель</li>
        <li><code style={codeStyle}>ancestor::</code> — любой предок</li>
        <li><code style={codeStyle}>child::</code> — прямой потомок</li>
        <li><code style={codeStyle}>descendant::</code> — любой потомок</li>
        <li><code style={codeStyle}>following-sibling::</code> — следующий сосед</li>
        <li><code style={codeStyle}>preceding-sibling::</code> — предыдущий сосед</li>
      </ul>

      <div style={warningStyle}>
        При написани локаторов следует минимизировать использование вертикальной навигации и избегать построения длинных цепочек из родительских узлов, так как это делает селектор крайне чувствительным к любым структурным изменениям страницы. Старайтесь идентифицировать целевой элемент напрямую через его уникальные атрибуты, прибегая к использованию осей XPath только в тех исключительных случаях, когда альтернативные способы прямой адресации отсутствуют.
      </div>

      <h4>Индексы и группировка</h4>
      <p>Если под условия попадает несколько элементов, можно выбрать конкретный по номеру.</p>

      <p><strong>XPath:</strong> Используются числа в скобках <code style={codeStyle}>(//input)[2]</code>. Важно: индексация в XPath начинается с 1. Функция <code style={codeStyle}>last()</code> позволяет выбрать последний элемент.</p>

      <p><strong>CSS:</strong> Используются псевдоклассы <code style={codeStyle}>:nth-child(n)</code>, <code style={codeStyle}>:first-child</code>, <code style={codeStyle}>:last-child</code>. В CSS индексация также обычно считается с 1 для этих селекторов.</p>

      <div style={noteStyle}>
        Использование индексов допустимо лишь в случаях, когда структура страницы гарантированно стабильна, однако в динамических списках их следует избегать из-за высокого риска ложной идентификации элементов. Всегда отдавайте предпочтение поиску по уникальным признакам вместо привязки к порядковому номеру в DOM-дереве. Также важно помнить, что индексация узлов начинается с 1.
      </div>

      <h4>Скрытые и динамические элементы</h4>
      <p><strong>Динамические атрибуты</strong> — это значения id,class,name,data-* и других атрибутов, которые изменяются при каждом обновлении страницы, при новой сессии или при повторном рендеринге компонента. Чаще всего динамическими бывают id, содержащие случайные числа, хеши или временные метки.</p>

      <p>Пример динамического id:</p>
      <pre style={preStyle}>{`id="user_12345"
id="user_98765"
id="input-abc-456xyz"`}</pre>

      <p>Если использовать строгий поиск:</p>
      <p>XPath:<code style={codeStyle}>//div[@id='user_12345']</code></p>
      <p>такой локатор перестанет работать после изменения числа.</p>

      <p>Для работы с динамическими атрибутами в XPath можно использовать:</p>

      <p><strong>1.starts-with()</strong></p>
      <p>Подходит, если стабильна начальная часть значения.</p>
      <pre style={preStyle}>{`//div[starts-with(@id,'user_')]
 //input[starts-with(@id,'input-')]`}</pre>

      <p><strong>2.contains()</strong></p>
      <p>Подходит, если стабильная часть находится в середине или конце.</p>
      <pre style={preStyle}>{`//div[contains(@id,'user_')]
 //button[contains(@class,'primary')]`}</pre>

      <p><strong>3.Комбинирование условий</strong></p>
      <p>Можно уточнять элемент дополнительными атрибутами.</p>
      <pre style={preStyle}>{`//div[starts-with(@id,'user_') and @role='dialog']`}</pre>

      <p><strong>4.Использование нескольких атрибутов вместо одного нестабильного</strong></p>
      <p>Если id нестабилен, но есть стабильный data-атрибут:</p>
      <p>Плохо:</p>
      <pre style={preStyle}>{`//button[contains(@id,'btn_')]`}</pre>
      <p>Лучше:</p>
      <pre style={preStyle}>{`//button[@data-testid='submit-button']`}</pre>

      <p><strong>5.Использование текста или контекста</strong></p>
      <p>Если атрибуты полностью динамические, можно искать через текст или через родителя.</p>
      <pre style={preStyle}>{`//div[starts-with(@id,'user_')]//span[text()='Иван']`}</pre>
      <p>или</p>
      <pre style={preStyle}>{`//label[text()='Email']/following-sibling::input`}</pre>

      <p><strong>Скрытые элементы</strong></p>
      <p>Идентификация элементов с помощью XPath и CSS-селекторов осуществляется на уровне DOM-структуры документа, поэтому такие свойства стилей, как display: none или visibility: hidden, не препятствуют их обнаружению. Проверка фактической видимости элемента для пользователя является задачей инструментов автоматизации (например, Selenium или Selenide), а не самого языка запросов. Несмотря на то что в локатор можно добавить фильтрацию по атрибутам — например, <code style={codeStyle}>//div[not(contains(@class, 'hidden'))]</code>, — окончательную верификацию отображения объекта на странице следует выносить в логику кода теста, а не перегружать ею селектор.</p>
    </div>
  );
};


export const XPathExamples: React.FC = () => {
  return (
    <div className="reference-section">
      <h3>Примеры</h3>

      <h4>1.Поиск по атрибутам и их комбинациям</h4>
      <p>Базовый поиск основывается на строгом соответствии значений атрибутов тега.</p>

      <p><strong>По идентификатору(ID):</strong></p>
      <ul>
        <li>XPath: <code style={codeStyle}>//input[@id='login-frame']</code></li>
        <li>CSS:<code style={codeStyle}>input#login-frame</code> или <code style={codeStyle}>input[id='login-frame']</code></li>
      </ul>

      <p><strong>По нескольким атрибутам(логическое AND):</strong></p>
      <ul>
        <li>XPath: <code style={codeStyle}>//input[@type='radio' and @name='gender']</code></li>
        <li>CSS: <code style={codeStyle}>input[type='radio'][name='gender']</code></li>
      </ul>

      <p><strong>Использование универсального селектора:</strong></p>
      <ul>
        <li>XPath: <code style={codeStyle}>//*[@data-testid='submit-button']</code></li>
        <li>CSS: <code style={codeStyle}>*[data-testid='submit-button']</code></li>
      </ul>

      <h4>2.Работа с текстовым контентом и частичным совпадением</h4>
      <p>XPath обладает уникальными функциями для анализа текстовых узлов, в то время как CSS ограничен атрибутами.</p>

      <p><strong>Точное совпадение текста(XPath):</strong></p>
      <ul>
        <li><code style={codeStyle}>//button[text()='Отправить']</code></li>
      </ul>

      <p><strong>Частичное вхождение в атрибут или текст:</strong></p>
      <ul>
        <li>XPath(атрибут): <code style={codeStyle}>//div[contains(@class,'container')]</code></li>
        <li>CSS(атрибут): <code style={codeStyle}>div[class*='container']</code></li>
        <li>XPath(текст): <code style={codeStyle}>//p[contains(text(),'результат операции')]</code></li>
      </ul>

      <p><strong>Нормализация пробелов(XPath):</strong></p>
      <ul>
        <li><code style={codeStyle}>//span[normalize-space(text())='Статус активен']</code></li>
      </ul>

      <h4>3.Навигация по иерархии(Оси)</h4>
      <p>Примеры поиска элементов на основе их расположения относительно других узлов.</p>

      <p><strong>От предка к потомку:</strong></p>
      <ul>
        <li>XPath(любой уровень): <code style={codeStyle}>//div[@class='footer']//a</code></li>
        <li>CSS(прямой потомок): <code style={codeStyle}>div.footer{'>'}a</code></li>
      </ul>

      <p><strong>Поиск родителя от известного потомка(XPath):</strong></p>
      <ul>
        <li><code style={codeStyle}>//input[@id='email']/parent::div</code></li>
        <li><code style={codeStyle}>//input[@id='email']/..</code></li>
      </ul>

      <p><strong>Поиск «соседа»(Sibling):</strong></p>
      <ul>
        <li>XPath(следующий элемент): <code style={codeStyle}>//label[text()='Имя']/following-sibling::input</code></li>
        <li>CSS(следующий элемент того же уровня): <code style={codeStyle}>label+input</code></li>
      </ul>

      <h4>4.Индексация и выбор из списка</h4>
      <p>Используется, когда необходимо извлечь конкретный элемент из набора однотипных узлов.</p>

      <p><strong>Выбор по порядковому номеру:</strong></p>
      <ul>
        <li>XPath(второй элемент): <code style={codeStyle}>(//li[@class='item'])[2]</code></li>
        <li>CSS(второй элемент): <code style={codeStyle}>li.item:nth-child(2)</code></li>
      </ul>

      <p><strong>Последний элемент в списке:</strong></p>
      <ul>
        <li>XPath: <code style={codeStyle}>//ul[@id='menu']/li[last()]</code></li>
        <li>CSS: <code style={codeStyle}>ul#menu{'>'}li:last-child</code></li>
      </ul>

      <h4>5.Динамические и специфические условия</h4>
      <p>Примеры для работы с изменяющимися данными и специальными типами элементов.</p>

      <p><strong>Поиск по началу значения(динамические ID):</strong></p>
      <ul>
        <li>XPath: <code style={codeStyle}>//*[starts-with(@id,'user_')]</code></li>
        <li>CSS: <code style={codeStyle}>[id^='user_']</code></li>
      </ul>

      <p><strong>Исключение элементов(NOT):</strong></p>
      <ul>
        <li>XPath: <code style={codeStyle}>//button[not(@disabled)]</code></li>
        <li>CSS: <code style={codeStyle}>button:not([disabled])</code></li>
      </ul>

      <p><strong>SVG-элементы(XPath):</strong></p>
      <ul>
        <li><code style={codeStyle}>//*[local-name()='svg' and @class='icon-close']</code></li>
      </ul>
    </div>
  );
};

export const XPathErrors: React.FC = () => {
  return (
    <div className="reference-section">
      <h3>Ошибки</h3>

      <div style={warningStyle}>
        <h4>1.Использование абсолютных путей</h4>
        <p><strong>Ошибка:</strong> <code style={codeStyle}>/html/body/div[2]/div[1]/span</code></p>
        <p>Такие локаторы ломаются при любом изменении верстки.</p>
        <p><strong>Правильно:</strong> использовать относительные пути и стабильные атрибуты: <code style={codeStyle}>//div[@class='card']//span</code></p>
      </div>

      <div style={warningStyle}>
        <h4>2.Чрезмерное использование индексов</h4>
        <p><strong>Ошибка:</strong> <code style={codeStyle}>(//button)[3]</code></p>
        <p>После добавления новой кнопки тест начнет работать с другим элементом. Индексы использовать только если нет других признаков для идентификации.</p>
      </div>

      <div style={warningStyle}>
        <h4>3.Путаница между //tag[2] и (//tag)[2]</h4>
        <p><code style={codeStyle}>//tag[2]</code> — второй элемент у каждого родителя.</p>
        <p><code style={codeStyle}>(//tag)[2]</code> — второй элемент во всем документе.</p>
        <p>Это частая причина неверного выбора элемента.</p>
      </div>

      <div style={warningStyle}>
        <h4>4.Игнорирование normalize-space()</h4>
        <p><strong>Ошибка:</strong> <code style={codeStyle}>//button[text()='Сохранить']</code></p>
        <p>Если в верстке есть лишние пробелы, локатор не сработает.</p>
        <p><strong>Правильно:</strong> <code style={codeStyle}>//button[normalize-space()='Сохранить']</code></p>
      </div>

      <div style={warningStyle}>
        <h4>5.Использование динамического id целиком</h4>
        <p><strong>Ошибка:</strong> <code style={codeStyle}>//div[@id='user_12345']</code></p>
        <p>После обновления страницы id изменится.</p>
        <p><strong>Правильно:</strong> <code style={codeStyle}>//div[starts-with(@id,'user_')]</code></p>
      </div>

      <div style={warningStyle}>
        <h4>6.Опора на auto-generated классы</h4>
        <p><strong>Ошибка:</strong> <code style={codeStyle}>//div[@class='css-1a2b3c']</code></p>
        <p>Такие классы генерируются автоматически и меняются при сборке. Лучше использовать data-атрибуты или смысловые признаки.</p>
      </div>

      <div style={warningStyle}>
        <h4>7.Неверное понимание области поиска</h4>
        <p><strong>Ошибка:</strong> <code style={codeStyle}>form//input</code></p>
        <p>Если форм несколько, может найтись не тот элемент.</p>
        <p><strong>Лучше уточнить:</strong> <code style={codeStyle}>//form[@id='loginForm']//input[@name='email']</code></p>
      </div>

      <div style={warningStyle}>
        <h4>8.Неправильная работа с текстом внутри вложенных тегов</h4>
        <p><strong>Ошибка:</strong> <code style={codeStyle}>//button[text()='Войти']</code></p>
        <p>Если текст внутри span, локатор не сработает.</p>
        <p><strong>Правильно:</strong> <code style={codeStyle}>//button[contains(.,'Войти')]</code></p>
      </div>

      <div style={warningStyle}>
        <h4>9.Игнорирование уникальности</h4>
        <p>Перед использованием локатора необходимо убедиться, что он возвращает ровно один элемент. Если локатор находит несколько элементов, тест может работать нестабильно.</p>
      </div>
    </div>
  );
};

export const XPathTips: React.FC = () => {
  return (
    <div className="reference-section">
      <h3>Советы</h3>

      <h4>Советы по работе с XPath и CSS-селекторами в автоматизации</h4>

      <ol>
        <li>
          <strong>Пишите устойчивые локаторы</strong>
          <br />Избегайте абсолютных путей вида /html/body/div[2]/div[1]/span. Такие локаторы ломаются при малейшем изменении верстки. Предпочитайте относительные пути с // и опору на стабильные атрибуты(id,name,data-*).
        </li>

        <li>
          <strong>Используйте уникальные атрибуты</strong>
          <br />Лучший вариант — id или специальные data-testid,data-qa,data-test. Если их нет, комбинируйте несколько атрибутов.
          <br />Пример: <code style={codeStyle}>//button[@type='submit' and @name='login']</code>
        </li>

        <li>
          <strong>Не завязывайтесь на визуальную структуру</strong>
          <br />Не стоит строить локатор через длинную цепочку вложенности.
          <br /><em>Плохо:</em> <code style={codeStyle}>//div[@class='wrapper']//div[2]//div[3]//span</code>
          <br />Лучше опереться на смысловой атрибут или текст.
        </li>

        <li>
          <strong>Осторожно с индексами</strong>
          <br />Индексы часто делают тесты нестабильными. (//button)[3] может начать указывать на другой элемент после изменения страницы. Используйте индекс только если нет другого способа.
        </li>

        <li>
          <strong>Работа с динамическими значениями</strong>
          <br />Если id или class генерируются динамически, используйте: contains(), starts-with()
          <br />Пример: <code style={codeStyle}>//div[starts-with(@id,'user_')]</code>
        </li>

        <li>
          <strong>Используйте normalize-space() при работе с текстом</strong>
          <br />Верстка часто содержит лишние пробелы или переносы строк. normalize-space() помогает избежать ложных падений тестов.
        </li>

        <li>
          <strong>Проверяйте область поиска</strong>
          <br /><code style={codeStyle}>//button</code> — ищет по всему документу.
          <br /><code style={codeStyle}>.form//button</code> — ищет только внутри формы.
          <br />Чем уже область поиска, тем быстрее и надежнее локатор.
        </li>

        <li>
          <strong>CSS или XPath?</strong>
          <br />Если можно решить задачу через CSS — используйте CSS. Он быстрее и проще.
          <br />XPath применяйте:
          <br />- для поиска по тексту
          <br />- для навигации к родителю
          <br />- при сложной логике фильтрации
        </li>

        <li>
          <strong>Проверяйте локаторы в DevTools</strong>
          <br />Всегда тестируйте XPath и CSS в браузере перед добавлением в тест. Это экономит время и снижает количество ошибок.
        </li>

        <li>
          <strong>Избегайте хрупких локаторов</strong>
          <br />Не используйте:
          <br />- auto-generated классы
          <br />- длинные динамические id
          <br />- позиционирование через div:nth-child(7), если есть смысловые признаки
          <br /><em>Хороший локатор переживает:</em>
          <br />- добавление новых блоков
          <br />- изменение вложенности
          <br />- изменение порядка элементов
          <br /><em>Плохой</em> — ломается при любом изменении структуры.
        </li>
      </ol>
    </div>
  );
};
