package practice.pir.wrapper;

public class StockElementTag {
    private String stockElementTag;

    public StockElementTag(String stockElementTag) {
        this.stockElementTag = stockElementTag;
    }

    public String getStockElementTag() {
        return stockElementTag;
    }

    public void changeStockElementTag(String tag){
        stockElementTag = tag;
    }
}
