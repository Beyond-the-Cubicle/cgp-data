package com.beyondthecubicle.collector.routeinformation.application;

import com.beyondthecubicle.collector.routeinformation.constant.Region;
import com.beyondthecubicle.collector.routeinformation.domain.BusRouteInformation;
import com.beyondthecubicle.collector.routeinformation.dto.GyeonggiBusRouteBaseResponse;
import com.beyondthecubicle.collector.routeinformation.dto.GyeonggiBusRouteBaseResponse.Row;
import com.beyondthecubicle.collector.routeinformation.dto.GyeonggiBusRouteInformationResponse;
import com.beyondthecubicle.collector.routeinformation.infrastructure.BusRouteInformationRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.codec.DecodingException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@Service
public class GyeonggiBusRouteService {

    private static final String DEFAULT_RESPONSE_TYPE = "xml";
    private static final int DEFAULT_PAGE_INDEX = 1;
    private static final int DEFAULT_PAGE_MAX_SIZE = 1000;
    private static final int SCHEDULE_PAGE_SIZE = 900;

    private final WebClient webClient;
    private final BusRouteInformationRepository busRouteInformationRepository;

    @Value("${data-dream.service-key}")
    private String dataDreamServiceKey;
    @Value("${data-portal.service-key}")
    private String dataPortalServiceKey;

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void sync() {
        log.info("test");
        List<String> gyeonggiBusRouteIdList = getGyeonggiBusRouteIdList();
        int pageSize = Long.valueOf(busRouteInformationRepository.countByRegion(Region.GYEONGGI) - SCHEDULE_PAGE_SIZE).intValue();
        if (pageSize > 0) {
            Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Direction.DESC, "updatedAt"));
            List<String> updatedGyeonggiBusRouteIdList = busRouteInformationRepository.findAllByRegion(Region.GYEONGGI, pageable)
                    .getContent()
                    .stream()
                    .map(BusRouteInformation::getRouteId)
                    .toList();
            gyeonggiBusRouteIdList.removeAll(updatedGyeonggiBusRouteIdList);
        } else {
            List<String> updatedGyeonggiBusRouteIdList = busRouteInformationRepository.findAllByRegion(Region.GYEONGGI)
                    .stream()
                    .map(BusRouteInformation::getRouteId)
                    .toList();
            gyeonggiBusRouteIdList.removeAll(updatedGyeonggiBusRouteIdList);
        }

        if (gyeonggiBusRouteIdList.size() > SCHEDULE_PAGE_SIZE) {
            gyeonggiBusRouteIdList = gyeonggiBusRouteIdList.subList(0, SCHEDULE_PAGE_SIZE);
        }

        List<BusRouteInformation> busRouteInformationList = getGyeonggiBusRouteInformationList(gyeonggiBusRouteIdList);
        busRouteInformationRepository.saveAll(busRouteInformationList);
    }

    public List<BusRouteInformation> getGyeonggiBusRouteInformationList(List<String> gyeonggiBusRouteIdList) {
        return Flux.fromIterable(gyeonggiBusRouteIdList)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(this::getGyeonggiBusRouteInformationResponse)
                .sequential()
                .toStream()
                .toList();
    }

    private List<String> getGyeonggiBusRouteIdList() {
        int totalPageSize = getGyeonggiBusRouteIdPageSize();
        List<String> gyeonggiBusRouteIdList = IntStream.range(1, totalPageSize + 1)
                .mapToObj(pageIndex -> Arrays.stream(getGyeonggiBusRouteBaseResponse(pageIndex).getRowArray())
                        .map(Row::getRouteId)
                        .toList()).flatMap(Collection::stream).toList();
        return gyeonggiBusRouteIdList.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private int getGyeonggiBusRouteIdPageSize() {
        return (getGyeonggiBusRouteBaseResponse(DEFAULT_PAGE_INDEX).getHeader().getTotalCount() / DEFAULT_PAGE_MAX_SIZE) + 1;
    }

    private GyeonggiBusRouteBaseResponse getGyeonggiBusRouteBaseResponse(int pageIndex) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("openapi.gg.go.kr")
                        .path("/TBBMSROUTEM")
                        .queryParam("KEY", dataDreamServiceKey)
                        .queryParam("Type", DEFAULT_RESPONSE_TYPE)
                        .queryParam("pIndex", pageIndex)
                        .queryParam("pSize", DEFAULT_PAGE_MAX_SIZE)
                        .build())
                .retrieve()
                .bodyToMono(GyeonggiBusRouteBaseResponse.class)
                .block();
    }

    private Mono<BusRouteInformation> getGyeonggiBusRouteInformationResponse(String gyeonggiBusRouteId) {
        log.info("gyeonggiBusRouteId = " + gyeonggiBusRouteId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("apis.data.go.kr")
                        .path("/6410000/busrouteservice/getBusRouteInfoItem")
                        .queryParam("serviceKey", dataPortalServiceKey)
                        .queryParam("routeId", gyeonggiBusRouteId)
                        .build())
                .exchangeToMono(response -> response.bodyToMono(GyeonggiBusRouteInformationResponse.class)
                        .map(gyeonggiBusRouteInformationResponse -> {
                            if (validateResponseHeader(gyeonggiBusRouteInformationResponse.getMessageHeader())) {
                                return BusRouteInformation.from(gyeonggiBusRouteInformationResponse);
                            } else {
                                return BusRouteInformation.error(Region.GYEONGGI, gyeonggiBusRouteId);
                            }
                        }))
                .onErrorResume(DecodingException.class, error -> Mono.just(BusRouteInformation.error(Region.GYEONGGI, gyeonggiBusRouteId)));
    }

    private boolean validateResponseHeader(GyeonggiBusRouteInformationResponse.MessageHeader messageHeader) {
        return messageHeader != null && messageHeader.getResultCode().equals("0");
    }
}
