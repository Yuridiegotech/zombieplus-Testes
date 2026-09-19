package support.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MoviesApi {

  private final APIRequestContext request;
  private final String token;
  private final String baseUrl;

  public MoviesApi(Playwright playwright) {
    String envUrl = System.getenv("API_URL");
    this.baseUrl = (envUrl != null && !envUrl.isEmpty()) ? envUrl : "http://localhost:3333";
    this.request = playwright.request().newContext(
        new APIRequest.NewContextOptions().setBaseURL(baseUrl)
    );
    this.token = generateToken();
  }

  @Step("API: Cadastrando filme '{movie}' via requisição POST /movies")
  public void createMovie(Map<String, Object> movie) {
    String title = (String) movie.get("title");
    String overview = (String) movie.get("overview");
    String company = (String) movie.get("company");
    String releaseYear = getIntegerValue(movie, "release_year");
    String cover = (String) movie.get("cover");
    boolean featured = getBooleanValue(movie, "featured");

    System.out.println("========== [API] CRIANDO FILME ==========");
    System.out.println("Title: " + title);
    System.out.println("Overview: " + overview);
    System.out.println("Company: " + company);
    System.out.println("Release Year: " + releaseYear);
    System.out.println("Cover: " + cover);
    System.out.println("Featured: " + featured);

    String companyId = getCompanies(company);

    FormData formData = FormData.create()
        .set("title", title)
        .set("overview", overview)
        .set("company_id", companyId)
        .set("release_year", releaseYear)
        .set("featured", String.valueOf(featured));

    if (cover != null && !cover.isEmpty()) {
      String cleanCover = cover.startsWith("/") ? cover.substring(1) : cover;
      String coverPath = "src/test/java/support/fixtures/" + cleanCover;
      formData.set("cover", Paths.get(coverPath));
    }

    APIResponse response = request.post("/movies",
        RequestOptions.create()
            .setHeader("Authorization", "Bearer " + token)
            .setMultipart(formData)
    );

    System.out.println("Status: " + response.status() + " " + response.statusText());
    System.out.println("Response: " + response.text());
    System.out.println("=========================================\n");

    Allure.addAttachment("API Request Movie Data", "application/json", new Gson().toJson(movie));
    Allure.addAttachment("API Response POST /movies (" + response.status() + ")", "application/json", response.text());

    if (!response.ok()) {
      throw new RuntimeException("Falha ao criar filme via API: " + response.status() + " - " + response.text());
    }
  }

  private String getIntegerValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Double) {
      return String.valueOf(((Double) value).intValue());
    }
    return String.valueOf(value);
  }

  private boolean getBooleanValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return false;
  }

  @Step("API: Gerando token de autenticação (/sessions)")
  private String generateToken() {
    String email = "admin@zombieplus.com";
    String password = "pwd123";

    Map<String, String> data = new HashMap<>();
    data.put("email", email);
    data.put("password", password);

    Gson gson = new Gson();
    APIResponse response = request.post("/sessions",
        RequestOptions.create()
            .setHeader("Content-Type", "application/json")
            .setData(gson.toJson(data))
    );

    if (!response.ok()) {
      throw new RuntimeException("Falha ao gerar token: " + response.status() + " - " + response.text());
    }

    JsonObject responseBody = gson.fromJson(response.text(), JsonObject.class);
    String generatedToken = responseBody.get("token").getAsString();
    System.out.println("🔑 [API] Token de admin gerado com sucesso.");
    return generatedToken;
  }

  @Step("API: Obtendo ID da produtora '{companyName}' (/companies)")
  private String getCompanies(String companyName) {
    APIResponse response = request.get("/companies",
        RequestOptions.create()
            .setHeader("Authorization", "Bearer " + token)
            .setQueryParam("name", companyName)
    );

    Gson gson = new Gson();
    JsonObject responseBody = gson.fromJson(response.text(), JsonObject.class);
    JsonArray data = responseBody.getAsJsonArray("data");

    if (data != null && !data.isEmpty()) {
      JsonObject firstCompany = data.get(0).getAsJsonObject();
      return firstCompany.get("id").getAsString();
    }

    throw new RuntimeException("Company não encontrada: " + companyName);
  }

  public void dispose() {
    if (request != null) {
      request.dispose();
    }
  }
}