package support.actions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class Login {

  private final Page page;
  private final String baseUrl;

  public Login(Page page) {
    this.page = page;
    String envUrl = System.getenv("BASE_URL");
    this.baseUrl = (envUrl != null && !envUrl.isEmpty()) ? envUrl : "http://localhost:3000";
  }

  @Step("Navegando para a página de Login")
  public void navigate() {
    String url = baseUrl + "/admin/login";
    System.out.println("🌐 [UI] Acessando URL de Login: " + url);
    page.navigate(url);
    page.locator(".login-form").waitFor();
  }

  @Step("Preenchendo formulário de login com email: '{email}'")
  public void submit(String email, String password) {
    System.out.println("🔑 [UI] Submetendo login para email: " + email);
    page.getByPlaceholder("E-mail").fill(email);
    page.getByPlaceholder("Senha").fill(password);
    page.locator("button[type=\"submit\"]").click();
  }

  @Step("Validando mensagens de alerta exibidas: {expectedTexts}")
  public void assertAlertsTexts(String... expectedTexts) {
    Locator alerts = page.locator("span[class$=alert]");
    int count = alerts.count();
    System.out.println("🔍 [UI] Validando alertas. Esperados: " + expectedTexts.length + ", Encontrados: " + count);
    assert count == expectedTexts.length : "Quantidade de alertas diferente do esperado. Esperado: " + expectedTexts.length + ", Obtido: " + count;
    for (int i = 0; i < count; i++) {
      alerts.nth(i).waitFor();
      String actual = alerts.nth(i).textContent().trim();
      System.out.println("   - Alerta [" + i + "]: " + actual + " (Esperado conter: '" + expectedTexts[i] + "')");
      assert actual.contains(expectedTexts[i]) :
          "Esperado conter: '" + expectedTexts[i] + "', mas foi obtido: '" + actual + "'";
    }
  }

  @Step("Validando que o usuário '{username}' está logado com sucesso")
  public void isLoggedIn(String username) {
    System.out.println("👤 [UI] Verificando se o usuário logado é: " + username);
    assertThat(page.locator(".logged-user")).hasText("Olá, " + username);
  }

  @Step("Fluxo completo de Login com email '{email}' e validação do usuário '{username}'")
  public void Login(String email, String password, String username) {
    navigate();
    submit(email, password);
    isLoggedIn(username);
  }
}