package support;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class database {

    // Configuração do banco de dados
    private static final String DB_HOST = System.getenv("DATABASE_HOST") != null ? System.getenv("DATABASE_HOST") : "localhost";
    private static final String DB_NAME = System.getenv("DATABASE_NAME") != null ? System.getenv("DATABASE_NAME") : "zombieplus";
    private static final String DB_USER = System.getenv("DATABASE_USER") != null ? System.getenv("DATABASE_USER") : "postgres";
    private static final String DB_PASS = System.getenv("DATABASE_PASSWORD") != null ? System.getenv("DATABASE_PASSWORD") : "pwd123";
    private static final String DB_PORT = System.getenv("DATABASE_PORT") != null ? System.getenv("DATABASE_PORT") : "5432";

    private static String getConnectionUrl() {
        String envUrl = System.getenv("DATABASE_URL");
        if (envUrl != null && !envUrl.isEmpty()) {
            return envUrl;
        }
        return String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);
    }

    @Step("Executando limpeza/script SQL no banco de dados: {sqlScript}")
    public static void executeSQL(String sqlScript) {
        System.out.println("💾 [DATABASE] Executando SQL: " + sqlScript);
        Allure.addAttachment("Script SQL Executado", "text/plain", sqlScript);
        try (Connection connection = DriverManager.getConnection(getConnectionUrl(), DB_USER, DB_PASS);
             Statement statement = connection.createStatement()) {

            int rowsAffected = statement.executeUpdate(sqlScript);
            System.out.println("✅ [DATABASE] SQL executado com sucesso. Linhas afetadas: " + rowsAffected);
            Allure.addAttachment("Resultado SQL", "text/plain", "Linhas afetadas: " + rowsAffected);

        } catch (SQLException e) {
            System.err.println("❌ [DATABASE] Erro ao executar SQL: " + e.getMessage());
            Allure.addAttachment("Erro SQL", "text/plain", e.getMessage());
            throw new RuntimeException("Erro ao executar SQL: " + e.getMessage(), e);
        }
    }
}