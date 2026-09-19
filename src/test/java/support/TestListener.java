package support;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.ByteArrayInputStream;
import java.util.Optional;

public class TestListener implements TestWatcher {

    @Override
    public void testSuccessful(ExtensionContext context) {
        System.out.println("✅ [PASS] " + context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        System.err.println("❌ [FAIL] " + context.getDisplayName() + " - Motivo: " + (cause != null ? cause.getMessage() : "Erro desconhecido"));

        Object testInstance = context.getRequiredTestInstance();
        if (testInstance instanceof BaseTest) {
            BaseTest baseTest = (BaseTest) testInstance;
            try {
                if (baseTest.getPage() != null && !baseTest.getPage().isClosed()) {
                    byte[] screenshot = baseTest.getPage().screenshot(new Page.ScreenshotOptions().setFullPage(true));
                    Allure.addAttachment("Screenshot na Falha", "image/png", new ByteArrayInputStream(screenshot), ".png");
                }
            } catch (Exception e) {
                System.err.println("Não foi possível capturar screenshot na falha: " + e.getMessage());
            }
        }
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        System.out.println("⚠️ [ABORTED] " + context.getDisplayName());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        System.out.println("⏸️ [SKIPPED] " + context.getDisplayName() + " - " + reason.orElse("Sem motivo informado"));
    }
}