package com.beyondthecubicle.collector.routeinformation.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class GyeonggiBusRouteInformationResponse {

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

        @XmlElement(name = "queryTime")
        private String queryTime;
        @XmlElement(name = "resultCode")
        private String resultCode;
        @XmlElement(name = "resultMessage")
        private String resultMessage;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "msgBody")
    public static class MessageBody {

        @XmlElement(name = "busRouteInfoItem")
        private BusRouteInformation busRouteInformation;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "busRouteInfoItem")
    public static class BusRouteInformation {

        private String companyId;
        private String companyName;
        private String companyTel;
        private String districtCd;
        private String downFirstTime;
        private String downLastTime;
        private String endMobileNo;
        private String endStationId;
        private String endStationName;
        private String peekAlloc;
        private String regionName;
        private String routeId;
        private String routeName;
        private String routeTypeCd;
        private String routeTypeName;
        private String startMobileNo;
        private String startStationId;
        private String startStationName;
        private String upFirstTime;
        private String upLastTime;
        private String nPeekAlloc;
    }
}
