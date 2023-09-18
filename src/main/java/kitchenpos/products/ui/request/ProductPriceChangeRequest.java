package kitchenpos.products.ui.request;

import java.math.BigDecimal;

public class ProductPriceChangeRequest {

    private BigDecimal price;

    public ProductPriceChangeRequest(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
