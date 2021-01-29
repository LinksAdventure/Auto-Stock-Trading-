package InteligenceTrade;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class TradeGroup {
    private HashSet<String> stocks;
    private int TRADE_PERIOD;
    public TradeGroup(int period){
        stocks = new HashSet<String>();
        TRADE_PERIOD = period;
        SellTimer sellTimer = new SellTimer();
        sellTimer.start();
    }
    public HashSet<String> getStocks(){
        return stocks;
    }
    public int getPeriod(){
        return TRADE_PERIOD;
    }
    //手动导入股票清单（excel）进入相应交易组（替换掉原组股票清单 ）
    //return the old list of Stocks
    public HashSet<String> replace(HashSet<String> newStocks){
        HashSet<String> old = stocks;
        stocks = newStocks;
        return old;
    }
    //在交易组基础上手动添加新入组股票(新增股票，不替换原清单)
    public boolean addStocks(HashSet<String> newStocks){
        stocks.addAll(newStocks);
        return true;
    }
    public boolean addStocks(String stock){
        stocks.add(stock);
        return true;
    }
    //delete stocks
    public boolean delete(String stock){
       return stocks.remove(stock);
    }
    public boolean delete(HashSet<String> stocks){
        return this.stocks.removeAll(stocks);
    }

    public HashSet<String> cleanAll(){
        HashSet<String> res = stocks;
        stocks = new HashSet<String>();
        return res;
    }

    //股票自动升级

    public HashSet<String> selfEvo(){
        //3分钟升级为10分钟
        HashSet<String> set = new HashSet<String>();
        if(TRADE_PERIOD == 3){
            for(String stock : stocks){
                if(Caculations.threeToTen(stock)){
                    set.add(stock);
                    System.out.println(stock +"从3分钟升级到10分钟");
                }
            }
        }else if(TRADE_PERIOD == 10){
            for(String stock : stocks){
                if(Caculations.tenToThirty(stock)){
                    set.add(stock);
                    System.out.println(stock +"从10分钟升级到30分钟");
                }
            }
        }else if(TRADE_PERIOD == 30){
            for(String stock : stocks){
                if(Caculations.thirtyToHour(stock)){
                    set.add(stock);
                    System.out.println(stock +"从30分钟升级到60分钟");
                }
            }
        }else if(TRADE_PERIOD == 60){
            for(String stock : stocks){
                if(Caculations.hourToDay(stock)){
                    set.add(stock);
                    System.out.println(stock +"从60分钟升级到1天");
                }
            }
        }
        delete(set);
        return set;
    }

    private class SellTimer extends Thread{
        @Override
        public void run() {

            if (TRADE_PERIOD == 3) {
                while (true) {
                    try {
                        System.out.println("3分钟组:" + stocks);
                        sleep(3 * 60 * 1000);
                        for(String stock : stocks){
                            if(Caculations.sell3(stock)){
                                delete(stock);
                                System.out.println(stock + "从3分钟组卖出");
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else if (TRADE_PERIOD == 10){
                while (true) {
                    try {
                        System.out.println("10分钟组:" + stocks);
                        sleep(10 * 60 * 1000);
                        for(String stock : stocks){
                            if(Caculations.sell10(stock)){
                                delete(stock);
                                System.out.println(stock + "从10分钟组卖出");
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else if (TRADE_PERIOD == 30){
                while (true) {
                    try {
                        System.out.println("30分钟组:" + stocks);
                        sleep(30 * 60 * 1000);
                        for(String stock : stocks){
                            if(Caculations.sell30(stock)){
                                delete(stock);
                                System.out.println(stock + "从30分钟组卖出");
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else if (TRADE_PERIOD == 60){
                while (true) {
                    try {
                        System.out.println("60分钟组:" + stocks);
                        sleep(60 * 60 * 1000);
                        for(String stock : stocks){
                            if(Caculations.sell60(stock)){
                                delete(stock);
                                System.out.println(stock + "从60分钟组卖出");
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else if (TRADE_PERIOD == 1440){
                while (true) {
                    try {
                        System.out.println("1天组:" + stocks);
                        sleep(1440 * 60 * 1000);
                        for(String stock : stocks){
                            if(Caculations.sellday(stock)){
                                delete(stock);
                                System.out.println(stock + "从1天组卖出");
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
