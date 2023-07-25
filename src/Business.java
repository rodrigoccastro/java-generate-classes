import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Business {

    public static void clearOldFiles() {
        String dir = "gerados/business/";
        File f = new File(dir);
        if (f.exists()) {
            f.delete();
        }
        new File(dir).mkdir();
    }
    public static void create(String table_name) throws Exception {

        String dir = "gerados/business/" +getModelName(table_name).toLowerCase()+"/";
        File f = new File(dir);
        if (f.exists()) {
            f.delete();
        }
        new File(dir).mkdir();
        getPrimaryKey(table_name);

        createDelete(table_name, dir);
        createList(table_name, dir);
        createFindId(table_name, dir);
        createSave(table_name, dir);
        createDTO(table_name, dir);
        //createUpdate(table_name, dir);

    }

    private static void createSave(String table_name, String dir) throws Exception {
        String business = Arquivo.readFile("fontes/BusinessSave.txt");
        String modelName = getModelName(table_name);
        business = business.replace("<model_name_lower>", modelName.toLowerCase());
        business = business.replace("<model_name>", modelName);
        business = business.replace("<update_timestamp>", getUpdateTimeStampToInsert(table_name,modelName));
        business = business.replace("<model_fields>", getModelFieldsSave(table_name));
        Arquivo.createAndPrintFile(dir+"Save"+modelName+".java", business);
    }
    private static void createDTO(String table_name, String dir) throws Exception {
        String business = Arquivo.readFile("fontes/BusinessDTO.txt");
        String modelName = getModelName(table_name);
        business = business.replace("<model_name_lower>", modelName.toLowerCase());
        business = business.replace("<model_name>", modelName);
        business = business.replace("<update_timestamp>", getUpdateTimeStampToInsert(table_name,modelName));
        business = business.replace("<model_fields>", getModelFields(table_name));
        Arquivo.createAndPrintFile(dir+modelName+"DTO.java", business);
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
            camelColumnName = camelColumnName.substring(0,1).toLowerCase().concat(camelColumnName.substring(1));
            boolean column_pk = rs.getString("pk")==null ? false : true;
            String is_nullable = rs.getBoolean("is_nullable")==true ? "true" : "false";
            ret.append("\n\tprivate String "+camelColumnName+";");
        }
        pstm.close();
        return ret.toString();
    }

    private static String getModelFieldsFind(String table_name) throws Exception {
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
            ret.append("\n\t\tobjDTO.set"+camelColumnName+"(getValueString(tuple.get(\""+column_name+"\")));");
        }
        pstm.close();
        return ret.toString();
    }

    private static String getModelFieldsSave(String table_name) throws Exception {
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
            String data_type_java = getDataTypeSave(data_type);
            String camelColumnName = getModelName(column_name);
            camelColumnName = camelColumnName.substring(0,1).toUpperCase().concat(camelColumnName.substring(1));
            ret.append("\n\t\tentity.set"+camelColumnName+"(getValue"+data_type_java+"(objDTO.get"+camelColumnName+"()));");
        }
        pstm.close();
        return ret.toString();
    }

    private static String getUpdateTimeStampToInsert(String table_name, String modelName) throws Exception {
        if (getUpdateTimeStamp(table_name)) {
            return "new"+modelName+".setUpdateTimestamp(java.time.OffsetDateTime.now());\n\t\t";
        } else {
            return "";
        }
    }

    private static String getUpdateTimeStampToUpdate(String table_name, String modelName) throws Exception {
        if (getUpdateTimeStamp(table_name)) {
            return "\n\t\t\t\told"+modelName+".setUpdateTimestamp(java.time.OffsetDateTime.now());";
        } else {
            return "";
        }
    }

    private static void createUpdate(String table_name, String dir) throws Exception {
        String business = Arquivo.readFile("fontes/BusinessUpdate.txt");
        String modelName = getModelName(table_name);
        business = business.replace("<model_name_lower>", modelName.toLowerCase());
        business = business.replace("<model_name>", modelName);
        business = business.replace("<model_pk_type>", getDataType(pk_type));
        business = business.replace("<model_pk_name>", pk_name);
        business = business.replace("<update_fields>", getUpdateFields(table_name,modelName));
        //business = business.replace("<update_timestamp>", getUpdateTimeStampToUpdate(table_name,modelName));
        Arquivo.createAndPrintFile(dir + "Update" + modelName + ".java", business);
    }

    private static String getUpdateFields(String table_name, String modelName) throws Exception {
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
            String fieldCamel = camelColumnName.substring(0,1).toLowerCase().concat(camelColumnName.substring(1));
            boolean column_pk = rs.getString("pk")==null ? false : true;
            if (( column_pk==true || column_name.equals("id"))==false) {
                if (column_name.equals("update_timestamp")) {
                    ret.append("\n\t\told" + modelName + ".setUpdateTimestamp(java.time.OffsetDateTime.now());");
                } else {
                    if (data_type.equals("boolean")) {
                        ret.append("\n\t\told" + modelName + ".set" + camelColumnName + "(new" + modelName + ".is" + camelColumnName + "());");
                    } else {
                        ret.append("\n\t\told" + modelName + ".set" + camelColumnName + "(new" + modelName + ".get" + camelColumnName + "());");
                    }
                }
            }
        }
        pstm.close();
        return ret.toString();
    }
    private static void createList(String table_name, String dir) throws Exception {
        String business = Arquivo.readFile("fontes/BusinessList.txt");
        String modelName = getModelName(table_name);
        business = business.replace("<model_name_lower>", modelName.toLowerCase());
        business = business.replace("<model_name>", modelName);
        business = business.replace("<model_fields>", getModelFieldsFind(table_name));

        //String dir = "gerados/business/" + modelName.toLowerCase()+"/";;
        Arquivo.createAndPrintFile(dir+"List"+modelName+".java", business);
    }
    private static void createFindId(String table_name, String dir) throws Exception {
        String business = Arquivo.readFile("fontes/BusinessFind.txt");
        String modelName = getModelName(table_name);
        business = business.replace("<model_name_lower>", modelName.toLowerCase());
        business = business.replace("<model_name>", modelName);
        business = business.replace("<model_pk_type>", getDataType(pk_type));
        business = business.replace("<model_pk_name>", pk_name);
        business = business.replace("<model_fields>", getModelFieldsFind(table_name));

        //String dir = "gerados/business/" + modelName.toLowerCase()+"/";;
        Arquivo.createAndPrintFile(dir+"Find"+modelName+".java", business);
    }

    private static void createDelete(String table_name, String dir) throws Exception {
        String business = Arquivo.readFile("fontes/BusinessDelete.txt");
        String modelName = getModelName(table_name);
        business = business.replace("<model_name_lower>", modelName.toLowerCase());
        business = business.replace("<model_name>", modelName);
        business = business.replace("<model_pk_type>", getDataType(pk_type));
        business = business.replace("<model_pk_name>", pk_name);
        Arquivo.createAndPrintFile(dir+"Delete"+modelName+".java", business);
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

    private static boolean getUpdateTimeStamp(String table_name) throws Exception {
        boolean ret = false;
        String sql = "SELECT column_name FROM information_schema.columns ";
        sql += "WHERE table_name = '"+table_name+"' and column_name='update_timestamp' ";
        PreparedStatement pstm = Conexao.pegaConexao().prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        if (rs.next()) {
            ret = true;
        }
        pstm.close();
        return ret;
    }

    public static String getModelName(String name) throws Exception {
        String modelName = "";
        String[] arr = name.split("_");
        for(int i=0; i< arr.length;i++){
            modelName += arr[i].substring(0,1).toUpperCase().concat(arr[i].substring(1));
        }
        return modelName;
    }

    private static String getDataTypeSave(String data_type) {
        if (data_type.contains("timestamp")) {
            return "Date";
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
                return "Date";
            case "text":
                return "String";
            case "numeric":
                return "BigDecimal";
            default:
                return data_type.substring(0,1).toUpperCase().concat(data_type.substring(1));
        }
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


}
