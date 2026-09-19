package support.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

public class Leads {

  private final Page page;
  private final String baseUrl;

  public Leads(Page page) {
    this.page = page;
    String envUrl = System.getenv("BASE_URL");
    this.baseUrl = (envUrl != null && !envUrl.isEmpty()) ? envUrl : "http://localhost:3000";
  }

  @Step("Acessando a página inicial (Home)")
  public void navigate() {
    System.out.println("🌐 [UI] Acessando Home: " + baseUrl);
    page.navigate(baseUrl);
  }

  @Step("Abrindo o modal de cadastro de Lead (Fila de espera)")
  public void openLeadModal() {
    System.out.println("🔘 [UI] Clicando no botão 'Aperte o play'");
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Aperte o play")).click();
    page.getByTestId("modal")
        .getByRole(AriaRole.HEADING)
        .filter(new Locator.FilterOptions().setHasText("Fila de espera"))
        .waitFor();
  }

  @Step("Preenchendo nome do lead: '{name}'")
  public void fillName(String name) {
    System.out.println("✍️ [UI] Preenchendo nome: " + name);
    page.getByPlaceholder("Informe seu nome").fill(name);
  }

  @Step("Preenchendo email do lead: '{email}'")
  public void fillEmail(String email) {
    System.out.println("✍️ [UI] Preenchendo email: " + email);
    page.getByPlaceholder("Informe seu email").fill(email);
  }

  @Step("Submetendo formulário de Lead ('Quero entrar na fila!')")
  public void submitLeadForm() {
    System.out.println("🚀 [UI] Clicando em 'Quero entrar na fila!'");
    page.waitForTimeout(1000);
    page.getByTestId("modal")
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Quero entrar na fila!"))
        .click();
  }

  @Step("Validando mensagens de alerta nos campos de Lead: {expectedTexts}")
  public void assertAlertsTexts(String... expectedTexts) {
    Locator alerts = page.locator(".alert");
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
}