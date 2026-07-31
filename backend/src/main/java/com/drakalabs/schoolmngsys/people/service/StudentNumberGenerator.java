package com.drakalabs.schoolmngsys.people.service;

import com.drakalabs.schoolmngsys.people.repository.StudentRepository;
import org.springframework.stereotype.Component;

/** BR-EN-002/A-05: {@code UBS-<entryYear>-<sequence>}, sequence zero-padded to 4 digits per year. */
@Component
public class StudentNumberGenerator {

    private final StudentRepository studentRepository;

    public StudentNumberGenerator(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public String generate(int entryYear) {
        String prefix = "UBS-" + entryYear + "-";
        long nextSequence = studentRepository.countByStudentNumberStartingWith(prefix) + 1;
        return prefix + String.format("%04d", nextSequence);
    }
}
