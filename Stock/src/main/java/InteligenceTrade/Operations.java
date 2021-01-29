package InteligenceTrade;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import yahoofinance.Stock;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Operations {
     TradeGroup trade_3min;
     TradeGroup trade_10min;
     TradeGroup trade_30min;
     TradeGroup trade_60min;
     TradeGroup trade_1day;

    public Operations(){
        trade_3min = new TradeGroup(3);
        trade_10min = new TradeGroup(10);
        trade_30min = new TradeGroup(30);
        trade_60min = new TradeGroup(60);
        trade_1day = new TradeGroup(1440);
        op();
        evolve();
    }
    //在这个地方添加股票
    public void op(){
        //如果要加单个股票就用add，格式如下
         add("sh688069", trade_3min);
        //如果用文件一次性添加很多股票用importStocks,格式如下
       // importStocks(new File("这里填文件路径"), trade_30min);

    }

    //return old stocks
    public HashSet<String> importStocks(File file, TradeGroup group){
        HashSet<String> stocks  = new HashSet<String>();
        Reader in = null;
        try {
            in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                String name = record.get(0);
                stocks.add(name);
            }

            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashSet<String> res = group.cleanAll();
        group.addStocks(stocks);
        return res;
    }


    public boolean add(String stock, TradeGroup group){
       return group.addStocks(stock);
    }

    public TradeGroup changeGroup(String stock, TradeGroup des_group){
        TradeGroup res = null;
        if(trade_3min.getStocks().contains(stock)){
            trade_3min.delete(stock);
            res = trade_3min;
        }else if(trade_10min.getStocks().contains(stock)){
            trade_10min.delete(stock);
            res = trade_10min;
        }else if(trade_30min.getStocks().contains(stock)){
            trade_30min.delete(stock);
            res = trade_30min;
        }else if(trade_60min.getStocks().contains(stock)){
            trade_60min.delete(stock);
            res = trade_60min;
        }else if(trade_1day.getStocks().contains(stock)){
            trade_1day.delete(stock);
            res = trade_1day;
        }
        des_group.addStocks(stock);
        return res;
    }

    //delete already complete trade stocks

    public  void evolve(){
        EvoTimer evoTimer3 = new EvoTimer(3);
        EvoTimer evoTimer10 = new EvoTimer(10);
        EvoTimer evoTimer30 = new EvoTimer(30);
        EvoTimer evoTimer60 = new EvoTimer(60);
        evoTimer3.start();
        evoTimer10.start();
        evoTimer30.start();
        evoTimer60.start();
    }


    private class EvoTimer extends Thread{
        private int interval;

        public EvoTimer(int interval){
            this.interval = interval;
        }

        @Override
        public void run() {
            if (interval == 3) {
                while (true) {
                    try {
                        System.out.println("3分钟组: " + trade_3min.getStocks());
                        System.out.println("10分钟组: " + trade_10min.getStocks());
                        sleep(30 * 60 * 1000);
                        HashSet set = trade_3min.selfEvo();
                        trade_10min.addStocks(set);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }else if (interval == 10){
                while (true) {
                    try {

                        System.out.println("10分钟组: " + trade_10min.getStocks());
                        System.out.println("30分钟组: " + trade_30min.getStocks());

                        sleep(60 * 60 * 1000);
                        HashSet set = trade_10min.selfEvo();
                        trade_30min.addStocks(set);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }else if (interval == 30){
                while (true) {
                    try {

                        System.out.println("30分钟组: " + trade_30min.getStocks());
                        System.out.println("60分钟组: " + trade_60min.getStocks());

                        sleep( 24 * 60 * 60 * 1000);
                        HashSet set = trade_30min.selfEvo();
                        trade_60min.addStocks(set);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }else if (interval == 60){
                while (true) {
                    try {

                        System.out.println("60分钟组: " + trade_60min.getStocks());
                        System.out.println("1天组: " + trade_1day.getStocks());
                        sleep(7 * 24 * 60 * 60 * 1000);
                        HashSet set = trade_60min.selfEvo();
                        trade_1day.addStocks(set);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }


}
