import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Table {

    public static ArrayList<String> getNames() throws Exception {
        ArrayList<String> toReturn = new ArrayList<String>();
        String sql= "SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'";
        PreparedStatement pstm = Conexao.pegaConexao().prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        while (rs.next())
        {
            toReturn.add(rs.getString("table_name"));
        }
        pstm.close();
        return toReturn;
    }
}
