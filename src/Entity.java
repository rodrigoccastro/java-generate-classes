import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

public class Entity {

    public static void clearOldFiles() {
        String dir = "gerados/entity/";
        File f = new File(dir);
        if (f.exists()) {
            f.delete();
        }
        new File(dir).mkdir();
    }

    public static void create(String table_name) throws Exception {
        String model = Arquivo.readFile("fontes/Model.txt");
        model = model.replace("<table_name>", table_name);
        String modelName = getModelName(table_name);
        model = model.replace("<model_name>", modelName);
        model = model.replace("<model_name_lower>", modelName.toLowerCase());
        model = model.replace("<model_fields>", getModelFields(table_name));
        model = model.replace("<model_to_string>", getModelToString(table_name));
        String dir = "gerados/entity/" + modelName.toLowerCase()+"/";
        new File(dir).mkdir();
        Arquivo.createAndPrintFile(dir+modelName+"Entity.java", model);
    }

    private static String getModelToString(String table_name) throws Exception {
        StringBuilder ret = new StringBuilder();
        String sql = "SELECT geral.column_name, geral.data_type, geral.ordinal_position, geral.is_nullable, geral.character_maximum_length ";
        sql += "FROM information_schema.columns as geral WHERE table_name = '"+table_name+"' order by geral.ordinal_position";
        PreparedStatement pstm = Conexao.pegaConexao().prepareStatement("select count(*) from ("+sql+") t");
        ResultSet rs = pstm.executeQuery();
        rs.next();
        int totalColumns = rs.getInt(1);
        pstm = Conexao.pegaConexao().prepareStatement(sql);
        rs = pstm.executeQuery();
        String[] typesprim = new String[]{"boolean", "byte", "char", "short", "int", "long", "float", "double"};
        List<String> listTypes = Arrays.asList(typesprim);
        int count = 0;
        while (rs.next())
        {
            String column_name = rs.getString("column_name");
            String data_type = rs.getString("data_type");
            String data_type_java = getDataType(data_type);
            String camelColumnName = getModelName(column_name);
            camelColumnName = camelColumnName.substring(0,1).toLowerCase().concat(camelColumnName.substring(1));
            if (count > 0) {
                ret.append("\n\t\t");
            }
            if (!column_name.contains("password")) {
                if (listTypes.contains(data_type_java)) {
                    ret.append("ret.append(\"\\\"" + camelColumnName + "\\\": \\\"\").append(" + camelColumnName + ")");
                } else {
                    ret.append("ret.append(\"\\\"" + camelColumnName + "\\\": \\\"\").append(getValue(" + camelColumnName + "))");
                }
                if ((count+1) < totalColumns) {
                    ret.append(".append(\"\\\",\");");
                } else {
                    ret.append(".append(\"\\\"\");");
                }
            }
            count++;
        }
        pstm.close();
        String rsstr = ret.toString();
        //return rsstr.substring(0,rsstr.length()-3).concat("\";");
        return rsstr;
    }

    public static String getModelName(String name) throws Exception {
        String modelName = "";
        String[] arr = name.split("_");
        for(int i=0; i< arr.length;i++){
            modelName += arr[i].substring(0,1).toUpperCase().concat(arr[i].substring(1));
        }
        return modelName;
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
            if ( column_pk==true || column_name.equals("id")) {
                ret.append("\n\n\t@Id");
            } else {
                ret.append("\n\n\t@Column(name=\""+column_name+"\", nullable="+is_nullable+")");
            }
            ret.append("\n\tprivate "+data_type_java+" "+camelColumnName+";");
        }
        pstm.close();
        return ret.toString();
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
            case "boolean":
                return "Boolean";
            default:
                return data_type;
        }
    }


}
