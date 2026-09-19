package support.api;

import com.google.gson.Gson;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

import java.util.HashMap;
import java.util.Map;

public class LeadsApi {

  private final APIRequestContext request;
  private final String baseUrl;

  public LeadsApi(Playwright playwright) {
    String envUrl = System.getenv("API_URL");
    this.baseUrl = (envUrl != null && !envUrl.isEmpty()) ? envUrl : "http://localhost:3333";
    this.request = playwright.request().newContext(
        new APIRequest.NewContextOptions().setBaseURL(baseUrl)
    );
  }

  @Step("API: Cadastrando lead '{name}' com email '{email}' (POST /leads)")
  public APIResponse createLead(String name, String email) {
    System.out.println("========== [API] CRIANDO LEAD ==========");
    System.out.println("Name: " + name);
    System.out.println("Email: " + email);

    Map<String, String> data = new HashMap<>();
    data.put("name", name);
    data.put("email", email);

    APIResponse response = request.post("/leads",
        RequestOptions.create().setData(data)
    );

    System.out.println("Status Code: " + response.status());
    System.out.println("Response Body: " + response.text());
    System.out.println("========================================\n");

    Allure.addAttachment("API Request Lead Data", "application/json", new Gson().toJson(data));
    Allure.addAttachment("API Response POST /leads (" + response.status() + ")", "application/json", response.text());

    return response;
  }

  public void dispose() {
    if (request != null) {
      request.dispose();
    }
  }
}