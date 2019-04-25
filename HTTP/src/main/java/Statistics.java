import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Statistics {

    Statistics(){ readDataFromFile(); }

    private ArrayList<ConData> statistics = new ArrayList<ConData>();

    private final String FILE_PATH = "E:\\STUDIA\\ITI\\Semestr_1\\SINT\\IdeaProjects\\HTTP\\src\\main\\resources\\statistics.csv";


    public void readDataFromFile() {
        String line;
        BufferedReader br;
        String lineSeparator = ",";

        try {
            br = new BufferedReader(new FileReader(FILE_PATH));
            br.readLine();

            while ((line = br.readLine()) != null) {
                ConData row = new ConData();
                String[] rowData = line.split(lineSeparator);

                row.url = rowData[0];
                row.requests = Long.parseLong(rowData[1]);
                row.dataSend = Long.parseLong(rowData[2]);
                row.dataGot = Long.parseLong(rowData[3]);
                statistics.add(row);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ConData get(ConData find){
        for (ConData domain : statistics){
            if (find.url.contains(domain.url)){
                return domain;
            }
        }
        return find;
    }

//    public void insertData(ConData newData){
//
//        boolean updated = false;
//
//        if (!updated) {
//            statistics.add(newData);
//        }
//        this.write(newData);
//    }


    public void write(ConData newData){

        try {
            Path filePath = Paths.get(FILE_PATH);
            List<String> fileContent = new ArrayList<>(Files.readAllLines(filePath, StandardCharsets.UTF_8));

            for (ConData row : statistics) {
                if (newData.url.equals(row.url)) {
                    newData.dataGot = row.dataGot + newData.dataGot;
                    newData.dataSend = row.dataSend + newData.dataSend;
                    newData.requests = ++row.requests;
                    break;
                }
            }

            boolean updated = false;
            for (int i = 0; i < fileContent.size(); i++) {
                if (fileContent.get(i).startsWith(newData.url)) {
                    System.out.println("updating");
                    fileContent.set(i, newData.toString());
                    updated = true;
                    break;
                }
            }
            if (!updated){
                newData.requests = 1;
                fileContent.add(newData.toString());
            }


            Files.write(filePath, fileContent, StandardCharsets.UTF_8);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

}