package support;

import com.github.javafaker.Faker;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.zombieplus.factory.BrowserFactory;
import support.actions.Components;
import support.actions.Leads;
import support.actions.Login;
import support.actions.Movies;
import support.actions.TvShows;
import support.api.LeadsApi;
import support.api.MoviesApi;
import support.api.TvShowsApi;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(TestListener.class)
public class BaseTest {

  protected BrowserContext context;
  protected Page page;
  protected Playwright playwright;

  // Page Objects - disponíveis para todos os testes
  protected Movies movies;
  protected TvShows tvShows;
  protected Login login;
  protected Leads leads;
  protected Components components;

  // API - disponível para todos os testes
  protected MoviesApi moviesApi;
  protected TvShowsApi tvShowsApi;
  protected LeadsApi leadsApi;

  // Faker - disponível para todos os testes
  protected Faker faker;

  public Page getPage() {
    return page;
  }

  public void takeScreenshot(String name) {
    try {
      if (page != null && !page.isClosed()) {
        byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), ".png");
      }
    } catch (Exception e) {
      System.err.println("Erro ao capturar screenshot: " + e.getMessage());
    }
  }

  @BeforeEach
  void setUp(TestInfo testInfo) {
    System.out.println("\n=======================================================");
    System.out.println("🚀 INICIANDO TESTE: " + testInfo.getDisplayName());
    System.out.println("=======================================================");

    BrowserFactory.headless = false;
    playwright = BrowserFactory.getPlaywright();
    context = BrowserFactory.createContext();
    page = context.newPage();

    // Inicializa todos os Page Objects automaticamente
    movies = new Movies(page);
    tvShows = new TvShows(page);
    login = new Login(page);
    leads = new Leads(page);
    components = new Components(page);

    // Inicializa a API
    moviesApi = new MoviesApi(playwright);
    tvShowsApi = new TvShowsApi(playwright);
    leadsApi = new LeadsApi(playwright);

    // Inicializa o Faker
    faker = new Faker();

    // Inicia o tracing
    context.tracing().start(new Tracing.StartOptions()
        .setScreenshots(true)
        .setSnapshots(true)
        .setSources(true));
  }

  @AfterEach
  void tearDown(TestInfo testInfo) {
    // 📸 Captura Screenshot de Evidência Final para cada teste
    takeScreenshot("Evidência Final - " + testInfo.getDisplayName());

    // 📦 Salva o trace do Playwright e anexa ao Allure
    String cleanTestName = testInfo.getDisplayName().replaceAll("[^a-zA-Z0-9.-]", "_");
    Path tracePath = Paths.get("build", "allure-results", "trace-" + this.getClass().getSimpleName() + "-" + cleanTestName + ".zip");

    try {
      if (tracePath.getParent() != null) {
        Files.createDirectories(tracePath.getParent());
      }
      context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
      if (Files.exists(tracePath)) {
        try (var is = new FileInputStream(tracePath.toFile())) {
          Allure.addAttachment("Playwright Trace (" + testInfo.getDisplayName() + ")", "application/zip", is, ".zip");
        }
      }
    } catch (Exception e) {
      System.err.println("Erro ao salvar Playwright Trace: " + e.getMessage());
    }

    // Fecha recursos da API
    if (moviesApi != null) {
      moviesApi.dispose();
    }
    if (tvShowsApi != null) {
      tvShowsApi.dispose();
    }
    if (leadsApi != null) {
      leadsApi.dispose();
    }

    if (context != null) {
      context.close();
    }

    System.out.println("🏁 FINALIZANDO TESTE: " + testInfo.getDisplayName());
    System.out.println("=======================================================\n");
  }
}