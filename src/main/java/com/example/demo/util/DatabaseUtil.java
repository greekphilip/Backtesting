package com.example.demo.util;

import com.example.demo.domain.CustomCandlestick;
import com.example.demo.service.CandlestickService;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.text.ParseException;
import java.util.*;

import static com.example.demo.Values.coins;

@Component
public class DatabaseUtil {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private CandlestickService candlestickService;

    private DataDownloader dataDownloader = new DataDownloader();

    private final String SQL_SCRIPT = "sqlScript.sql";

    private Map<String, Integer> openTimesTimes = new HashMap<>();
    private Map<String, String> openTimes = new HashMap<>();
    private Map<Long, Integer> sizeText = new HashMap<>();
    private Map<String, Long> sizeTextCoins = new HashMap<>();

    private Map<String, Integer> openTimesTimesDB = new HashMap<>();
    private Map<String, String> openTimesDB = new HashMap<>();
    private Map<Long, Integer> sizeDB = new HashMap<>();
    private Map<String, Long> sizeDBCoins = new HashMap<>();

    public DatabaseUtil() throws FileNotFoundException, ParseException {
    }


    public boolean assertData() throws InstantiationException, IllegalAccessException, FileNotFoundException {

        initCoins();

        try (FileWriter fw = new FileWriter(SQL_SCRIPT);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter printer = new PrintWriter(bw)) {

            for (CustomCandlestick coin : coins) {
                String tableName = coin.getClass().getSimpleName().toLowerCase();

                printer.println("CREATE TABLE IF NOT EXISTS backtesting." + tableName + " (" +
                                        "\topentime int8 NULL," +
                                        "\t\"open\" numeric NULL," +
                                        "\thigh numeric NULL," +
                                        "\tlow numeric NULL," +
                                        "\t\"close\" numeric NULL," +
                                        "\tid int4 NOT NULL," +
                                        "\tCONSTRAINT " + tableName + "_pk PRIMARY KEY (id)" +
                                        ");");
            }
        } catch (IOException e) {
            System.err.println("Something went worng. Input Output");
        }
        executeScript();

        if (!assertDatesDB()) {
            Scanner userInput = new Scanner(System.in);
            boolean validChoice = false;
            while (!validChoice) {
                System.out.println("\n1. exit" +
                                           "\n2. assert texts" +
                                           "\n3. coin name you want to set as valid dates");

                String choice = userInput.nextLine();

                switch (choice) {
                    case "exit":
                        System.exit(0);
                        break;
                    case "assert texts":
                        if (assertDatesText()) {
                            for (CustomCandlestick coin : coins) {
                                if (!assertIndividualDB(coin.getClass().getSimpleName())) {
                                    System.out.println("Resetting " + coin.getClass().getSimpleName());
                                    candlestickService.deleteData(coin.getClass().getSimpleName());
                                    insertData(coin.getClass().getSimpleName());
                                }
                            }
                            validChoice = true;
                        }
                        break;
                    default:

                        for (CustomCandlestick coin : coins) {
                            if (choice.equals(coin.getClass().getSimpleName() + " db")) {
                                choice = choice.replace(" db", "");
                                updateDatabase(choice);
                                validChoice = true;
                                break;
                            } else if (choice.equals(coin.getClass().getSimpleName() + " text")) {
                                choice = choice.replace(" text", "");
                                updateTextFiles(choice);
                                validChoice = true;
                                break;
                            }
                        }
                }
            }
        }

        if (!assertDatesDB()) {
            throw new IllegalArgumentException("Something is wrong with the data! DEBUG");
        }

        System.out.println("DATA IS VALID");
        return true;
    }

    public boolean assertIndividualDB(String coinName) {
        String validOpenDate = openTimes.get(coinName);
        long validSize = sizeTextCoins.get(coinName);
        String actualOpenDate = candlestickService.getOpenDate(coinName) + "";
        long actualSize = candlestickService.size(coinName);

        if (validSize == actualSize && validOpenDate.equals(actualOpenDate)) {
            return true;
        }

        return false;
    }

    public void updateDatabase(String coinName) throws FileNotFoundException, InstantiationException, IllegalAccessException {
        String validOpenDate = openTimesDB.get(coinName);
        long validSize = sizeDBCoins.get(coinName);

        for (CustomCandlestick coin : coins) {
            if (!validOpenDate.equals(openTimesDB.get(coin.getClass().getSimpleName())) || validSize != sizeDBCoins.get(coin.getClass().getSimpleName())) {
                if (!assertIndividualTextFile(coin.getClass().getSimpleName(), validOpenDate, validSize)) {
                    downloadValidData(coin.getClass().getSimpleName(), validOpenDate, validSize);
                    insertData(coin.getClass().getSimpleName());
                } else {
                    insertData(coin.getClass().getSimpleName());
                }
            }
        }
    }

