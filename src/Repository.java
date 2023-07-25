import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Repository {

    public static void create(String table_name) throws Exception {
        String repository = Arquivo.readFile("fontes/Repository.txt");
        String modelName = Entity.getModelName(table_name);
        repository = repository.replace("ModelName", modelName);
        repository = repository.replace("<model_name_lower>", modelName.toLowerCase());
        String typeCrudKey = getTypeCrud(table_name);
        repository = repository.replace("TypeCrudKey", typeCrudKey);
        String dir = "gerados/entity/" + modelName.toLowerCase()+"/";
        new File(dir).mkdir();
        Arquivo.createAndPrintFile(dir+modelName+"Repository.java", repository);
    }

    private static String getTypeCrud(String table_name) throws Exception {
        String data_type = "";
        String sql = "SELECT data_type FROM information_schema.columns WHERE table_name = '"+table_name+"' ";
        sql += "and ordinal_position=1 order by ordinal_position";
        PreparedStatement pstm = Conexao.pegaConexao().prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        if (rs.next()) {
            data_type = rs.getString("data_type");
        }
        pstm.close();
        if (data_type.equals("integer")) { data_type = "Integer"; }
        if (data_type.equals("uuid")) { data_type = "java.util.UUID"; }
        if (data_type.equals("character varying")) { data_type = "String"; }
        return data_type;
    }
}
