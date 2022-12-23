package practice.pir.extractor;

import org.springframework.stereotype.Component;
import practice.pir.annotation.StockQuantity;
import practice.pir.wrapper.StockElementTag;

@Component
public class ListedStockQuantityExtractor implements ScrapedStockExtractable {

    @Override
    @StockQuantity
    public StockElementTag extract(StockElementTag stockElementTag) {
        return stockElementTag;
    }
}
