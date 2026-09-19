package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.BaseTest;

@Epic("Autenticação")
@Feature("Login de Administrador")
public class LoginTest extends BaseTest {

  @Test
  @Story("Login com credenciais válidas")
  @Severity(SeverityLevel.BLOCKER)
  @DisplayName("Deve logar como Admin com sucesso")
  void PageLoginAdmin() {
    login.navigate();
    login.submit("admin@zombieplus.com", "pwd123");
    login.isLoggedIn("Admin");
  }

  @Test
  @Story("Validação de credenciais incorretas")
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("Não deve logar com senha incorreta")
  void PageLoginAdminIncorrectPassword() {
    login.navigate();
    login.submit("admin@zombieplus.com", "errada");
    String message = "Ocorreu um erro ao tentar efetuar o login. Por favor, verifique suas credenciais e tente novamente.";
    components.waitForPopupMessage(message);
  }

  @Test
  @Story("Validação de formato de email")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Não deve logar quando o email é inválido")
  void PageLoginAdminEmailWithIncorrect() {
    login.navigate();
    login.submit("yuridiegotech.vercel", "abc123");
    login.assertAlertsTexts("Email incorreto");
  }

  @Test
  @Story("Validação de campos obrigatórios")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Não deve logar quando o email não é preenchido")
  void PageLoginAdminEmailNull() {
    login.navigate();
    login.submit("", "errada");
    login.assertAlertsTexts("Campo obrigatório");
  }

  @Test
  @Story("Validação de campos obrigatórios")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Não deve logar quando a senha não é preenchida")
  void PageLoginAdminPasswordNull() {
    login.navigate();
    login.submit("Teste@teste.com", "");
    login.assertAlertsTexts("Campo obrigatório");
  }

  @Test
  @Story("Validação de campos obrigatórios")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Não deve logar quando senha e email não são preenchidos")
  void PageLoginAdminPasswordAndEmailWithNull() {
    login.navigate();
    login.submit("", "");
    login.assertAlertsTexts("Campo obrigatório", "Campo obrigatório");
  }

}