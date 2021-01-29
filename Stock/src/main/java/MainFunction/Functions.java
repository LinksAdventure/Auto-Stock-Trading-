package MainFunction;


import org.apache.commons.csv.*;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Functions {

    //this method is used for get stock's close value from yahoofinance, return history close value of time period.
    public static List<Double> getClosePrice(String name, int timePeriod){
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -timePeriod);
        List<Double> closePrice = new ArrayList<Double>();
        try {
            Stock tesla = YahooFinance.get(name, from, to, Interval.WEEKLY);
            for (HistoricalQuote quote : tesla.getHistory()) {

                closePrice.add(quote.getClose().doubleValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return closePrice;
    }
    //this method is used for get stock's close value from csv file, return the whole column value.
    public static List<Double> getClosePrice(File file) {
        List<Double> closePrice = new ArrayList<Double>();
        Reader in = null;
        try {
            in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                String price = record.get("TXG.Close");
                closePrice.add(Double.parseDouble(price));
            }

            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return closePrice;
    }

    // this method is used for calculate  annualized volatility, and this one need input a daily scale
    public static double yearVol(double[] val, int dailyScale){
        double[] dailyChange = new double[val.length - 1];
        for(int i = 1; i < val.length; i++){
            double change = (val[i] - val[i - 1]) / val[i - 1];
            dailyChange[i - 1] = change;
        }
        double standardDev = calStandardDev(dailyChange);
        return standardDev * Math.sqrt(dailyScale);
    }


    // this method is overload of above one, also used for calculate  annualized volatility, and this one assume
    // daily scale default as 252
    public static double yearVol(double[] val){
        double[] dailyChange = new double[val.length - 1];
        for(int i = 1; i < val.length; i++){
            double change = (val[i] - val[i - 1]) / val[i - 1];
            dailyChange[i - 1] = change;
        }
        double standardDev = calStandardDev(dailyChange);
        return standardDev * Math.sqrt(252);
    }


    //this method is used to calculate standard deviation which will  help us calculate annualized volatility
    public static double calStandardDev(double[] sd) {

        double sum = 0;
        double newSum = 0;

        for (int i = 0; i < sd.length; i++) {
            sum = sum + sd[i];
        }
        double mean = (sum) / (sd.length);

        for (int j = 0; j < sd.length; j++) {
            // put the calculation right in there
            newSum = newSum + ((sd[j] - mean) * (sd[j] - mean));
        }
        double squaredDiffMean = (newSum) / (sd.length);
        double standardDev = (Math.sqrt(squaredDiffMean));

        return standardDev;
    }

    //this method is used for calculate moving average, set element of return array as -1 when scanned elements
    // is less than K(MA.K)
    public static double[] movingAverage(List<Double> val, int k){
        double[] res = new double[val.size()];
        double total = 0;
        for (int i = 0; i < res.length; i++){
            total += val.get(i);
            if( i < k - 1){
                res[i] = -1;
            }else if(i == k - 1){
                res[i] = total / k;
             }else{
                total -= val.get(i - k);
                res[i] = total / k;
            }
        }
        return  res;
    }

    //this function is  used to decide trade strategy, return an array which 0 means not hold stock and 1 means hold
    //the input parameter maSlow longer period of moving average and maFast mean shorter period of moving average
    //price is stock's price over a period
    public static boolean[] maTrade(List<Double> price, int maFast, int maSlow){
        double[] MA_Fast = movingAverage(price, maFast);
        System.out.println(Arrays.toString(MA_Fast));
        double[] MA_Slow = movingAverage(price, maSlow);
        System.out.println(Arrays.toString(MA_Slow));
        boolean[] ifHolding = new boolean[price.size()];
        for (int i = 0; i < ifHolding.length; i++){
            if(i == 0 || MA_Fast[i - 1] == -1 || MA_Slow[i - 1] == -1){
                ifHolding[i] = false;
            }else if(MA_Fast[i - 1] > MA_Slow[i - 1]){
                ifHolding[i] = true;
            }else if(MA_Fast[i - 1] == MA_Slow[i - 1]){
                ifHolding[i] = ifHolding[i - 1];
            }else {
                ifHolding[i] = false;
            }
        }
        return ifHolding;
    }

    //this function is used to calculate the equity under different stock holding strategy
    //price: a vector of stock price over n time points List<Integer>
    //i.hold: a vector of indicator over n time points, 0 means not hold the stock, 1 means hold boolean
    //cash: the initial cash amount, the default value is the value of price at day 1 (price[1])
    public static double[] price2Invest(List<Double> price, boolean[] isHold, double cash){
        double[] equity = new double[isHold.length];
        equity[0] = cash;
        //assume can only start hold from second day
        for(int i = 1; i < isHold.length;i++){
            if(isHold[i]){
                equity[i] = equity[i - 1] * (price.get(i)/price.get(i - 1));
            }else{
                equity[i] = equity[i - 1];
            }
        }
        return equity;
    }
    //overload of above method, default cash as price[1]
    public static double[] price2Invest(List<Double> price, boolean[] isHold){
        double[] equity = new double[isHold.length];
        equity[0] = price.get(0);
        //assume can only start hold from second day
        for(int i = 1; i < isHold.length;i++){
            if(isHold[i]){
                equity[i] = equity[i - 1] * (price.get(i)/price.get(i - 1));
            }else{
                equity[i] = equity[i - 1];
            }
        }
        return equity;
    }

    //this function is used to calculate max drawdown
    public static double maxDrawdown(double[] val){
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for(int i = 0; i < val.length; i++){
            if(val[i] > max){
                max = val[i];
            }
            if(val[i] < min){
                min = val[i];
            }
        }
        return (min - max) / max;

    }

    //this method is used for calculate annualized return
    public static double yearReturn(double[] val, int dailyScale){
        double start = val[0];
        double end = val[val.length - 1];
        double periodReturn = (end - start) / start;
        double res = Math.pow((1 + periodReturn),((double) dailyScale / val.length)) - 1;
        return res;
    }
    //this is overload of above method which assume dailyScale default as 252
    public static double yearReturn(double[] val){
        double start = val[0];
        double end = val[val.length - 1];
        double periodReturn = (end - start) / start;
        double res = Math.pow(((double) 1 + periodReturn),((double) 252 / val.length)) - 1;
        return res;
    }
}

