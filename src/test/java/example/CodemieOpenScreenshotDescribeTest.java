package example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CodemieOpenScreenshotDescribeTest {
  private static Playwright playwright;
  private static Browser browser;

  private BrowserContext context;
  private Page page;

  @BeforeAll
  static void beforeAll() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
      .setHeadless(false)); // headed Chromium
  }

  @AfterAll
  static void afterAll() {
    if (browser != null) browser.close();
    if (playwright != null) playwright.close();
  }

  @BeforeEach
  void beforeEach() {
    context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(1280, 800));
    page = context.newPage();
  }

  @AfterEach
  void afterEach() {
    if (context != null) context.close();
  }

  @Test
  void open_verifyNavigation_screenshot_describe() throws Exception {
    String url = "https://codemie.lab.epam.com/";

    // Ask: "Open https://codemie.lab.epam.com/ in the browser"
    Response resp = page.navigate(url, new Page.NavigateOptions()
      .setWaitUntil(LoadState.DOMCONTENTLOADED));
    page.waitForLoadState(LoadState.NETWORKIDLE);

    // Verify: Assistant confirms successful navigation
    // (automation equivalent: navigation succeeded + expected page is actually loaded)
    assertNotNull(resp, "No response received from navigation (may be blocked / failed).");
    assertTrue(resp.ok(), "Expected 2xx/3xx response, got " + resp.status() + " " + resp.statusText());
    assertTrue(page.url().startsWith(url), "Expected URL starting with " + url + " but was " + page.url());
    assertTrue(page.locator("body").isVisible(), "Page body should be visible after navigation.");

    // Ask: create a screenshot
    Path outDir = Paths.get("target", "artifacts");
    Files.createDirectories(outDir);

    String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    Path screenshotPath = outDir.resolve("codemie-" + ts + ".png");

    page.screenshot(new Page.ScreenshotOptions()
      .setPath(screenshotPath)
      .setFullPage(true));

    assertTrue(Files.exists(screenshotPath), "Screenshot was not created: " + screenshotPath);

    // Ask: describe what is on the page (basic runtime-derived description)
    String title = page.title();

    List<String> headings = new ArrayList<>();
    headings.addAll(readTopTexts(page.locator("h1"), 5));
    headings.addAll(readTopTexts(page.locator("h2"), 5));

    boolean hasHeader = isVisibleOrFalse(page.locator("header").first());
    boolean hasNav = isVisibleOrFalse(page.locator("nav").first());
    boolean hasMain = isVisibleOrFalse(page.locator("main").first());
    boolean hasFooter = isVisibleOrFalse(page.locator("footer").first());

    String description = buildDescription(title, headings, hasHeader, hasNav, hasMain, hasFooter, screenshotPath);

    // Print to logs so it's visible in CI output
    System.out.println(description);

    assertFalse(description.isBlank(), "Description should not be blank.");
  }

  private static boolean isVisibleOrFalse(Locator locator) {
    try {
      return locator.isVisible();
    } catch (PlaywrightException e) {
      return false;
    }
  }

  private static List<String> readTopTexts(Locator locator, int max) {
    List<String> texts = new ArrayList<>();
    int count = Math.min(locator.count(), max);
    for (int i = 0; i < count; i++) {
      String t = locator.nth(i).innerText().trim();
      if (!t.isEmpty()) texts.add(t);
    }
    return texts;
  }

  private static String buildDescription(String title,
                                         List<String> headings,
                                         boolean hasHeader,
                                         boolean hasNav,
                                         boolean hasMain,
                                         boolean hasFooter,
                                         Path screenshotPath) {
    StringBuilder sb = new StringBuilder();
    sb.append("Navigation successful: opened https://codemie.lab.epam.com/\n");
    sb.append("Page title: ")
      .append(title == null || title.isBlank() ? "(no title)" : title)
      .append("\n");
    sb.append("Screenshot saved: ").append(screenshotPath.toAbsolutePath()).append("\n");

    sb.append("Landmarks visible: ");
    List<String> landmarks = new ArrayList<>();
    if (hasHeader) landmarks.add("header");
    if (hasNav) landmarks.add("nav");
    if (hasMain) landmarks.add("main");
    if (hasFooter) landmarks.add("footer");
    sb.append(landmarks.isEmpty() ? "(none detected)" : String.join(", ", landmarks));
    sb.append("\n");

    sb.append("Headings (H1/H2):\n");
    if (headings.isEmpty()) {
      sb.append(" - (none detected)\n");
    } else {
      for (String h : headings) sb.append(" - ").append(h).append("\n");
    }
    return sb.toString();
  }
}
