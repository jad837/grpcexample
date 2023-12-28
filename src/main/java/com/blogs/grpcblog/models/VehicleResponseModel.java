package com.blogs.grpcblog.models;

import jakarta.xml.bind.annotation.*;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@NoArgsConstructor
@ToString
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class VehicleResponseModel {
    @XmlAttribute
    public Long id;
    @XmlAttribute
    public String vin;
    @XmlAttribute
    public String county;
    @XmlAttribute
    public String city;
    @XmlAttribute
    public String state;
    @XmlAttribute
    public long postalCode;
    @XmlAttribute
    public int modelYear;
    @XmlAttribute
    public String make;
    @XmlAttribute
    public String model;
    @XmlAttribute
    public String type;
    //Clean Alternative Fuel Vehicle
    @XmlAttribute
    public String cleanAlternativeFuelVehicle;
    @XmlAttribute
    public int electricRange;
    @XmlAttribute
    public int baseMsrp;
    @XmlAttribute
    public int legislativeDistrict;
    @XmlAttribute
    public String vehicleId;
    @XmlAttribute
    public String electricUtility;
    @XmlAttribute
    public long censusTract;

}
