import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class Controller {

    public static void clearOldFiles() {
        String dir = "gerados/controller/";
        File f = new File(dir);
        if (f.exists()) {
            f.delete();
        }
        new File(dir).mkdir();

        dir = "gerados/controllerTest/";
        f = new File(dir);
        if (f.exists()) {
            f.delete();
        }
        new File(dir).mkdir();

        dir = "gerados/controllerPostman/";
        f = new File(dir);
        if (f.exists()) {
            f.delete();
        }
        new File(dir).mkdir();
    }
    public static void create(String table_name) throws Exception {
        String controller = Arquivo.readFile("fontes/Controller.txt");
        controller = controller.replace("<table_name>", table_name);
        String modelName = getModelName(table_name);
        controller = controller.replace("<model_name>", modelName);
        controller = controller.replace("<model_name_lower>", modelName.toLowerCase());
        getPrimaryKey(table_name);
        controller = controller.replace("<model_pk_name>", pk_name);
        controller = controller.replace("<model_pk_convert>", getPkConvert());
        String dir = "gerados/controller/";
        new File(dir).mkdir();
        Arquivo.createAndPrintFile(dir+modelName+"Controller.java", controller);

        createT(table_name);
        createP(table_name);
    }

    public static void createT(String table_name) throws Exception {
        String controller = Arquivo.readFile("fontes/ControllerT.txt");
        controller = controller.replace("<table_name>", table_name);
        String modelName = getModelName(table_name);
        controller = controller.replace("<model_name>", modelName);
        controller = controller.replace("<model_name_lower>", modelName.toLowerCase());
        getPrimaryKey(table_name);
        controller = controller.replace("<model_pk_name>", pk_name);
        controller = controller.replace("<model_pk_convert>", getPkConvert());
        controller = controller.replace("<model_fields>", getModelFields(table_name));

        String dir = "gerados/controllerTest/";
        new File(dir).mkdir();
        Arquivo.createAndPrintFile(dir+modelName+"ControllerTest.java", controller);
    }

    public static void createP(String table_name) throws Exception {
        String controller = Arquivo.readFile("fontes/ControllerP.txt");
        controller = controller.replace("<table_name>", table_name);
        String modelName = getModelName(table_name);
        controller = controller.replace("<model_name>", modelName);
        controller = controller.replace("<model_name_lower>", modelName.toLowerCase());
        getPrimaryKey(table_name);
        controller = controller.replace("<model_pk_name>", pk_name);
        controller = controller.replace("<model_pk_convert>", getPkConvert());
        controller = controller.replace("<model_fields>", getModelFieldsPt(table_name));

        String dir = "gerados/controllerPostman/";
        new File(dir).mkdir();
        Arquivo.createAndPrintFile(dir+modelName+"Controller.txt", controller);
    }

    private static String getModelFields(String table_name) throws Exception {
        StringBuilder ret = new StringBuilder();
        String sql = "SELECT geral.column_name, geral.data_type, geral.ordinal_position, geral.is_nullable, geral.character_maximum_length,";
        sql += "( select '1' from information_schema.table_constraints tco join information_schema.key_column_usage kcu ";
        sql += "on kcu.constraint_name = tco.constraint_name and kcu.constraint_schema = tco.constraint_schema and kcu.constraint_name = tco.constraint_name ";
        sql += "where tco.constraint_type = 'PRIMARY KEY' and kcu.table_name='"+table_name+"' and  kcu.column_name=geral.column_name) as pk ";
        sql += "FROM information_schema.columns as geral WHERE table_name = '"+table_name+"' order by geral.ordinal_position";
        PreparedStatement pstm = Conexao.pegaConexao().prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        while (rs.next())
        {
            String column_name = rs.getString("column_name");
            String data_type = rs.getString("data_type");
            String data_type_java = getDataType(data_type);
            String camelColumnName = getModelName(column_name);
            camelColumnName = camelColumnName.substring(0,1).toUpperCase().concat(camelColumnName.substring(1));
            boolean column_pk = rs.getString("pk")==null ? false : true;
            String is_nullable = rs.getBoolean("is_nullable")==true ? "true" : "false";
            ret.append("\n\t\tobj.set"+camelColumnName+"(\""+camelColumnName+"\");");
        }
        pstm.close();
        return ret.toString();
    }
    private static String getModelFieldsPt(String table_name) throws Exception {
        StringBuilder ret = new StringBuilder();
        String sql = "SELECT geral.column_name, geral.data_type, geral.ordinal_position, geral.is_nullable, geral.character_maximum_length,";
        sql += "( select '1' from information_schema.table_constraints tco join information_schema.key_column_usage kcu ";
        sql += "on kcu.constraint_name = tco.constraint_name and kcu.constraint_schema = tco.constraint_schema and kcu.constraint_name = tco.constraint_name ";
        sql += "where tco.constraint_type = 'PRIMARY KEY' and kcu.table_name='"+table_name+"' and  kcu.column_name=geral.column_name) as pk ";
        sql += "FROM information_schema.columns as geral WHERE table_name = '"+table_name+"' order by geral.ordinal_position";
        PreparedStatement pstm = Conexao.pegaConexao().prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        while (rs.next())
        {
            String column_name = rs.getString("column_name");
            String data_type = rs.getString("data_type");
            String data_type_java = getDataType(data_type);
            String camelColumnName = getModelName(column_name);
            camelColumnName = camelColumnName.substring(0,1).toLowerCase().concat(camelColumnName.substring(1));
            boolean column_pk = rs.getString("pk")==null ? false : true;
            String is_nullable = rs.getBoolean("is_nullable")==true ? "true" : "false";
            ret.append("\n\t\t\t\""+camelColumnName+"\": \""+camelColumnName+"\",");
        }
        pstm.close();
        return ret.toString();
    }
    private static String pk_type;
    private static String pk_name;
    private static void getPrimaryKey(String table_name) throws Exception {
        String sql = "SELECT distinct geral.column_name, geral.data_type FROM information_schema.columns as geral ";
        sql += "inner join information_schema.key_column_usage kcu on geral.column_name=kcu.column_name ";
        sql += "inner join information_schema.table_constraints tco on kcu.constraint_name = tco.constraint_name ";
        sql += "and kcu.constraint_schema = tco.constraint_schema and kcu.constraint_name = tco.constraint_name ";
        sql += "WHERE tco.constraint_type = 'PRIMARY KEY' and geral.table_name = '"+table_name+"'";
        PreparedStatement pstm = Conexao.pegaConexao().prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        if (rs.next()) {
            pk_name = rs.getString("column_name");
            pk_type = rs.getString("data_type");
        }
        pstm.close();
    }

    private static String getDataType(String data_type) {
        if (data_type.contains("timestamp")) {
            return "java.time.OffsetDateTime";
        }
        switch (data_type) {
            case "character varying":
                return "String";
            case "uuid":
                return "java.util.UUID";
            case "integer":
                return "Integer";
            case "smallint":
                return "Integer";
            case "date":
                return "java.sql.Date";
            case "text":
                return "String";
            case "numeric":
                return "java.math.BigDecimal";
            default:
                return data_type;
        }
    }
    private static String getPkConvert() {
        String java_data_type = getDataType(pk_type);
        if (java_data_type.equals("int")) { return "Integer.parseInt("+pk_name+")"; }
        if (java_data_type.equals("String")) { return pk_name; }
        if (java_data_type.equals("java.util.UUID")) { return "java.util.UUID.fromString("+pk_name+")"; }
        return pk_name;
    }

    public static String getModelName(String name) throws Exception {
        String modelName = "";
        String[] arr = name.split("_");
        for(int i=0; i< arr.length;i++){
            modelName += arr[i].substring(0,1).toUpperCase().concat(arr[i].substring(1));
        }
        return modelName;
    }

}
