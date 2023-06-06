package practice.pir.extractor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import practice.pir.wrapper.StockElementTag;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class HotStockQuantityExtractorTest {

    @Autowired
    HotStockQuantityExtractor hotStockQuantityExtractor;

    @Test
    void 긁어온_인기_종목_태그에서_종목수량_추출(){
        final String tag = "<tr class='test'> 1,200,300 주 </tr>";
        final StockElementTag stockElementTag = new StockElementTag(tag);

        final StockElementTag extract = hotStockQuantityExtractor.extract(stockElementTag);

        assertEquals(extract.getStockElementTag(), "1200300");
    }
}