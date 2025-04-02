package co.com.nequi.api_franchises.repository;

import co.com.nequi.api_franchises.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findTopBySucursal_FranchiseIdOrderByStockDesc(Long franchiseId);
}

