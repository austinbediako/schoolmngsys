package com.drakalabs.schoolmngsys.shared.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TestWidgetService {

    private final TestWidgetRepository repository;

    TestWidgetService(TestWidgetRepository repository) {
        this.repository = repository;
    }

    @Audited(action = "TEST_WIDGET_CREATED", entityType = "TestWidget")
    @Transactional
    TestWidget create(String name) {
        return repository.save(new TestWidget(name));
    }
}
