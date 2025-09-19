package com.example.bootcamp.domain.exceptions;

import com.example.bootcamp.domain.enums.Message;

import lombok.Getter;

@Getter
public class DomainException extends ProcessorException {
    public DomainException(Message technicalMessage) {
        super(technicalMessage.getMessage(), technicalMessage);
    }
}
