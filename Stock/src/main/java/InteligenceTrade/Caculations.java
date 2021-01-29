package InteligenceTrade;

import MainFunction.StockHistory;
import com.google.gson.*;
import netscape.javascript.JSObject;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import javax.swing.text.StyleContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Caculations {

    public static double RSV(String stockName, int index, timeInterval interval){
        String wyName;
        if (stockName.substring(0,2).equals("sh")){
            wyName = "0" + stockName.substring(2);
        }else {
            wyName = "1" + stockName.substring(2);
        }
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        Calendar initial = from;
        initial.add(Calendar.YEAR, -9 - index);
        File file = getFile(wyName, initial, to);
        double res = -1;
        try {
            switch (interval){
                case Month:
                    from.add(Calendar.MONTH, -9 - index);
                    to.add(Calendar.MONTH, -index);
                    break;
                case Day:
                    from.add(Calendar.DATE, -9 - index);
                    to.add(Calendar.DATE, -index);
                    break;
                case Week:
                    from.add(Calendar.WEEK_OF_YEAR, -9 - index);
                    to.add(Calendar.WEEK_OF_YEAR, -index);
                    break;
                case Hours:
                    from.add(Calendar.HOUR, -9 - index);
                    to.add(Calendar.HOUR, -index);
                    JsonArray array = getSinaPath(
                            "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=" + stockName +"&scale=60&ma=no&datalen=1023");

                   double highest = Collections.max(getJsonHigh(array, from, 9));
                   double lowest = Collections.min(getJsonLow(array, from, 9));
                   List<Double> closes = getJsonClose(array, from, 9);
                   double close = closes.get(closes.size() - 1);
                    res = (close - lowest) / (highest - lowest) * 100;
                    return res;
                case Half_Hour:
                    from.add(Calendar.MINUTE, (-9 - index) * 30);
                    to.add(Calendar.MINUTE, -index * 30);
                    array = getSinaPath(
                            "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=" + stockName +"&scale=30&ma=no&datalen=1023");

                    highest = Collections.max(getJsonHigh(array, from, 9));
                    lowest = Collections.min(getJsonLow(array, from, 9));
                    closes = getJsonClose(array, from, 9);
                    close = closes.get(closes.size() - 1);
                    res = (close - lowest) / (highest - lowest) * 100;
                    return res;

                case Quarter_Year:
                    from.add(Calendar.MONTH, (-9 - index) * 3);
                    to.add(Calendar.MONTH, -index * 3);
                    break;
                case Year:
                    from.add(Calendar.YEAR, -9 - index);
                    to.add(Calendar.YEAR, -index);
                    break;
            }
            List<Double> high = getFileHigh(file, from, to);
            List<Double> low = getFileLow(file, from, to);
            List<Double> closes = getFileclose(file, from, to);
            double close = closes.get(closes.size() - 1);
            double highest = Collections.max(high);

            double lowest = Collections.min(low);

             res = (close - lowest) / (highest - lowest) * 100;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static double SMA(List<Double> x, int n, int m){
        if(x.size() == 0 || n == 0){
            return 0;
        }
        if (n >= x.size()){
            return SMA(x, n - 1, m);
        }
        return x.get(n) * (m/n) + SMA(x, n - 1, m) *(n - m) / n;
    }

    public static double diff(String stockName, int small, int large, int offset, timeInterval interval){
        String wyName;
        if (stockName.substring(0,2).equals("sh")){
            wyName = "0" + stockName.substring(2);
        }else {
            wyName = "1" + stockName.substring(2);
        }
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        Calendar initial = from;
        initial.add(Calendar.YEAR, -large - offset);
        File file = getFile(wyName, initial, to);
        double res = -1;
        try {
            switch (interval){
                case Month:
                    from.add(Calendar.MONTH, -large - offset);
                    to.add(Calendar.MONTH, -offset);
                    break;
                case Day:
                    from.add(Calendar.DATE, -large - offset);
                    to.add(Calendar.DATE, -offset);
                    break;
                case Week:
                    from.add(Calendar.WEEK_OF_YEAR, -large - offset);
                    to.add(Calendar.WEEK_OF_YEAR, -offset);
                    break;
                case Hours:
                    from.add(Calendar.HOUR, -large - offset);
                    to.add(Calendar.HOUR, -offset);
                    JsonArray array = getSinaPath(
                            "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=" + stockName +"&scale=60&ma=no&datalen=1023");
                    List<Double> high = new ArrayList<Double>();
                    List<Double> low = new ArrayList<Double>();
                    List<Double> all = getJsonClose(array, from, 9);
                    for(int i = 0; i < large; i ++){
                        if(i < all.size()) {
                            if (i < small) {
                                low.add(all.get(i));
                            }
                            high.add(all.get(i));
                        }
                    }
                    res = SMA(low,small,1) - SMA(high, large, 1);
                    return res;
                case Half_Hour:
                    from.add(Calendar.MINUTE, (-large - offset) * 30);
                    to.add(Calendar.MINUTE, -offset * 30);
                    array = getSinaPath(
                            "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=" + stockName +"&scale=60&ma=no&datalen=1023");
                    high = new ArrayList<Double>();
                    low = new ArrayList<Double>();
                    all = getJsonClose(array, from, 9);
                    for(int i = 0; i < large; i ++){
                        if (i < small) {
                            low.add(all.get(i));
                        }
                        high.add(all.get(i));
                    }
                    res = SMA(low,small,1) - SMA(high, large, 1);
                    return res;
                case Quarter_Year:
                    from.add(Calendar.MONTH, (-large - offset) * 3);
                    to.add(Calendar.MONTH, -offset * 3);
                   break;
                case Year:
                    from.add(Calendar.YEAR, -large - offset);
                    to.add(Calendar.YEAR, -offset);
                    break;
            }
            List<Double> high = getDiffclose(file, from, to, interval);
            List<Double> low = new ArrayList<Double>();
            for(int i = 0; i < small; i ++){
                if (i < high.size()) {
                    low.add(high.get(i));
                }else {
                    break;
                }
            }
            res = SMA(low,12,1) - SMA(high, 26, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static double pb(String stock, timeInterval interval, int k, int offset){

        return (SMA(getClose(stock, interval, k,0), k, 1) +mv(getClose(stock, interval,2 * k,0)) + mv(getClose(stock, interval, k * 3,0))) / 3;
    }

    public static double mv(List<Double> close){
        double total = 0;
        for(int i = 0; i < close.size(); i++){
            total += close.get(i);
        }
        return total / close.size();
    }

    public static double A01(String stockName,int offset, timeInterval interval){
        String wyName;
        if (stockName.substring(0,2).equals("sh")){
            wyName = "0" + stockName.substring(2);
        }else {
            wyName = "1" + stockName.substring(2);
        }
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        Calendar initial = from;
        initial.add(Calendar.MONTH, (-22-offset) * 3);
        File file = getFile(wyName, initial, to);
        double res = -1;
        switch (interval){
           case Quarter_Year:
                from.add(Calendar.MONTH, (-22-offset) * 3);
                to.add(Calendar.MONTH, -offset * 3);

            case Month:
                from.add(Calendar.MONTH, -22-offset);
                to.add(Calendar.MONTH, -offset);
            case Week:
                from.add(Calendar.WEEK_OF_YEAR, -22-offset);
                to.add(Calendar.WEEK_OF_YEAR, -offset);
            case Hours:
                from.add(Calendar.HOUR, -22-offset);
                to.add(Calendar.HOUR, -offset);
                JsonArray array = getSinaPath(
                        "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=" + stockName +"&scale=60&ma=no&datalen=1023");
                List<Double> ema7 = new ArrayList<Double>();
                List<Double> ema21 = new ArrayList<Double>();
                List<Double> close = getJsonClose(array, from, 22);
                for(int i = 1; i >=0; i--){
                    ema7.add(SMA(close.subList(close.size() - 7 -i, close.size() - i), 7, 1));
                    ema21.add(SMA(close.subList(close.size() - 21 - i, close.size() - i), 21, 1));
                }
                res = SMA(ema7, 2, 1) - SMA(ema21, 2, 1);
                return res;
            case Day:
                from.add(Calendar.DATE, -22-offset);
                to.add(Calendar.DATE, -offset);
            case Half_Hour:
                from.add(Calendar.MINUTE, -(22 + offset) * 30);
                to.add(Calendar.MINUTE, -offset * 30);
                array = getSinaPath(
                        "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=" + stockName +"&scale=60&ma=no&datalen=1023");
                ema7 = new ArrayList<Double>();
                ema21 = new ArrayList<Double>();
                close = getJsonClose(array, from, 22);
                for(int i = 1; i >=0; i--){
                    ema7.add(SMA(close.subList(close.size() - 7 -i, close.size() - i), 7, 1));
                    ema21.add(SMA(close.subList(close.size() - 21 - i, close.size() - i), 21, 1));
                }
                res = SMA(ema7, 2, 1) - SMA(ema21, 2, 1);
                return res;
        }
        List<Double> ema7 = new ArrayList<Double>();
        List<Double> ema21 = new ArrayList<Double>();
        List<Double> close = getDiffclose(file, from, to, interval);
        for(int i = 1; i >=0; i--){
            ema7.add(SMA(close.subList(close.size() - 7 -i, close.size() - i), 7, 1));
            ema21.add(SMA(close.subList(close.size() - 21 - i, close.size() - i), 21, 1));
        }
        res = SMA(ema7, 2, 1) - SMA(ema21, 2, 1);
        return res;
    }

    public static double B01(String stockName, int offset, timeInterval interval){
       return  0.66 * A01(stockName, 1 + offset, interval) + 0.33 * A01(stockName, offset, interval);
    }

    public static boolean ZXZ1(String stockName, timeInterval interval){
        return B01(stockName, 0, interval) < 0 && B01(stockName, 0, interval) > B01(stockName, 1, interval);
    }

    public static boolean ZXZ2(String stockName, timeInterval interval){
        return B01(stockName, 0, interval) > 0 && B01(stockName, 0, interval) > B01(stockName, 1, interval);
    }

    public static boolean ZXF1(String stockName, timeInterval interval){
        return B01(stockName, 0, interval) > 0 && B01(stockName, 0, interval) < B01(stockName, 1, interval);
    }

    public static boolean ZXF2(String stockName, timeInterval interval){
        return B01(stockName, 0, interval) < 0 && B01(stockName, 0, interval) < B01(stockName, 1, interval);
    }

    public static boolean check1(String stock, timeInterval interval){
        List<Double> k = new ArrayList<Double>();
        for(int i = 2; i >= 0; i-- ) {
            List<Double> rsv = new ArrayList<Double>();
            for (int j = 2 + i; j >= i; j--) {
                rsv.add(RSV(stock, j, interval));
            }
            k.add(SMA(rsv,3,1));
        }
        double d = SMA(k, 3, 1);
        List<Double> diff = new ArrayList<Double>();
        for(int i = 8; i >= 0; i--){
            diff.add(diff(stock, 12, 26, i, interval));
        }
        double dea = SMA(diff, 9, 1);
        double pb13 = pb(stock, interval,13, 0);
        if(k.get(k.size() - 1) > d && (diff.get(diff.size() - 1)) > 0 || getClose(stock, interval,1, 0).get(0) > pb13){
            return true;
        }
        return false;
    }


    public static boolean check2(String stock, timeInterval interval){
        List<Double> k = new ArrayList<Double>();
        for(int i = 2; i >= 0; i-- ) {
            List<Double> rsv = new ArrayList<Double>();
            for (int j = 2 + i; j >= i; j--) {
                rsv.add(RSV(stock, j, interval));
            }
            k.add(SMA(rsv,3,1));
        }
        double d = SMA(k, 3, 1);
        List<Double> diff = new ArrayList<Double>();
        for(int i = 8; i >= 0; i--){
            diff.add(diff(stock, 12, 26, i,interval));
        }
        double dea = SMA(diff, 9, 1);
        double pb13 = pb(stock, interval,13, 0);
        if((diff.get(diff.size() - 1) > dea || ZXZ2(stock, interval) || ZXZ1(stock, interval)) && (k.get(k.size() - 1) > d) &&(diff.get(diff.size() - 1)) > 0 || getClose(stock, interval,1,0).get(0) > pb13){
            return true;
        }
        return false;
    }

    public static boolean check3(String stock, timeInterval interval){
        List<Double> k = new ArrayList<Double>();
        for(int i = 2; i >= 0; i-- ) {
            List<Double> rsv = new ArrayList<Double>();
            for (int j = 2 + i; j >= i; j--) {
                rsv.add(RSV(stock, j, interval));
            }
            k.add(SMA(rsv,3,1));
        }
        double d = SMA(k, 3, 1);
        List<Double> diff = new ArrayList<Double>();
        for(int i = 8; i >= 0; i--){
            diff.add(diff(stock, 12, 26, i,interval));
        }
        double dea = SMA(diff, 9, 1);
        double pb13 = pb(stock, interval,13, 0);
        if((diff.get(diff.size() - 1) > dea || ZXZ2(stock, interval) || ZXZ1(stock, interval)) && (k.get(k.size() - 1) > d) &&(diff.get(diff.size() - 1)) > 0 || getClose(stock, interval,1,0).get(0) > pb13){
            return true;
        }
        return false;
    }

    public static boolean check4(String stock, timeInterval interval){
        double pb13 = pb(stock, interval,13, 0);
        double pb24 = pb(stock, interval, 24, 0);
         if (pb13 > pb24){
             return true;
         }
        return false;
    }

    //3 fen sheng ji 10 fen
    public static boolean threeToTen(String stock){
        if(check1(stock, timeInterval.Week) && check2(stock, timeInterval.Day) && check3(stock, timeInterval.Hours) && check4(stock, timeInterval.Half_Hour)){
            return true;
        }
        return false;
    }

    public static boolean tenToThirty(String stock){
        if(check1(stock, timeInterval.Month) && check2(stock, timeInterval.Week) && check3(stock, timeInterval.Day) && check4(stock, timeInterval.Hours)){
            return  true;
        }
        return false;
    }

    public static boolean thirtyToHour(String stock){
        if(check1(stock, timeInterval.Quarter_Year) && check2(stock, timeInterval.Month) && check3(stock, timeInterval.Week) && check4(stock, timeInterval.Day)){
            return true;
        }
        return false;
    }

    public static boolean hourToDay(String stock){
        if (check1(stock,timeInterval.Year) && check2(stock, timeInterval.Quarter_Year) && check3(stock, timeInterval.Month) && check4(stock, timeInterval.Week)){
            return  true;
        }
        return false;
    }
    //stock sell
    public static boolean sell3(String stock){
        if(pb(stock, timeInterval.Three_Min, 13, 1)  > pb(stock, timeInterval.Three_Min, 24, 1) && pb(stock, timeInterval.Three_Min, 13, 0) < pb(stock, timeInterval.Three_Min, 24, 0)){
            return true;
        }
        return false;
    }
    public static boolean sell10(String stock){
        if(pb(stock, timeInterval.Ten_Min, 13, 1)  > pb(stock, timeInterval.Ten_Min, 24, 1) && pb(stock, timeInterval.Ten_Min, 13, 0) < pb(stock, timeInterval.Ten_Min, 24, 0)){
            return true;
        }
        return false;
    }
    public static boolean sell30(String stock){
        if(pb(stock, timeInterval.Half_Hour, 13, 1)  > pb(stock, timeInterval.Half_Hour, 24, 1) && pb(stock, timeInterval.Half_Hour, 13, 0) < pb(stock, timeInterval.Half_Hour, 24, 0)){
            return true;
        }
        return false;
    }
    public static boolean sell60(String stock){
        if(pb(stock, timeInterval.Hours, 13, 1)  > pb(stock, timeInterval.Hours, 24, 1) && pb(stock, timeInterval.Hours, 13, 0) < pb(stock, timeInterval.Hours, 24, 0)){
            return true;
        }
        return false;
    }
    public static boolean sellday(String stock){
        if(pb(stock, timeInterval.Day, 13, 1)  > pb(stock, timeInterval.Day, 24, 1) && pb(stock, timeInterval.Day, 13, 0) < pb(stock, timeInterval.Day, 24, 0)){
            return true;
        }
        return false;
    }


    public static List<Double> getClose(String stockName, timeInterval interval, int k, int offset){
        String wyName;
        if (stockName.substring(0,2).equals("sh")){
            wyName = "0" + stockName.substring(2);
        }else {
            wyName = "1" + stockName.substring(2);
        }
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        Calendar initial = from;
        initial.add(Calendar.MONTH, - offset - k);
        File file = getFile(wyName, initial, to);
        switch (interval){
            case Month:
                from.add(Calendar.MONTH, - offset - k);
                to.add(Calendar.MONTH, - offset);

            case Week:
                from.add(Calendar.WEEK_OF_YEAR, -offset - k);
                to.add(Calendar.WEEK_OF_YEAR, - offset);
            case Day:
                from.add(Calendar.DATE, -offset- k);
                to.add(Calendar.DATE, - offset);
            case Half_Hour:
                from.add(Calendar.MINUTE, -(offset - k) * 30);
                to.add(Calendar.MINUTE, - offset * 30);
               JsonArray array = getSinaPath(
                        "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=" + stockName +"&scale=60&ma=no&datalen=1023");
                return getJsonClose(array,from, k);
               case Minute:
                from.add(Calendar.MINUTE, -offset- k);
                to.add(Calendar.MINUTE, - offset);
               JsonObject obj = getWYPath(
                        "http://img1.money.126.net/data/hs/time/today/"+ wyName +".json");
               List<Double> result = getJsonMinClose(obj, from, k);
                return getJsonMinClose(obj, from, k);
            case Three_Min:
                from.add(Calendar.MINUTE, (-offset - k)* 3);
                to.add(Calendar.MINUTE, - offset * 3);
                obj = getWYPath(
                        "http://img1.money.126.net/data/hs/time/today/"+ wyName +".json");
                List<Double> list = getJsonMinClose(obj, from, 3 * k);
                List<Double> res = new ArrayList<Double>();
                for(int i = 1 ; i * 3 < res.size(); i++){
                    res.add(list.get(i * 3 - 1));
                }
                return res;
            case Hours:
                from.add(Calendar.HOUR, -offset- k);
                to.add(Calendar.HOUR, - offset);
                array = getSinaPath(
                        "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=" + stockName +"&scale=60&ma=no&datalen=1023");
                return getJsonClose(array,from, k);
            case Ten_Min:
                from.add(Calendar.MINUTE, (-offset - k)* 10);
                to.add(Calendar.MINUTE, - offset * 10);
                obj = getWYPath(
                        "http://img1.money.126.net/data/hs/time/today/"+ wyName +".json");
                 list = getJsonMinClose(obj, from, 10 * k);
                 res = new ArrayList<Double>();
                for(int i = 1 ; i * 10 < res.size(); i++){
                    res.add(list.get(i * 10 - 1));
                }
                return res;

        }
        List<Double> closes = getDiffclose(file, from, to, interval);
        Collections.reverse(closes);
        return closes;
    }

    public static List<Double> getJsonHigh(JsonArray array, Calendar from, int count){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date_from = from.getTime();
        String date = format.format(date_from);
        List<Double> list = new ArrayList<Double>();
        int i;
        for(i = array.size() - 1; i > 0; i--){
           JsonObject obj =  array.get(i).getAsJsonObject();
           String day = obj.get("day").getAsString();
           if(day.equals(date)){
               break;
           }
        }
        for(int j = i; j < i + count; j++){
            if (j < array.size()) {
                list.add(array.get(j).getAsJsonObject().get("high").getAsDouble());
            }else {
                break;
            }
        }
        return list;


    }

    public static List<Double> getJsonLow(JsonArray array, Calendar from, int count){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date_from = from.getTime();
        String date = format.format(date_from);
        List<Double> list = new ArrayList<Double>();
        int i;
        for(i = array.size() - 1; i > 0; i--){
            JsonObject obj =  array.get(i).getAsJsonObject();
            String day = obj.get("day").getAsString();
            if(day.equals(date)){
                break;
            }
        }
        for(int j = i; j < i + count; j++){
            if (j < array.size()) {
                list.add(array.get(j).getAsJsonObject().get("low").getAsDouble());
            }else {
                break;
            }
        }
        return list;

    }

    public static List<Double> getJsonClose(JsonArray array, Calendar from, int count){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date_from = from.getTime();
        String date = format.format(date_from);
        List<Double> list = new ArrayList<Double>();
        int i;
        for(i = array.size() - 1; i > 0; i--){
            JsonObject obj =  array.get(i).getAsJsonObject();
            String day = obj.get("day").getAsString();
            if(day.equals(date)){
                break;
            }
        }
        for(int j = i; j < i + count; j++){
            if (j < array.size()) {
                list.add(array.get(j).getAsJsonObject().get("close").getAsDouble());
            }else {
                break;
            }
        }
        return list;

    }
    public static List<Double> getJsonMinClose(JsonObject object, Calendar from, int count){
        DateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");
        Date date_from = from.getTime();
        String Bigdate = format.format(date_from);
        String date = Bigdate.substring(0, 8);
        String today = format.format(new Date()).substring(0,8);
//        if(!date.equals(today)){
//            System.out.println("need get yesterday's data");
//            return null;
//        }
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
            if (j < array.size()) {
                list.add(array.get(j).getAsJsonArray().get(1).getAsDouble());
            }else {
                break;
            }
        }
        return list;

    }

    public static List<Double> getFileHigh(File file, Calendar from, Calendar to) {
        List<Double> high = new ArrayList<Double>();
        Reader in = null;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date_from = format.format(from.getTime());
        String date_to = format.format(to.getTime());

        try {
            in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            boolean flag = false;
            for (CSVRecord record : records) {
                String theDate = record.get(0);
                if(theDate.equals(date_to)){
                    flag = true;
                }
                if (true){
                    high.add(Double.parseDouble(record.get(4)));
                }
                if (theDate.equals(date_from)){
                    break;
                }
            }

            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.reverse(high);
        return high;
    }

    public static List<Double> getFileLow(File file, Calendar from, Calendar to) {
        List<Double> low = new ArrayList<Double>();
        Reader in = null;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date_from = format.format(from.getTime());
        String date_to = format.format(to.getTime());

        try {
            in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            boolean flag = false;
            for (CSVRecord record : records) {
                String theDate = record.get(0);
                if(theDate.equals(date_to)){
                    flag = true;
                }
                if (true){
                    low.add(Double.parseDouble(record.get(5)));
                }
                if (theDate.equals(date_from)){
                    break;
                }
            }

            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.reverse(low);
        return low;
    }

    public static List<Double> getFileclose(File file, Calendar from, Calendar to) {
        List<Double> close = new ArrayList<Double>();
        Reader in = null;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date_from = format.format(from.getTime());
        String date_to = format.format(to.getTime());

        try {
            in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            boolean flag = false;
            for (CSVRecord record : records) {
                String theDate = record.get(0);
                if(theDate.equals(date_to)){
                    flag = true;
                }
                if (true){
                    close.add(Double.parseDouble(record.get(3)));
                }
                if (theDate.equals(date_from)){
                    break;
                }
            }

            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.reverse(close);
        return close;
    }

    public static List<Double> getDiffclose(File file, Calendar from, Calendar to, timeInterval interval) {
        List<Double> close = new ArrayList<Double>();
        Reader in = null;
        DateFormat ft = null;
        boolean isQuater = false;
        boolean isWeek = false;
        switch (interval){
            case Year:
                ft = new SimpleDateFormat("yyyy");
                break;
            case Quarter_Year:
                ft = new SimpleDateFormat("yyyy");
                isQuater = true;
                break;
            case Month:
                ft = new SimpleDateFormat("yyyyMM");
                break;
            case Week:
                ft = new SimpleDateFormat("yyyyMMdd");
                isWeek = true;
                break;
            case Day:
                ft = new SimpleDateFormat("yyyyMMdd");
                break;
        }
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date_from = format.format(from.getTime());
        String date_to = format.format(to.getTime());

        try {
            in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            String pre = null;
            for (CSVRecord record : records) {

                Date theDay = format.parse(record.get(0));
                String theDate = ft.format(theDay);
                if (!theDate.equals(pre)){
                    close.add(Double.parseDouble(record.get(3)));
                    pre = theDate;
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (isQuater){
            List<Double> newClose = new ArrayList<Double>();
            for (int i = 0; i < close.size(); i++){
                if (i % 3 == 0){
                    newClose.add(close.get(i));
                }
            }
            close = newClose;
        }
        if (isWeek){
            List<Double> newClose = new ArrayList<Double>();
            for (int i = 0; i < close.size(); i++){
                if (i % 5 == 0){
                    newClose.add(close.get(i));
                }
            }
            close = newClose;
        }
        return close;
    }

    public static File getFile(String stockName, Calendar from, Calendar to){

        DateFormat ft =new  SimpleDateFormat("yyyyMMdd");
        String start = ft.format(from.getTime());
        String end = ft.format(to.getTime());
        //这里设置自己的保存文件路径
        String filepath = "/Users/coolan/Downloads/data3";
        String url = "http://quotes.money.163.com/service/chddata.html?code="+ stockName +"&start="+ start +"&end="+ end +"&fields=TCLOSE;HIGH;LOW";
        File file = saveUrlAs(url, filepath, "GET", start, end, stockName);
        return file;
    }

    public static File saveUrlAs(String url, String filePath, String method, String start, String end, String stockName){
        //System.out.println("fileName---->"+filePath);
        //创建不同的文件夹目录
        File file=new File(filePath);
        //判断文件夹是否存在
        if (!file.exists())
        {
            //如果文件夹不存在，则创建新的的文件夹
            file.mkdirs();
        }
        FileOutputStream fileOut = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try
        {
            // 建立链接
            URL httpUrl=new URL(url);
            conn=(HttpURLConnection) httpUrl.openConnection();
            //以Post方式提交表单，默认get方式
            conn.setRequestMethod(method);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            // post方式不能使用缓存
            conn.setUseCaches(false);
            //连接指定的资源
            conn.connect();
            //获取网络输入流
            inputStream=conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            //判断文件的保存路径后面是否以/结尾
            if (!filePath.endsWith("/")) {

                filePath += "/";

            }
            //写入到文件（注意文件保存路径的后面一定要加上文件的名称）
            fileOut = new FileOutputStream(filePath+ stockName + start + "_" + end +".csv");
            BufferedOutputStream bos = new BufferedOutputStream(fileOut);

            byte[] buf = new byte[4096];
            int length = bis.read(buf);
            //保存文件
            while(length != -1)
            {
                bos.write(buf, 0, length);
                length = bis.read(buf);
            }
            bos.close();
            bis.close();
            conn.disconnect();
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("抛出异常！！");
        }
        file = new File(filePath+ stockName + start + "_" + end +".csv");
        return file;

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
                Gson gson = new Gson();
//                JsonReader jsonReader = new JsonReader(new StringReader(res));
//                jsonReader.setLenient(true);
                object =gson.fromJson(res, JsonArray.class);
                //object = (JsonObject)JsonParser.parseString(res);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return object;
    }

    public static JsonObject getWYPath(String requestUrl){

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


enum timeInterval{
   Year, Quarter_Year, Month, Week, Day, Hours, Half_Hour, Minute, Three_Min, Ten_Min
}