package ru.practicum.ewm.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.category.modul.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
