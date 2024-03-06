package com.beyondthecubicle.collector.routeinformation.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "TBBMSROUTEM")
public class GyeonggiBusRouteBaseResponse {
    
    @XmlElement(name = "head")
    private Header header;
    @XmlElement(name = "row")
    private Row[] rowArray;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "head")
    public static class Header {

        @XmlElement(name = "list_total_count")
        private int totalCount;
        @XmlElement(name = "RESULT")
        private Result result;
        @XmlElement(name = "api_version")
        private String apiVersion;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "RESULT")
    public static class Result {

        @XmlElement(name = "CODE")
        private String code;
        @XmlElement(name = "MESSAGE")
        private String message;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "row")
    public static class Row {

        @XmlElement(name = "ROUTE_ID")
        private String routeId;
        @XmlElement(name = "ROUTE_NM")
        private String routeName;
    }
}
