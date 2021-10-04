package kitchenpos.menus.tobe.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class ProductPrice {

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    protected ProductPrice() {};

    public ProductPrice(BigDecimal price) {
        validationPrice(price);
        this.price = price;
    }

    public void changePrice(BigDecimal price) {
        validationPrice(price);
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    private void validationPrice(BigDecimal price) {
        if (Objects.isNull(price) || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductPrice that = (ProductPrice) o;
        return Objects.equals(getPrice(), that.getPrice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPrice());
    }
}