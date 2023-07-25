import java.sql.Connection;
import java.sql.DriverManager;

public class Conexao {

    private static Connection conn;
    public static Connection pegaConexao() throws Exception {
        if (conn == null) {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/manufacturer", "postgres", "barnabe123");
        }
        return conn;
    }

}
