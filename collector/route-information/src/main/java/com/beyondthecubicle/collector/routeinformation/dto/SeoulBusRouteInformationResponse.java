package com.beyondthecubicle.collector.routeinformation.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ServiceResult")
public class SeoulBusRouteInformationResponse {

    @XmlElement(name = "comMsgHeader")
    private CommonMessageHeader commonMessageHeader;
    @XmlElement(name = "msgHeader")
    private MessageHeader messageHeader;
    @XmlElement(name = "msgBody")
    private MessageBody messageBody;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "comMsgHeader")
    public static class CommonMessageHeader {

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "msgHeader")
    public static class MessageHeader {

        @XmlElement(name = "headerCd")
        private String headerCode;
        @XmlElement(name = "headerMsg")
        private String headerMessage;
        @XmlElement(name = "itemCount")
        private String itemCount;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "msgBody")
    public static class MessageBody {

        @XmlElement(name = "itemList")
        private BusRouteInformation[] busRouteInformationArray;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "busRouteInfoItem")
    public static class BusRouteInformation {

        @XmlElement(name = "busRouteAbrv")
        private String busRouteAbbreviation;
        @XmlElement(name = "busRouteId")
        private String busRouteId;
        @XmlElement(name = "busRouteNm")
        private String busRouteName;
        @XmlElement(name = "corpNm")
        private String corporationName;
        @XmlElement(name = "edStationNm")
        private String endStationName;
        @XmlElement(name = "firstBusTm")
        private String firstBusTime;
        @XmlElement(name = "firstLowTm")
        private String firstLowTime;
        @XmlElement(name = "lastBusTm")
        private String lastBusTime;
        @XmlElement(name = "lastBusYn")
        private String lastBusYn;
        @XmlElement(name = "lastLowTm")
        private String lastLowTime;
        @XmlElement(name = "length")
        private String length;
        @XmlElement(name = "routeType")
        private String routeType;
        @XmlElement(name = "stStationNm")
        private String startStationName;
        @XmlElement(name = "term")
        private String term;
    }
}
