package com.example.bootcamp.domain.exceptions;

import com.example.bootcamp.domain.enums.Message;

import lombok.Getter;

@Getter
public class TechnicalException extends ProcessorException {
    public TechnicalException(Throwable cause, Message technicalMessage) {
        super(cause, technicalMessage);
    }

    public TechnicalException(Message technicalMessage) {
        super(technicalMessage.getMessage(), technicalMessage);
    }
}