package kitchenpos.products.application;

import kitchenpos.menus.application.InMemoryMenuRepository;
import kitchenpos.menus.domain.Menu;
import kitchenpos.menus.domain.MenuRepository;
import kitchenpos.products.domain.Product;
import kitchenpos.products.domain.ProductRepository;
import kitchenpos.products.domain.tobe.domain.InMemoryTobeProductRepository;
import kitchenpos.products.domain.tobe.domain.TobeProduct;
import kitchenpos.products.domain.tobe.domain.TobeProductRepository;
import kitchenpos.products.domain.tobe.domain.vo.ProductId;
import kitchenpos.products.domain.tobe.policy.FakeFailProductNamingRule;
import kitchenpos.products.domain.tobe.policy.FakeFailProductPricingRule;
import kitchenpos.products.domain.tobe.policy.FakeSuccessProductNamingRule;
import kitchenpos.products.domain.tobe.policy.FakeSuccessProductPricingRule;
import kitchenpos.products.dto.ProductPriceChangeRequest;
import kitchenpos.products.dto.ProductRegisterRequest;
import kitchenpos.products.exception.ProductNamingRuleViolationException;
import kitchenpos.products.exception.ProductPricingRuleViolationException;
import kitchenpos.products.infra.PurgomalumClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class TobeProductServiceTest {
    private TobeProductRepository productRepository;
    private MenuRepository menuRepository;
    private TobeProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryTobeProductRepository();
        menuRepository = new InMemoryMenuRepository();
        productService = new TobeProductService(productRepository, menuRepository);
    }

    @DisplayName("상품을 등록할 수 있다.")
    @Test
    void create_success() {
        //given
        final ProductRegisterRequest 상품등록요청 =
                new ProductRegisterRequest("후라이드", new FakeSuccessProductNamingRule(),
                        BigDecimal.valueOf(16_000L), new FakeSuccessProductPricingRule());
        //when
        final TobeProduct 상품 = productService.create(상품등록요청);

        //then
        assertThat(상품).isNotNull();
        assertAll(
            () -> assertThat(상품.getId()).isNotNull(),
            () -> assertThat(상품.getName().getValue()).isEqualTo(상품등록요청.getName()),
            () -> assertThat(상품.getPrice().getValue()).isEqualTo(상품등록요청.getPrice())
        );
    }

    @DisplayName("상품의 가격이 올바르지 않으면 등록할 수 없다.")
    @Test
    void create_fail_pricing_rule_violation( ) {
        //given
        final ProductRegisterRequest 상품등록요청 =
                new ProductRegisterRequest("후라이드", new FakeSuccessProductNamingRule(),
                        BigDecimal.valueOf(16_000L), new FakeFailProductPricingRule());
        //when&&then
        assertThatThrownBy(() -> productService.create(상품등록요청))
                .isInstanceOf(ProductPricingRuleViolationException.class);
    }

    @DisplayName("상품의 이름이 올바르지 않으면 등록할 수 없다.")
    @Test
    void create_fail_naming_rule_violation() {
        //given
        final ProductRegisterRequest 상품등록요청 =
                new ProductRegisterRequest("후라이드", new FakeFailProductNamingRule(),
                        BigDecimal.valueOf(16_000L), new FakeSuccessProductPricingRule());
        //when&&then
        assertThatThrownBy(() -> productService.create(상품등록요청))
                .isInstanceOf(ProductNamingRuleViolationException.class);
    }

    @DisplayName("상품의 가격을 변경할 수 있다.")
    @Test
    void changePrice_success() {
        //given
        final TobeProduct 상품 = productRepository.save(tobeProduct("후라이드", BigDecimal.valueOf(16_000L)));
        final ProductPriceChangeRequest 가격변경요청 = new ProductPriceChangeRequest(상품.getId(), BigDecimal.valueOf(15_000L), new FakeSuccessProductPricingRule());

        //when
        final TobeProduct 가격변경상품 = productService.changePrice(가격변경요청);

        //then
        assertThat(가격변경상품).isNotNull();
        assertAll(
                () -> assertThat(가격변경상품.getPrice().getValue()).isEqualTo(가격변경요청.getPrice()),
                () -> assertThat(가격변경상품.getId()).isEqualTo(상품.getId()),
                () -> assertThat(가격변경상품.getName()).isEqualTo(상품.getName())
        );
    }

    @DisplayName("상품의 가격이 올바르지 않으면 변경할 수 없다.")
    @Test
    void changePrice_fail() {
        //given
        final TobeProduct 상품 = productRepository.save(tobeProduct("후라이드", BigDecimal.valueOf(16_000L)));
        final ProductPriceChangeRequest 가격변경요청 = new ProductPriceChangeRequest(상품.getId(), BigDecimal.valueOf(15_000L), new FakeFailProductPricingRule());

        //when&&then
        assertThatThrownBy(() -> productService.changePrice(가격변경요청))
                .isInstanceOf(ProductPricingRuleViolationException.class);
    }

    @Disabled
    @DisplayName("상품의 가격이 변경될 때 메뉴의 가격이 메뉴에 속한 상품 금액의 합보다 크면 메뉴가 숨겨진다.")
    @Test
    void changePriceInMenu() {
    }

    @DisplayName("상품의 목록을 조회할 수 있다.")
    @Test
    void findAll() {
        //given
        productRepository.save(tobeProduct("후라이드", BigDecimal.valueOf(16_000L)));
        productRepository.save(tobeProduct("양념치킨", BigDecimal.valueOf(16_000L)));

        //when
        final List<TobeProduct> 상품목록 = productService.findAll();

        //then
        assertThat(상품목록).hasSize(2);
    }
}