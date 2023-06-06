package practice.pir.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import practice.pir.wrapper.StockElementTag;

@Aspect
@Component
public class StockQuantityAspect {

    @AfterReturning(value = "@annotation(practice.pir.annotation.StockQuantity)", returning = "stockElementTag")
    public StockElementTag extractNumber(StockElementTag stockElementTag){
        String tag = stockElementTag.getStockElementTag();

        StringBuilder stringBuilder = new StringBuilder();
        for (char c : tag.toCharArray()) {
            if(Character.isDigit(c)){
                stringBuilder.append(c);
            }
        }

        stockElementTag.changeStockElementTag(stringBuilder.toString());
        return stockElementTag;
    }
}