    public void updateTextFiles(String coinName) throws FileNotFoundException, InstantiationException, IllegalAccessException {
        String validOpenDate = openTimes.get(coinName);
        long validSize = sizeTextCoins.get(coinName);

        insertData(coinName);

        for (CustomCandlestick coin : coins) {
            if (!validOpenDate.equals(openTimes.get(coin.getClass().getSimpleName())) || validSize != sizeTextCoins.get(coin.getClass().getSimpleName())) {
                downloadValidData(coin.getClass().getSimpleName(), validOpenDate, validSize);
                insertData(coin.getClass().getSimpleName());
            }
        }
    }

    public void downloadValidData(String coinName, String validOpenDate, long validSize) {
        validSize--;
        Long openTime = Long.parseLong(validOpenDate);
        Long endTime = openTime + (validSize * 60000);


        try {
            dataDownloader.getData(coinName, openTime, endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public boolean assertIndividualTextFile(String coinName, String validOpenDate, long validSize) {
        candlestickService.deleteData(coinName);

        long size = 0;
        try {
            Scanner scanner = new Scanner(new FileInputStream("Historical/" + coinName + "history.txt"));
            String line;
            boolean firstLine = true;
            while (scanner.hasNext()) {
                size++;
                line = scanner.nextLine();
                if (firstLine) {
                    String[] array = line.split(",");

                    String openTime = array[0].replace("Candlestick[openTime=", "");

                    if (!openTime.equals(validOpenDate)) {
                        return false;
                    }

                    firstLine = false;
                }
            }

            if (size != validSize) {
                return false;
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public void initCoins() throws IllegalAccessException, InstantiationException {
        Reflections reflections = new Reflections("com.example.demo");
        Set<Class<? extends CustomCandlestick>> classes = reflections.getSubTypesOf(CustomCandlestick.class);
        for (Class<? extends CustomCandlestick> coin : classes) {
            coins.add(coin.newInstance());
        }
    }

    public void executeScript() throws FileNotFoundException {
        EntityManager em = entityManagerFactory.createEntityManager();


        Scanner input = new Scanner(new FileInputStream(SQL_SCRIPT));
        String line;
        while (input.hasNext()) {
            line = input.nextLine();

            em.getTransaction().begin();
            em.createNativeQuery(line).executeUpdate();
            em.getTransaction().commit();
        }
    }

    public void resetCheckVariables() {
        openTimes.clear();
        openTimesTimes.clear();
        openTimesDB.clear();
        openTimesTimesDB.clear();

        sizeDB.clear();
        sizeDBCoins.clear();

        sizeText.clear();
        sizeTextCoins.clear();
    }

    public boolean assertDatesDB() {

        resetCheckVariables();

        System.out.println("ASSERTING DATES IN DATABASE");


        for (CustomCandlestick coin : coins) {

            long size = candlestickService.size(coin.getClass().getSimpleName());
            long open = candlestickService.getOpenDate(coin.getClass().getSimpleName());


            if (sizeDB.get(size) == null) {
                sizeDB.put(size, 1);
            } else {
                sizeDB.put(size, sizeDB.get(size) + 1);
            }
            sizeDBCoins.put(coin.getClass().getSimpleName(), size);


            if (openTimesTimesDB.get(open) == null) {
                openTimesTimesDB.put(open + "", 1);
            } else {
                openTimesTimesDB.put(open + "", openTimesTimesDB.get(open) + 1);
            }
            openTimesDB.put(coin.getClass().getSimpleName(), open + "");

        }

        boolean allEmpty = true;

        for (CustomCandlestick coin : coins) {
            if (sizeDBCoins.get(coin.getClass().getSimpleName()) != 0) {
                allEmpty = false;
                break;
            }
        }

        if (openTimesTimesDB.size() == 1 && sizeDB.size() == 1 && !allEmpty) {
            return true;
        }

        System.out.println("STARTING DATES DB..................................................");
        for (Map.Entry<String, Integer> entry : openTimesTimesDB.entrySet()) {
            for (CustomCandlestick coin : coins) {
                if (openTimesDB.get(coin.getClass().getSimpleName()).equals(entry.getKey())) {
                    System.out.println(coin.getClass().getSimpleName() + "  :  " + entry.getKey());
                }
            }
            System.out.println("-----------------------------------------");
        }


        System.out.println("SIZES DB");
        for (Map.Entry<Long, Integer> entry : sizeDB.entrySet()) {
            for (CustomCandlestick coin : coins) {
                if (sizeDBCoins.get(coin.getClass().getSimpleName()).equals(entry.getKey())) {
                    System.out.println(coin.getClass().getSimpleName() + "   :   " + entry.getKey());
                }
            }
            System.out.println("-----------------------------------------");
        }

        return false;

    }

    public boolean assertDatesText() {

        System.out.println("ASSERTING DATES IN TEXT FILES");
        Scanner scanner;

        for (CustomCandlestick coin : coins) {
            long size = 0;
            try {
                scanner = new Scanner(new FileInputStream("Historical/" + coin.getClass().getSimpleName() + "history.txt"));
                String line;
                boolean firstLine = true;
                while (scanner.hasNext()) {
                    size++;
                    line = scanner.nextLine();
                    if (firstLine) {
                        String[] array = line.split(",");

                        String openTime = array[0].replace("Candlestick[openTime=", "");

                        if (openTimesTimes.get(openTime) != null) {
                            openTimesTimes.put(openTime, openTimesTimes.get(openTime) + 1);
                        } else {
                            openTimesTimes.put(openTime, 1);
                        }
                        openTimes.put(coin.getClass().getSimpleName(), openTime);
                        firstLine = false;
                    }
                }
                if (size == 0) {
                    openTimes.put(coin.getClass().getSimpleName(), "0");
                }

                if (sizeText.get(size) == null) {
                    sizeText.put(size, 1);
                } else {
                    sizeText.put(size, sizeText.get(size) + 1);
                }

                sizeTextCoins.put(coin.getClass().getSimpleName(), size);
            } catch (FileNotFoundException e) {
//                System.out.println("--ATTENTION! "+coin.getClass().getSimpleName()+", DATA NOT FOUND!");
                openTimesTimes.put(coin.getClass().getSimpleName(), 0);
                e.printStackTrace();
            } catch (Exception e) {
                openTimesTimes.put(coin.getClass().getSimpleName(), 0);
                e.printStackTrace();
            }
        }

        if (openTimesTimes.size() == 1 && sizeText.size() == 1) {
            return true;
        }

        System.out.println("STARTING DATES TEXT FILES");
        for (Map.Entry<String, Integer> entry : openTimesTimes.entrySet()) {
            for (CustomCandlestick coin : coins) {
                if (openTimes.get(coin.getClass().getSimpleName()).equals(entry.getKey())) {
                    System.out.println(coin.getClass().getSimpleName() + "  :  " + entry.getKey());
                }
            }
            System.out.println("-----------------------------------------");
        }


        System.out.println("SIZES TEXT FILES");
        for (Map.Entry<Long, Integer> entry : sizeText.entrySet()) {
            for (CustomCandlestick coin : coins) {
                if (sizeTextCoins.get(coin.getClass().getSimpleName()).equals(entry.getKey())) {
                    System.out.println(coin.getClass().getSimpleName() + "   :   " + entry.getKey());
                }
            }
            System.out.println("-----------------------------------------");
        }

        return false;
    }

    public void insertData(String symbol) throws FileNotFoundException, IllegalAccessException, InstantiationException {
        Scanner input = new Scanner(new FileInputStream("Historical/" + symbol + "history.txt"));
        String line;

        for (CustomCandlestick candlestick : coins) {
            int id = 0;
            if (candlestick.getClass().getSimpleName().equals(symbol)) {
                while (input.hasNext()) {
                    id++;
                    line = input.nextLine();
                    String[] array = line.split(",");

                    String openTime = array[0].replace("Candlestick[openTime=", "");
                    String open = array[1].replace("open=", "");
                    String high = array[2].replace("high=", "");
                    String low = array[3].replace("low=", "");
                    String close = array[4].replace("close=", "");

                    candlestick.setOpenTime(Long.parseLong(openTime));
                    candlestick.setOpen(Double.parseDouble(open));
                    candlestick.setHigh(Double.parseDouble(high));
                    candlestick.setLow(Double.parseDouble(low));
                    candlestick.setClose(Double.parseDouble(close));

                    candlestick.setId(id);

                    candlestickService.save(candlestick);
                }
            }

        }

    }

}

