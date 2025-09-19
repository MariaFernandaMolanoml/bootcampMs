package com.example.bootcamp.domain.exceptions;

import com.example.bootcamp.domain.enums.Message;

import lombok.Getter;
@Getter
public class ProcessorException extends RuntimeException {
    private final Message technicalMessage;

    public ProcessorException(String message, Message technicalMessage) {
        super(message);
        this.technicalMessage = technicalMessage;
    }

    public ProcessorException(Throwable cause, Message message) {
        super(cause);
        this.technicalMessage = message;
    }
}
