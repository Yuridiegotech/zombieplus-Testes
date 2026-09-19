package support.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.FilterOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import java.nio.file.Paths;
import java.util.List;

public class Movies {

  private final Page page;

  public Movies(Page page) {
    this.page = page;
  }

  @Step("Navegando para o formulário de cadastro de filme")
  public void goForm() {
    System.out.println("➕ [UI] Clicando em cadastrar novo filme");
    page.locator("a[href$=\"register\"]").click();
  }

  @Step("Submetendo o formulário de cadastro de filme")
  public void submitForm() {
    System.out.println("💾 [UI] Clicando no botão 'Cadastrar'");
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cadastrar")).click();
  }

  @Step("Validando mensagens de alerta nos campos de filme: {expectedTexts}")
  public void assertAlertsTexts(String... expectedTexts) {
    Locator alerts = page.locator(".alert");
    int count = alerts.count();
    System.out.println("🔍 [UI] Validando alertas. Esperados: " + expectedTexts.length + ", Encontrados: " + count);
    assert count == expectedTexts.length : "Quantidade de alertas diferente do esperado. Esperado: " + expectedTexts.length + ", Obtido: " + count;
    for (int i = 0; i < count; i++) {
      alerts.nth(i).waitFor();
      String actual = alerts.nth(i).textContent().trim();
      System.out.println("   - Alerta [" + i + "]: " + actual + " (Esperado: '" + expectedTexts[i] + "')");
      assert actual.contains(expectedTexts[i]) :
          "Esperado conter: '" + expectedTexts[i] + "', mas foi obtido: '" + actual + "'";
    }
  }

  @Step("Cadastrando filme '{title}' (Ano: {release_year}, Produtora: {companyName}, Destaque: {featured})")
  public void createNewMovie(String title, String overview, String companyName,
      String release_year, String cover, boolean featured) {

    System.out.println("🎬 [UI] Preenchendo dados do filme: " + title);
    goForm();
    page.getByLabel("Titulo do filme").fill(title);
    page.getByLabel("Sinopse").fill(overview);

    // Seleciona Companhia
    page.locator("#select_company_id .react-select__indicator").click();
    page.locator(".react-select__option").filter(new FilterOptions().setHasText(companyName)).click();

    // Seleciona Ano de lançamento
    page.locator("#select_year .react-select__indicator").click();
    page.locator(".react-select__option").filter(new FilterOptions().setHasText(release_year)).click();

    if (cover != null && !cover.isEmpty()) {
      String cleanCover = cover.startsWith("/") ? cover.substring(1) : cover;
      page.locator("input[name=cover]")
          .setInputFiles(Paths.get("src/test/java/support/fixtures/" + cleanCover));
    }

    if (featured) {
      page.locator(".featured .react-switch").click();
    }

    submitForm();
  }

  @Step("Excluindo o filme: '{movieTitle}'")
  public void deleteMovie(String movieTitle) {
    System.out.println("🗑️ [UI] Excluindo o filme: " + movieTitle);
    page.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName(movieTitle))
        .getByRole(AriaRole.BUTTON)
        .click();

    page.locator(".confirm-removal").click();
  }

  @Step("Buscando filme pelo termo: '{target}'")
  public void searchMovie(String target) {
    System.out.println("🔎 [UI] Buscando filme com o termo: " + target);
    page.getByPlaceholder("Busque pelo nome").fill(target);
    page.click(".actions button");
  }

  @Step("Validando resultados da busca de filmes. Esperados: {expectedTitles}")
  public void assertSearchResults(List<String> expectedTitles) {
    page.locator("td.title").first().waitFor();

    Locator titles = page.locator("td.title");
    List<String> foundTitles = titles.allTextContents();

    System.out.println("===================================================");
    System.out.println("VALIDANDO RESULTADOS DA BUSCA DE FILMES");
    System.out.println("Títulos localizados (td.title): " + foundTitles);
    System.out.println("Títulos esperados (movie.title): " + expectedTitles);
    System.out.println("===================================================");

    for (String expectedTitle : expectedTitles) {
      System.out.println("  ✓ Validando presença do filme: " + expectedTitle);
      assert foundTitles.contains(expectedTitle) :
          "Filme não encontrado nos resultados da busca: " + expectedTitle +
              "\nTítulos encontrados: " + foundTitles;
    }

    System.out.println("✅ Todos os " + expectedTitles.size() + " filmes foram encontrados na busca.\n");
  }
}