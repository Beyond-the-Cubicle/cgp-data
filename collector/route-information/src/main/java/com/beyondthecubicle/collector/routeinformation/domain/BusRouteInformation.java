package com.beyondthecubicle.collector.routeinformation.domain;

import com.beyondthecubicle.collector.routeinformation.constant.Region;
import com.beyondthecubicle.collector.routeinformation.dto.GyeonggiBusRouteInformationResponse;
import com.beyondthecubicle.collector.routeinformation.dto.SeoulBusRouteInformationResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "bus_route_information")
public class BusRouteInformation {

    @Id
    @Comment("노선ID")
    private String routeId;

    @Comment("지역")
    @Enumerated(EnumType.STRING)
    private Region region;

    @Comment("노선번호")
    private String routeName;

    @Comment("배차간격")
    private Integer dispatchInterval;

    @Comment("운행횟수")
    private Integer operationCount;

    @Comment("기점")
    private String startStationName;

    @Comment("기점 첫차 시간")
    private LocalTime startStationFirstTime;

    @Comment("기점 막차 시간")
    private LocalTime startStationLastTime;

    @Comment("종점")
    private String endStationName;

    @Comment("종점 첫차 시간")
    private LocalTime endStationFirstTime;

    @Comment("종점 막차 시간")
    private LocalTime endStationLastTime;

    @Comment("최초 생성 시간")
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Comment("마지막 갱신 시간")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static BusRouteInformation from(GyeonggiBusRouteInformationResponse gyeonggiBusRouteInformationResponse) {
        GyeonggiBusRouteInformationResponse.BusRouteInformation busRouteInformation = gyeonggiBusRouteInformationResponse.getMessageBody()
                .getBusRouteInformation();
        return BusRouteInformation.builder()
                .region(Region.GYEONGGI)
                .routeId(busRouteInformation.getRouteId())
                .routeName(busRouteInformation.getRouteName())
                .dispatchInterval(calculateDispatchInterval(busRouteInformation))
                .operationCount(null)
                .startStationName(busRouteInformation.getStartStationName())
                .startStationFirstTime(parseBusRouteTime(busRouteInformation.getUpFirstTime()))
                .startStationLastTime(parseBusRouteTime(busRouteInformation.getUpLastTime()))
                .endStationName(busRouteInformation.getEndStationName())
                .endStationFirstTime(parseBusRouteTime(busRouteInformation.getDownFirstTime()))
                .endStationLastTime(parseBusRouteTime(busRouteInformation.getDownLastTime()))
                .build();
    }

    public static BusRouteInformation from(SeoulBusRouteInformationResponse.BusRouteInformation seoulBusRouteInformationResponse) {
        return BusRouteInformation.builder()
                .region(Region.SEOUL)
                .routeId(seoulBusRouteInformationResponse.getBusRouteId())
                .routeName(seoulBusRouteInformationResponse.getBusRouteName())
                .dispatchInterval(Integer.parseInt(seoulBusRouteInformationResponse.getTerm()))
                .operationCount(null)
                .startStationName(seoulBusRouteInformationResponse.getStartStationName())
                .startStationFirstTime(parseBusRouteTime(seoulBusRouteInformationResponse.getFirstBusTime()))
                .startStationLastTime(parseBusRouteTime(seoulBusRouteInformationResponse.getLastBusTime()))
                .endStationName(seoulBusRouteInformationResponse.getEndStationName())
                .endStationFirstTime(null)
                .endStationLastTime(null)
                .build();
    }

    public static List<BusRouteInformation> from(
            SeoulBusRouteInformationResponse.BusRouteInformation[] seoulBusRouteInformationResponseArray) {
        return Arrays.stream(seoulBusRouteInformationResponseArray)
                .map(BusRouteInformation::from)
                .toList();
    }

    public static BusRouteInformation error(Region region, String routeId) {
        return BusRouteInformation.builder()
                .region(region)
                .routeId(routeId)
                .build();
    }

    private static Integer calculateDispatchInterval(GyeonggiBusRouteInformationResponse.BusRouteInformation busRouteInformation) {
        return Integer.parseInt(busRouteInformation.getPeekAlloc()) + Integer.parseInt(busRouteInformation.getNPeekAlloc()) / 2;
    }

    private static LocalTime parseBusRouteTime(String time) {
        if (time != null && time.length() == 5) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return LocalTime.parse(time, dateTimeFormatter);
        } else if (time != null && time.length() == 14) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            return LocalTime.parse(time, dateTimeFormatter);
        }
        return null;
    }
}
