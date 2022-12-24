package practice.pir.event;

import org.springframework.context.ApplicationEvent;
import practice.pir.domain.UnlistedStock;

public class ListedStockEvent extends ApplicationEvent {
    private UnlistedStock unlistedStock;

    public ListedStockEvent(Object source, UnlistedStock unlistedStock){
        super(source);
        this.unlistedStock = unlistedStock;
    }

    public UnlistedStock getUnlistedStock() {
        return unlistedStock;
    }
}
