package MainFunction;

import com.google.gson.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StockHistory {
    public static void getHistory(String ticker, Date date){
        Calendar from = Calendar.getInstance();
        from.setTime(date);
        from.add(Calendar.MONTH, -3);
        Calendar to = Calendar.getInstance();
        to.setTime(date);
        try {
            Stock stock = YahooFinance.get(ticker, from, to, Interval.DAILY);
            if(stock == null){
                return;
            }
            List<HistoricalQuote> historicalQuotes = stock.getHistory();
            SimpleDateFormat ft = new SimpleDateFormat("MM-dd-yyyy");
            //remember to modify the path to a general one
            File file = new File("/Users/coolan/Downloads/data2/"+ ticker + "_" + ft.format(date) +".csv");

            FileWriter fileWriter = new FileWriter(file);
            CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("index", "date", "open",
                    "close", "high", "low", "volume"));
            for(int i = 0; i < historicalQuotes.size(); i++){
                List record = new ArrayList();
                HistoricalQuote cur = historicalQuotes.get(i);
                record.add(i);
                record.add(ft.format(cur.getDate().getTime()));
                record.add(cur.getOpen());
                record.add(cur.getClose());
                record.add(cur.getHigh());
                record.add(cur.getLow());
                record.add(cur.getVolume());
                csvPrinter.printRecord(record);
            }
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateFile(File file){
            Reader in = null;
            try {
                in = new FileReader(file);
                Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
                for (CSVRecord record : records) {
                    String ticker = record.get(0);
                    String PDUFA_date = record.get(2);
                    DateFormat ft = new SimpleDateFormat("MM/dd/yyyy");
                    Date date = ft.parse(PDUFA_date);
                    for(int i = 0; i < ticker.length(); i++){
                        if(ticker.charAt(i) == ';'){
                            getHistory(ticker.substring(0, i),date);
                            getHistory(ticker.substring(i + 1), date);
                            break;
                        }
                    }
                    getHistory(ticker, date);
                }
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

    }

    public static JsonObject getXpath(String requestUrl){
        String res="";
        JsonObject object = null;
        StringBuffer buffer = new StringBuffer();
        try{
            URL url = new URL(requestUrl);
            HttpURLConnection urlCon= (HttpURLConnection)url.openConnection();
            if(200==urlCon.getResponseCode()){
                InputStream is = urlCon.getInputStream();
                InputStreamReader isr = new InputStreamReader(is,"utf-8");
                BufferedReader br = new BufferedReader(isr);

                String str = null;
                while((str = br.readLine())!=null){
                    buffer.append(str);
                }
                br.close();
                isr.close();
                is.close();
                res = buffer.toString();
                Gson gson = new Gson();
//                JsonReader jsonReader = new JsonReader(new StringReader(res));
//                jsonReader.setLenient(true);
                object =gson.fromJson(res, JsonObject.class);
                //object = (JsonObject)JsonParser.parseString(res);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return object;
    }

    //{"data":[{"id":"TXG","attributes":{"shortIntPctFloat":5.6134,"marketCap":9427904437.0}}]}

    public static JsonElement[]  dealJson(JsonObject jsonObject){
        JsonArray data = jsonObject.get("data").getAsJsonArray();
        JsonObject object = data.get(0).getAsJsonObject();
        JsonElement attr = object.get("attributes");
        JsonElement shortInt = attr.getAsJsonObject().get("shortIntPctFloat");
        JsonElement marketCap = attr.getAsJsonObject().get("marketCap");
        return new JsonElement[]{shortInt, marketCap};
    }

    public static void findIntAndMkt(File inputFile, File outputFile) {
        Reader in = null;
        FileWriter fileWriter = null;
        try {
            in = new FileReader(inputFile);
            fileWriter = new FileWriter(outputFile);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("Ticker", "Short Interest", "Market Cap"));
            HashSet<String> set = new HashSet<String>();
            for (CSVRecord record : records) {
                String ticker = record.get(0);
                String ticker2 = null;
                if (set.contains(ticker)) {
                    continue;
                }
                set.add(ticker);
                boolean flag = false;
                for (int i = 0; i < ticker.length(); i++) {
                    if (ticker.charAt(i) == ';') {
                        ticker2 = ticker.substring(i + 1);
                        ticker = ticker.substring(0, i);
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    JsonObject object = StockHistory.getXpath("https://seekingalpha.com/api/v3/symbol_data?fields[]=shortIntPctFloat&fields[]=marketCap&fields[]&slugs=" + ticker);
                    JsonElement[] shortIntAndMkt = dealJson(object);
                    if(shortIntAndMkt[0].toString().equals("null")){
                        continue;
                    }
                    List writeRecord = new ArrayList();
                    writeRecord.add(ticker);
                    writeRecord.add(shortIntAndMkt[0].toString() + "%");
                    writeRecord.add(shortIntAndMkt[1].toString());
                    csvPrinter.printRecord(writeRecord);
                    object = StockHistory.getXpath("https://seekingalpha.com/api/v3/symbol_data?fields[]=shortIntPctFloat&fields[]=marketCap&fields[]&slugs=" + ticker2);
                    shortIntAndMkt = dealJson(object);
                    if(shortIntAndMkt[0].toString().equals("null")){
                        continue;
                    }
                    writeRecord = new ArrayList();
                    writeRecord.add(ticker2);
                    writeRecord.add(shortIntAndMkt[0].toString() + "%");
                    writeRecord.add(shortIntAndMkt[1].toString());
                    csvPrinter.printRecord(writeRecord);
                } else {
                    JsonObject object = StockHistory.getXpath("https://seekingalpha.com/api/v3/symbol_data?fields[]=shortIntPctFloat&fields[]=marketCap&fields[]&slugs=" + ticker);
                    JsonElement[] shortIntAndMkt = dealJson(object);
                    if (shortIntAndMkt[0].toString().equals("null")) {
                        continue;
                    }
                    List writeRecord = new ArrayList();
                    writeRecord.add(ticker);
                    writeRecord.add(shortIntAndMkt[0].toString() + "%");
                    writeRecord.add(shortIntAndMkt[1].toString());
                    csvPrinter.printRecord(writeRecord);
                }
            }
            in.close();
            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void handleDiffPlatform(File file, String filePath){
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            int input;
            in = new FileInputStream(file);
            out = new FileOutputStream(filePath);
            while ((input = in.read()) != -1){
                if(input != '\r'){
                    if(input == '\n'){
                        System.out.println("n");
                    }
                    out.write(input);
                }else {
                    System.out.println("r");
                }
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void valid(String filePath) {
        FileInputStream in = null;
        try {
            int input;
            in = new FileInputStream(filePath);
            while ((input = in.read()) != -1) {
                if (input == '\r') {
                    System.out.println("r");
                } else if (input == '\n') {
                    System.out.println('n');
                }
            }
                in.close();

            } catch(IOException e){
                e.printStackTrace();
            }
        }

}
