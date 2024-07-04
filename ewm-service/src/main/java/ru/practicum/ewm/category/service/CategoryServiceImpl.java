package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategoryDtoMapper;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.UniqueException;
import ru.practicum.ewm.utility.Pagination;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public Category createCategory(NewCategoryDto categoryDto) {
        try {
            return categoryRepository.save(CategoryDtoMapper.toCategory(categoryDto));
        } catch (DataIntegrityViolationException e) {
            throw new UniqueException(String.format("Category name:%s is not unique.", categoryDto.getName()));
        }
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = checkExist(id);
        List<Event> events = eventRepository.findByCategory(category);
        if (!events.isEmpty()) {
            throw new ConflictException("Can't delete category due to using for some events");
        }
        categoryRepository.delete(checkExist(id));
    }

    @Override
    public Category updateCategory(Long id, NewCategoryDto categoryDto) {
        try {
            Category category = checkExist(id);
            category.setName(categoryDto.getName());
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new UniqueException(String.format("Category name:%s is not unique.", categoryDto.getName()));
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        Pagination pagination = new Pagination(from, size);
        return CategoryDtoMapper.toDtos(categoryRepository.findAll(pagination)
                .stream().collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryDto getCategory(Long id) {
        return CategoryDtoMapper.toDto(checkExist(id));
    }

    private Category checkExist(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Category:%d is not found.", id)));
    }
}
