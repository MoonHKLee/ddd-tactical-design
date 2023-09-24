package kitchenpos.menus.application;


import kitchenpos.common.domain.DisplayNameChecker;
import kitchenpos.common.domain.DisplayedName;
import kitchenpos.common.domain.Price;
import kitchenpos.menus.application.dto.*;
import kitchenpos.menus.tobe.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import static kitchenpos.menus.exception.MenuProductExceptionMessage.NOT_EQUAL_MENU_PRODUCT_SIZE;

@Service
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final DisplayNameChecker displayNameChecker;
    private final ProductRepostiory productRepostiory;

    public MenuService(
            final MenuRepository menuRepository,
            final MenuGroupRepository menuGroupRepository,
            final DisplayNameChecker displayNameChecker,
            final ProductRepostiory productRepostiory
    ) {
        this.menuRepository = menuRepository;
        this.menuGroupRepository = menuGroupRepository;
        this.displayNameChecker = displayNameChecker;
        this.productRepostiory = productRepostiory;
    }

    @Transactional
    public MenuInfoResponse create(final MenuCreateRequest request) {
        final NewMenuGroup newMenuGroup = findById(request.getMenuGroupId());
        List<UUID> productIds = getProductIds(request.getMenuProducts());
        List<NewProduct> products = productRepostiory.findAllByIdIn(productIds);
        validateExistsProduct(productIds, products);

        NewMenu savedMenu = menuRepository.save(
                NewMenu.create(
                        UUID.randomUUID(),
                        newMenuGroup.getId(),
                        DisplayedName.of(request.getName(), displayNameChecker),
                        Price.of(request.getPrice()),
                        MenuProducts.create(createMenuProducts(products, request.getMenuProducts())),
                        request.isDisplayed()
                ));
        return createResponse(savedMenu);
    }

    @Transactional
    public MenuChangePriceResponse changePrice(final UUID menuId, MenuChangePriceRequest request) {
        final NewMenu newMenu = findMenuById(menuId);
        newMenu.changePrice(Price.of(request.getPrice()));
        return new MenuChangePriceResponse(newMenu.getId(), newMenu.getPriceValue());
    }

    @Transactional
    public MenuDisplayResponse display(final UUID menuId) {
        final NewMenu newMenu = findMenuById(menuId);
        newMenu.displayed();
        return new MenuDisplayResponse(newMenu.getId(), newMenu.isDisplayed());
    }

    @Transactional
    public MenuDisplayResponse hide(final UUID menuId) {
        final NewMenu newMenu = findMenuById(menuId);
        newMenu.notDisplayed();
        return new MenuDisplayResponse(newMenu.getId(), newMenu.isDisplayed());
    }

    @Transactional(readOnly = true)
    public List<MenuInfoResponse> findAll() {
        List<NewMenu> savedMenusList = menuRepository.findAll();
        return savedMenusList.stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    private NewMenuGroup findById(UUID id) {
        return menuGroupRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    private NewMenu findMenuById(UUID menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(NoSuchElementException::new);
    }

    private void validateExistsProduct(List<UUID> productIds, List<NewProduct> productList) {
        if (productIds.size() != productList.size()) {
            throw new IllegalArgumentException(NOT_EQUAL_MENU_PRODUCT_SIZE);
        }
    }

    private List<UUID> getProductIds(List<MenuProductCreateRequest> menuProducts) {
        if (menuProducts == null || menuProducts.isEmpty()) {
            throw new IllegalArgumentException(NOT_EQUAL_MENU_PRODUCT_SIZE);
        }
        return menuProducts.stream()
                .map(MenuProductCreateRequest::getProductId)
                .collect(Collectors.toList());
    }

    private MenuInfoResponse createResponse(NewMenu savedMenu) {
        return new MenuInfoResponse(
                savedMenu.getId(),
                savedMenu.getName(),
                savedMenu.getPriceValue(),
                savedMenu.getMenuGroupId(),
                savedMenu.isDisplayed(),
                savedMenu.getMenuProductList()
                        .stream()
                        .map(m -> new MenuProductInfoResponse(m.getProductId(), m.getQuantity()))
                        .collect(Collectors.toList())
        );
    }

    private List<NewMenuProduct> createMenuProducts(List<NewProduct> products, List<MenuProductCreateRequest> requests) {
        return requests.stream()
                .map(k -> NewMenuProduct.create(findByProductId(products, k.getProductId()), k.getQuantity()))
                .collect(Collectors.toList());
    }

    private NewProduct findByProductId(List<NewProduct> products, UUID productId) {
        return products.stream()
                .filter(p -> productId.equals(p.getId()))
                .findAny()
                .orElseThrow(NoSuchElementException::new);
    }

}
