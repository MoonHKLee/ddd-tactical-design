package kitchenpos.domain.product;

import kitchenpos.domain.product.domain.Product;
import kitchenpos.domain.product.domain.ProductRepository;

import java.util.*;

public class InMemoryProductRepository implements ProductRepository {
    private final Map<UUID, Product> products = new HashMap<>();

    @Override
    public Product save(final Product product) {
        products.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(final UUID id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    @Override
    public List<Product> findAllByIdIn(final List<UUID> ids) {
        return products.values()
            .stream()
            .filter(product -> ids.contains(product.getId()))
            .toList();
    }
}