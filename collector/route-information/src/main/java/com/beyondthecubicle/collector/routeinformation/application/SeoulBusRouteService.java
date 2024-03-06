package com.beyondthecubicle.collector.routeinformation.application;

import com.beyondthecubicle.collector.routeinformation.domain.BusRouteInformation;
import com.beyondthecubicle.collector.routeinformation.dto.SeoulBusRouteInformationResponse;
import com.beyondthecubicle.collector.routeinformation.infrastructure.BusRouteInformationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Service
public class SeoulBusRouteService {

    private final WebClient webClient;
    private final BusRouteInformationRepository busRouteInformationRepository;

    @Value("${data-portal.service-key}")
    private String dataPortalServiceKey;

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void sync() {
        List<BusRouteInformation> busRouteInformationList = getSeoulBusRouteInformationList();
        busRouteInformationRepository.saveAll(busRouteInformationList);
    }

    private List<BusRouteInformation> getSeoulBusRouteInformationList() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("ws.bus.go.kr")
                        .path("/api/rest/busRouteInfo/getBusRouteList")
                        .queryParam("serviceKey", dataPortalServiceKey)
                        .build())
                .exchangeToMono(response -> response.bodyToMono(SeoulBusRouteInformationResponse.class)
                        .map(seoulBusRouteInformationResponse -> BusRouteInformation.from(
                                seoulBusRouteInformationResponse.getMessageBody().getBusRouteInformationArray())))
                .block();
    }
}
