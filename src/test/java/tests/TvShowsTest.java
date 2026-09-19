package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.BaseTest;
import support.database;
import support.fixtures.tvshows.TvShowsData;

@Epic("Gestão de Catálogo")
@Feature("Administração de Séries de TV")
public class TvShowsTest extends BaseTest {

  @Test
  @Story("Cadastro de séries")
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("Deve poder cadastrar uma nova série com sucesso")
  void pageTvShowsNewRecord() {
    var tvShow = TvShowsData.get("create");

    // Deleta a série do banco de dados caso já exista
    String tvShowTitle = TvShowsData.getStringValue(tvShow, "title");
    database.executeSQL(String.format("DELETE FROM tvshows WHERE title = '%s'", tvShowTitle));

    // Devo estar logado
    login.Login("admin@zombieplus.com", "pwd123", "Admin");

    tvShows.goToPageTvShow();

    page.waitForTimeout(5000);

    // Cadastrar nova série
    tvShows.createNewTvShow(
        TvShowsData.getStringValue(tvShow, "title"),
        TvShowsData.getStringValue(tvShow, "overview"),
        TvShowsData.getStringValue(tvShow, "company"),
        TvShowsData.getIntegerValue(tvShow, "release_year"),
        TvShowsData.getIntegerValue(tvShow, "season"),
        TvShowsData.getStringValue(tvShow, "cover"),
        TvShowsData.getBooleanValue(tvShow, "featured")
    );
    components.waitForPopupMessage("A série '" + tvShowTitle + "' foi adicionada ao catálogo.");
  }

  @Test
  @Story("Cadastro de séries")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Não deve permitir cadastrar série duplicada")
  void pageTvShowsNewRecordDuplicate() {
    var tvShow = TvShowsData.get("duplicate");

    // Deleta a série do banco de dados caso já exista
    String tvShowTitle = TvShowsData.getStringValue(tvShow, "title");
    database.executeSQL(String.format("DELETE FROM tvshows WHERE title = '%s'", tvShowTitle));

    // Insere a série via API (massa de teste)
    tvShowsApi.createTvShow(tvShow);

    // Devo estar logado
    login.Login("admin@zombieplus.com", "pwd123", "Admin");

    tvShows.goToPageTvShow();

    // Cadastrar nova série duplicada
    tvShows.createNewTvShow(
        TvShowsData.getStringValue(tvShow, "title"),
        TvShowsData.getStringValue(tvShow, "overview"),
        TvShowsData.getStringValue(tvShow, "company"),
        TvShowsData.getIntegerValue(tvShow, "release_year"),
        TvShowsData.getIntegerValue(tvShow, "season"),
        TvShowsData.getStringValue(tvShow, "cover"),
        TvShowsData.getBooleanValue(tvShow, "featured")
    );

    components.waitForPopupMessage(
        "O título '" + tvShowTitle
            + "' já consta em nosso catálogo. Por favor, verifique se há necessidade de atualizações ou correções para este item.");
  }

  @Test
  @Story("Validação de formulário de cadastro")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Não deve cadastrar série quando os campos obrigatórios não forem preenchidos")
  void pageTvShowsRecordWithFieldNull() {
    // Devo estar logado
    login.Login("admin@zombieplus.com", "pwd123", "Admin");

    // Acessar a página de séries
    tvShows.goToPageTvShow();

    tvShows.goForm();
    tvShows.submitForm();
    tvShows.assertAlertsTexts("Campo obrigatório",
        "Campo obrigatório",
        "Campo obrigatório",
        "Campo obrigatório",
        "Campo obrigatório (apenas números)"
    );
  }

  @Test
  @Story("Exclusão de séries")
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("Deve permitir excluir uma série existente com sucesso")
  void pageTvShowsDeleteTvShow() {
    var tvShow = TvShowsData.get("delete");

    // Deleta a série do banco de dados caso já exista
    String tvShowTitle = TvShowsData.getStringValue(tvShow, "title");
    database.executeSQL(String.format("DELETE FROM tvshows WHERE title = '%s'", tvShowTitle));

    // Insere a série via API (massa de teste)
    tvShowsApi.createTvShow(tvShow);

    // Devo estar logado
    login.Login("admin@zombieplus.com", "pwd123", "Admin");

    // Acessar a página de séries
    tvShows.goToPageTvShow();

    // Excluir a série
    tvShows.deleteTvShow(tvShowTitle);

    components.waitForPopupMessage("Série removida com sucesso.");
  }

  @Test
  @Story("Busca de séries")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Deve realizar busca de séries pelo termo 'zombie'")
  void pageTvShowsSearchTvShow() {
    var tvShow = TvShowsData.get("search");

    // Pega o array de séries do JSON
    var tvShowsList = TvShowsData.getTvShowsList(tvShow, "data");

    // Deleta todas as séries do array do banco de dados
    for (var tv : tvShowsList) {
      String tvShowTitle = TvShowsData.getStringValue(tv, "title");
      database.executeSQL(String.format("DELETE FROM tvshows WHERE title = '%s'", tvShowTitle));
    }

    // Cria todas as séries via API
    for (var tv : tvShowsList) {
      tvShowsApi.createTvShow(tv);
    }

    // Devo estar logado
    login.Login("admin@zombieplus.com", "pwd123", "Admin");

    // Acessar a página de séries
    tvShows.goToPageTvShow();

    // Realizar a busca
    tvShows.searchTvShow(TvShowsData.getStringValue(tvShow, "input"));

    // Valida que todos os títulos das séries criadas estão nos resultados
    var expectedTitles = tvShowsList.stream()
        .map(tv -> TvShowsData.getStringValue(tv, "title"))
        .toList();
    tvShows.assertSearchResults(expectedTitles);
  }

}