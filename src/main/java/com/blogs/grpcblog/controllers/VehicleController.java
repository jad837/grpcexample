package com.blogs.grpcblog.controllers;

import com.blogs.grpcblog.models.VehicleResponseModel;
import com.blogs.grpcblog.proto.VehiclePopulation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
@Slf4j
public class VehicleController {
    @GetMapping(value = "/v1/getdata", produces = "application/json")
    public List<VehicleResponseModel> getVehicleData(@RequestParam Integer limit, @RequestParam Integer offset) throws IOException {
        log.info("get vehicle data endpoint");
        String[] csvHeaders = {"vin", "county","city","state","postal_code","model_year","make","model","type","cafv","range","msrp","legislative_district","vehicle_id","location","electric_utility","census_tract"};

        var csvFormat = CSVFormat.DEFAULT.builder().setHeader(csvHeaders).setSkipHeaderRecord(true).build();
        var file = new File("F:\\projects\\blogs\\grpcblog\\src\\main\\resources\\static\\datasets\\electic_vehicle_population_data.csv");
        try(
                var csvReader = new FileReader(file);
                var records = csvFormat.parse(csvReader);
        ){
            log.info("entered into lions cave");
            var validRecords = records.getRecords().stream().filter(rec -> rec.getRecordNumber()< offset+limit && rec.getRecordNumber() >=offset).toList();
            return validRecords.stream().map(this::recordToJsonResponse).toList();
        }

    }

    private List<CSVRecord> getCSVRecords() throws IOException {
        String[] csvHeaders = {"vin", "county","city","state","postal_code","model_year","make","model","type","cafv","range","msrp","legislative_district","vehicle_id","location","electric_utility","census_tract"};

        var csvFormat = CSVFormat.DEFAULT.builder().setHeader(csvHeaders).setSkipHeaderRecord(true).build();
        var file = new File("F:\\projects\\blogs\\grpcblog\\src\\main\\resources\\static\\datasets\\electic_vehicle_population_data.csv");
        try(
                var csvReader = new FileReader(file);
                var records = csvFormat.parse(csvReader);
        ) {
            return records.getRecords();
        }
    }

    @GetMapping(value = "/v1/compareproto", produces = "application/json")
    public void getVehiclePopulation() throws IOException {
        log.info("protobuf vs xml vs json");
        var records = getCSVRecords();
        var watch = new StopWatch();
        log.info("starting json parsing");
        watch.start("json");
        var jsonRecords = records.stream().map(this::recordToJsonResponse).toList();
        watch.stop();
        log.info("time taken to complete json parsing : {}", watch.prettyPrint(TimeUnit.MILLISECONDS));

        log.info("starting proto parsing");
        watch.start("proto");
        var protoRecords = records.stream().map(this::recordToProtoResponse).toList();
        watch.stop();
        log.info("time taken to complete proto parsing : {}", watch.prettyPrint(TimeUnit.MILLISECONDS));

        log.info("comparison is done for protobuf vs json vs xml");
    }

    private VehicleResponseModel recordToJsonResponse(CSVRecord record) {
        VehicleResponseModel response = new VehicleResponseModel();
        response.setVin(record.get(0));
        response.setCounty(record.get(1));
        response.setCity(record.get(2));
        response.setState(record.get(3));
        if(StringUtils.hasText(record.get(4))) response.setPostalCode(Long.parseLong(record.get(4)));
        if(StringUtils.hasText(record.get(5)))response.setModelYear(Integer.parseInt(record.get(5)));
        response.setMake(record.get(6));
        response.setModel(record.get(7));
        response.setType(record.get(8));
        response.setCleanAlternativeFuelVehicle(record.get(9));
        if(StringUtils.hasText(record.get(10)))response.setElectricRange(Integer.parseInt(record.get(10)));
        if(StringUtils.hasText(record.get(11)))response.setBaseMsrp(Integer.parseInt(record.get(11)));
        if(StringUtils.hasText(record.get(12)))response.setLegislativeDistrict(Integer.parseInt(record.get(12)));
        response.setVehicleId(record.get(13));
        //record.get(0)
        response.setElectricUtility(record.get(15));
        if(StringUtils.hasText(record.get(16))) response.setCensusTract(Long.parseLong(record.get(16)));
        response.setId(record.getRecordNumber());
        return response;
    }

    private VehiclePopulation recordToProtoResponse(CSVRecord record) {
        VehiclePopulation.Builder vehiclePopulationBuilder = VehiclePopulation.newBuilder();
        vehiclePopulationBuilder.setVin(record.get(0));
        vehiclePopulationBuilder.setCounty(record.get(1));
        vehiclePopulationBuilder.setCity(record.get(2));
        vehiclePopulationBuilder.setState(record.get(3));
        if(StringUtils.hasText(record.get(4))) vehiclePopulationBuilder.setPostalCode(Long.parseLong(record.get(4)));
        if(StringUtils.hasText(record.get(5))) vehiclePopulationBuilder.setModelYear(Integer.parseInt(record.get(5)));
        vehiclePopulationBuilder.setMake(record.get(6));
        vehiclePopulationBuilder.setModel(record.get(7));
        vehiclePopulationBuilder.setType(record.get(8));
        vehiclePopulationBuilder.setCleanAlternativeFuelVehicle(record.get(9));
        if(StringUtils.hasText(record.get(10))) vehiclePopulationBuilder.setElectricRange(Integer.parseInt(record.get(10)));
        if(StringUtils.hasText(record.get(11))) vehiclePopulationBuilder.setBaseMsrp(Integer.parseInt(record.get(11)));
        if(StringUtils.hasText(record.get(12))) vehiclePopulationBuilder.setLegislativeDistrict(Integer.parseInt(record.get(12)));
        vehiclePopulationBuilder.setVehicleId(record.get(13));
        //record.get(0)
        vehiclePopulationBuilder.setElectricUtility(record.get(15));
        if(StringUtils.hasText(record.get(16))) vehiclePopulationBuilder.setCensusTract(Long.parseLong(record.get(16)));
        vehiclePopulationBuilder.setId(record.getRecordNumber());
        return vehiclePopulationBuilder.build();
    }

}
