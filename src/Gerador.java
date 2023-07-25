import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Gerador {

    private static Connection conn;

    public static void main(String[] args) {

        try {

            ArrayList<String> sql = new ArrayList(
                    Arrays.asList(
                            "cashier","charity","device",
                            "device_lobby_game_order","distributor","distributor_server",
                            "location","portal_role","portal_role_permission","portal_user",
                            "pulltab_game_definition","pulltab_game_definition_variant"));

            for (String tableName : sql) {
//                System.out.println("CREATE SEQUENCE serial_"+tableName+" AS integer OWNED BY "+tableName+".id;");
//                System.out.println("select setval('serial_"+tableName+"', (select coalesce(max(id), 0)+1 from "+tableName+"));");
//                System.out.println("ALTER TABLE "+tableName+" ALTER COLUMN id SET DEFAULT nextval('serial_"+tableName+"');");

                System.out.println("ALTER TABLE "+tableName+" ALTER COLUMN id SET DEFAULT null;");
                System.out.println("drop SEQUENCE serial_"+tableName+";");
            }







//                Integer teste1 = 1;
//                Object ob1 = teste1;
//                Double teste2 = Double.valueOf(1);
//                Object ob2 = teste2;
//
//                int value = (int) ((Number) ob1).doubleValue();
//                int value2 = (int) ((Number) ob2).doubleValue();

//            ArrayList<String> strListas = new ArrayList<>();
//            strListas.add("Paulo");
//            strListas.add("Adriano");
//            strListas.add("Paula");
//
//            List<String> matches = strListas.parallelStream()
//                    .filter((element) -> element.contains("Pa"))
//                    .collect(Collectors.toList());
//            System.out.println(matches.toString());
//
//
            ArrayList<String> listTables = Table.getNames();
            System.out.println("count: " + listTables.size());

            Entity.clearOldFiles();
            Business.clearOldFiles();
            Controller.clearOldFiles();

            for (int i=0; i<listTables.size();i++) {
                String table_name = listTables.get(i);
                System.out.println("tabela: " + table_name);

                Entity.create(table_name);
                System.out.println("entity criado");

                Repository.create(table_name);
                System.out.println("repository criado");

                Business.create(table_name);
                System.out.println("business criado");

                Controller.create(table_name);
                System.out.println("controller criado");
            }

        } catch (Exception e) {
            System.out.println("erro:"+ e.getMessage());
        }
    }

}