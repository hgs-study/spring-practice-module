package practice.pir.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import practice.pir.domain.UnlistedStock;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UnlistedStockServiceTest {

    @Autowired
    UnlistedStockService unlistedStockService;

    @Test
    void 비상장종목이_상장했을경우_상장된_코드_알림() {
        final UnlistedStock unlistedStock = new UnlistedStock("A389930", "두나무", true);
        unlistedStock.unlisted();

        unlistedStockService.listed(unlistedStock);

        assertEquals(unlistedStock.getUnlistedStockState(), UnlistedStock.UnlistedStockState.LISTED);
        assertEquals(unlistedStock.getActivation(), false);
    }
}