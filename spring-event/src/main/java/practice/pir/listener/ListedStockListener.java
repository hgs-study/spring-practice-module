package practice.pir.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import practice.pir.domain.UnlistedStock;
import practice.pir.event.ListedStockEvent;

@Slf4j
@Component
public class ListedStockListener {

    @EventListener
    public void processListedStock(ListedStockEvent listedStockEvent){
        UnlistedStock unlistedStock = listedStockEvent.getUnlistedStock();

        notifyListedStock(unlistedStock);
    }

    private void notifyListedStock(UnlistedStock stock){
        log.info("Notice : New Listed Stock : {}({})", stock.getName(), stock.getCode());
    }
}
