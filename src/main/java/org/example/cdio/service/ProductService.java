    package org.example.cdio.service;

    import lombok.RequiredArgsConstructor;
    import org.example.cdio.entity.Product;
    import org.example.cdio.repository.ProductRepository;
    import org.springframework.stereotype.Service;

    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class ProductService {

        private final ProductRepository repo;

        public List<Product> findAll() {
            return repo.findAll();
        }

        public Product findById(Long id) {
            return repo.findById(id).orElseThrow();
        }

        public void save(Product product) {
            repo.save(product);
        }

        // SOFT DELETE
        public void delete(Long id) {
            Product p = repo.findById(id)
                    .orElseThrow();

            p.setIsActive(false);
            repo.save(p);
        }
    }