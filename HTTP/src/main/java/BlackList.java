import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class BlackList {
    BlackList(){ getBlackList(); }

    private LinkedList<String> blackList = new LinkedList<>();


    public LinkedList<String> getBlackList() {
        String blackListFile = "E:\\STUDIA\\ITI\\Semestr_1\\SINT\\IdeaProjects\\HTTP\\src\\main\\resources\\blacklist.txt";
        String line;
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(blackListFile));
            while ((line = br.readLine()) != null) {
                if (!blackList.contains(line)){
                    blackList.add(line);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blackList;
    }

    public boolean contains(String urlToCheck){
        System.out.println("Url to check: " + urlToCheck);

        for (String url : blackList){
            System.out.println("Url on the list: " + url);
            if (urlToCheck.startsWith(url)){
                return true;
            }
        }
        return false;
    }
}