package support.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.FilterOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import io.qameta.allure.Step;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TvShows {

  private final Page page;

  public TvShows(Page page) {
    this.page = page;
  }

  @Step("Navegando para a página de Séries de TV")
  public void goToPageTvShow() {
    System.out.println("📺 [UI] Acessando listagem de séries (/tvshows)");
    page.locator("a[href$='/tvshows']").click();
    page.waitForURL("**/admin/tvshows");
    page.waitForLoadState(LoadState.NETWORKIDLE);
  }

  @Step("Navegando para o formulário de cadastro de série")
  public void goForm() {
    System.out.println("➕ [UI] Clicando em cadastrar nova série");
    page.locator("a[href=\"/admin/tvshows/register\"]").click();
  }

  @Step("Submetendo o formulário de cadastro de série")
  public void submitForm() {
    System.out.println("💾 [UI] Clicando no botão 'Cadastrar'");
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cadastrar")).click();
  }

  @Step("Validando mensagens de alerta nos campos de série: {expectedTexts}")
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

  @Step("Cadastrando série '{title}' (Temporadas: {season}, Ano: {releaseYear}, Produtora: {companyName}, Destaque: {featured})")
  public void createNewTvShow(String title, String overview, String companyName,
      String releaseYear, String season, String cover, boolean featured) {

    System.out.println("📺 [UI] Preenchendo formulário da série: " + title);
    goForm();
    page.getByLabel("Titulo da série").fill(title);
    page.getByLabel("Sinopse").fill(overview);

    // Seleciona Companhia
    page.locator("#select_company_id .react-select__indicator").click();
    page.locator(".react-select__option").filter(new FilterOptions().setHasText(companyName)).click();

    // Seleciona Ano de lançamento
    page.locator("#select_year .react-select__indicator").click();
    page.locator(".react-select__option").filter(new FilterOptions().setHasText(releaseYear)).click();

    // Preenche temporadas
    page.getByLabel("Temporadas").fill(season);

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

  @Step("Excluindo a série: '{tvShowTitle}'")
  public void deleteTvShow(String tvShowTitle) {
    System.out.println("🗑️ [UI] Excluindo série: " + tvShowTitle);
    page.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName(tvShowTitle))
        .getByRole(AriaRole.BUTTON)
        .click();

    page.locator(".confirm-removal").click();
  }

  @Step("Buscando série pelo termo: '{target}'")
  public void searchTvShow(String target) {
    System.out.println("🔎 [UI] Buscando série com o termo: " + target);
    page.getByPlaceholder("Busque pelo nome").fill(target);
    page.click(".actions button");
  }

  @Step("Validando resultados da busca por séries. Esperados: {expectedTitles}")
  public void assertSearchResults(List<String> expectedTitles) {
    page.locator("td.title").first().waitFor();

    Locator titleCells = page.locator("td.title");
    List<String> foundTitles = new ArrayList<>();

    int count = titleCells.count();
    for (int i = 0; i < count; i++) {
      String title = (String) titleCells.nth(i).evaluate(
          "element => Array.from(element.childNodes)" +
              ".filter(node => node.nodeType === Node.TEXT_NODE)" +
              ".map(node => node.textContent.trim())" +
              ".join(' ')"
      );
      foundTitles.add(title);
    }

    System.out.println("===================================================");
    System.out.println("VALIDANDO RESULTADOS DA BUSCA DE SÉRIES");
    System.out.println("Títulos localizados (td.title): " + foundTitles);
    System.out.println("Títulos esperados (tvshow.title): " + expectedTitles);
    System.out.println("===================================================");

    for (String expectedTitle : expectedTitles) {
      System.out.println("  ✓ Validando presença da série: " + expectedTitle);
      assert foundTitles.contains(expectedTitle) :
          "Série não encontrada nos resultados da busca: " + expectedTitle +
              "\nTítulos encontrados: " + foundTitles;
    }

    System.out.println("✅ Todos os " + expectedTitles.size() + " TV Shows foram encontrados na busca.\n");
  }
}