import React from 'react';

const preStyle: React.CSSProperties = {
  background: '#f5f5f5',
  color: '#333',
  padding: '15px',
  borderRadius: '5px',
  overflowX: 'auto' as const,
  margin: '10px 0'
};

const codeStyle: React.CSSProperties = {
  background: '#e8f0f8',
  padding: '2px 6px',
  borderRadius: '3px',
  fontFamily: "'Courier New', monospace",
  fontSize: '0.9em'
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

export const POMBasics: React.FC = () => {
  return (
    <div className="reference-section">
      <h3>Основы</h3>

      <p>
        <strong>Page Object Model (POM)</strong> — это шаблон проектирования для автоматизации тестирования,
        который предполагает разделение структуры страницы и логики тестов. Основная идея заключается в том,
        чтобы каждая страница или компонент страницы представлялись отдельным классом, инкапсулирующим элементы и действия с ними.
        Это упрощает поддержку тестов, делает их более читаемыми и снижает дублирование кода.
      </p>

      <h4>Трехуровневая архитектура (Test — Page — Element)</h4>
      <p>В развитой архитектуре POM взаимодействие строится по цепочке: Класс Теста → Класс Страницы → Класс Элемента.</p>
      
      <ol>
        <li>
          <strong>Тест (Test class)</strong> — оперирует исключительно методами страниц. Тест «не знает», 
          какие локаторы используются и как устроены элементы. Его задача — вызвать действие и проверить результат, 
          не вмешиваясь в детали реализации элементов. Такая практика позволяет при изменении верстки или локаторов 
          не переписывать тесты, а только обновлять соответствующие Page Objects.
        </li>
        <li>
          <strong>Страница (Page class)</strong> — представляет HTML-документ с уникальным URL или модальное окно. 
          Страница объединяет элементы в логические группы и предоставляет методы для работы с ними (например, метод search()). 
          На уровне страницы запрещено проводить проверки через assert: методы должны возвращать данные, элементы или объекты других страниц. 
          Это облегчает построение цепочек действий (Fluent API) и делает код теста читаемым.
        </li>
        <li>
          <strong>Элемент (Element class)</strong> — это интерактивные компоненты, с которыми взаимодействует пользователь: 
          кнопки, поля ввода, таблицы, виджеты. Элементы инкапсулируют поиск в DOM и базовые действия 
          (клик, ввод текста, проверка видимости). Разделение на отдельные классы позволяет избежать дублирования кода 
          и упрощает поддержку при изменении верстки.
        </li>
      </ol>

      <h4>Классификация элементов</h4>
      <p>Элементы страницы разделяются на две категории по сложности их структуры:</p>
      
      <ol>
        <li>
          <strong>Базисные элементы</strong> — атомарные компоненты, функционал которых идентичен в большинстве систем 
          (кнопки, чекбоксы, поля ввода). Их поведение стандартное, и они чаще всего наследуются от BaseElement 
          с добавлением минимальной логики.
        </li>
        <li>
          <strong>Составные элементы</strong> — более сложные компоненты, логика которых зависит от внутренней структуры 
          (например, таблицы, виджеты, карточки товаров). Составные элементы могут содержать базисные элементы внутри себя. 
          Например, Table может включать строки с кнопками и ссылками, а DatePicker — отдельные элементы для выбора дня, месяца и года. 
          Работа с составными элементами через инкапсулированные методы позволяет тестам не «заглядывать внутрь» структуры 
          и поддерживать высокий уровень абстракции.
        </li>
      </ol>

      <h4>Иерархия и наследование</h4>
      <p>Для управления кодом и обеспечения повторного использования применяются базовые классы:</p>
      
      <ol>
        <li>
          <strong>BaseElement</strong> — общий предок для всех элементов. Реализует базовый функционал, 
          например проверку видимости через метод isDisplayed(). Это позволяет стандартно обрабатывать ожидания и исключения для всех элементов.
        </li>
        <li>
          <strong>BasePage</strong> — родительский класс для всех страниц. Определяет общую логику страниц, 
          например метод обновления refresh(), методы для работы с модальными окнами и безопасного клика по элементам. 
          Позволяет дочерним страницам использовать общие методы без дублирования кода.
        </li>
        <li>
          <strong>BaseTest</strong> — класс, содержащий конфигурацию браузера, глобальные настройки Selenide, 
          методы авторизации, а также операции, выполняемые перед (@BeforeEach) и после (@AfterEach) каждого теста. 
          Обеспечивает стабильное и предсказуемое выполнение тестов и изоляцию тестовой среды.
        </li>
      </ol>

      <h4>Пример создания базового элемента BaseElement:</h4>
      <pre style={preStyle}>
        <code>{`public class BaseElement {
    protected SelenideElement baseElement; // основной элемент страницы, с которым будет происходить взаимодействие
    protected static final int WAIT_SECONDS = 5; // стандартное время ожидания видимости элемента

    // Конструктор принимает xpath и значение атрибута для динамического поиска
    protected BaseElement(String xpath, String attributeValue) {
        // $x возвращает SelenideElement по XPath
        this.baseElement = $x(String.format(xpath, attributeValue));
    }

    // Метод проверки видимости элемента с обработкой исключений
    public boolean isDisplayed() {
        try {
            baseElement.shouldBe(visible, Duration.ofSeconds(WAIT_SECONDS));
            return true; // элемент видим
        } catch (ElementNotFound e) {
            return false; // элемент не найден, безопасно вернуть false
        }
    }

    // Защищённый метод поиска вложенных элементов строго внутри baseElement
    protected SelenideElement findInside(String relativeXpath) {
        return baseElement.$x(relativeXpath); // поиск элемента относительно baseElement
    }
}`}</code>
      </pre>

      <h4>Пример создания базисного элемента Input:</h4>
      <pre style={preStyle}>
        <code>{`public class Input extends BaseElement {
    private static final String ID_XPATH = "//input[@id='%s']"; // шаблон для поиска по id
    private static final String NAME_XPATH = "//input[@name='%s']"; // шаблон для поиска по имени
    private static final String PLACEHOLDER_XPATH = "//input[@placeholder='%s']"; // шаблон для поиска по placeholder

    // Приватный конструктор, используется только через статические методы
    private Input(String xpath, String value) {
        super(xpath, value);
    }

    // Метод для заполнения поля текстом
    public Input fill(String value) {
        baseElement.clear(); // очищаем поле перед вводом
        baseElement.setValue(value); // вводим значение
        return this; // возвращаем объект для цепочки вызовов (Fluent API)
    }

    // Статические методы для создания элемента через разные атрибуты
    public static Input byId(String id) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id cannot be empty");
        return new Input(ID_XPATH, id);
    }

    public static Input byName(String name) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name cannot be empty");
        return new Input(NAME_XPATH, name);
    }

    public static Input byPlaceholder(String placeholder) {
        if (placeholder == null || placeholder.isEmpty()) throw new IllegalArgumentException("placeholder cannot be empty");
        return new Input(PLACEHOLDER_XPATH, placeholder);
    }
}`}</code>
      </pre>

      <h4>Пример использования Input на странице LoginPage:</h4>
      <pre style={preStyle}>
        <code>{`public class LoginPage extends BasePage {
    private final Input loginInput = Input.byId("login"); // поле логина
    private final Input passwordInput = Input.byName("password"); // поле пароля
    private final Button submitButton = new Button("//button[@type='submit']", "Submit"); // кнопка отправки формы

    public LoginPage fillLogin(String login) {
        loginInput.fill(login); // заполняем поле логина
        return this; // возвращаем объект страницы для цепочки вызовов
    }

    public LoginPage fillPassword(String password) {
        passwordInput.fill(password); // заполняем поле пароля
        return this;
    }

    public HomePage clickLoginButton() {
        submitButton.click(); // выполняем клик по кнопке
        return page(HomePage.class); // возвращаем объект HomePage после успешного входа
    }
}`}</code>
      </pre>
    </div>
  );
};

export const POMExamples: React.FC = () => {
  return (
    <div className="reference-section">
      <h3>Примеры</h3>

      <h4>1. Создание составного элемента (ItemComponent):</h4>
      <pre style={preStyle}>
        <code>{`public class ItemComponent extends BaseElement {
    public ItemComponent(SelenideElement element) {
        super(element.getSelector(), ""); // используем существующий элемент
        this.baseElement = element; // инкапсулируем SelenideElement
    }

    public String getTitle() {
        return baseElement.$x(".//h2").getText(); // получаем текст заголовка
    }

    public void clickActionButton(String buttonText) {
        baseElement.$x(".//button[text()='" + buttonText + "']").click(); // кликаем по кнопке внутри карточки
    }
}`}</code>
      </pre>

      <h4>2. Контейнер элементов (ItemsContainer):</h4>
      <pre style={preStyle}>
        <code>{`public class ItemsContainer extends BaseElement {
    public ItemsContainer(SelenideElement element) {
        super("", "");
        this.baseElement = element; // контейнер для группы карточек
    }

    public List<ItemComponent> getItems() {
        // находим все карточки внутри контейнера и оборачиваем их в ItemComponent
        return baseElement.$$x(".//div[contains(@class,'item-card')]")
                .stream()
                .map(ItemComponent::new)
                .collect(Collectors.toList());
    }

    public Optional<ItemComponent> getItemByTitle(String title) {
        return getItems().stream()
                .filter(item -> item.getTitle().equals(title))
                .findFirst(); // возвращаем первую карточку с совпадающим заголовком
    }
}`}</code>
      </pre>

      <h4>3. Работа с модальными окнами:</h4>
      <pre style={preStyle}>
        <code>{`public class ModalWindow extends BasePage {
    private final Button closeButton = new Button(".//button[@class='close']", "Close");

    public ModalWindow close() {
        closeButton.click(); // закрываем модальное окно
        return this; // возвращаем текущий объект для цепочки вызовов
    }
}`}</code>
      </pre>

      <h4>4. Тест с использованием POM:</h4>
      <pre style={preStyle}>
        <code>{`@Test
public void testLogin() {
    LoginPage loginPage = page(LoginPage.class);
    HomePage homePage = loginPage.fillLogin("user")
                                .fillPassword("password")
                                .clickLoginButton();
    homePage.shouldBeLoaded();
}`}</code>
      </pre>

      <div style={noteStyle}>
        <strong>Важно:</strong> Все методы страниц должны возвращать объекты (this или другую страницу) для построения цепочек вызовов (Fluent API).
      </div>
    </div>
  );
};

export const POMErrors: React.FC = () => {
  return (
    <div className="reference-section">
      <h3>Ошибки</h3>

      <div style={warningStyle}>
        <h4>1. Прямой доступ к локаторам в тестах</h4>
        <pre style={preStyle}><code>{`@Test
public void testLoginDirectly() {
    $("#login").setValue("user"); // плохая практика: локатор прямо в тесте
    $("#password").setValue("1234");
    $("#submit").click();
}`}</code></pre>
        <p><strong>Почему это плохо:</strong> При изменении верстки придется менять каждый тест вручную.</p>
        <p><strong>Правильный подход:</strong> использовать методы страницы, которые инкапсулируют локаторы.</p>
        <pre style={preStyle}><code>{`LoginPage loginPage = page(LoginPage.class);
loginPage.fillLogin("user")
         .fillPassword("1234")
         .clickLoginButton();`}</code></pre>
      </div>

      <div style={warningStyle}>
        <h4>2. Использование assert в Page или Component</h4>
        <pre style={preStyle}><code>{`public void checkLoginVisible() {
    assertTrue(baseElement.isDisplayed()); // запрещено
}`}</code></pre>
        <p><strong>Почему это плохо:</strong> Нарушает принцип разделения ответственности. Страница должна возвращать данные или объекты, проверку делает тест.</p>
        <p><strong>Правильный подход:</strong></p>
        <pre style={preStyle}><code>{`public boolean isLoginVisible() {
    return baseElement.isDisplayed(); // возвращаем boolean
}
@Test
public void testLoginVisible() {
    LoginPage loginPage = page(LoginPage.class);
    assertTrue(loginPage.isLoginVisible());
}`}</code></pre>
      </div>

      <div style={warningStyle}>
        <h4>3. Неправильное наследование компонентов</h4>
        <pre style={preStyle}><code>{`public class LoginInput {
    private SelenideElement element = $("#login");
}`}</code></pre>
        <p><strong>Почему это плохо:</strong> Дублирование кода, отсутствие общих методов.</p>
        <p><strong>Правильный подход:</strong> наследовать BaseElement и использовать общую логику.</p>
        <pre style={preStyle}><code>{`public class Input extends BaseElement {
    public Input(String xpath, String value) {
        super(xpath, value);
    }
}`}</code></pre>
      </div>

      <div style={warningStyle}>
        <h4>4. Игнорирование инкапсуляции страниц</h4>
        <pre style={preStyle}><code>{`public Input loginInput; // плохая практика`}</code></pre>
        <p><strong>Почему это плохо:</strong> Поля элементов public позволяют тестам работать с ними напрямую.</p>
        <p><strong>Правильный подход:</strong> оставлять элементы private и предоставлять публичные методы для действий.</p>
        <pre style={preStyle}><code>{`private final Input loginInput = Input.byId("login");

public LoginPage fillLogin(String login) {
    loginInput.fill(login);
    return this;
}`}</code></pre>
      </div>

      <div style={warningStyle}>
        <h4>5. Отсутствие инкапсуляции действий внутри компонентов</h4>
        <pre style={preStyle}><code>{`@Test
public void testAddProduct() {
    $("#product-name").setValue("Laptop"); // плохая практика
    $("#add-button").click();
}`}</code></pre>
        <p><strong>Почему это плохо:</strong> При изменении верстки придется менять каждый тест, дублируется код.</p>
        <p><strong>Правильный подход:</strong> все действия должны быть инкапсулированы в Page или Component</p>
        <pre style={preStyle}><code>{`public class ProductPage extends BasePage {
    private final Input productName = Input.byId("product-name");
    private final Button addButton = new Button("Add");

    public ProductPage addProduct(String name) {
        productName.fill(name);
        addButton.click();
        return this;
    }
}`}</code></pre>
      </div>

      <div style={warningStyle}>
        <h4>6. Смешивание логики страницы и тестов</h4>
        <pre style={preStyle}><code>{`public void checkProductAdded() {
    assertTrue($(".product").exists()); // запрещено
}`}</code></pre>
        <p><strong>Почему это плохо:</strong> Страница выполняет проверки (asserts) и одновременно возвращает элементы для теста.</p>
        <p><strong>Правильный подход:</strong> метод страницы должен возвращать состояние или объект:</p>
        <pre style={preStyle}><code>{`public boolean isProductAdded() {
    return $(".product").exists();
}`}</code></pre>
      </div>

      <div style={warningStyle}>
        <h4>7. Игнорирование базового контейнера страницы</h4>
        <pre style={preStyle}><code>{`$(".item").click(); // может попасть на элемент другой страницы или блока`}</code></pre>
        <p><strong>Почему это плохо:</strong> Элементы ищутся глобально по всему DOM, вместо того чтобы ограничивать поиск своим контейнером.</p>
        <p><strong>Правильный подход:</strong> использовать baseElement или контейнер страницы/компонента:</p>
        <pre style={preStyle}><code>{`baseElement.$(".item").click(); // поиск строго внутри текущей страницы/компонента`}</code></pre>
      </div>

      <div style={warningStyle}>
        <h4>8. Игнорирование наследования при создании новых страниц</h4>
        <pre style={preStyle}><code>{`public class HomePage {
    private final SelenideElement header = $("#header");
}`}</code></pre>
        <p><strong>Почему это плохо:</strong> Создание страниц без наследования BasePage, дублирование общих методов.</p>
        <p><strong>Правильный подход:</strong> использовать BasePage для общих компонентов и логики:</p>
        <pre style={preStyle}><code>{`public class HomePage extends BasePage {
    private final HeaderComponent header = new HeaderComponent(baseElement);
}`}</code></pre>
      </div>
    </div>
  );
};

export const POMTips: React.FC = () => {
  return (
    <div className="reference-section">
      <h3>Советы</h3>

      <ol>
        <li>
          <strong>При работе с POM важно всегда инкапсулировать взаимодействие с элементами внутри компонентов и страниц.</strong> 
          Не стоит писать прямые обращения к локаторам в тестах — тесты должны оперировать только методами страниц и компонентов. 
          Это делает тесты более устойчивыми к изменениям верстки и упрощает поддержку. 
          Например, метод addProduct(String name) в ProductPage должен сам выполнять ввод текста и клик по кнопке, а тест лишь вызывает этот метод.
        </li>

        <li>
          <strong>Используйте базовые классы для повторяющейся логики.</strong> 
          BaseElement должен включать общие методы, такие как isDisplayed() или клик, чтобы все элементы наследников автоматически имели эти функции. 
          Это позволяет не дублировать код и сохранять единый подход ко взаимодействию с элементами. 
          Аналогично BasePage позволяет хранить общий функционал страницы, такой как обновление через refresh(), доступ к header, footer и другие общие компоненты.
        </li>

        <li>
          <strong>Старайтесь ограничивать область поиска элементов.</strong> 
          Все элементы должны быть найдены внутри своего контейнера (baseElement) или страницы, чтобы избежать случайного попадания на элементы других страниц или блоков. 
          Это особенно важно для повторяющихся элементов и динамических списков, где могут появляться похожие компоненты на одной странице.
        </li>

        <li>
          <strong>Инкапсулируйте проверки состояния в методах страницы или компонентов через возвращаемые boolean значения, а не через assert.</strong> 
          Тесты должны решать, как проверять результат, а страницы лишь предоставлять информацию о состоянии элементов. 
          Например, метод isLoginButtonEnabled() возвращает true/false, а тест уже выполняет assert на основе этого значения.
        </li>

        <li>
          <strong>Используйте наследование компонентов и страниц, чтобы избежать дублирования кода.</strong> 
          Например, если несколько страниц содержат одинаковый header или footer, создайте отдельные компоненты и подключайте их через BasePage. 
          Это позволяет централизованно изменять логику работы с общими элементами, не трогая дочерние страницы.
        </li>

        <li>
          <strong>При работе с составными элементами, такими как таблицы, карточки продуктов или виджеты, создавайте отдельные компоненты для этих блоков.</strong> 
          Внутри компонента инкапсулируйте все методы взаимодействия с его внутренними элементами. 
          Тест должен вызывать методы компонента, не заботясь о том, какие локаторы или кнопки находятся внутри.
        </li>

        <li>
          <strong>Для методов, которые возвращают другие страницы или компоненты, используйте fluent-интерфейсы</strong> 
          (возвращение this или объекта новой страницы), чтобы можно было строить цепочки вызовов. 
          Это делает тесты читаемыми и позволяет логично следовать сценарию пользователя.
        </li>
      </ol>
    </div>
  );
};
