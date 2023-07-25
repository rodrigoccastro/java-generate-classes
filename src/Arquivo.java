import java.io.*;

public class Arquivo {

    public static String readFile(String name) throws IOException {
        StringBuilder ret = new StringBuilder();
        BufferedReader buffRead = new BufferedReader(new FileReader(name));
        String linha = "";
        while (true) {
            linha = buffRead.readLine();
            if (linha != null) {
                ret.append(linha + "\n");
            } else
                break;
        }
        buffRead.close();
        return ret.toString();
    }

    public static void createAndPrintFile(String name, String text) throws IOException {
        FileWriter arq = new FileWriter(name);
        PrintWriter gravarArq = new PrintWriter(arq);
        gravarArq.printf(text);
        arq.close();
    }
}
