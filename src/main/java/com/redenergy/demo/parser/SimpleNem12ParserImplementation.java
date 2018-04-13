package com.redenergy.demo.parser;

import com.redenergy.demo.domain.EnergyUnit;
import com.redenergy.demo.domain.MeterRead;
import com.redenergy.demo.domain.MeterVolume;
import com.redenergy.demo.domain.Quality;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.exit;

public class SimpleNem12ParserImplementation implements SimpleNem12Parser {

    @Override
    public Collection<MeterRead> parseSimpleNem12(File simpleNem12File) {


        List<String> lineList = new ArrayList();
        ArrayList<MeterRead> meterReads = new ArrayList<>();

        //Read file
        try (Stream<String> streamLines = Files.lines(simpleNem12File.toPath())) {
            lineList = streamLines.collect(Collectors.toList());
            streamLines.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Check if file start with 100 and end with 900
        if(!"100".equals(lineList.get(0))
                || !"900".equals(lineList.get(lineList.size() - 1))) {
            System.out.println("File not start with 100 or end with 900");
            exit(0);
        }

        //Loop every line in the file
        lineList.forEach(line -> {
            //If 200 type, create a meterRead object and add to Collection
            if(line.startsWith("200")) {
                String[] lineArray200 = line.split(",");
                //Verify if valid
                if(!verify200TypeLine(lineArray200)) {
                    System.out.println("Wrong input for RecordType 200");
                    exit(0);
                }
                MeterRead meterRead = new MeterRead(lineArray200[1], EnergyUnit.valueOf(lineArray200[2]));
                SortedMap<LocalDate, MeterVolume> sortedMap = new TreeMap<>();
                meterRead.setVolumes(sortedMap);
                meterReads.add(meterRead);
            }

            //If 300 type, create a volume and add to last meter read
            if(line.startsWith("300")) {
                String[] lineArray300 = line.split(",");
                //Verify if valid
                if(!verify300TypeLine(lineArray300)) {
                    System.out.println("Wrong input for RecordType 300");
                    exit(0);
                }
                MeterVolume meterVolume = new MeterVolume(new BigDecimal(lineArray300[2]),
                        Quality.valueOf(lineArray300[3]));
                meterReads.get(meterReads.size() - 1).getVolumes().put(parseDate(lineArray300[1]).get(), meterVolume);
            }
        });

        return meterReads;
    }

    private boolean verify200TypeLine(String[] line) {
        return line.length == 3 && line[1].length() == 10 && contains(EnergyUnit.class, line[2]);
    }

    private boolean verify300TypeLine(String[] line) {
        return line.length == 4 && parseDate(line[1]).isPresent()
                && checkNumeric(line[2])
                && contains(Quality.class, line[3]);
    }

    //Parse local date and return an Optional, for verify purpose
    private Optional<LocalDate> parseDate(String time) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            LocalDate localDate = LocalDate.parse(time, dateTimeFormatter);
            return Optional.of(localDate);
        } catch (Exception e) {
            return Optional.empty();
        }

    }

    //Verify enum
    private  <E extends Enum<E>> boolean contains(Class<E> _enumClass, String value) {
        try {
            return EnumSet.allOf(_enumClass)
                    .contains(Enum.valueOf(_enumClass, value));
        } catch (Exception e) {
            return false;
        }
    }

    //Verify numeric
    private boolean checkNumeric(String str){
        String temp;
        if(str.startsWith("-")){ //checks for negative values
            temp=str.substring(1);
            if(temp.matches("[+]?\\d*(\\.\\d+)?")){
                return true;
            }
        }

        if(str.matches("[+]?\\d*(\\.\\d+)?")) {
             return true;
        }
        return false;
    }

}
