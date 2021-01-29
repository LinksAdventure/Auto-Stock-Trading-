package MainFunction;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        // write your code here
        //File file = new File("/Users/coolan/Downloads/lab/txg01012020to06062020.csv");
        //List<Double> closePrice = Functions.getClosePrice(file);
//        List<Double> closePrice = Functions.getClosePrice("TSLA", 1);
//        boolean[] hold = Functions.maTrade(closePrice, 5, 10);
//        boolean[] keepHold = new boolean[hold.length];
//        Arrays.fill(keepHold, true);
//        double[] equity = Functions.price2Invest(closePrice,hold);
//        double[] equityOfKeep = Functions.price2Invest(closePrice, keepHold);
//        double yearVol = Functions.yearVol(equity);
//        double yearVolOfKeep = Functions.yearVol(equityOfKeep);
//        double maxDrawDown = Functions.maxDrawdown(equity);
//        double maxDrawDownOfKeep = Functions.maxDrawdown(equityOfKeep);
//        double yearReturn = Functions.yearReturn(equity);
//        double yearReturnOfKeep = Functions.yearReturn(equityOfKeep);
//        System.out.println("equity :" + Arrays.toString(equity));
//        System.out.println("equity of keep" + Arrays.toString(equityOfKeep));
//        System.out.println("trade stratege :" + Arrays.toString(hold));
//        System.out.println("yearVol :" + yearVol);
//        System.out.println("year return :" + yearReturn);
//        System.out.println("max Drawdown :" + maxDrawDown);
//        System.out.println(maxDrawDownOfKeep);
//        System.out.println(yearReturnOfKeep);
//        System.out.println(yearVolOfKeep);
//        File file = new File("/Users/coolan/Downloads/lab/TimeList.csv");
//        StockHistory.generateFile(file);
//        File file1 = new File("/Users/coolan/Downloads/lab/TimeList.csv");
//        File file2 = new File("/Users/coolan/Downloads/lab/ShortIntAndMktCap.csv");
//        StockHistory.findIntAndMkt(file1, file2);
//        File file = new File("/Users/coolan/Downloads/lab/ShortIntAndMktCap.csv");
//        StockHistory.handleDiffPlatform(file, "/Users/coolan/Downloads/lab/ShortIntAndMktCap_win.csv");
//        StockHistory.valid("/Users/coolan/Downloads/lab/ShortIntAndMktCap_win.csv");

       JsonObject object = getXpath(
                "http://img1.money.126.net/data/hs/time/today/0688069.json");

       System.out.println(object);
//        JsonArray array = getSinaPath("http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sz002095&scale=5&ma=no&datalen=1023");
//        System.out.println(array);
    }

    public static JsonArray getSinaPath(String requestUrl){
        String res="";
        JsonArray object = null;
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
                object = JsonParser.parseString(res).getAsJsonArray();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return object;
    }

    public static List<Double> getJsonMinClose(JsonObject object, Calendar from, int count){
        DateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm:ss");
        Date date_from = from.getTime();
        String Bigdate = format.format(date_from);
        String date = Bigdate.substring(0, 8);
        String today = format.format(new Date()).substring(0,8);
        if(!date.equals(today)){
            System.out.println("need get yesterday's data");
            return null;
        }
        String time = Bigdate.substring(8);
        List<Double> list = new ArrayList<Double>();
        JsonArray array = object.get("data").getAsJsonArray();
        int i;
        for(i = 0; i < array.size(); i++){
            String curTime = array.get(i).getAsJsonArray().get(0).getAsString();
            if(time.equals(curTime)){
                break;
            }
        }
        for(int j = i ; j < i + count; j++){
            list.add(array.get(j).getAsJsonArray().get(1).getAsDouble());
        }
        return list;

    }

    public static JsonObject getXpath(String requestUrl){

        OkHttpClient client = new OkHttpClient().newBuilder().connectionPool(new ConnectionPool(0, 5, TimeUnit.SECONDS)).readTimeout(10, TimeUnit.SECONDS).build();
        JsonObject object = null;
       Request request = new Request.Builder()
                .url(requestUrl)
                .get()
                .build();

        Response response = null ;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                 object = JsonParser.parseString(response.body().string()).getAsJsonObject();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return object;
    }
}