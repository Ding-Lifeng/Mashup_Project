import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.util.LinkedList;
import java.util.*;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        try {
            //键盘输入Categories
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            String[] target = input.split(",");

            // 读入csv文件
            String dir = "C:\\Users\\25761\\Desktop\\Java课设\\代码\\data\\";
            String apiCsvFile = "api.csv";
            String mashupCsvFile = "mashup.csv";
            CSVReader apiCsv = new CSVReader(new FileReader(dir + apiCsvFile));
            CSVReader mashupCsv = new CSVReader(new FileReader(dir + mashupCsvFile));
            Graph g = new Graph();  //建图

            // 跳过表头
            apiCsv.skip(1);
            mashupCsv.skip(1);

            // 读取API信息，建立图结点
            String[] data;
            while ((data = apiCsv.readNext()) != null) {
                LinkedList<String> list = new LinkedList<>();
                String api_name = data[1];
                // 获取api的主要种类和次要种类
                list.add(data[3]);
                String[] categories = data[5].split(",");
                Collections.addAll(list, categories);

                // 观察输出的API信息
//                System.out.print("[api_name]" + api_name + " [category] ");
//                for(String category : list){
//                    System.out.print(category + ",");
//                }
//                System.out.println();
                g.addVNode(api_name, list);
            }
            // 读取mashup信息,建立边和权值
            while ((data = mashupCsv.readNext()) != null)
            {
                String[] re_Apis = data[2].split(",");
                for(int i =0;i<re_Apis.length;i++)
                {
                    for(int j =i+1;j<re_Apis.length;j++)
                        g.addEdge(re_Apis[i],re_Apis[j]);
                }
            }
            // 清除无关结点
            g.clearVNode();

            // 根据潜在结点选出关键结点
            // 筛选潜在结点-具有target中的特征
            ArrayList<String> pro_apis = g.search_pro_Node(target);
            // 根据潜在结点选出关键结点
            Set<String> critical_apis = g.select_critical_apis(pro_apis, target);


            HashMap<String,Integer> ApiNameToNum = new HashMap<>(); //建立API由name到编号的映射关系
            Set<String> API_name = g.getAllNodeKeys();          //API的Name集合
            int i = 0;                                          //API编号初始化
            Iterator<String> iterator = API_name.iterator();
            while(iterator.hasNext()) {                 //判断是否还有元素可迭代
                ApiNameToNum.put(iterator.next(),i);    //将API及其对应的编号加入到映射关系中
                i++;                                    //编号+1
            }
            HashSet<Integer> CriticalNumber = new HashSet<>();
            if(critical_apis != null) {
                Iterator<String> CriticalIterator = critical_apis.iterator();
                while (CriticalIterator.hasNext()) {                 //判断是否还有元素可迭代
                    CriticalNumber.add(ApiNameToNum.get(CriticalIterator.next()));  //将对应的关键结点编号添加到关键编号集
                }
            }
            System.out.println(CriticalNumber);



        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }
}

