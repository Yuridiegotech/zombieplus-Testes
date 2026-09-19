package support.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;

public class Components {

  private final Page page;

  public Components(Page page) {
    this.page = page;
  }

  @Step("Aguardando exibição e ocultação da mensagem Toast: '{message}'")
  public void waitForToastMessageHidden(String message) {
    System.out.println("⏳ [UI] Aguardando Toast ficar visível: " + message);
    page.locator(".toast").getByText(message)
        .waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
        );
    page.waitForTimeout(5000);
    System.out.println("⏳ [UI] Aguardando Toast ocultar: " + message);
    page.locator(".toast").getByText(message)
        .waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.HIDDEN)
        );
  }

  @Step("Aguardando mensagem no Popup modal: '{message}'")
  public void waitForPopupMessage(String message) {
    System.out.println("💬 [UI] Aguardando Popup com mensagem: " + message);
    page.locator(".swal2-html-container").getByText(message)
        .waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
        );
  }
}