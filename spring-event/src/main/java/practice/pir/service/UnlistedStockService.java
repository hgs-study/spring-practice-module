package practice.pir.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import practice.pir.domain.UnlistedStock;
import practice.pir.event.ListedStockEvent;

@Service
public class UnlistedStockService implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    public void listed(UnlistedStock unlistedStock){
        unlistedStock.listed();
        unlistedStock.Inactivation();

        publisher.publishEvent(new ListedStockEvent(this, unlistedStock));
    }
}
