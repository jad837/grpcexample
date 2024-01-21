package com.blogs.grpcblog.controllers;

import com.blogs.grpcblog.models.VehicleResponseModel;
import com.blogs.grpcblog.proto.Response;
import com.blogs.grpcblog.proto.VehiclePopulation;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@Slf4j
public class VehicleController {

    private final ObjectMapper objectMapper = new ObjectMapper();


    @GetMapping(value = "/v1/getdata", produces = "application/json")
    public List<VehicleResponseModel> getVehicleData(@RequestParam Integer limit, @RequestParam Integer offset, HttpServletResponse response) throws IOException {
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
//            response.addHeader("Access-Control-Allow-Origin", "https://127.0.0.1");
            response.addHeader("Access-Control-Allow-Private-Network", "true");

            return validRecords.stream().map(this::recordToJsonResponse).toList();
        }

    }

    private List<CSVRecord> getCSVRecords() throws IOException {
        String[] csvHeaders = {"vin", "county","city","state","postal_code","model_year","make","model","type","cafv","range","msrp","legislative_district","vehicle_id","location","electric_utility","census_tract"};

        var csvFormat = CSVFormat.DEFAULT.builder().setDelimiter(',').setHeader(csvHeaders).setSkipHeaderRecord(true).build();
        var file = new File("F:\\projects\\blogs\\grpcblog\\src\\main\\resources\\static\\datasets\\electic_vehicle_population_data.csv");
        try(
                var csvReader = new FileReader(file);
                var records = csvFormat.parse(csvReader);
        ) {
            return records.getRecords();
        }
    }

    @GetMapping(value = "/v1/compare", produces = "application/json")
    public void getVehiclePopulation() throws IOException {
        var records = getCSVRecords();
        var watch = new StopWatch();

        watch.start("csvtojsonpojo");
        List<VehicleResponseModel> jsonRecords = records.stream().map(this::recordToJsonResponse).toList();
        watch.stop();

        watch.start("serialization-pojo-to-jsonbytes");
        var serializedjson = objectMapper.writeValueAsBytes(jsonRecords);
        watch.stop();
        log.info("size of serialized json: {}", serializedjson.length);

        watch.start("deserialize-jsonbytes-to-pojo");
        var deserializedjson = objectMapper.readValue(serializedjson, VehicleResponseModel[].class);
        watch.stop();

        watch.start("csvtoprotopojo");
        var protoRecords = records.stream().map(this::recordToProtoResponse).toList();
        var protos = Response.newBuilder().addAllVehiclePopulations(protoRecords).build();
        watch.stop();

        watch.start("serialize-pojo-to-protobytes");
        var serializedProto = protos.toByteArray();
        watch.stop();
        log.info("size of serialized proto : {}", serializedProto.length);

        watch.start("deserialize-protoBytes-to-pojo");
        var deserializedProto = VehiclePopulation.parseFrom(serializedProto);
        watch.stop();

        log.info("time taken by each process of parsing : {}", watch.prettyPrint(TimeUnit.MILLISECONDS));
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
