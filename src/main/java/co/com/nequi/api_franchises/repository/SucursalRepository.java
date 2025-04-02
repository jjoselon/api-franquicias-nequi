package co.com.nequi.api_franchises.repository;

import co.com.nequi.api_franchises.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    @Query("SELECT s FROM Sucursal s LEFT JOIN FETCH s.productos WHERE s.franchise.id = :franchiseId")
    List<Sucursal> findByFranchiseIdWithProducts(Long franchiseId);
}

