package com.beyondthecubicle.collector.routeinformation.infrastructure;

import com.beyondthecubicle.collector.routeinformation.constant.Region;
import com.beyondthecubicle.collector.routeinformation.domain.BusRouteInformation;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusRouteInformationRepository extends JpaRepository<BusRouteInformation, Long> {

    Long countByRegion(Region region);

    Page<BusRouteInformation> findAllByRegion(Region region, Pageable pageable);

    List<BusRouteInformation> findAllByRegion(Region region);
}
