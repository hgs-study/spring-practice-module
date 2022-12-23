package practice.pir.extractor;

import practice.pir.wrapper.StockElementTag;

@FunctionalInterface
public interface ScrapedStockExtractable {
    StockElementTag extract(StockElementTag stockElementTag);
}
