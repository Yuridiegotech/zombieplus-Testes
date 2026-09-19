package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.BaseTest;

@Epic("Captação de Leads")
@Feature("Fila de Espera (Landing Page)")
public class LeadsTest extends BaseTest {

  @Test
  @Story("Cadastro com sucesso")
  @Severity(SeverityLevel.BLOCKER)
  @DisplayName("Deve cadastrar um lead na fila de espera com sucesso")
  void fluxoPrincipal() {
    String leadName = faker.name().fullName();
    String leadEmail = faker.internet().emailAddress();

    leads.navigate();
    leads.openLeadModal();
    leads.fillName(leadName);
    leads.fillEmail(leadEmail);
    leads.submitLeadForm();
    String message = "Agradecemos por compartilhar seus dados conosco. Em breve, nossa equipe entrará em contato.";
    components.waitForPopupMessage(message);
  }

  @Test
  @Story("Validação de duplicidade")
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("Não deve cadastrar quando o email já existe na lista")
  void FluxoPrincipalNaoCadastroEmailExistente() {
    String leadName = faker.name().fullName();
    String leadEmail = faker.internet().emailAddress();

    // Cadastra o lead via API primeiro (massa de teste)
    var response = leadsApi.createLead(leadName, leadEmail);
    assert response.status() == 201 : "Falha ao cadastrar lead via API. Status: " + response.status();

    // Tenta cadastrar o mesmo lead via UI
    leads.navigate();
    leads.openLeadModal();
    leads.fillName(leadName);
    leads.fillEmail(leadEmail);
    leads.submitLeadForm();
    String message = "Verificamos que o endereço de e-mail fornecido já consta em nossa lista de espera. Isso significa que você está um passo mais perto de aproveitar nossos serviços.";
    components.waitForPopupMessage(message);
  }

  @Test
  @Story("Validação de formato de email")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Deve retornar erro ao cadastrar email inválido")
  void fluxoPrincipalEmailIncorreto() {
    leads.navigate();
    leads.openLeadModal();
    leads.fillName("Diego Yuri1");
    leads.fillEmail("yuridiegotech1.gmail.com");
    leads.submitLeadForm();
    leads.assertAlertsTexts("Email incorreto");
  }

  @Test
  @Story("Validação de campos obrigatórios")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Deve retornar erro ao cadastrar sem preencher o nome")
  void campoObrigatorioNome() {
    leads.navigate();
    leads.openLeadModal();
    leads.fillEmail("yuridiegotech1@gmail.com");
    leads.submitLeadForm();
    leads.assertAlertsTexts("Campo obrigatório");
  }

  @Test
  @Story("Validação de campos obrigatórios")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Deve retornar erro ao cadastrar sem preencher o email")
  void campoObrigatorioEmail() {
    leads.navigate();
    leads.openLeadModal();
    leads.fillName("Diego Yuri1");
    leads.submitLeadForm();
    leads.assertAlertsTexts("Campo obrigatório");
  }

  @Test
  @Story("Validação de campos obrigatórios")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Deve retornar erro ao submeter formulário de lead vazio")
  void campoObrigatorio() {
    leads.navigate();
    leads.openLeadModal();
    leads.submitLeadForm();
    leads.assertAlertsTexts("Campo obrigatório", "Campo obrigatório");
  }

}